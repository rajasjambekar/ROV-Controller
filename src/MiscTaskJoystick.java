import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*
 * This thread will perform the misc tasks such as camera servo control, led control, etc
 * The reading and setting of new servo values is done with a one second interval
 * This is because the joystick analog input is very sensitive and does not react well to 
 * incremental/decremental value change
 * Each servo direction is associated with a timer which is checks the timegap between each read
 */
public class MiscTaskJoystick implements Runnable{
	
	private JoystickContainer jC;
	private Socket client;
	private TCPSender tcpSender;
	private ThreadEnable threadEnable;
	private DataAccumulator dataStore;
	private boolean ledArray1;			//led1 on/off toggle
	private boolean ledArray2;			//led2 on/off toggle
	private boolean led1ZeroValTracker; //checks if button released/axis value 0 before toggle led
	private boolean led2ZeroValTracker; //checks if button released/axis value 0 before toggle led
	private int noLed = 2;				//number of leds
	private int[] cam1Servo;	//servos for camera1	- pan,tilt
	private int[] cam2Servo;	//servos for camera2	- pan,tilt
	private int servoAngleJump = 10;	//jump 10degrees for change in servo angle
	private long c1s1LTimer;	//timers to check time since last command
	private long c1s1RTimer;
	private long c1s2LTimer;
	private long c1s2RTimer;
	private long c2s1LTimer;
	private long c2s1RTimer;
	private long c2s2LTimer;
	private long c2s2RTimer;
	private long timeGap = 500;	//time gap for abv timers
	private int noCams = 2;		//number of cameras used
	private int cam1ServoCount = 2;		//no of servos on cam1
	private int cam2ServoCount = 2;		//no of servos on cam2
	private int minServoAngle = 0;		//minimum angle of servo
	private int defaultServoAngle = 90;		//minimum angle of servo
	private int maxServoAngle = 180;		//minimum angle of servo
	
	public MiscTaskJoystick(JoystickContainer jC, Socket client, ThreadEnable threadEnable, DataAccumulator dataStore) {
		this.jC = jC;
		this.client = client;
		this.threadEnable = threadEnable;
		this.dataStore = dataStore;
		cam1Servo = new int[cam1ServoCount];
		cam2Servo = new int[cam2ServoCount];
		led2ZeroValTracker = true;
		led2ZeroValTracker = true;
		turnOffLed();
		resetServoPos();

		try {
			tcpSender = new TCPSender(this.client, threadEnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		c1s1LTimer = System.currentTimeMillis();
		c1s1RTimer = System.currentTimeMillis();
		c1s2LTimer = System.currentTimeMillis();
		c1s2RTimer = System.currentTimeMillis();
		c2s1LTimer = System.currentTimeMillis();
		c2s1RTimer = System.currentTimeMillis();
		c2s2LTimer = System.currentTimeMillis();
		c2s2RTimer = System.currentTimeMillis();
		
	}

	@Override
	public void run() {
		//check if controller is still connected
		//Stop thread from executing if controller gets disconnected
		//check if tcp is connected
		//Stop thread from executing if tcp gets disconnected
		while(threadEnable.getThreadState() && threadEnable.getTcpState() && jC.getPoll()) {
			setVal();
			updateDataAccumulator();
			dataStore.dispServoValues();
			dataStore.dispLed();
			sleep(10);
		}
		//send stop values to arduino immediately
		System.out.println("Stopping Leds");
		sendStopVal();
		//update values in dataAccumulator one last time
		updateDataAccumulator();
		dataStore.dispServoValues();
		dataStore.dispLed();
	}
	
	//updates the values on object of DataAccumulator
	private void updateDataAccumulator() {
		dataStore.setCameraServo(1, 0, cam1Servo[0]);
		dataStore.setCameraServo(1, 1, cam1Servo[1]);
		dataStore.setCameraServo(2, 0, cam2Servo[0]);
		dataStore.setCameraServo(2, 1, cam2Servo[1]);
		dataStore.setLedValue(ledArray1, ledArray2);
	}
	
	//gets the input from 
	private void setVal() {
		//get raw axes data
		float axesValPercent[] = new float[jC.getAxisCount()];
		jC.getAxesData(axesValPercent);
		
		//check all axisTasks
		ArrayList<AxisTask> axisTaskList = jC.getAxisTaskList();
		for(int i=0;i<axisTaskList.size();i++) {
			AxisTask task = axisTaskList.get(i);
			if(task.getTaskType().equalsIgnoreCase("Led") || task.getTaskType().equalsIgnoreCase("CamServo")) {
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
					//task not triggered by toggle button
					//if toggle button is pressed for this axis do not read value
					if(!jC.checkAxisToggleButtonPressed(task.getAxisNumber())) {
						//task not triggered by toggle button
						calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode());
					}
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
			if(task.getTaskType().equalsIgnoreCase("Led") || task.getTaskType().equalsIgnoreCase("CamServo")) {
				float val = -1;
				//check if button pressed
				if(buttonVal[task.getButtonNumber()-1])
					val = -2;
				calVal(task.getTaskName(), val, task.getCode());
			}
		}
	}
	
	//categorizes the task by taskname and direction and checks time since last check
	private void calVal(String taskName, float newVal, int code) {
		if(taskName.equalsIgnoreCase("C1S1_LEFT") && System.currentTimeMillis()-c1s1LTimer>timeGap) {
			int dir = 1;
			setCamServoVal(1, 0, newVal, code, dir);
			c1s1LTimer = System.currentTimeMillis();
		}
		else if(taskName.equalsIgnoreCase("C1S1_RIGHT") && System.currentTimeMillis()-c1s1RTimer>timeGap) {
			int dir = 2;
			setCamServoVal(1, 0, newVal, code, dir);
			c1s1RTimer = System.currentTimeMillis();
		}
		else if(taskName.equalsIgnoreCase("C1S2_DOWN") && System.currentTimeMillis()-c1s2LTimer>timeGap) {
			int dir = 1;
			setCamServoVal(1, 1, newVal, code, dir);
			c1s2LTimer = System.currentTimeMillis();
		}
		else if(taskName.equalsIgnoreCase("C1S2_UP") && System.currentTimeMillis()-c1s2RTimer>timeGap) {
			int dir = 2;
			setCamServoVal(1, 1, newVal, code, dir);
			c1s2RTimer = System.currentTimeMillis();
		}
		else if(taskName.equalsIgnoreCase("C2S1_LEFT") && (System.currentTimeMillis()-c2s1LTimer)>timeGap) {
			int dir = 1;
			setCamServoVal(2, 0, newVal, code, dir);
			c2s1LTimer = System.currentTimeMillis();
		}
		else if(taskName.equalsIgnoreCase("C2S1_RIGHT") && System.currentTimeMillis()-c2s1RTimer>timeGap) {
			int dir = 2;
			setCamServoVal(2, 0, newVal, code, dir);
			c2s1RTimer = System.currentTimeMillis();
		}
		else if(taskName.equalsIgnoreCase("C2S2_DOWN") && System.currentTimeMillis()-c2s2LTimer>timeGap) {
			int dir = 1;
			setCamServoVal(2, 1, newVal, code, dir);
			c2s2LTimer = System.currentTimeMillis();
		}
		else if(taskName.equalsIgnoreCase("C2S2_UP") && System.currentTimeMillis()-c2s2RTimer>timeGap) {
			int dir = 2;
			setCamServoVal(2, 1, newVal, code, dir);
			c2s2RTimer = System.currentTimeMillis();
		}
		else if(taskName.equalsIgnoreCase("LED1")) {
			setLedVal(1, newVal, code);
		}
		else if(taskName.equalsIgnoreCase("LED2")) {
			setLedVal(2, newVal, code);
		}
	}
	
	//sets the new value for led
	private void setLedVal(int ledNo, float newVal, int code) {
		if(ledNo<=noLed) {
			//-1 is set as button not pressed value in setVal
			//50 is the value when joystick is at rest
			//check if led button/axis has returned to default position before toggle led
			if(ledNo==1) {
				//button not pressed value or joystick stick at rest position value
				if(newVal==-1 || newVal==50) {
					led1ZeroValTracker = true;
				}
				else if(led1ZeroValTracker){
					led1ZeroValTracker = false;
					ledArray1 = !ledArray1;
					System.out.println("1");
					if(ledArray1) {
						tcpSender.sendData(code, 1);
					}
					else {
						tcpSender.sendData(code, 0);
					}
				}
			}
			else if(ledNo==2) {
				//button not pressed value or joystick stick at rest position value
				if(newVal==-1 || newVal==50) {
					led2ZeroValTracker = true;
				}
				else if(led2ZeroValTracker){
					led2ZeroValTracker = false;
					ledArray2 = !ledArray2;
					System.out.println("2");
					if(ledArray2) {
						tcpSender.sendData(code, 1);
					}
					else {
						tcpSender.sendData(code, 0);
					}
				}
			}
		}
	}

	//rounds off newVal by subtracting last digit
	private void setCamServoVal(int camNo, int servoNo, float newVal, int code, int dir) {
		//will be -1/-2 if button triggered task
		if(newVal>=0) {
			//convert % to angle
			int newAngle = minServoAngle + (int) ((newVal/100)*(maxServoAngle - minServoAngle));
			
			//round of angle
			if(newAngle<(maxServoAngle - minServoAngle)/2 - 5)
				newAngle -= newAngle%10;
			else if(newAngle>(maxServoAngle - minServoAngle)/2 + 5)
				newAngle += newAngle%10;
			else if(newAngle>=((maxServoAngle - minServoAngle)/2 - 5) && newAngle<=((maxServoAngle - minServoAngle)/2 + 5))
				newAngle = (maxServoAngle - minServoAngle)/2;
			
			if(camNo==1 && servoNo<cam1Servo.length) {
				//if dir == 1 reduce value
				if(dir==1 && cam1Servo[servoNo]>=servoAngleJump && newAngle<=50) {
					cam1Servo[servoNo] -= servoAngleJump;
					tcpSender.sendData(code, cam1Servo[servoNo]);
				}
				//if dir == 2 increment value
				else if(dir==2 && cam1Servo[servoNo]<=(180-servoAngleJump) && newAngle>=130) {
					cam1Servo[servoNo] += servoAngleJump;
					tcpSender.sendData(code, cam1Servo[servoNo]);
				}

			}
			else if(camNo==2 && servoNo<cam2Servo.length) {
				if(dir==1 && cam2Servo[servoNo]>=servoAngleJump && newAngle<=50) {
					cam2Servo[servoNo] -= servoAngleJump;
					tcpSender.sendData(code, cam2Servo[servoNo]);
				}
				else if(dir==2 && cam2Servo[servoNo]<=(180-servoAngleJump) && newAngle>=130) {
					cam2Servo[servoNo] += servoAngleJump;
					tcpSender.sendData(code, cam2Servo[servoNo]);
				}
			}
		}
		else {
			if(newVal==-2) {
				//button pressed
				if(dir==1) {
					//min
					if(camNo==1 && servoNo<cam1Servo.length && cam1Servo[servoNo]>=minServoAngle+servoAngleJump) {
						cam1Servo[servoNo] -= servoAngleJump;
						tcpSender.sendData(code, cam1Servo[servoNo]);
					}
					else if(camNo==2 && servoNo<cam2Servo.length && cam2Servo[servoNo]>=minServoAngle+servoAngleJump) {
						cam2Servo[servoNo] -= servoAngleJump;
						tcpSender.sendData(code, cam2Servo[servoNo]);
					}
				}
				else if(dir==2) {
					//max
					if(camNo==1 && servoNo<cam1Servo.length && cam1Servo[servoNo]<=maxServoAngle-servoAngleJump) {
						cam1Servo[servoNo] += servoAngleJump;
						tcpSender.sendData(code, cam1Servo[servoNo]);
					}
					else if(camNo==2 && servoNo<cam2Servo.length && cam2Servo[servoNo]<=maxServoAngle-servoAngleJump) {
						cam2Servo[servoNo] += servoAngleJump;
						tcpSender.sendData(code, cam2Servo[servoNo]);
					}
				}
			}
		}
	}
	
	//turns off leds
	private void turnOffLed() {
		ledArray1 = false;
		ledArray2 = false;
	}
	
	private void resetServoPos() {
		cam1Servo[0] = defaultServoAngle;
		cam1Servo[1] = defaultServoAngle;
		cam2Servo[0] = defaultServoAngle;
		cam2Servo[1] = defaultServoAngle;
	}
	
	//displays current val of all thrusters
	private void dispValues() {
		System.out.println("Cam1: Servo1: " + cam1Servo[0] + " Cam1: Servo2: " + cam1Servo[1]);
		System.out.println("Cam2: Servo1: " + cam2Servo[0] + " Cam2: Servo2: " + cam2Servo[1]);
		System.out.println("LED1: " + ledArray1 + " LED2: " + ledArray2);
	}
	
	private void sleep(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void sendStopVal() {
		turnOffLed();
		resetServoPos();
		ArrayList<AxisTask> axisTaskList = jC.getAxisTaskList();
		for(AxisTask task: axisTaskList) {
			if(task.getTaskType().equalsIgnoreCase("Led")) {
				int code = task.getCode();
				tcpSender.sendData(code, 0);
				sleep(10);
			}
			else if(task.getTaskType().equalsIgnoreCase("CamServo")) {
				int code = task.getCode();
				tcpSender.sendData(code, defaultServoAngle);
				sleep(10);
			}
		}
		ArrayList<ButtonTask> buttonTaskList = jC.getButtonTaskList();
		for(ButtonTask task: buttonTaskList) {
			if(task.getTaskType().equalsIgnoreCase("Led") && !task.getTaskName().equalsIgnoreCase("Toggle")) {
				int code = task.getCode();
				tcpSender.sendData(code, 0);
				sleep(10);
			}
			else if(task.getTaskType().equalsIgnoreCase("CamServo") && !task.getTaskName().equalsIgnoreCase("Toggle")) {
				int code = task.getCode();
				tcpSender.sendData(code, defaultServoAngle);
				sleep(10);
			}
		}
	}
}
