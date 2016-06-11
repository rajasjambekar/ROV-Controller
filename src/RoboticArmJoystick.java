import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.java.games.input.Controller;

public class RoboticArmJoystick implements Runnable {

	private int motorVal[];			//value of all motors
	private long motorTimer[];			//timer for all motors
	private long timeGap = 100;		//timegap for repeated commands (ms)
	private int numMotors = 7;		//number of motors
	private int stopVal = 0;		//Motor turn off
	private int fullSpeedVal = 255;		//Motor running at full speed
	private long axisTime;
	private JoystickContainer jC;
	private Socket client;
	private TCPSender tcpSender;
	private ThreadEnable threadEnable;
	private  DataAccumulator dataStore;
	
	public RoboticArmJoystick(JoystickContainer jC, Socket client, ThreadEnable threadEnable, DataAccumulator dataStore) {
		this.jC = jC;
		this.dataStore = dataStore;
		this.client = client;
		this.threadEnable = threadEnable;
		try {
			tcpSender = new TCPSender(this.client, threadEnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		motorVal = new int[numMotors];
		motorTimer = new long[numMotors*2];
		//set all motors with stop val
		stopMotors();
		initMotorTimer();
	}

	@Override
	public void run() {
		//check if controller is still connected
		//Stop thread from executing if controller gets disconnected
		//check if tcp is connected
		//Stop thread from executing if tcp gets disconnected
		//thread will restart when controller is rediscovered
		//Also keep checking if controller contains task to control roboticarm
		while(threadEnable.getThreadState() && threadEnable.getTcpState()) {
			setMotorVal();
			updateDataAccumulator();
			sleep(10);
		}
		//send stop values to arduino immediately
		System.out.println("Stopping Motors");
		sendStopVal();
		//update values in dataAccumulator one last time
		updateDataAccumulator();
	}
	
	//updates the values on object of DataAccumulator
	private void updateDataAccumulator() {
		dataStore.setMotorValues(motorVal);
		//dataStore.dispMotorValues();
	}

	//gets the relevant axes data and calculates the corresponding thruster data
	//motors will be run at full speed mostly due to low rpm
	//each motor will be associated with a timer to prevent continuous values from being sent to
	//arduino
	private void setMotorVal() {
		//get raw axes data
		double axesValPercent[] = new double[jC.getAxisCount()];
		boolean buttonVal[] = new boolean[jC.getButtonCount()];
		jC.getAxesData(axesValPercent);
		jC.getButtonsData(buttonVal);
		
		//update values for all tasks of type maneuver
		//check all axisTasks
		ArrayList<AxisTask> axisTaskList = jC.getAxisTaskList();
		for(int i=0;i<axisTaskList.size();i++) {
			AxisTask task = axisTaskList.get(i);
			if(task.getTaskType().equalsIgnoreCase("RoboticArm")) {
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
						//task not triggered by toggle button and axis toggle button not pressed
						calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode(), task.getAxisSide());
				}
			}
		}
		
		//check all buttonTasks 
		ArrayList<ButtonTask> buttonTaskList = jC.getButtonTaskList();
		for(int i=0;i<buttonTaskList.size();i++) {
			ButtonTask task = buttonTaskList.get(i);
			if(task.getTaskType().equalsIgnoreCase("RoboticArm")) {
				float val = -1;
				//check if button pressed
				if(buttonVal[task.getButtonNumber()-1])
					val = -2;
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
	//set timer for each task. Task is evaluated only if the time since previous evaluation is 
	//greater than timegap
	private void calVal(String taskName, double axisVal, int code, float axisSide) {
		if((taskName.equalsIgnoreCase("ARM_LT") || taskName.equalsIgnoreCase("ARM_RT"))) {
			calVal2(taskName, "ARM_LT", "ARM_RT", -1, 1, 0, axisVal, code, axisSide, 0);
		}
		else if(taskName.equalsIgnoreCase("ARM_FW") || taskName.equalsIgnoreCase("ARM_BW")) {
			calVal2(taskName, "ARM_FW", "ARM_BW", -1, 1, 1, axisVal, code, axisSide, 2);
		}
		else if(taskName.equalsIgnoreCase("ARM_DN") || taskName.equalsIgnoreCase("ARM_UP")) {
			calVal2(taskName, "ARM_DN", "ARM_UP", -1, 1, 2, axisVal, code, axisSide, 4);
		}
		else if(taskName.equalsIgnoreCase("GRP_360_ACLK") || taskName.equalsIgnoreCase("GRP_360_CLKW")) {
			calVal2(taskName, "GRP_360_ACLK", "GRP_360_CLKW", -1, 1, 3, axisVal, code, axisSide, 6);
		}
		else if(taskName.equalsIgnoreCase("GRP_180_UP") || taskName.equalsIgnoreCase("GRP_180_DN")) {
			calVal2(taskName, "GRP_180_UP", "GRP_180_DN", -1, 1, 4, axisVal, code, axisSide, 8);
		}
		else if(taskName.equalsIgnoreCase("GRP_CL") || taskName.equalsIgnoreCase("GRP_OP")) {
			calVal2(taskName, "GRP_CL", "GRP_OP", -1, 1, 5, axisVal, code, axisSide, 10);
		}
		else if(taskName.equalsIgnoreCase("CLW_CL") || taskName.equalsIgnoreCase("CLW_OP")) {
			calVal2(taskName, "CLW_CL", "CLW_OP", -1, 1, 6, axisVal, code, axisSide, 12);
		}
	}
	
	private void calVal2(String taskName, String taskName1, String taskName2, int dir1, int dir2, int pos, double axisVal, int code, float axisSide, int timerPos1) {
		int dir = dir1;
		if(taskName.equalsIgnoreCase(taskName2))
			dir = dir2;
		
		//set axisVal in case motors triggered by buttons
		if(axisVal==-2 && taskName.equalsIgnoreCase(taskName1))
			axisVal = 100;	//forward
		else if(axisVal==-2 && taskName.equalsIgnoreCase(taskName2))
			axisVal = 0;	//reverse
		else if(axisVal==-1)
			axisVal = 50;	//stop

		//call function only when axisSide for task corresponds with current value of 
		//axis
		//or call function if task is buttonTask
		if(axisSide<0 && axisVal>=50 || (axisSide>0 && axisVal<=50)) {
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		//in case of buttonTask check if previous thruster value corresponds to task name
		//This is to prevent one task from resetting value set by paired task when button
		//not pressed
		else if(axisSide==0 && motorVal[pos]>=stopVal && taskName.equalsIgnoreCase(taskName1) && System.currentTimeMillis()-motorTimer[timerPos1]>timeGap) {
			setMotorVal(motorVal[pos], axisVal, code, -dir, pos);
			motorTimer[timerPos1] = System.currentTimeMillis();
		}
		else if(axisSide==0 && motorVal[pos]<=stopVal && taskName.equalsIgnoreCase(taskName2) && System.currentTimeMillis()-motorTimer[timerPos1+1]>timeGap) {
			setMotorVal(motorVal[pos], axisVal, code, -dir, pos);
			motorTimer[timerPos1+1] = System.currentTimeMillis();
		}
	}
	
	//generalized function to get new value for motors
	//set dir to motorVal
	//pos is the position of this motor val in motorVal[]
	//if val is changed, send tcp
	private void setMotorVal(int prevVal, double axisVal, int code, int dir, int pos) {
		//forward direction of motor is paired with -1f on axis. 
		//reverse direction of motor is paired with 1f on axis. 
		//reverse axisVal to get correct motor value
		if((dir==-1 && axisVal<50) || (dir==1 && axisVal>50)) {
			axisVal = 100 - axisVal;
		}
		
		//get motor new speed
		int mVal = 0;
		//System.out.println(axisVal);
		if(axisVal>60 && dir==-1) {
			mVal = -fullSpeedVal;
			//new value is different from previous value
			//send new value over tcp
			int temp = motorVal[pos];
			motorVal[pos] = mVal;
			//System.out.println("data send1: " + mVal);
			if(temp!=motorVal[pos])
				tcpSender.sendData(code, Math.abs(motorVal[pos]));
		}
		else if(axisVal<40 && dir==1) {
			mVal = fullSpeedVal;
			//new value is different from previous value
			//send new value over tcp
			int temp = motorVal[pos];
			motorVal[pos] = mVal;
			
			//System.out.println("data send2: " + mVal);
			if(temp!=motorVal[pos])
				tcpSender.sendData(code, Math.abs(motorVal[pos]));
			
		}
		//for axis task. allow boundary of 40-60 as 50
		else if(dir==-1 && axisVal<60 && axisVal>40 && System.currentTimeMillis()-motorTimer[pos*2]>timeGap) {
			mVal = 0;
			if(motorVal[pos]!=mVal) {
				motorVal[pos] = mVal;
				tcpSender.sendData(code, Math.abs(motorVal[pos]));
			}
		}
		else if(dir==1 && axisVal<60 && axisVal>40 && System.currentTimeMillis()-motorTimer[pos*2+1]>timeGap) {
			mVal = 0;
			if(motorVal[pos]!=mVal) {
				motorVal[pos] = mVal;
				tcpSender.sendData(code, Math.abs(motorVal[pos]));
			}
		}
		//System.out.println(axisVal + " " + motorVal[0] + " 1dir: " + dir);
	}
	
	//check if there is a change in motor value
	//We are going to run the motor only at full speed for both dir
	//But we have added support to detect speed change provided a difference of 50 from prev val
	private boolean compareMotorVal(int prevVal, int newVal) {
		int limit = 50;	//gap is ideal to get discrete values
		if((newVal>prevVal && newVal-prevVal>limit) || (newVal<prevVal && prevVal-newVal>limit)) {
			//thruster value changed
			return true;
		}
		return false;
	}
		
	//stops all motors using stopVal
	private void stopMotors() {
		for(int i=0;i<motorVal.length;i++)
			motorVal[i] = stopVal;
	}
	
	//displays current val of all motors
	public void dispValues() {
		for(int i=0;i<motorVal.length;i++)
			System.out.println("Motor " + (i+1) + ": " + motorVal[i]);
	}
	
	private void sleep(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//send command to stop motors
	private void sendStopVal() {
		stopMotors();
		ArrayList<AxisTask> axisTaskList = jC.getAxisTaskList();
		for(AxisTask task: axisTaskList) {
			if(task.getTaskType().equalsIgnoreCase("RoboticArm")) {
				int code = task.getCode();
				tcpSender.sendData(code, stopVal);
				sleep(10);
			}
		}
		ArrayList<ButtonTask> buttonTaskList = jC.getButtonTaskList();
		for(ButtonTask task: buttonTaskList) {
			if(task.getTaskType().equalsIgnoreCase("RoboticArm") && !task.getTaskName().equalsIgnoreCase("Toggle")) {
				int code = task.getCode();
				tcpSender.sendData(code, stopVal);
				sleep(10);
			}
		}
	}
	

	//init motor timer with current time values
	private void initMotorTimer() {
		for(int i = 0;i<motorTimer.length;i++)
			motorTimer[i] = System.currentTimeMillis();
	}
}
