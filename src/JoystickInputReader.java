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
	TCPSender tcpSender;
	
	public JoystickInputReader(JoystickContainer jc, Socket client) {
		this.jContainer = jc;
		this.client = client;
		checkTasks();
	}
	
	private void checkTasks() {
		//check if this joystick controls maneuvering
		if(jContainer.containsAxisTaskType("Maneuver")) {
			Runnable mJ = new ManeuveringJoystick(jContainer, client);
			new Thread(mJ).start();
		}
		//check if this joystick controls robotic arm
		else if(jContainer.containsAxisTaskType("RoboticArm")) {
			Runnable rJ = new RoboticArmJoystick(jContainer, client);
			new Thread(rJ).start();
		}
	}

	@Override
	public void run() {
		//Run while the controller is still connected
		while(jContainer.getPoll()) {
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
