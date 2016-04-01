import java.io.*;
import java.net.Socket;
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
	private Socket client;
	TCPSender tcpSender;
	int thrusterValRange;
	
	public ManeuveringJoystick(JoystickContainer jC, Socket client) {
		this.jC = jC;
		this.client = client;
		try {
			tcpSender = new TCPSender(this.client);
		} catch (Exception e) {
			e.printStackTrace();
		}
		thrusterVal = new int[numThrusters];
		thrusterValRange = THRUSTER_FULL_FW-THRUSTER_FULL_BW;
		//set all thrusters with stop val
		stopThrusters();
	}

	@Override
	public void run() {
		//check if controller is still connected
		//Stop thread from executing if controller gets disconnected
		//thread will restart when controller is rediscovered
		//Also keep checking if controller contains task to maneuver rov
		while(jC.getPoll() && jC.containsAxisTaskType("Maneuver")) {
			setThrusterVal();
			//dispValues();
			sleep(1000);
		}
	}
	
	//gets the relevant axes data and calculates the corresponding thruster data
	public void setThrusterVal() {
		//get raw axes data
		float axesValPercent[] = new float[jC.getAxisCount()];
		jC.getAxesData(axesValPercent);
		
		//update values for all tasks of type maneuver
		ArrayList<AxisTask> axisTaskList = jC.getAxisTaskList();
		for(int i=0;i<axisTaskList.size();i++) {
			AxisTask task = axisTaskList.get(i);
			if(task.getTaskType()=="Maneuver") {
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
					calVal(task.getTaskName(), axesValPercent[task.getAxisNumber()], task.getCode());
				}
			}
		}
	}
	
	//calculate the final value of the task. If any other task is to be added, add a condition 
	//checking for the taskName.
	//Send the value over tcp only if the new value deviates from the previous value over a limit
	//code value is combination of 2 thruster codes each 2 digit thus making 4 digit code
	//while sending over tcp, separate the codes of both thrusters and send them after *100
	private void calVal(String taskName, float axisVal, int code) {
		if(taskName=="LFRT") {
			//horizontal movement thrusters
			int tVal = THRUSTER_FULL_BW + thrusterValRange - (int) ((axisVal/100)*(thrusterValRange));
			int code1 = (code/100)*100;
			int code2 = (code%100)*100;
			if(compareThrusterVal(thrusterVal[0], tVal)) {
				//set new val and send over tcp
				thrusterVal[0] = tVal;
				tcpSender.sendData(code1, tVal);
			}
			if(compareThrusterVal(thrusterVal[1], tVal)) {
				thrusterVal[1] = tVal;
				tcpSender.sendData(code2, tVal);
			}
		}
		else if(taskName=="FWBW") {
			//fw/bw thrusters
			int tVal = THRUSTER_FULL_BW + thrusterValRange - (int) ((axisVal/100)*(thrusterValRange));
			int code1 = (code/100)*100;
			int code2 = (code%100)*100;
			if(compareThrusterVal(thrusterVal[2], tVal)) {
				//set new val and send over tcp
				thrusterVal[2] = tVal;
				tcpSender.sendData(code1, tVal);
			}
			if(compareThrusterVal(thrusterVal[3], tVal)) {
				thrusterVal[3] = tVal;
				tcpSender.sendData(code2, tVal);
			}
		}
		else if(taskName=="UPDN") {
			//up/down thrusters
			int tVal = THRUSTER_FULL_BW + thrusterValRange - (int) ((axisVal/100)*(thrusterValRange));
			int code1 = (code/100)*100;
			int code2 = (code%100)*100;
			if(compareThrusterVal(thrusterVal[4], tVal)) {
				//set new val and send over tcp
				thrusterVal[4] = tVal;
				tcpSender.sendData(code1, tVal);
			}
			if(compareThrusterVal(thrusterVal[5], tVal)) {
				thrusterVal[5] = tVal;
				tcpSender.sendData(code2, tVal);
			}
		}
		else if(taskName=="CLKA") {
			//rotation thrusters.
			//one motor fw and other reverse
			int t0 = THRUSTER_FULL_FW - (int) ((axisVal/100)*(thrusterValRange));
			int t1 = THRUSTER_FULL_BW + (int) ((axisVal/100)*(thrusterValRange));
			int code1 = (code/100)*100;
			int code2 = (code%100)*100;
			if(compareThrusterVal(thrusterVal[0], t0)) {
				//set new val and send over tcp
				thrusterVal[0] = t0;
				tcpSender.sendData(code1, t0);
			}
			if(compareThrusterVal(thrusterVal[1], t1)) {
				thrusterVal[1] = t1;
				tcpSender.sendData(code2, t1);
			}
		}
	}
	
	//check if there is a change in thruster value
	private boolean compareThrusterVal(int prevVal, int newVal) {
		if((newVal>prevVal && newVal-prevVal>100) || (newVal<prevVal && prevVal-newVal>100)) {
			//thruster value changed
			return true;
		}
		return false;
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
			e.printStackTrace();
		}
	}
}
