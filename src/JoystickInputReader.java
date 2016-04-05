import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
 * Thread for monitoring single joystick
 * Checks tasks and dispatches thread for continuous raw data to useful data conversion
 * Continuously calls functions for reading value and sending data using TCPSender object
 */
public class JoystickInputReader implements Runnable {

	private JoystickContainer jContainer;
	private Runnable mJ = null;
	private Runnable rJ = null;
	private Socket client;
	private TCPSender tcpSender;
	private ThreadEnable threadEnable;
	private DataAccumulator dataStore;
	
	public JoystickInputReader(JoystickContainer jc, Socket client, ThreadEnable threadEnable, DataAccumulator dataStore) {
		this.jContainer = jc;
		this.dataStore = dataStore;
		this.client = client;
		this.threadEnable = threadEnable;
		checkTasks();
	}
	
	//creates threads based on type of tasks
	private void checkTasks() {
		//check if this joystick controls maneuvering
		if(jContainer.containsAxisTaskType("Maneuver")) {
			Runnable mJ = new ManeuveringJoystick(jContainer, client, threadEnable, dataStore);
			new Thread(mJ).start();
		}
		//check if this joystick controls robotic arm
		if(jContainer.containsAxisTaskType("RoboticArm")) {
			Runnable rJ = new RoboticArmJoystick(jContainer, client, threadEnable, dataStore);
			new Thread(rJ).start();
		}
		//check if this joystick controls leds, camera servos, etc
		if(jContainer.containsAxisTaskType("Led") || jContainer.containsAxisTaskType("CamServo")) {
			Runnable mTJ = new MiscTaskJoystick(jContainer, client, threadEnable, dataStore);
			new Thread(mTJ).start();
		}
	}

	@Override
	public void run() {
		//Run while the controller is still connected
		while(threadEnable.getThreadState() && jContainer.getPoll()) {
			//read raw data from controller
			jContainer.readAxes();
			jContainer.readButtons();
			//jContainer.dispValues();
			//sleep(10);
		}
	}

	private void sleep(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
