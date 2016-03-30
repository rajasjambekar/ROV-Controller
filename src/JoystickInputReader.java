import java.io.*;
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
	
	
	public JoystickInputReader(JoystickContainer jc) {
		this.jContainer = jc;
		checkTasks();
	}
	
	private void checkTasks() {
		//check if this joystick controls maneuvering
		if(jContainer.getTaskList().contains("Maneuver")) {
			//Runnable mJ = new ManeuveringJoystick();
		}
		//check if this joystick controls robotic arm
		else if(jContainer.getTaskList().contains("RoboticArm")) {
			//Runnable rJ = new RoboticArmJoystick();
		}
	}

	@Override
	public void run() {
		//Run while the controller is still connected
		while(jContainer.getPoll()) {
			//read raw data from controller
			jContainer.readAxes();
			jContainer.readButtons();
			jContainer.dispValues();
			sleep(1000);
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
