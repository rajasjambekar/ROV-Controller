import java.io.*;
import java.util.*;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Component.Identifier;

/*
 * Container class for joystick data
 */
public class JoystickContainer {
	float joystickMaxVal = 1;
	float joystickMinVal = -1;
	private int axesCount = 0;
	private int buttonCount = 0;
	private int hatSwitchCount = 0;
	private double axes[] = null;
	private boolean buttons[] = null;
	private double hatSwitches[] = null;
	private Controller joystick = null;
	//component list maps each axis and button to physical axis and button on the joystick for 
	//quick access to object while reading value
	private ArrayList<Component> buttonComponentList = null, axesComponentList = null, hatSwitchComponentList = null;
	private ArrayList<AxisTask> axisTaskList;
	private ArrayList<ButtonTask> buttonTaskList;
	
	public JoystickContainer(Controller c) {
		joystick = c;
		discoverAxisCount();
		discoverButtonCount();
		discoverHatSwitchCount();
		axes = new double[getAxisCount()];
		buttons = new boolean[getButtonCount()];
		hatSwitches = new double[getHatSwitchCount()];
		buttonComponentList = new ArrayList<Component>();
		axesComponentList = new ArrayList<Component>();
		hatSwitchComponentList = new ArrayList<Component>();
		axisTaskList = new ArrayList<AxisTask>();
		buttonTaskList = new ArrayList<ButtonTask>();
		setComponentList();
	}
	
	//displays current val of all axes and buttons
	public void dispValues() {
		for(int i=0;i<axes.length;i++)
			System.out.println(i + ": " + axes[i]);
		for(int i=0;i<buttons.length;i++)
			System.out.println(i + ": " + buttons[i]);
		for(int i=0;i<hatSwitches.length;i++)
			System.out.println(i + ": " + hatSwitches[i]);
	}
	
	//reads data from all axes of this joystick
	public void readAxes() {
		//check if joystick is still connected before reading value
		for(int i=0;i<axesComponentList.size() && getPoll();i++) {
			//read data while chance of disconnection is least
			double axisValue = axesComponentList.get(i).getPollData();
			//convert axisValue to percentage
			double axisValueInPercentage = getAxisValueInPercentage(axisValue);
			//round off boundary values around 5%
			axisValueInPercentage = roundOffValInPercentage(axisValueInPercentage);
			//System.out.println(axisValueInPercentage);
			//check type of axis and store its data
			Identifier axisIdentifier = axesComponentList.get(i).getIdentifier();
			if(axisIdentifier == Component.Identifier.Axis.X) {
				this.axes[0] = axisValueInPercentage;
			}
			else if(axisIdentifier == Component.Identifier.Axis.Y) {
				this.axes[1] = axisValueInPercentage;
			}
			else if(axisIdentifier == Component.Identifier.Axis.Z) {
				this.axes[2] = axisValueInPercentage;
			}
		}
	}
	
	//reads data from all buttons of this joystick
	public void readButtons() {
		//check if joystick is still connected before reading value
		for(int i=0;i<buttonComponentList.size() && getPoll();i++) {
			//read data while chance of disconnection is least
			if(buttonComponentList.get(i).getPollData() == 0.0f) {
				this.buttons[i] = false;
			}
			else {
				this.buttons[i] = true;
			}
		}
	}
	
	//reads data from hat switch of this joystick
	public void readHatSwitch() {
		//check if joystick is still connected before reading value
		for(int i=0;i<hatSwitchComponentList.size() && getPoll();i++) {
			//read data while chance of disconnection is least
			double hatSwitchValue = hatSwitchComponentList.get(i).getPollData();
			double hatSwitchInPercentage = getAxisValueInPercentage(hatSwitchValue);
			this.hatSwitches[i] = hatSwitchInPercentage;
		}
	}
	
	//gets state of only the toggle button associated with task t
	public boolean getToggleButtonState(AxisTask t) {
		return buttons[t.getToggleButtonNumber() - 1];
	}
	
	//check if any task has registered a togglebutton for this axis
	//if yes, check if button pressed
	public boolean checkAxisToggleButtonPressed(int axisNo) {
		for(AxisTask axisTask:axisTaskList) {
			if(axisTask.containsToggleButton() && axisTask.getAxisNumber()==axisNo) {
				if(buttons[axisTask.getToggleButtonNumber()-1])
					return true;
			}
		}
		return false;
	}
	
	//get the entire axisTaskList
	public ArrayList<AxisTask> getAxisTaskList() {
		return axisTaskList;
	}
	
	//get the entire buttonTaskList
	public ArrayList<ButtonTask> getButtonTaskList() {
		return buttonTaskList;
	}
	
	//get axisTask
	public AxisTask getAxisTask(String taskName) {
		for(AxisTask t: axisTaskList) {
			if(t.getTaskName().equalsIgnoreCase(taskName)) {
				return t;
			}
		}
		return null;
	}
	
	//get buttonTask
	public ButtonTask getButtonTask(String taskName) {
		for(ButtonTask t: buttonTaskList) {
			if(t.getTaskName().equalsIgnoreCase(taskName)) {
				return t;
			}
		}
		return null;
	}
	
	//add task to axisTaskList
	public void addAxisTask(AxisTask t) {
		axisTaskList.add(t);
	}
	
	//add task to buttonTaskList
	//check if button numbers are repeating
	//multi function buttons are allowed but toggle button can only be used to toggle
	public void addButtonTask(ButtonTask t) {
		boolean flag = true;
		for(ButtonTask b:buttonTaskList) {
			if(b.getButtonNumber()==t.getButtonNumber()) {
				//System.out.println(t.getTaskName() + " " + b.getTaskName() );
				if((t.getTaskName().equalsIgnoreCase("Toggle") && !b.getTaskName().equalsIgnoreCase("Toggle"))
						|| (!t.getTaskName().equalsIgnoreCase("Toggle") && b.getTaskName().equalsIgnoreCase("Toggle"))) {
					flag = false;
				}
			}
		}
		if(flag) {
			buttonTaskList.add(t);
		}
	}
	
	//removes task from axisTaskList
	public void removeAxisTask(String taskName) {
		for(AxisTask t: axisTaskList) {
			if(t.getTaskName().equalsIgnoreCase(taskName)) {
				axisTaskList.remove(t);
			}
		}
	}
	
	//removes task from buttonTaskList
	public void removeButtonTask(String taskName) {
		for(ButtonTask t: buttonTaskList) {
			if(t.getTaskName().equalsIgnoreCase(taskName)) {
				buttonTaskList.remove(t);
			}
		}
	}
	
	//check whether axisTaskList contains task
	public boolean containsAxisTask(String taskName) {
		for(AxisTask t: axisTaskList) {
			if(t.getTaskName().equalsIgnoreCase(taskName)) {
				return true;
			}
		}
		return false;
	}
	
	//check whether axisTaskList contains task type
	public boolean containsAxisTaskType(String taskType) {
		for(AxisTask t: axisTaskList) {
			//System.out.println(t.getTaskType());
			if(t.getTaskType().equalsIgnoreCase(taskType)) {
				return true;
			}
		}
		return false;
	}
	
	//check whether buttonTaskList contains task type
	public boolean containsButtonTaskType(String taskType) {
		//System.out.println(buttonTaskList.size());
		for(ButtonTask t: buttonTaskList) {
			//System.out.println(t.getTaskType());
			if(t.getTaskType().equalsIgnoreCase(taskType)) {
				return true;
			}
		}
		return false;
	}
	
	//check whether buttonTaskList contains task
	public boolean containsButtonTask(String taskName) {
		for(ButtonTask t: buttonTaskList) {
			if(t.getTaskName().equalsIgnoreCase(taskName)) {
				return true;
			}
		}
		return false;
	}

	//copies data from button to supplied object
	//no need to return buttonData
	//reference of buttons object not to be given for security
	public synchronized void getButtonsData(boolean buttonData[]) {
		for(int i=0; i<buttonData.length; i++) {
			buttonData[i] = buttons[i];
		}
	}
	
	//copies data from axes to supplied object
	//no need to return axesData
	//reference of axes object not to be given for security
	public synchronized void getAxesData(double axesData[]) {
		for(int i=0; i<axes.length && i<axesData.length; i++) {
			axesData[i] = axes[i];
		}
	}
	
	//determines whether controller is connected or not
	public boolean getPoll() {
		return joystick.poll();
	}
	
	//returns number of buttons in this joystick
	public int getButtonCount() {
		return buttonCount;
	}
	
	//returns number of axes present in this joystick
	public int getAxisCount() {
		return axesCount;
	}
	
	//returns number of hat switch present in this joystick
	public int getHatSwitchCount() {
		return hatSwitchCount;
	}
	
	//round off values near boundaries to prevent accidental thruster movement
	private double roundOffValInPercentage(double axisValueInPercentage) {
		//lower boundary
		if(axisValueInPercentage<=5)
			axisValueInPercentage = 0;
		//central boundary
		else if((axisValueInPercentage<=50 && axisValueInPercentage>=45) || ((axisValueInPercentage>=50 && axisValueInPercentage<=55)))
			axisValueInPercentage = 50;
		//higher boundary
		else if(axisValueInPercentage>=95)
			axisValueInPercentage = 100;
		else {
			//axisValueInPercentage -= axisValueInPercentage%10;
		}
		return axisValueInPercentage;
	}
	
	/**
	 * Given value of axis in percentage.
	 * Percentages increases from left/top to right/bottom.
	 * If idle (in center) returns 50, if joystick axis is pushed to the left/top 
	 * edge returns 0 and if it's pushed to the right/bottom returns 100.
	 * 
	 * @return value of axis in percentage.
	 */
	public double getAxisValueInPercentage(double axisValue)
	{
		return ((((joystickMaxVal-joystickMinVal) - (joystickMaxVal - axisValue)) * 100) / 2);
	}
	
	//prefetches all components of the controller and maps them for easier access
	private void setComponentList() {
		//check if joystick is still connected before reading value
		if(getPoll()) {
			//get all controller components
			Component[] controllerComponents = joystick.getComponents();
			for(int i=0;i<controllerComponents.length;i++) {
				//check type of each controllerComponent
				//add them to either axes or button component list
				Identifier componentIdentifier = controllerComponents[i].getIdentifier();
				if(componentIdentifier.getName().matches("^[0-9]*$")) {
					//Component is a button
					//add to button component list
					buttonComponentList.add(controllerComponents[i]);
				}
				else if(componentIdentifier == Component.Identifier.Axis.POV){
					//Component is an axis
					//add to axis component list
					hatSwitchComponentList.add(controllerComponents[i]);
				}
				else if(controllerComponents[i].isAnalog()){
					//Component is an axis
					//add to axis component list
					axesComponentList.add(controllerComponents[i]);
				}
			}
		}
	}

	//Counts and stores the number of analog and POV axes present in this joystick
	private void discoverAxisCount() {
		int count=0;
		Component[] controllerComponents = joystick.getComponents();
		for(int i=0; i<controllerComponents.length;i++) {
			if(controllerComponents[i].isAnalog())
				count++;
		}
		axesCount = count;
	}
	
	
	//Counts and stores the number of buttons present in this joystick
	private void discoverButtonCount() {
		int count=0;
		Component[] controllerComponents = joystick.getComponents();
		for(int i=0; i<controllerComponents.length;i++) {
			Identifier componentIdentifier = controllerComponents[i].getIdentifier();
			if(componentIdentifier.getName().matches("^[0-9]*$"))
				count++;
		}
		buttonCount = count;
	}
	
	//Counts and stores the number of buttons present in this joystick
	private void discoverHatSwitchCount() {
		int count=0;
		Component[] controllerComponents = joystick.getComponents();
		for(int i=0; i<controllerComponents.length;i++) {
			Identifier componentIdentifier = controllerComponents[i].getIdentifier();
			if(componentIdentifier==Component.Identifier.Axis.POV)
				count++;
		}
		hatSwitchCount = count;
	}
	
	//returns connected controller
	public Controller getController() {
		return joystick;
	}
	
	//returns the number of tasks assigned.
	//returns 0 if no tasks assigned
	public int getTaskCount() {
		int count = 0;
		count += axisTaskList.size();
		count += buttonTaskList.size();
		return count;
	}
}
