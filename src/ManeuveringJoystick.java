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
	
	public ManeuveringJoystick(JoystickContainer jC, Socket client, ThreadEnable threadEnable, DataAccumulator dataStore) {
		this.jC = jC;
		this.dataStore = dataStore;
		this.client = client;
		this.threadEnable = threadEnable;
		/*try {
			tcpSender = new TCPSender(this.client, threadEnable);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		thrusterVal = new int[numThrusters];
		thrusterValRange = THRUSTER_FULL_FW-THRUSTER_FULL_BW;
		//set all thrusters with stop val
		stopThrusters();
	}

	@Override
	public void run() {
		//check if controller is still connected
		//Stop thread from executing if controller gets disconnected
		//check if tcp is connected
		//Stop thread from executing if tcp gets disconnected
		//Also keep checking if controller contains task to maneuver rov
		while(threadEnable.getThreadState() && threadEnable.getTcpState() && jC.getPoll()) {
			setThrusterVal();
			updateDataAccumulator();
			dataStore.dispThrusterValues();
			sleep(10);
		}
		//send stop values to arduino immediately
		System.out.println("Stopping Thrusters");
		sendStopVal();
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
		float axesValPercent[] = new float[jC.getAxisCount()];
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
						calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode());
					}
					else {
						//toggle button is not pressed
					}
				}
				else {
					//if toggle button is pressed for this axis do not read value
					if(!jC.checkAxisToggleButtonPressed(task.getAxisNumber()))
						//task not triggered by toggle button
						calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode());
				}
			}
		}
		
		//get all button values
		boolean buttonVal[] = new boolean[jC.getButtonCount()];
		
		//check all buttonTasks 
		ArrayList<ButtonTask> buttonTaskList = jC.getButtonTaskList();
		for(int i=0;i<buttonTaskList.size();i++) {
			ButtonTask task = buttonTaskList.get(i);
			if(task.getTaskType().equalsIgnoreCase("Maneuver")) {
				float val = -1;
				//check if button pressed
				if(buttonVal[task.getButtonNumber()-1])
					val = -2;
				calVal(task.getTaskName(), val, task.getCode());
			}
		}
	}
	
	//calculate the final value of the task. If any other task is to be added, add a condition 
	//checking for the taskName.
	//Send the value over tcp only if the new value deviates from the previous value over a limit
	//code value is combination of 2 thruster codes each 2 digit thus making 4 digit code
	//This code is common for buttons and axis
	//In case of buttons, the value of axisVal is 1/0. 
	private void calVal(String taskName, float axisVal, int code) {
		if(taskName.equalsIgnoreCase("TH_LR1")) {
			int dir = 1;
			int pos = 0;
			setThrusterVal(thrusterVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("TH_LR2")) {
			int dir = 1;
			int pos = 1;
			setThrusterVal(thrusterVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("TH_FB1")) {
			int dir = 1;
			int pos = 2;
			setThrusterVal(thrusterVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("TH_FB2")) {
			int dir = 1;
			int pos = 3;
			setThrusterVal(thrusterVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("TH_UD1")) {
			int dir = 1;
			int pos = 4;
			setThrusterVal(thrusterVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("TH_UD2")) {
			int dir = 1;
			int pos = 5;
			setThrusterVal(thrusterVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("TH_RT1")) {
			int dir = 1;
			int pos = 0;
			setThrusterVal(thrusterVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("TH_RT2")) {
			int dir = -1;
			int pos = 1;
			setThrusterVal(thrusterVal[pos], axisVal, code, dir, pos);
		}
	}
	
	private void setThrusterVal(int prevVal, float axisVal, int code, int dir, int pos) {
		//convert button press val to appropriate val
		if(axisVal==-2)	//thruster on
			axisVal = 90;	//buttons are on off. So do not power thrusters to full value
		else if(axisVal==-1)
			axisVal = 50;
		
		int t = 0;
		if(dir==1)
			//normal thrusters
			t = THRUSTER_FULL_FW - (int) ((axisVal/100)*(thrusterValRange));
		else
			//rotation thruster.
			//one motor fw and other reverse
			t = THRUSTER_FULL_BW + (int) ((axisVal/100)*(thrusterValRange));
		if(compareThrusterVal(thrusterVal[1], t)) {
			if(t<1600 && t>1400)
				t = THRUSTER_STOP;
			thrusterVal[pos] = t;
			//tcpSender.sendData(code, t);
		}
	}
	
	//check if there is a change in thruster value
	private boolean compareThrusterVal(int prevVal, int newVal) {
		int limit = 50;	//gap is ideal to get discrete values
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
				//tcpSender.sendData(code, THRUSTER_STOP);
				sleep(10);
			}
		}
		ArrayList<ButtonTask> buttonTaskList = jC.getButtonTaskList();
		for(ButtonTask task: buttonTaskList) {
			if(task.getTaskType().equalsIgnoreCase("Maneuver") && !task.getTaskName().equalsIgnoreCase("Toggle")) {
				int code = task.getCode();
				//tcpSender.sendData(code, THRUSTER_STOP);
				sleep(10);
			}
		}
	}
}
