import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.java.games.input.Controller;

public class ManeuveringJoystick implements Runnable{

	private int thrusterVal[];			//value of all thrusters
	private int numThrusters = 6;		//number of thrusters
	private int THRUSTER_STOP = 1500;	//thruster stop val
	private int THRUSTER_FULL_FW = 1850;	//thruster full forward val
	private int THRUSTER_FULL_BW = 1150;	//thruster full backward val
	private int thrusterValRange;	//Difference of thruster max and min value
	private JoystickContainer jC;
	private Socket client;
	private TCPSender tcpSender;
	private ThreadEnable threadEnable;
	private DataAccumulator dataStore;
	private long repeatTimeGapMillis = 5000;
	private long minTimeGapMillis = 300;
	private long prevTimeMillis[];
	
	public ManeuveringJoystick(JoystickContainer jC, Socket client, ThreadEnable threadEnable, DataAccumulator dataStore) {
		this.jC = jC;
		this.dataStore = dataStore;
		this.client = client;
		this.threadEnable = threadEnable;
		try {
			tcpSender = new TCPSender(this.client, threadEnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		thrusterVal = new int[numThrusters];
		thrusterValRange = THRUSTER_FULL_FW-THRUSTER_FULL_BW;
		//set all thrusters with stop val
		stopThrusters();
		
		prevTimeMillis = new long[numThrusters];
		setCurrentTime();
	}

	@Override
	public void run() {
		//check if controller is still connected
		//Stop thread from executing if controller gets disconnected
		//check if tcp is connected
		//Stop thread from executing if tcp gets disconnected
		//Also keep checking if controller contains task to maneuver rov
		while(threadEnable.getThreadState() && threadEnable.getTcpState()) {
			//check if thrusters are enabled by the user using the UI
			if(threadEnable.getThrusterEnableState()) {
				setThrusterVal();
				updateDataAccumulator();
				//dataStore.dispThrusterValues();
			}
			sleep(10);
		}
		//send stop values to arduino immediately
		System.out.println("Stopping Thrusters");
		sendStopVal();
		setCurrentTime();
		//update values in dataAccumulator one last time
		updateDataAccumulator();
		dataStore.dispThrusterValues();
	}
	
	//updates the values on object of DataAccumulator
	private void updateDataAccumulator() {
		dataStore.setThrusterValues(thrusterVal);
	}
	
	//gets the relevant axes data and calculates the corresponding thruster data
	public void setThrusterVal() {
		//get raw axes data
		double axesValPercent[] = new double[jC.getAxisCount()];
		jC.getAxesData(axesValPercent);
		//update values for all tasks of type maneuver
		//check all axisTasks
		ArrayList<AxisTask> axisTaskList = jC.getAxisTaskList();
		for(int i=0;i<axisTaskList.size();i++) {
			AxisTask task = axisTaskList.get(i);
			if(task.getTaskType().equalsIgnoreCase("Maneuver")) {
				if(task.containsToggleButton()) {
					//task triggered by toggle button
					if(jC.getToggleButtonState(task)) {
						//toggle button is pressed
						calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode(), task.getAxisSide());
					}
					else {
						//toggle button is not pressed
					}
				}
				else {
					//if toggle button is pressed for this axis do not read value
					if(!jC.checkAxisToggleButtonPressed(task.getAxisNumber()))
						//task not triggered by toggle button
						calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode(), task.getAxisSide());
				}
			}
		}
		
		//get all button values
		boolean buttonVal[] = new boolean[jC.getButtonCount()];
		jC.getButtonsData(buttonVal);
		//check all buttonTasks 
		ArrayList<ButtonTask> buttonTaskList = jC.getButtonTaskList();
		for(int i=0;i<buttonTaskList.size();i++) {
			ButtonTask task = buttonTaskList.get(i);
			if(task.getTaskType().equalsIgnoreCase("Maneuver")) {
				float val = -1;		//set default value to button not pressed
				//check if button pressed
				if(buttonVal[task.getButtonNumber()-1])
					val = -2;		//button pressed value
				calVal(task.getTaskName(), val, task.getCode(), 0);
			}
		}
	}
	
	//calculate the final value of the task. If any other task is to be added, add a condition 
	//checking for the taskName.
	//Send the value over tcp only if the new value deviates from the previous value over a limit
	//code value is combination of 2 thruster codes each 2 digit thus making 4 digit code
	//This code is common for buttons and axis
	//In case of buttons, the value of axisVal is 1/0. 
	private void calVal(String taskName, double axisVal, int code, float axisSide) {
		if(taskName.equalsIgnoreCase("TH_LT") || taskName.equalsIgnoreCase("TH_RT")) {
			int dir = 1;
			if(taskName.equalsIgnoreCase("TH_RT"))
				dir = -1;
			
			int thPos1 = 0;
			int thPos2 = 1;
			
			//set axisVal in case thrusters triggered by buttons
			if(axisVal==-2 && taskName.equalsIgnoreCase("TH_LT"))
				axisVal = 95;	//forward
			else if(axisVal==-2 && taskName.equalsIgnoreCase("TH_RT"))
				axisVal = 5;	//reverse
			else if(axisVal==-1)
				axisVal = 50;	//stop
			
			//call function only when axisSide for task corresponds with current value of 
			//axis
			//or call function if task is buttonTask
			if((axisSide<0 && axisVal<=50 && ((thrusterVal[thPos1]<=THRUSTER_STOP && thrusterVal[thPos2]<=THRUSTER_STOP) || ((thrusterVal[thPos1]>=THRUSTER_STOP && thrusterVal[thPos2]>=THRUSTER_STOP)))) || (axisSide>0 && axisVal>=50 && ((thrusterVal[thPos1]<=THRUSTER_STOP && thrusterVal[thPos2]<=THRUSTER_STOP) || ((thrusterVal[thPos1]>=THRUSTER_STOP && thrusterVal[thPos2]>=THRUSTER_STOP))))) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
			//in case of buttonTask check if previous thruster value corresponds to task name
			//This is to prevent one task from resetting value set by paired task when button
			//not pressed
			else if(axisSide==0 && thrusterVal[thPos1]==thrusterVal[thPos2] && thrusterVal[thPos1]>=THRUSTER_STOP && taskName.equalsIgnoreCase("TH_LT")) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
			else if(axisSide==0 && thrusterVal[thPos1]==thrusterVal[thPos2] && thrusterVal[thPos1]<=THRUSTER_STOP && taskName.equalsIgnoreCase("TH_RT")) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
		}
		else if(taskName.equalsIgnoreCase("TH_FW") || taskName.equalsIgnoreCase("TH_BW")) {
			int dir = 1;
			if(taskName.equalsIgnoreCase("TH_BW"))
				dir = -1;
			
			int thPos1 = 2;
			int thPos2 = 3;
			
			//set axisVal in case thrusters triggered by buttons
			if(axisVal==-2 && taskName.equalsIgnoreCase("TH_FW"))
				axisVal = 95;	//forward
			else if(axisVal==-2 && taskName.equalsIgnoreCase("TH_BW"))
				axisVal = 5;	//reverse
			else if(axisVal==-1)
				axisVal = 50;	//stop
			
			//call function only when axisSide for task corresponds with current value of 
			//axis
			//or call function if task is buttonTask
			if(axisSide<0 && axisVal<=50 || (axisSide>0 && axisVal>=50)) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
			//in case of buttonTask check if previous thruster value corresponds to task name
			//This is to prevent one task from resetting value set by paired task when button
			//not pressed
			else if(axisSide==0 && thrusterVal[thPos1]>=THRUSTER_STOP && taskName.equalsIgnoreCase("TH_FW")) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
			else if(axisSide==0 && thrusterVal[thPos1]<=THRUSTER_STOP && taskName.equalsIgnoreCase("TH_BW")) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
		}
		else if(taskName.equalsIgnoreCase("TH_UP") || (taskName.equalsIgnoreCase("TH_DN"))) {
			int dir = 1;
			if(taskName.equalsIgnoreCase("TH_UP"))
				dir = -1;
			
			int thPos1 = 4;
			int thPos2 = 5;
			
			//set axisVal in case thrusters triggered by buttons
			if(axisVal==-2 && taskName.equalsIgnoreCase("TH_DN"))
				axisVal = 95;	//forward
			else if(axisVal==-2 && taskName.equalsIgnoreCase("TH_UP"))
				axisVal = 5;	//reverse
			else if(axisVal==-1)
				axisVal = 50;	//stop
			
			//call function only when axisSide for task corresponds with current value of 
			//axis
			//or call function if task is buttonTask
			if(axisSide<0 && axisVal<=50 || (axisSide>0 && axisVal>=50)) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
			//in case of buttonTask check if previous thruster value corresponds to task name
			//This is to prevent one task from resetting value set by paired task when button
			//not pressed
			else if(axisSide==0 && thrusterVal[thPos1]>=THRUSTER_STOP && taskName.equalsIgnoreCase("TH_DN")) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
			else if(axisSide==0 && thrusterVal[thPos1]<=THRUSTER_STOP && taskName.equalsIgnoreCase("TH_UP")) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, dir, thPos2);
			}
		}
		else if(taskName.equalsIgnoreCase("TH_RTL") || (taskName.equalsIgnoreCase("TH_RTR"))) {
			int dir = 1;
			if(taskName.equalsIgnoreCase("TH_RTR"))
				dir = -1;
			
			int thPos1 = 0;
			int thPos2 = 1;
			
			//set axisVal in case thrusters triggered by buttons
			if(axisVal==-2 && taskName.equalsIgnoreCase("TH_RTL"))
				axisVal = 95;	//forward
			else if(axisVal==-2 && taskName.equalsIgnoreCase("TH_RTR"))
				axisVal = 5;	//reverse
			else if(axisVal==-1)
				axisVal = 50;	//stop
			//call function only when axisSide for task corresponds with current value of 
			//axis
			//or call function if task is buttonTask
			if((axisSide<0 && axisVal<=50 && ((thrusterVal[thPos1]<=THRUSTER_STOP && thrusterVal[thPos2]>=THRUSTER_STOP) || ((thrusterVal[thPos1]>=THRUSTER_STOP && thrusterVal[thPos2]<=THRUSTER_STOP)))) || ((axisSide>0 && axisVal>=50) && ((thrusterVal[thPos1]<=THRUSTER_STOP && thrusterVal[thPos2]>=THRUSTER_STOP) || ((thrusterVal[thPos1]>=THRUSTER_STOP && thrusterVal[thPos2]<=THRUSTER_STOP))))) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, -dir, thPos2);
			}
			//in case of buttonTask check if previous thruster value corresponds to task name
			//This is to prevent one task from resetting value set by paired task when button
			//not pressed
			else if(axisSide==0 && ((thrusterVal[thPos1]==thrusterVal[thPos2] && thrusterVal[thPos1]==THRUSTER_STOP) || (thrusterVal[thPos1]!=thrusterVal[thPos2] && thrusterVal[thPos1]>THRUSTER_STOP)) && taskName.equalsIgnoreCase("TH_RTL")) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, -dir, thPos2);
			}
			else if(axisSide==0 && ((thrusterVal[thPos1]==thrusterVal[thPos2] && thrusterVal[thPos1]==THRUSTER_STOP) || (thrusterVal[thPos1]!=thrusterVal[thPos2] && thrusterVal[thPos1]<THRUSTER_STOP)) && taskName.equalsIgnoreCase("TH_RTR")) {
				setThrusterVal(thrusterVal[thPos1], axisVal, (code/100)*100, dir, thPos1);
				setThrusterVal(thrusterVal[thPos2], axisVal, (code%100)*100, -dir, thPos2);
			}
		}
	}
	
	private void setThrusterVal(int prevVal, double axisVal, int code, int dir, int pos) {
		int t = 0;
		//forward direction of thruster is paired with -1f on axis. 
		//reverse direction of thruster is paired with 1f on axis. 
		//reverse axisVal to get correct thruster value
		if((dir==1 && axisVal<50) || (dir==-1 && axisVal>50)) {
			axisVal = 100 - axisVal;
		}
		
		//forward movement of thruster
		//t = THRUSTER_FULL_FW - (int) ((axisVal/100)*(thrusterValRange));
		t = THRUSTER_FULL_BW + (int) ((axisVal/100)*(thrusterValRange));
		//check for change in thruster value
		//if no change but min time elapsed resend data
		if(((compareThrusterVal(thrusterVal[pos], t) || (System.currentTimeMillis() - prevTimeMillis[pos])>repeatTimeGapMillis)) && (System.currentTimeMillis() - prevTimeMillis[pos])>minTimeGapMillis) {
			//round off thruster values between 1600 and 1400 to 1500
			thrusterVal[pos] = t;	//assign new thruster value
			//System.out.println(t);
			tcpSender.sendData(code, t);	//send new thruster value over tcp
			prevTimeMillis[pos] = System.currentTimeMillis();
		}
	}
	
	//check if there is a change in thruster value
	private boolean compareThrusterVal(int prevVal, int newVal) {
		int limit = 10;	//gap is ideal to get discrete values
		if((newVal>prevVal && newVal-prevVal>limit) || (newVal<prevVal && prevVal-newVal>limit)) {
			//thruster value changed
			return true;
		}
		return false;
	}

	//displays current val of all thrusters
	public void dispValues() {
		for(int i=0;i<thrusterVal.length;i++)
			System.out.println("Thruster " + (i+1) + ": " + thrusterVal[i]);
	}

	//get thruster values
	public int[] getThrustersVal() {
		return thrusterVal;
	}
	
	//set all thrusters with stop val
	private void stopThrusters() {
		for(int i=0;i<numThrusters;i++) {
			thrusterVal[i] = THRUSTER_STOP;
		}
	}

	private void sleep(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//sends commands to stop all thrusters
	private void sendStopVal() {
		stopThrusters();
		ArrayList<AxisTask> axisTaskList = jC.getAxisTaskList();
		for(AxisTask task: axisTaskList) {
			if(task.getTaskType().equalsIgnoreCase("Maneuver")) {
				int code = task.getCode();
				tcpSender.sendData(code, THRUSTER_STOP);
				sleep(10);
			}
		}
		ArrayList<ButtonTask> buttonTaskList = jC.getButtonTaskList();
		for(ButtonTask task: buttonTaskList) {
			if(task.getTaskType().equalsIgnoreCase("Maneuver") && !task.getTaskName().equalsIgnoreCase("Toggle")) {
				int code = task.getCode();
				tcpSender.sendData(code, THRUSTER_STOP);
				sleep(10);
			}
		}
	}
	
	//set current time for all thrusters
	private void setCurrentTime() {
		for(int i=0;i<prevTimeMillis.length;i++)
			prevTimeMillis[i] = System.currentTimeMillis();
	}
}
