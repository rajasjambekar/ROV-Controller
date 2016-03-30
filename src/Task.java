import java.io.*;
import java.util.*;

/*
 * Container class for storing task and mapping with a button
 * Object of this class is created by configuratorController class and stored by 
 * any controller class (e.g. ManeuveringJoystick or roboticArmJoystick)
 */
public class Task {
	private String taskName;
	private int code;
	private int buttonNumber;
	
	public Task(String taskName,int  code, int buttonNumber) {
		this.taskName = taskName;  //name of task for user reference
		this.code = code;   // Code used by arduino to identify component connected to arduino
		this.buttonNumber = buttonNumber; //button number of the joystick holding object of this class
	}
	
	public void setTaskName(String taskName) {
		this.taskName = taskName;
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
}
