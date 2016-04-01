import java.io.*;
import java.util.*;

/*
 * Container class for storing task and mapping with a button
 * Object of this class is created by configuratorController class and stored by 
 * any controller class (e.g. ManeuveringJoystick or roboticArmJoystick)
 * If a task name is set to toggle, no other tasks can be coupled with this button
 * One button may act as toggle for multiple tasks. 
 */
public class ButtonTask {
	private String taskName;
	private String taskType;
	private int code;
	private int buttonNumber;
	
	public ButtonTask(String taskName, String taskType,int  code, int buttonNumber) {
		this.taskName = taskName;  //name of task for user reference
		this.taskType = taskType;  //type of task for user reference
		this.code = code;   // Code used by arduino to identify component connected to arduino
		this.buttonNumber = buttonNumber; //button number of the joystick holding object of this class
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
	
	public void setbuttonNumber(int buttonNumber) {
		this.buttonNumber = buttonNumber;
	}
	
	public int getButtonNumber() {
		return buttonNumber;
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
