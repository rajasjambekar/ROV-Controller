import java.io.*;
import java.util.*;

/*
 * Container class for storing task and mapping with an axis
 * Object of this class is created by configuratorController class and stored by 
 * any controller class (e.g. ManeuveringJoystick or roboticArmJoystick)
 */
public class AxisTask {
	private String taskName;
	private String taskType;
	private int code;
	private int axisNumber;  //x - 0, y - 1, z - 2
	private int toggleButton = 0;
	
	public AxisTask(String taskName, String taskType, int  code, int axisNumber) {
		this.taskName = taskName;  //name of task for user reference
		this.taskType = taskType;  //type of task for user reference
		this.code = code;   // Code used by arduino to identify component connected to arduino
		this.axisNumber = axisNumber; //button number of the joystick holding object of this class
	}
	
	//check if this task requires press of toggle button
	public boolean containsToggleButton() {
		if(toggleButton==0)
			return false;
		return true;
	}
	
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	
	public void setcode(int  code) {
		this.code = code;
	}
	
	public void setAxisNumber(int axisNumber) {
		this.axisNumber = axisNumber;
	}
	
	public void setToggleButtonNumber(int toggleButtonNumber) {
		this.toggleButton = toggleButtonNumber;
	}
	
	public int getAxisNumber() {
		return axisNumber;
	}
	
	//default is 0
	public int getToggleButtonNumber() {
		return toggleButton;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getTaskName() {
		return taskName;
	}
	
	public String getTaskType() {
		return taskType;
	}
}
