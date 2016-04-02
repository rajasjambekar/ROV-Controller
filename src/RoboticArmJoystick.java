import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.java.games.input.Controller;

public class RoboticArmJoystick implements Runnable {

	private int motorVal[];			//value of all motors
	private long motorTimer[];			//timer for all motors
	private long timeGap = 500;		//timegap for repeated commands (ms)
	private int numMotors = 7;		//number of motors
	private int stopVal = 0;		//Motor turn off
	private int fullSpeedVal = 255;		//Motor running at full speed
	private JoystickContainer jC;
	private Socket client;
	TCPSender tcpSender;
	long axisTime;
	
	public RoboticArmJoystick(JoystickContainer jC, Socket client) {
		this.jC = jC;
		this.client = client;
		try {
			//tcpSender = new TCPSender(this.client);
		} catch (Exception e) {
			e.printStackTrace();
		}
		motorVal = new int[numMotors];
		motorTimer = new long[numMotors];
		//set all motors with stop val
		stopMotors();
	}
	
	@Override
	public void run() {
		//check if controller is still connected
		//Stop thread from executing if controller gets disconnected
		//thread will restart when controller is rediscovered
		//Also keep checking if controller contains task to control roboticarm
		while(jC.getPoll() && jC.containsAxisTaskType("RoboticArm")) {
			setMotorVal();
			dispValues();
			sleep(10);
		}
	}

	//gets the relevant axes data and calculates the corresponding thruster data
	//motors will be run at full speed mostly due to low rpm
	//each motor will be associated with a timer to prevent continuous values from being sent to
	//arduino
	private void setMotorVal() {
		//get raw axes data
		float axesValPercent[] = new float[jC.getAxisCount()];
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
						calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode());
					}
					else {
						//toggle button is not pressed
					}
				}
				else {	
					//if toggle button is pressed for this axis do not read value
					if(!jC.checkAxisToggleButtonPressed(task.getAxisNumber()))
						//task not triggered by toggle button and axis toggle button not pressed
						calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode());
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
		if(taskName.equalsIgnoreCase("ARM_LT")) {
			int pos = 0;
			int dir = 1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("ARM_RT")) {
			int pos = 0;
			int dir = -1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("ARM_FW")) {
			int pos = 1;
			int dir = 1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("ARM_BW")) {
			int pos = 1;
			int dir = -1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("ARM_UP")) {
			int pos = 2;
			int dir = 1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("ARM_DN")) {
			int pos = 2;
			int dir = -1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("GRP_360_ACLK")) {
			int pos = 3;
			int dir = 1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("GRP_360_CLKW")) {
			int pos = 3;
			int dir = -1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("GRP_180_DN")) {
			int pos = 4;
			int dir = 1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("GRP_180_UP")) {
			int pos = 4;
			int dir = -1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("GRP_CL")) {
			int pos = 5;
			int dir = 1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("GRP_OP")) {
			int pos = 5;
			int dir = -1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("CLW_CL")) {
			int pos = 6;
			int dir = 1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
		else if(taskName.equalsIgnoreCase("CLW_OP")) {
			int pos = 6;
			int dir = -1;
			setMotorVal(motorVal[pos], axisVal, code, dir, pos);
		}
	}
	
	//generalized function to get new value for motors
	//set dir to motorVal
	//pos is the position of this motor val in motorVal[]
	//if val is changed, send tcp
	private void setMotorVal(int prevVal, float axisVal, int code, int dir, int pos) {
		if(axisVal==-2)	//motor on
			axisVal = 100;	//set motor speed to full
		else if(axisVal==-1) //motor off
			axisVal = 50;	//set motor speed to 0
			
		//get thruster new speed
		int mVal = motorVal[pos];
		if(axisVal>60 && dir==-1)
			mVal = -fullSpeedVal;
		else if(axisVal<40 && dir==1)
			mVal = fullSpeedVal;
		else if(axisVal<60 && axisVal>40)
			mVal = 0;
		if(compareMotorVal(prevVal, mVal)) {
			//new value is different from previous value
			//send new value over tcp
			motorVal[pos] = mVal;
			//tcpSender.sendData(code, Math.abs(motorVal[pos]));
		}
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
}
