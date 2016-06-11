import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import net.java.games.input.Controller;

public class ControllerGUI{
	
	@FXML private MenuItem menuRediscover;
	@FXML private MenuItem menuControllerConfig;
	@FXML private MenuItem menuQuit;
	@FXML private MenuItem menuStartEngines;
	@FXML private MenuItem menuStopEngines;
	
	@FXML private MenuBar menuBar;

    @FXML private ProgressBar TH_LR1_PB;
    @FXML private ProgressBar TH_LR2_PB;
    @FXML private ProgressBar TH_FB1_PB;
    @FXML private ProgressBar TH_FB2_PB;
    @FXML private ProgressBar TH_UD1_PB;
    @FXML private ProgressBar TH_UD2_PB;
    
    @FXML private Label ARM_LR;
    @FXML private Label ARM_FB;
    @FXML private Label ARM_UD;
    @FXML private Label ARM_GPR_RT;
    @FXML private Label ARM_GPR_TILT;
    @FXML private Label ARM_GPR_OC;
    @FXML private Label CLAW_OC;
    @FXML private Label CAM_PAN;
    @FXML private Label CAM_TILT;
    @FXML private Label temperature;
    @FXML private Label pressure;
    @FXML private Label depth;
    @FXML private Label engine_state;
    @FXML private Label comm_state;
    @FXML private Label msgBoard;
    @FXML private Label txt_timer;
    
    @FXML private Button btnSensorUpdate;
    @FXML private Button button_timer_reset;
    @FXML private Button button_timer_start;
    
    @FXML private Circle engine_color;
    @FXML private Circle comm_color;
    @FXML private Circle th_color;
    @FXML private Circle led_color;
    
    @FXML private ToggleButton th_toggle;
    
    @FXML private Pane rearView;
    @FXML private Pane leftView;

	
	private HashMap<String,Controller> connectedControllers;
	private Socket client;
	private String serverName = "192.168.0.10";
	private int port = 23;
	private List<String> lines;
	private static ThreadEnable threadEnable;
	private String configFile;
	private ArrayList<JoystickContainer> joystickContainerList;
	private DataAccumulator dataStore;
	private Label armManLabelList[];
	private Label armGrpLabelList[];
	private Label camLabelList[];
	private Label sensorLabelList[];
	private ProgressBar pBarList[];
	
	private boolean sensorDataRequestFlag;
	private long sensorDataRequestTimer;
	private long sensorDataRequestTimeGap = 2000;

	private int THRUSTER_STOP = 1500;	//thruster stop val
	private int THRUSTER_FULL_FW = 1850;	//thruster full forward val
	private int THRUSTER_FULL_BW = 1150;	//thruster full backward val
	
	private long timerEndTime;
	private long timerElapsedTime = 0;
	private double timerDisplayTime;
	private double timerResetTime = 0.00;
	private boolean timerState = false;
	private Calendar date = Calendar.getInstance();
	private Calendar endDate = Calendar.getInstance();
	private Calendar dateCurrent = Calendar.getInstance();
	
	Stage primaryStage;
	
	@FXML
    private void initialize() {
		setHandler();
    }
	
	private void setHandler() {
		menuRediscover.setOnAction(configMenuHandler);
		menuControllerConfig.setOnAction(configMenuHandler);
		menuQuit.setOnAction(configMenuHandler);
		menuStartEngines.setOnAction(engineMenuHandler);
		menuStopEngines.setOnAction(engineMenuHandler);
		btnSensorUpdate.setOnAction(UIButtonHandler);
		th_toggle.setOnAction(toggleButtonHandler);
		button_timer_start.setOnAction(timerEventHandler);
		button_timer_reset.setOnAction(timerEventHandler);
	}
	
	//init non-fxml content
	public void init(Stage stage) {
		primaryStage = stage;
		//catch window 'x' button and stop engines
		primaryStage.setOnCloseRequest(event -> {
			stopEngines();
			timerState = false;
		});
		threadEnable = new ThreadEnable();
		reDiscoverControllers();
    	configFile = "Config.txt";
    	readConfigurationFile();
    	if(!matchConfiguration())
    		openControllerConfiguration();
    	armManLabelList = new Label[] {ARM_LR, ARM_FB, ARM_UD};
    	armGrpLabelList = new Label[] {ARM_GPR_RT, ARM_GPR_TILT, ARM_GPR_OC, CLAW_OC};
    	camLabelList = new Label[] {CAM_PAN, CAM_TILT};
    	sensorLabelList = new Label[] {temperature, pressure, depth};
    	pBarList = new ProgressBar[] {TH_LR1_PB, TH_LR2_PB, TH_FB1_PB, TH_FB2_PB, TH_UD1_PB, TH_UD2_PB};
	}
	
	//handles configuation menu button events
	final EventHandler<ActionEvent> toggleButtonHandler = new EventHandler<ActionEvent>(){
        @Override
        public void handle(final ActionEvent event) {
        	ToggleButton item = (ToggleButton) event.getSource();
        	if(item==th_toggle && th_toggle.isSelected()) {
        		thrustersEnable();
        	}
        	//Do not allow configuration of controllers when threads are running
        	else if(item==th_toggle && !th_toggle.isSelected()) {
        		thrustersDisable();
        	}
        }
    };
    
    //handles timer events
	final EventHandler<ActionEvent> timerEventHandler = new EventHandler<ActionEvent>(){
		@Override
		public void handle(final ActionEvent event) {
			Button item = (Button) event.getSource();
			//start/stop timer
			if(item==button_timer_start) {
				//start timer countdown
				if(button_timer_start.textProperty().getValue().equalsIgnoreCase("Start Timer")) {
					if(dateCurrent.compareTo(endDate)==0) {
						//get new calendar instances
						date = Calendar.getInstance();
						
						//get time after 15minutes
						endDate = Calendar.getInstance();
						endDate.add(Calendar.MINUTE, 15);
					}
					else {
						//get new date instance and adjust for stopped time
						date = Calendar.getInstance();
						date.add(Calendar.SECOND, -dateCurrent.get(Calendar.SECOND));
						date.add(Calendar.MINUTE, -dateCurrent.get(Calendar.MINUTE));
						date.add(Calendar.HOUR, -dateCurrent.get(Calendar.HOUR));
					}
					//switch timer flag
					timerState = true;
					button_timer_start.textProperty().setValue("Stop Timer");
					createTimerUpdaterTask();
				}
				//stop timer countdown
				else if(button_timer_start.textProperty().getValue().equalsIgnoreCase("Stop Timer")) {
					timerState = false;
					button_timer_start.textProperty().setValue("Start Timer");
				}
	      	}
	      	//Reset timer to max time
	      	else if(item==button_timer_reset) {
	      		date = Calendar.getInstance();
	      		endDate.setTime(date.getTime());
	      		dateCurrent.setTime(date.getTime());
	      		txt_timer.textProperty().setValue("00:00");
	      	}
	      }
	};
	
	//handles configuation menu button events
	final EventHandler<ActionEvent> configMenuHandler = new EventHandler<ActionEvent>(){
        @Override
        public void handle(final ActionEvent event) {
        	MenuItem item = (MenuItem) event.getSource();
        	//Do not allow rediscovery of controllers when threads are running
        	if(item==menuRediscover && !threadEnable.getThreadState()) {
        		reDiscoverControllers();
        	}
        	//Do not allow configuration of controllers when threads are running
        	else if(item==menuControllerConfig && !threadEnable.getThreadState()) {
        		openControllerConfiguration();
        	}
        	//shut down threads before exit
        	else if(item==menuQuit) {
        		stopEngines();
        		Platform.exit();
        	}
        }
    };
    
    //handles UI button events
  	final EventHandler<ActionEvent> UIButtonHandler = new EventHandler<ActionEvent>(){
  		@Override
  		public void handle(final ActionEvent event) {
  			Button item = (Button) event.getSource();
  			//Do not allow rediscovery of controllers when threads are running
  			if(item==btnSensorUpdate && threadEnable.getThreadState()) {
  				if(item.getText().equalsIgnoreCase("Start Update")) {
  					item.setText("Stop Update");
  					sensorDataRequestFlag = true;
  				}
  				else if(item.getText().equalsIgnoreCase("Stop Update")) {
  					item.setText("Start Update");
  					sensorDataRequestFlag = false;
  				}
  			}
  		}
  	};
  
  	//handles engines menu button events
  	final EventHandler<ActionEvent> engineMenuHandler = new EventHandler<ActionEvent>(){
  		@Override
  		public void handle(final ActionEvent event) {
  			MenuItem item = (MenuItem) event.getSource();
  			if(item==menuStartEngines && !threadEnable.getThreadState()) {
  				startEngines();
  			}
  			else if(item==menuStopEngines && threadEnable.getThreadState()) {
  				stopEngines();
  			}
  		}
    };
    
    //creates a task to update the timer time
    private void createTimerUpdaterTask() {
		Task<Void> task = new Task<Void>() {
		  @Override
		  public Void call() throws Exception {
		    while (timerState) {
		    	Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		            	int sec = dateCurrent.get(Calendar.SECOND);
						updateTimerTime();	//get the new time for timer
						updateTimerDisplay();	//display time on UI
		            }
		          });
		      Thread.sleep(100);
		    }
			return null;
		  }
		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}
    
    //change the thrusterenable flag in thread enable to allow thruster values to
    //be computed and sent to arduino
    //also send enable flag to arduino
    private void thrustersEnable() {
    	sendData(1999,1);
    	threadEnable.setThrusterEnableState(true);
		th_color.setFill(Color.GREEN);
    }
    
    //change the thrusterenable flag in thread enable to stop thruster values from
    //being computed and sent to arduino
  //also send disable flag to arduino
    private void thrustersDisable() {
    	sendData(1999,0);
    	threadEnable.setThrusterEnableState(false);
    	th_toggle.selectedProperty().setValue(false);;
		th_color.setFill(Color.RED);
    }
    
    //creates a new tcpSender object and sends command to arduino
    private void sendData(int code, int value) {
    	try {
			new TCPSender(client, threadEnable).sendData(code, value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	//rediscover all connected controllers
	private void reDiscoverControllers() {
		connectedControllers = new HashMap<String,Controller>();
		(new DiscoverControllers(this.connectedControllers)).discover();
	}
	
	//stops threads started by startEngines like joystickInputReader, TCPReceiver, GUIUpdater
	//changes boolean threadEnable to false
	protected void stopEngines() {
		engine_color.setFill(Color.RED);
		threadEnable.setThreadState(false);
		
		//disable thrusters
		thrustersDisable();
		
		//wait 100ms to ensure all stop data is sent over tcp
		sleep(100);
		
		//close the tcp connection with arduino
		closeTcpConnection();
		client = null;
		
		//disable thruster toggle button
		th_toggle.disarm();
		//enable start engines menu option
  		menuStartEngines.disableProperty().set(false);
  		//disable stop engines menu option
  		menuStopEngines.disableProperty().set(true);
  		//enable re discover controllers menu option
  		menuRediscover.disableProperty().set(false);
  		//enable configure controllers engines menu option
  		menuControllerConfig.disableProperty().set(false);
	}

	//starts threads for joystickInputReader, TCPReceiver, GUIUpdater
	//passes boolean threadEnable to threads to control them from this class
	private void startEngines() {
		engine_color.setFill(Color.GREEN);
		msgBoard.setText("");
		client = null;
		//re initialize the flag to automatically request sensor data
		sensorDataRequestFlag = false;
		btnSensorUpdate.setText("Start Update");
		//init a new dataStore object
		dataStore = new DataAccumulator();
		//initialize joystick container list
		joystickContainerList = new ArrayList<JoystickContainer>();

		//create a new tcp connection with arduino on separate thread		
		tcpConnect();
		//check tcp connected state
		if(threadEnable.getTcpState()) {
			//reads the latest configuration from the config file
			readConfigurationFile();
			
			//change the threadEnable state to true to allow running of threads
			threadEnable.setThreadState(true);
			createTCPReceiver();
			
			//create receiver thread
			createControllerContainers();
			sleep(50);
			createUIUpdaterTask();
			
			//enable stop engines menu option
	  		menuStopEngines.disableProperty().set(false);
	  		//disable start engines menu option
	  		menuStartEngines.disableProperty().set(true);
	  		//disable re discover controllers menu option
	  		menuRediscover.disableProperty().set(true);
	  		//disable configure controllers engines menu option
	  		menuControllerConfig.disableProperty().set(true);
		}
		else {
			stopEngines();
		}
	}

	//attempt a new connection with arduino over tcp
	private void tcpConnect() {
		Thread th = new Thread() {
			public void run() {
				startTcpConnection();
			}
		};
		th.start();
		//wait for thread to finish attempt to connect
		try {
			th.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createUIUpdaterTask() {
		Task<Void> task = new Task<Void>() {
			  @Override
			  public Void call() throws Exception {
			    while (threadEnable.getThreadState()) {
			    	Platform.runLater(new Runnable() {
			            @Override
			            public void run() {
			            	if(!threadEnable.getTcpState() || !dataStore.getPingStatus()) {	
			            		//System.out.println(!dataStore.getPingStatus());
			            		//tcp not connected
			            		comm_color.setFill(Color.RED);
			            		stopEngines();
			            		msgBoard.setText("TCP Disconnected. Engines Shutoff. Try to restart engines");
			            	}
			            	if(threadEnable.getJoystickDisconnectedState()) {
			            		//joystick disconnected during operation
			            		msgBoard.setText("Joystick Disconnected. Use Re-Discover Option");
			            		stopEngines();
			            	}
			            	else {
			            		//joystick reconnected. Remove the joystick-disconnected message
			            		if(msgBoard.getText().equalsIgnoreCase("Joystick Disconnected. Use Re-Discover Option")) {
			            			msgBoard.setText("");
			            		}
			            	}
			            	if(threadEnable.getThreadState()) {
			            		//threads are working. Engines are on.
				            	updateThrusterPBar();
				            	updateDCMState();
				    			updateServoState();
				    			updateSensorData();
				            	updateMPU9250State();
				    			updateLedState();
			            	}
			            }
			          });
			      Thread.sleep(10);
			    }
				return null;
			  }
			};
			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();
	}
	
	//creates controller container objects and add them to global container lists
	private void createControllerContainers() {
		for(Map.Entry<String, Controller> entry:connectedControllers.entrySet()) {
			if(entry.getValue().getType().toString()=="Stick" && entry.getValue().poll()) {
				JoystickContainer jc = new JoystickContainer(entry.getValue());
				try {
					addTasks(jc);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Assign containers and start threads for controllers with tasks only
				if(jc.getTaskCount()>0) {
					joystickContainerList.add(jc);
					new Thread(new JoystickInputReader(jc, client, threadEnable, dataStore)).start();
				}
			}
		}
	}

	//read the configuration stored in lines and assign tasks to respective controllers
	private void addTasks(JoystickContainer jc) {
		for(int i=0;i<lines.size();i++) {
			String parts[] = lines.get(i).split(":");
			if(parts[3].contains("AXIS")) {
				String name = parts[6];
				String type = parts[1];
				int code = Integer.parseInt(parts[5]);
				int axisNo = Integer.parseInt(parts[3].substring(parts[3].length()-1));
				int toggleNo = Integer.parseInt(parts[4]);
				float axisSide = 1f;
				if(parts[3].startsWith("-"))
					axisSide = -1f;
				AxisTask task = new AxisTask(name, type, code, axisNo, toggleNo, axisSide);
				jc.addAxisTask(task);
				
				//if toggle button is set for the axis task, then add the button to the 
				//buttontasklist for the joystickController
				if(Integer.parseInt(parts[4])!=0) {
					ButtonTask bTask = new ButtonTask("Toggle", type, 0, toggleNo);
					jc.addButtonTask(bTask);
				}
			}
			else if(parts[3].contains("BUTTON")) {
				String name = parts[6];
				String type = parts[1];
				int code = Integer.parseInt(parts[5]);
				int buttonNo = Integer.parseInt(parts[3].substring(6));
				ButtonTask task = new ButtonTask(name, type, code, buttonNo);
				jc.addButtonTask(task);
			}
		}
	}

	//launches a new stage for configuring controllers to perform tasks
	private void openControllerConfiguration() {
		Parent root;
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(this.getClass().getResource("InputControlScheme.fxml"));
			root = loader.load();
			Stage stage = new Stage();
			stage.setScene(new Scene(root, 1000, 700));
			stage.show();
            ConfigurationUI configUI = loader.getController();
            configUI.init(connectedControllers);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//matches the controller configuration with connected controllers to check for missing controllers
	//if controllers are missing then open controllerConfigurationUI
	private boolean matchConfiguration() {
		for(int i=0;i<lines.size();i++) {
			String parts[] = null;
			if(lines.get(i).contains(":")) {
				parts = lines.get(i).split(":");
				if(!connectedControllers.containsKey(parts[2]) && !parts[2].equalsIgnoreCase("Unassigned")) {
					return false;
				}
			}
		}
		return true;
	}

	//read config file
	private void readConfigurationFile() {
    	if(Files.exists(Paths.get(configFile))) {
    		try {
				lines = Files.readAllLines(Paths.get(configFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
	
	//creates a new instance of TCPReceiver thread and starts it
	private void createTCPReceiver() {
		try {
			new Thread(new TCPReceiver(client, threadEnable, dataStore)).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//start tcp connection
	private void startTcpConnection(){
	   System.out.println("Connecting to " + serverName +" on port " + port);
	   try {
		   client = new Socket(serverName, port);
	   } catch (IOException e) {
		   e.printStackTrace();
	   }
	   //check if tcp is connected and has a valid socket address
	   if(client.getRemoteSocketAddress().toString().length()>0) {
		   threadEnable.setTcpState(true);
		   System.out.println("Just connected to " + client.getRemoteSocketAddress());
		   comm_color.setFill(Color.GREEN);
	   }
	}
	
	//closes tcp connection
	private void closeTcpConnection() {
		if(client!=null) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			client = null;
			threadEnable.setTcpState(false);
			comm_color.setFill(Color.RED);
			System.out.println("Tcp connection closed");
		}
	}
	
	private void sleep(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//gets dc motor values from data accumulator class and sets values for motors
	private void updateDCMState() {
		//get number of thrusters from data accumulator
		int numMotors = dataStore.getNumMotors();
		//verify that number of motors is same as number of labels
		if(numMotors==(armManLabelList.length + armGrpLabelList.length)) {
			//update motor data
			getArmManValueText(dataStore.getMotorVal(0), armManLabelList[0], "LEFT", "RIGHT");
			getArmManValueText(dataStore.getMotorVal(1), armManLabelList[1], "FW", "BW");
			getArmManValueText(dataStore.getMotorVal(2), armManLabelList[2], "UP", "DOWN");
			getArmManValueText(dataStore.getMotorVal(3), armGrpLabelList[0], "ACLK", "CLK");
			getArmManValueText(dataStore.getMotorVal(4), armGrpLabelList[1], "UP", "DOWN");
			getArmManValueText(dataStore.getMotorVal(5), armGrpLabelList[2], "OPEN", "CLOSE");
			getArmManValueText(dataStore.getMotorVal(6), armGrpLabelList[3], "OPEN", "CLOSE");
		}
	}

	//update servo data
	private void updateServoState() {
		String cam1Servo1 = Integer.toString(dataStore.getCameraServoPos(1, 0));
		String cam1Servo2 = Integer.toString(dataStore.getCameraServoPos(1, 1));
		camLabelList[0].textProperty().set(cam1Servo1);
		camLabelList[1].textProperty().set(cam1Servo2);
	}

	//update sensor data
	private void updateSensorData() {
		//request update of temperature and pressure sensor data after every 2 seconds
		if(sensorDataRequestFlag && (System.currentTimeMillis()-sensorDataRequestTimer>sensorDataRequestTimeGap)) {
			sendData(2100, 0);
			sendData(3100, 0);
			sensorDataRequestTimer = System.currentTimeMillis();
		}
		String temperature = Double.toString(dataStore.getTemperature());
		String pressure = Double.toString(dataStore.getPressure());
		String depth = Double.toString(dataStore.getDepth());
		sensorLabelList[0].textProperty().setValue(temperature);
		sensorLabelList[1].textProperty().setValue(pressure);
		sensorLabelList[2].textProperty().setValue(depth);
	}
	
	//update led state
	private void updateLedState() {
		int state = dataStore.getLedState();
		if(state==0) {
			//no leds on
			//display red color
			led_color.setFill(Color.RED);
		}
		else if(state==1) {
			//one led on. 
			//display yellow color
			led_color.setFill(Color.YELLOW);
		}
		else if(state==2) {
			//both leds on. 
			//display green color
			led_color.setFill(Color.GREEN);
		}
	} 
	
	//convert value of arm maneuver dc motor to text
	private void getArmManValueText(int val, Label lbl, String opt1, String opt2) {
		if(val==255) {
			lbl.setText(opt1);
		}
		else if(val==-255) {
			lbl.setText(opt2);
		}
		else if(val==0) {
			//display previous state of motor
			String prevText = lbl.getText();
			if(prevText.contains(opt1))
				lbl.setText("Stopped " + opt1);
			else if(prevText.contains(opt2))
				lbl.setText("Stopped " + opt2);
			else
				lbl.setText("Stopped");
		}
		else {
			lbl.setText("Invalid Value");
		}
	}

	//gets thruster values from data accumulator class and sets values for thrusters
	private void updateThrusterPBar() {
		//get number of thrusters from data accumulator
		int numThrusters = dataStore.getNumThrusters();
		//verify that number of thrusters is same as number of progress bars
		if(numThrusters==pBarList.length) {
			//update all progress bar values
			for(int i=0;i<numThrusters;i++) {
				//get thruster data
				int val = dataStore.getThrusterVal(i);
				//convert data to progress bar value
				double pVal = convertThrusterData(val);
				//set orientation for pbar. pBar is right-left when thruster is reversed
				if(pVal<0) {
					pBarList[i].nodeOrientationProperty().set(NodeOrientation.RIGHT_TO_LEFT);
					//set value to pBar
					pBarList[i].progressProperty().set(-pVal);
				}
				else {
					pBarList[i].nodeOrientationProperty().set(NodeOrientation.LEFT_TO_RIGHT);
					//set value to pBar
					pBarList[i].progressProperty().set(pVal);
				}
			}
		}
	}

	//converts thruster value into progress bar data (0-1)
	private double convertThrusterData(int val) {
		double pVal = 0;
		if(val<THRUSTER_STOP) {
			pVal = -(double)(THRUSTER_STOP - val)/(double)(THRUSTER_STOP-THRUSTER_FULL_BW);
		}
		else if(val>THRUSTER_STOP) {
			pVal = (double)(val - THRUSTER_STOP)/(double)(THRUSTER_FULL_FW-THRUSTER_STOP);
		}
		else {
			pVal = 0;
		}
		return pVal;
	}
	
	//updates the rotation of rearView pane and leftView pane
	private void updateMPU9250State() {
		int acc[] = dataStore.getAccelerometer();
		rearView.rotateProperty().setValue(acc[0]);
		leftView.rotateProperty().set(-acc[1]);
	}
	
	//updates the timer
	private void updateTimerTime() {
		if(timerState) {
			//get current time
			dateCurrent = Calendar.getInstance();
			Calendar temp = Calendar.getInstance();
			temp.setTime(date.getTime());
			temp.add(Calendar.MINUTE, 1);
			//timer countdown not complete
			if(dateCurrent.compareTo(temp)<0) {
				//subtract start time from current time to get the elapsed time
				dateCurrent.add(Calendar.SECOND, -date.get(Calendar.SECOND));
				dateCurrent.add(Calendar.MINUTE, -date.get(Calendar.MINUTE));
				dateCurrent.add(Calendar.HOUR, -date.get(Calendar.HOUR));
				//System.out.println(dateCurrent.get(Calendar.MINUTE) + ":" + dateCurrent.get(Calendar.SECOND));
			}
			//timer countdown complete
			else {
				timerState = false;
			}
		}
	}
	
	//updates the time on the timer
	private void updateTimerDisplay() {
		if(!timerState)
			button_timer_start.textProperty().setValue("Start Timer");
		else if(timerState){
			StringBuilder str = new StringBuilder();
			if(dateCurrent.get(Calendar.MINUTE)<10) {
				str.append("0" + dateCurrent.get(Calendar.MINUTE));
			}
			else {
				str.append(dateCurrent.get(Calendar.MINUTE));
			}
			str.append(":");
			if(dateCurrent.get(Calendar.SECOND)<10) {
				str.append("0" + dateCurrent.get(Calendar.SECOND));
			}
			else {
				str.append(dateCurrent.get(Calendar.SECOND));
			}
			txt_timer.textProperty().setValue(str.toString());
		}
	}
}
