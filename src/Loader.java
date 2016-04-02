import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import net.java.games.input.Controller;

public class Loader {

	/*
	 * Loader class is the starting point of this controller
	 * Responsible for initializing the list of all connected controllers
	 * Responsible for initializing and launching the main UI 
	 */
	HashMap<String,Controller> connectedControllers;
	Socket client;
	
	public Loader() {
		initConnectedControllers();
		try {
			//startTcpConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Loader load = new Loader();
		(new DiscoverControllers(load.connectedControllers)).discover();
		for(Map.Entry<String, Controller> entry:load.connectedControllers.entrySet()) {
			if(entry.getValue().getType().toString()=="Stick" && entry.getValue().poll()) {
				JoystickContainer jc = new JoystickContainer(entry.getValue());
				try {
					addTasks(jc);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new Thread(new JoystickInputReader(jc, load.client)).start();
			}
		}
	}
	
	private static void addTasks(JoystickContainer jc) throws Exception {
		BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter tasks:");
		String str = "";
		while((str=buff.readLine()).contains(":")) {
			String[] parts = str.split(":");
			AxisTask task = new AxisTask(parts[0],parts[1],Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));
			task.setToggleButtonNumber(Integer.parseInt(parts[4]));
			if(Integer.parseInt(parts[4])!=0) {
				ButtonTask bTask = new ButtonTask("Toggle", parts[1], 0, Integer.parseInt(parts[4]));
				jc.addButtonTask(bTask);
			}
			jc.addAxisTask(task);
			System.out.println("Enter tasks:");
		}
	}

	//start tcp connection
	@SuppressWarnings("unused")
	private void startTcpConnection() throws Exception {
	   String serverName = "192.168.1.177";
	   int port = 23;
	   System.out.println("Connecting to " + serverName +" on port " + port);
	   client = new Socket(serverName, port);
	   System.out.println("Just connected to " + client.getRemoteSocketAddress());
	}

	//initialized the list of all controllers connected to the system
	//this list is a global controller list
	private void initConnectedControllers() {
		connectedControllers = new HashMap<String,Controller>();
	}

}
