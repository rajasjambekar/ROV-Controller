
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
	
	private double temperature = 0;		//temperature value received from arduino
	private double pressure = 0;		//pressure value received from arduino
	private double fluidDensity = 1029; //fluid density for pressure to depth calculation
	private double depth = 0;		//pressure value received from arduino
	
	private int accX = 0;	//x axis of accelerometer
	private int accY = 0;	//y axis of accelerometer
	private int accZ = 0;	//z axis of accelerometer
	private int gyroX = 0;	//x axis of gyroscope
	private int gyroY = 0;	//y axis of gyroscope
	private int gyroZ = 0;	//z axis of gyroscope
	private int magX = 0;	//x axis of magnetometer
	private int magY = 0;	//y axis of magnetometer
	private int magZ = 0;	//z axis of magnetometer
	
	private int[] cam1Servo;	//servos for camera1	- pan,tilt
	private int[] cam2Servo;	//servos for camera2	- pan,tilt
	private int cam1ServoCount = 2;		//no of servos on cam1
	private int cam2ServoCount = 2;		//no of servos on cam2
	private int minServoAngle = 0;		//minimum angle of servo
	private int defaultServoAngle = 90;		//minimum angle of servo
	private int maxServoAngle = 180;		//minimum angle of servo
	
	private boolean ledArray1;
	private boolean ledArray2;
	
	private long pingTimer;			//timer for last ping
	private long pingInterval = 3000;	//max interval for ping
	
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
		
		pingTimer = System.currentTimeMillis();
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
	
	//updates the accelerometer value
	public void setAccelerometer(int value, int axis) {
		if(axis==1)
			accX = value;
		else if(axis==2)
			accY = value;
		else if(axis==3)
			accZ = value;
	}
	
	//updates the Gyroscope value
	public void setGyroscope(int value, int axis) {
		if(axis==1)
			gyroX = value;
		else if(axis==2)
			gyroY = value;
		else if(axis==3)
			gyroZ = value;
	}
	
	//updates the magnetometer value
	public void setMagnetometer(int value, int axis) {
		if(axis==1)
			magX = value;
		else if(axis==2)
			magY = value;
		else if(axis==3)
			magZ = value;
	}
	
	//sets the time for recieved ping from arduino
	public void setPing(long time) {
		this.pingTimer = time;
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
	
	//returns the number of thrusters
	public int getNumThrusters() {
		return numThrusters;
	}
	
	//returns the number of motors
	public int getNumMotors() {
		return numMotors;
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
	
	//return 0 if no leds are on
	//return 1 if one led is on
	//return 2 if both leds are on
	public int getLedState() {
		int count = 0;
		if(ledArray1==true)
			count++;
		if(ledArray2==true)
			count++;
		return count;
	}
	
	//returns the value of temperature sensor
	//returns temperature upto 2 decimal places
	public double getTemperature() {
		temperature = (int)temperature + (double)((int)((temperature-(int)temperature)*100))/100;
		return temperature;
	}
	
	//returns the value of pressure sensor
	//returns pressure upto 2 decimal places
	public double getPressure() {
		pressure = (int)pressure + (double)((int)((pressure-(int)pressure)*100))/100;
		return pressure;
	}
	
	//return depth value
	//returns depth upto 2 decimal places
	public double getDepth() {
		double depth = ((pressure-101300)/(fluidDensity*9.80665));
		depth = (int)depth + (double)((int)((depth-(int)depth)*100))/100;
		return depth;
	}
	
	//returns the accelerometer value
	public int[] getAccelerometer() {
		return (new int[] {accX, accY, accZ});
	}
	
	//returns the Gyroscope value
	public int[] getGyroscope() {
		return (new int[] {gyroX, gyroY, gyroZ});
	}
	
	//returns the magnetometer value
	public int[] getMagnetometer() {
		return (new int[] {magX, magY, magZ});
	}
	
	//returns the status of the last ping from arduino
	public boolean getPingStatus() {
		if(System.currentTimeMillis()-pingTimer>pingInterval) 
			return false;
		return true;
	}
	
	//displays all thruster values
	public void dispThrusterValues() {
		for(int i=0;i<thrusterVal.length;i++) {
			System.out.println("Thruster " + (i+1) + ": " + thrusterVal[i]);
		}
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
