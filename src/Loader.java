import java.io.*;
import java.util.*;
import net.java.games.input.Controller;

public class Loader {

	/*
	 * Loader class is the starting point of this controller
	 * Responsible for initializing the list of all connected controllers
	 * Responsible for initializing and launching the main UI 
	 */
	HashMap<String,Controller> connectedControllers;
	
	public Loader() {
		initConnectedControllers();
	}
	
	public static void main(String[] args) {
		Loader load = new Loader();
		(new DiscoverControllers(load.connectedControllers)).discover();
		for(Map.Entry<String, Controller> entry:load.connectedControllers.entrySet()) {
			if(entry.getValue().getType().toString()=="Stick" && entry.getValue().poll())
				new Thread(new JoystickInputReader(new JoystickContainer(entry.getValue()))).start();
		}
	}

	//initialized the list of all controllers connected to the system
	//this list is a global controller list
	private void initConnectedControllers() {
		connectedControllers = new HashMap<String,Controller>();
	}

}
