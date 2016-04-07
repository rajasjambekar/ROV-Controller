import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*
 * This thread will perform the misc tasks such as camera servo control, led control, etc
 */
public class MiscTaskJoystick implements Runnable{
	
	private JoystickContainer jC;
	private Socket client;
	private ThreadEnable threadEnable;
	private DataAccumulator dataStore;
	private boolean ledArray1;
	private boolean ledArray2;
	private boolean led1ZeroValTracker; //checks if button released/axis value 0 before toggle led
	private boolean led2ZeroValTracker; //checks if button released/axis value 0 before toggle led
	private int noLed = 2;
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
		ledArray1 = false;
		ledArray2 = false;
		led2ZeroValTracker = true;
		led2ZeroValTracker = true;
		cam1Servo = new int[cam1ServoCount];
		cam2Servo = new int[cam2ServoCount];

		try {
			//tcpSender = new TCPSender(this.client);
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
		while(threadEnable.getThreadState() && jC.getPoll()) {
			updateDataAccumulator();
			dataStore.dispThrusterValues();
			sleep(10);
		}
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
	
	private void setLedVal(int ledNo, float newVal, int code) {
		if(ledNo<=noLed) {
			//-1 is set as button not pressed value in setVal
			//0 is the value when joystick 
			//check if led button/axis has returned to default position before toggle led
			if(ledNo==1) {
				if(newVal==-1 || newVal==0) {
					led1ZeroValTracker = true;
				}
				else if(led1ZeroValTracker){
					led1ZeroValTracker = false;
					ledArray1 = !ledArray1;
					//tcpSender.sendData(code, ledArray1);
				}
			}
			else if(ledNo==2) {
				if(newVal==-1 || newVal==0) {
					led2ZeroValTracker = true;
				}
				else if(led2ZeroValTracker){
					led2ZeroValTracker = false;
					ledArray2 = !ledArray2;
					//tcpSender.sendData(code, ledArray2);
				}
			}
		}
	}

	private void setCamServoVal(int camNo, int servoNo, float newVal, int code, int dir) {
		if(newVal>=0) {
			int newAngle = minServoAngle + (int) ((newVal/100)*(maxServoAngle - minServoAngle));
			if(newAngle<(maxServoAngle - minServoAngle)/2 - 5)
				newAngle -= newAngle/10;
			else if(newAngle>(maxServoAngle - minServoAngle)/2 + 5)
				newAngle += newAngle/10;
			else if(newAngle>=((maxServoAngle - minServoAngle)/2 - 5) && newAngle<=((maxServoAngle - minServoAngle)/2 + 5))
				newAngle = (maxServoAngle - minServoAngle)/2;
			
			if(camNo==1 && servoNo<cam1Servo.length && newAngle>=0 && cam1Servo[servoNo]!=newAngle) {
				cam1Servo[servoNo] = newAngle;
				//tcpSender.sendData(code, cam1Servo[servoNo]);
			}
			else if(camNo==2 && servoNo<cam2Servo.length && newAngle>=0 && cam2Servo[servoNo]!=newAngle) {
				cam2Servo[servoNo] = newAngle;
				//tcpSender.sendData(code, cam2Servo[servoNo]);
			}
		}
		else {
			if(newVal==-2) {
				//button pressed
				if(dir==1) {
					//min
					if(camNo==1 && servoNo<cam1Servo.length && cam1Servo[servoNo]>=minServoAngle+10) {
						cam1Servo[servoNo] -= 10;
						//tcpSender.sendData(code, cam1Servo[servoNo]);
					}
					else if(camNo==2 && servoNo<cam2Servo.length && cam2Servo[servoNo]>=minServoAngle+10) {
						cam2Servo[servoNo] -= 10;
						//tcpSender.sendData(code, cam2Servo[servoNo]);
					}
				}
				else if(dir==2) {
					//max
					if(camNo==1 && servoNo<cam1Servo.length && cam1Servo[servoNo]<=maxServoAngle-10) {
						cam1Servo[servoNo] += 10;
						//tcpSender.sendData(code, cam1Servo[servoNo]);
					}
					else if(camNo==2 && servoNo<cam2Servo.length && cam2Servo[servoNo]<=maxServoAngle-10) {
						cam2Servo[servoNo] += 10;
						//tcpSender.sendData(code, cam2Servo[servoNo]);
					}
				}
			}
		}
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
}
