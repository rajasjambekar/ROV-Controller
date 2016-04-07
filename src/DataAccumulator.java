
public class DataAccumulator {
	private int thrusterVal[];			//value of all thrusters
	private int numThrusters = 6;		//number of thrusters
	private int THRUSTER_STOP = 1500;	//thruster stop val
	private int THRUSTER_FULL_FW = 1850;	//thruster full forward val
	private int THRUSTER_FULL_BW = 1150;	//thruster full backward val
	
	private int motorVal[];			//value of all motors
	private int numMotors = 7;		//number of motors
	private int MOTOR_STOP = 0;		//stop val for motor
	private int MOTOR_FULL_FW = 255;	//motor full forward val
	private int MOTOR_FULL_BW = -255;	//motor full backward val
	
	private double temperature;		//temperature value received from arduino
	private double pressure;		//pressure value received from arduino
	
	private int[] cam1Servo;	//servos for camera1	- pan,tilt
	private int[] cam2Servo;	//servos for camera2	- pan,tilt
	private int cam1ServoCount = 2;		//no of servos on cam1
	private int cam2ServoCount = 2;		//no of servos on cam2
	private int minServoAngle = 0;		//minimum angle of servo
	private int defaultServoAngle = 90;		//minimum angle of servo
	private int maxServoAngle = 180;		//minimum angle of servo
	
	private boolean ledArray1;
	private boolean ledArray2;
	
	public DataAccumulator() {
		thrusterVal = new int[numThrusters];
		motorVal = new int[numMotors];
		cam1Servo = new int[cam1ServoCount];
		cam2Servo = new int[cam2ServoCount];
		
		initThrusterVal();
		initMotorVal();
		initCameraServo();
		
		temperature = 0;
		pressure = 0;
		
		ledArray1 = false;
		ledArray2 = false;
	}
	
	//set the servos to 0degrees
	private void initCameraServo() {
		cam1Servo[0] = defaultServoAngle;
		cam1Servo[1] = defaultServoAngle;
		cam2Servo[0] = defaultServoAngle;
		cam2Servo[1] = defaultServoAngle;
	}

	//set the stop value for all motors
	private void initMotorVal() {
		for(int i=0;i<motorVal.length;i++) {
			this.motorVal[i] = MOTOR_STOP;
		}
	}

	//set the stop value for all thrusters
	private void initThrusterVal() {
		for(int i=0;i<thrusterVal.length;i++) {
			this.thrusterVal[i] = THRUSTER_STOP;
		}
	}

	//update thruster values
	public void setThrusterValues(int []thrusterVal) {
		for(int i=0;i<this.thrusterVal.length;i++) {
			this.thrusterVal[i] = thrusterVal[i];
		}
	}
	
	//update motor values
	public void setMotorValues(int []motorVal) {
		for(int i=0;i<this.motorVal.length;i++) {
			this.motorVal[i] = motorVal[i];
		}
	}
	
	//updates the camera servo value
	public void setCameraServo(int camNo, int servoNo, int angle) {
		if(camNo==1 && servoNo<cam1Servo.length && angle>=0)
			cam1Servo[servoNo] = angle;
		else if(camNo==2 && servoNo<cam2Servo.length && angle>=0)
			cam2Servo[servoNo] = angle;
	}
	
	//updates the led on/off values
	public void setLedValue(boolean ledArray1, boolean ledArray2) {
		this.ledArray1 = ledArray1;
		this.ledArray2 = ledArray2;
	}
	
	//updates the temperature value
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	
	//updates the temperature value
	public void setPressure(double pressure) {
		this.pressure = pressure;
	}
	
	//returns the value of the motor at position i
	public int getMotorVal(int i) {
		if(i<motorVal.length)
			return motorVal[i];
		return 0;
	}
	
	//returns the value of the motor at position i
	public int getThrusterVal(int i) {
		if(i<thrusterVal.length)
			return thrusterVal[i];
		return 0;
	}
	
	//returns servo position of camera servo
	public int getCameraServoPos(int camNo, int servoNo) {
		if(camNo==1 && servoNo<cam1Servo.length) {
			return cam1Servo[servoNo];
		}
		else if(camNo==2 && servoNo<cam2Servo.length) {
			return cam2Servo[servoNo];
		}
		return -1;
	}
	
	//returns the value of temperature sensor
	public double getTemperature() {
		return temperature;
	}
	
	//returns the value of pressure sensor
	public double getPressure() {
		return pressure;
	}
	
	//displays all thruster values
	public void dispThrusterValues() {
		for(int i=0;i<thrusterVal.length;i++) {
			System.out.println("Thruster " + (i+1) + ": " + thrusterVal[i]);
		}
		/*for(int i=0;i<motorVal.length;i++) {
			System.out.println("Motor " + (i+1) + ": " + motorVal[i]);
		}*/
	}
	
	//displays all motor values
	public void dispMotorValues() {
		for(int i=0;i<motorVal.length;i++) {
			System.out.println("Motor " + (i+1) + ": " + motorVal[i]);
		}
	}
	
	//displays all motor values
	public void dispServoValues() {
		for(int i=0;i<cam1Servo.length;i++) {
			System.out.println("Camera 1 Servo " + (i+1) + ": " + cam1Servo[i]);
		}
		for(int i=0;i<cam2Servo.length;i++) {
			System.out.println("Camera 2 Servo " + (i+1) + ": " + cam2Servo[i]);
		}
	}
	
	//displays led value
	public void dispLed() {
		System.out.println("LED1: " + ledArray1 + " " + " LED2: " + ledArray2);
	}
}
