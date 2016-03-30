import java.io.*;
import java.util.*;
import net.java.games.input.Controller;

public class ManeuveringJoystick implements Runnable{

	private int thrusterVal[];			//value of all thrusters
	private int numThrusters = 6;		//number of thrusters
	private int THRUSTER_STOP = 1500;	//thruster stop val
	private int THRUSTER_FULL_FW = 1850;	//thruster full forward val
	private int THRUSTER_FULL_BW = 1150;	//thruster full backward val
	private int tolerancePercent = 5;	//tolerance for boundary values for thrusters
	JoystickContainer jC;
	
	public ManeuveringJoystick(JoystickContainer jC) {
		this.jC = jC;
		thrusterVal = new int[numThrusters];
		//set all thrusters with stop val
		stopThrusters();
	}

	@Override
	public void run() {
		//check if controller is still connected
		//Stop thread from executing if controller gets disconnected
		//thread will restart when controller is rediscovered
		//Also keep checking if controller contains task to maneuver rov
		while(jC.getPoll() && jC.containsTask("Maneuver")) {
			
		}
	}
	
	//gets the relevant axes data and calculates the corresponding thruster data
	public void setThrusterVal() {
		//get raw axes data
		float axesValPercent[] = new float[jC.getAxisCount()];
		jC.getAxesData(axesValPercent);
		boolean toggleButtonState = jC.getToggleButtonState();
		stopThrusters();	//re init thruster values to stop val for safety
		if(!toggleButtonState) {
			//toggle button not pressed
			//normal maneuvering calculations
			calThrusterValNormal(axesValPercent);
		}
		else {
			//toggle button pressed
			//ROV horizonal movement only
		}
	}
	
	//convert raw axes values to thruster values
	//thrusters only work with integer values
	//horizonal movement thrusters are in last positions of array and do not work in normal mode 
	private void calThrusterValNormal(float[] axesValPercent) {
		int thrusterValRange = THRUSTER_FULL_FW-THRUSTER_FULL_BW;
		for(int i=0;i<thrusterVal.length-2 && i<axesValPercent.length-2;i++) {
			thrusterVal[i] = (int) ((axesValPercent[i]/100)*(thrusterValRange)) + THRUSTER_FULL_BW;
		}
	}

	//get thruster values
	public int[] getThrustersVal() {
		return thrusterVal;
	}
	
	//set all thrusters with stop val
	private void stopThrusters() {
		for(int i=0;i<numThrusters;i++) {
			thrusterVal[i] = THRUSTER_STOP;
		}
	}

}
