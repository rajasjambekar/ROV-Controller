import java.io.*;
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
	JoystickContainer jC;
	
	public RoboticArmJoystick(JoystickContainer jC) {
		this.jC = jC;
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
		while(jC.getPoll() && jC.containsTask("RoboticArm")) {
			setMotorVal();
			dispValues();
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
		stopMotors();
		getMotorValues(axesValPercent, buttonVal);
	}

	//motors need direction and speed.
	//direction to driver is provided as high/low combination
	//For one direction, we will send pure speed as value
	//For other direction, we will add 1000 to speed e.g. speed = 255 + 1000 = 1255
	//arduino will read the 4th digit and change the direction
	//we keep a margin of 10% to prevent accidental movement of motor since actual motor
	//control is not possible
	private void getMotorValues(float[] axesValPercent, boolean[] buttonVal) {
		//get motor values
		axisMotorValues(axesValPercent[0], 0);
		axisMotorValues(axesValPercent[1], 1);
		axisMotorValues(axesValPercent[2], 2);
		
		gripperValue(buttonVal, "360Degree-", "360Degree+", 3);
		gripperValue(buttonVal, "180Degree-", "180Degree+", 4);
		gripperValue(buttonVal, "Gripper-", "Gripper+", 5);
	}
	
	//gripper control
	//since buttons are being used for turning motor on and off with fw and reverse dir
	//we have created 2 tasks to enable custom configuration on joystick
	//motor stops when both buttons are pressed together
	//motor moves in one direction when one button is pressed while it changes direction when 
	//the other button is pressed
	private void gripperValue(boolean[] buttonVal, String name1, String name2, int motorPos) {
		Task task1 = jC.getTask(name1);
		Task task2 = jC.getTask(name2);
		if(task1!=null && task2!=null) {
			int buttonNo1 = task1.getButtonNumber();
			int taskCode1 = task1.getCode();
			int buttonNo2 = task2.getButtonNumber();
			int taskCode2 = task2.getCode();
			//check if both buttons pressed
			if(buttonNo1<buttonVal.length && buttonVal[buttonNo1]==true &&
					buttonNo2<buttonVal.length && buttonVal[buttonNo2]==true) {
				motorVal[motorPos] = stopVal;
			}
			//check if first button pressed
			else if(buttonNo1<buttonVal.length && buttonVal[buttonNo1]==true) {
				motorVal[motorPos] = fullSpeedVal;
			}
			//check if other button pressed
			else if(buttonNo2<buttonVal.length && buttonVal[buttonNo2]==true) {
				motorVal[motorPos] = fullSpeedVal + 1000;
			}
		}
	}

	//sets the motor values for 'arm' of robotic arm system
	private void axisMotorValues(float x, int axis) {
		if(x<40)
			motorVal[axis] = fullSpeedVal;
		else if(x>60)
			motorVal[axis] = fullSpeedVal + 1000;
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
}
