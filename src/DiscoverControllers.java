import java.io.*;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/*
 * Discovers all controllers connected to the system
 * Use this class to rediscover all the controllers connected to the system in case any controller
 * is not detected or disconnected during operation
 * Gives a unique id to each discovered controller and adds the controllers to the 
 * global connectedControllers list.
 * 
 * Includes a static block to turn off unidentified windows version. 
 */
public class DiscoverControllers {
	
	HashMap<String,Controller> connectedControllers;
	Controller connectedControllerList[];
	
	public DiscoverControllers(HashMap<String,Controller> connectedControllers) {
		this.connectedControllers = connectedControllers;
	}
	
	//generate the list of controllers and start assigning id to each controller
	public void discover() {
		getControllerList();
		if(connectedControllers.size()>0)
			//if connectedControllers contains entries, remove all entries
			removeAllIdentifiedControllers();
		startAssignId();
	}
	
	private void removeAllIdentifiedControllers() {
		String keys[] = new String[connectedControllers.size()];
		connectedControllers.keySet().toArray(keys);
		for(String key:keys) {
			connectedControllers.remove(key);
		}
	}
	
	//assigns a unique identifier to the connected controllers
	private void startAssignId() {
		for(Controller c:connectedControllerList) {
			if(!connectedControllers.containsValue(c)) {
				//controllers of this type are not encountered
				assignId(c.getType().toString());
			}
		}
	}
	
	//search all controllers of the same type and assign id
	private void assignId(String type) {
		int count = 1;
		for(Controller c:connectedControllerList) {
			//check type
			if(c.getType().toString()==type && !connectedControllers.containsValue(c)) {
				//assign id as 'type+count'
				String id = c.getType().toString() + Integer.toString(count++);
				//check if id is assigned
				while(connectedControllers.containsKey(id)) {
					//generate new id
					id = c.getType().toString() + Integer.toString(count++);
				}
				//key not used previously
				connectedControllers.put(id, c);
				//System.out.println(id + " " + c.getName() + " " + c.getType());
			}
		}
	}

	//retrieves the list of connected controllers to the default environment.
	private void getControllerList() {
		try {
			connectedControllerList = createDefaultEnvironment().getControllers();
		} catch (ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//This method creates the environment everytime it is called to obtain a new list
	//of connected controllers
	@SuppressWarnings("unchecked")
	private static ControllerEnvironment createDefaultEnvironment() throws ReflectiveOperationException {
		// Find constructor (class is package private, so we can't access it directly)
		Constructor<ControllerEnvironment> constructor = (Constructor<ControllerEnvironment>)Class.forName("net.java.games.input.DefaultControllerEnvironment").getDeclaredConstructors()[0];
		// Constructor is package private, so we have to deactivate access control checks
		constructor.setAccessible(true);
		// Create object with default constructor
		return constructor.newInstance();
	}
	
	
	//Fix windows 8 warnings by defining a working plugin
	static {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				String os = System.getProperty("os.name", "").trim();
				if (os.startsWith("Windows 10") || os.startsWith("Windows 8") || os.startsWith("Windows 8.1")) {  // 8, 8.1 etc.
					// disable default plugin lookup
					System.setProperty("jinput.useDefaultPlugin", "false");
					// set to same as windows 7 (tested for windows 8 and 8.1)
					System.setProperty("net.java.games.input.plugins", "net.java.games.input.DirectAndRawInputEnvironmentPlugin");
				}
				return null;
			}
		});
	}
}
