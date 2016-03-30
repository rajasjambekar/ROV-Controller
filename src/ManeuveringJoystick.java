import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import net.java.games.input.Controller;

public class ManeuveringJoystick implements Runnable{

	private int thrusterVal[];			//value of all thrusters
	private int numThrusters = 6;		//number of thrusters
	private int THRUSTER_STOP = 1500;	//thruster stop val
	private int THRUSTER_FULL_FW = 1850;	//thruster full forward val
	private int THRUSTER_FULL_BW = 1150;	//thruster full backward val
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
			setThrusterVal();
			dispValues();
			sleep(100);
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
			//ROV rotation movement only
			calThrusterValHorizontal(axesValPercent);
		}
	}
	
	//convert raw axes values to thruster values
	//thrusters only work with integer values
	//only fw/bw movement thrusters work in horizontal mode in reverse dir
	//On joystick top left is FW FW and right bottom is BW BW for thrusters
	//on z axis + is fw and - is reverse
	private void calThrusterValNormal(float[] axesValPercent) {
		int thrusterValRange = THRUSTER_FULL_FW-THRUSTER_FULL_BW;
		//horizontal movement thrusters
		thrusterVal[0] = THRUSTER_FULL_BW + thrusterValRange - (int) ((axesValPercent[0]/100)*(thrusterValRange));
		thrusterVal[1] = thrusterVal[0];
		
		//fw/bw thrusters
		thrusterVal[2] = THRUSTER_FULL_BW + thrusterValRange - (int) ((axesValPercent[1]/100)*(thrusterValRange));
		thrusterVal[3] = thrusterVal[2];
	
		//up/down thrusters
		thrusterVal[4] = THRUSTER_FULL_BW + thrusterValRange - (int) ((axesValPercent[2]/100)*(thrusterValRange));
		thrusterVal[5] = thrusterVal[4];
	}
	
	//convert raw axes values to thruster values
	//thrusters only work with integer values
	private void calThrusterValHorizontal(float[] axesValPercent) {
		int thrusterValRange = THRUSTER_FULL_FW-THRUSTER_FULL_BW;
		//for left th[0] runs fw and th[1] runs bw
		//for right vice versa
		thrusterVal[0] = THRUSTER_FULL_BW + thrusterValRange - (int) ((axesValPercent[0]/100)*(thrusterValRange));
		thrusterVal[1] = THRUSTER_FULL_BW + (int) ((axesValPercent[0]/100)*(thrusterValRange));
	}
	
	//displays current val of all thrusters
	public void dispValues() {
		for(int i=0;i<thrusterVal.length;i++)
			System.out.println("Thruster " + (i+1) + ": " + thrusterVal[i]);
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

	private void sleep(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
