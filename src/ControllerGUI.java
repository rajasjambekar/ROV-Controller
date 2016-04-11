import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import net.java.games.input.Controller;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class ControllerGUI{
	
	@FXML MenuBar menuBar;
	
	@FXML MenuItem menuRediscover;
	@FXML MenuItem menuControllerConfig;
	@FXML MenuItem menuQuit;
	@FXML MenuItem menuStartEngines;
	@FXML MenuItem menuStopEngines;
	
	//@FXML MediaView mediaView;
	@FXML Canvas video1Canvas;
	
	private HashMap<String,Controller> connectedControllers;
	Socket client;
	private List<String> lines;
	private MenuItem[] configMmenuItemList;
	private MenuItem[] enginesMmenuItemList;
	private ThreadEnable threadEnable;
	private String configFile;
	private String[] controllerTypeList;
	private ArrayList<JoystickContainer> joystickContainerList;
	DataAccumulator dataStore;
	
	@FXML
    private void initialize() {
		configMmenuItemList = new MenuItem[] {menuRediscover, menuControllerConfig, menuQuit};
		enginesMmenuItemList = new MenuItem[] {menuStartEngines, menuStopEngines};
		setHandler();
    }
	
	private void setHandler() {
		menuRediscover.setOnAction(configMenuHandler);
		menuControllerConfig.setOnAction(configMenuHandler);
		menuQuit.setOnAction(configMenuHandler);
		menuStartEngines.setOnAction(engineMenuHandler);
		menuStopEngines.setOnAction(engineMenuHandler);
	}
	
	//init non-fxml content
	public void init() {
		threadEnable = new ThreadEnable();
		controllerTypeList = new String[] {"Stick", "Keyboard", "Mouse", "Gamepad"};
		reDiscoverControllers();
    	configFile = "Config.txt";
    	readConfigurationFile();
    	if(!matchConfiguration())
    		openControllerConfiguration();
    }
	
	//handles save button press
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
    
  //handles save button press
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
	
	//rediscover all connected controllers
	private void reDiscoverControllers() {
		connectedControllers = new HashMap<String,Controller>();
		(new DiscoverControllers(this.connectedControllers)).discover();
	}
	
	//stops threads started by startEngines like joystickInputReader, TCPReceiver, GUIUpdater
	//changes boolean threadEnable to false
	protected void stopEngines() {
		threadEnable.setThreadState(false);
		//close the tcp connection with arduino
		//closeTcpConnection();
	}

	//starts threads for joystickInputReader, TCPReceiver, GUIUpdater
	//passes boolean threadEnable to threads to control them from this class
	private void startEngines() {
		//create a new tcp connection with arduino
		//startTcpConnection();
		//reads the latest configuration from the config file
		readConfigurationFile();
		//change the threadEnable state to true to allow running of threads
		threadEnable.setThreadState(true);
		//init a new dataStore object
		dataStore = new DataAccumulator();
		
		initLists();
		createControllerContainers();
	}
	
	//test code
	//for vlc rtsp
	void startVlc() {
		JFrame f = new JFrame();
		f.setLocation(100, 100);
		f.setSize(1000, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		Canvas c = new Canvas();
		c.setBackground(Color.BLACK);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(c);
		f.add(p);
		
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:/Program Files (x86)/VideoLAN/VLC");
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		MediaPlayerFactory mpf = new MediaPlayerFactory();
		EmbeddedMediaPlayer emp = mpf.newEmbeddedMediaPlayer(new Win32FullScreenStrategy(f));
		emp.setVideoSurface(mpf.newVideoSurface(c));
		
		String file = "test.mp4";
		emp.prepareMedia(file);
		emp.play();
		/*HeadlessMediaPlayer mPlayer = mpf.newHeadlessMediaPlayer();

		    String mrl = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";

		    String options = ":sout=#transcode{vcodec=h264,venc=x264{cfr=16},scale=1,acodec=mp4a,ab=160,channels=2,samplerate=44100}"
		            + ":file{dst=C:/Users/the man/yahoo.mp4}";

		    mPlayer.playMedia(mrl, options);*/
	}
	
	private void initLists() {
		joystickContainerList = new ArrayList<JoystickContainer>();
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
				int axisNo = Integer.parseInt(parts[3].substring(4));
				int toggleNo = Integer.parseInt(parts[4]);
				AxisTask task = new AxisTask(name, type, code, axisNo, toggleNo);
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
	
	//check if string is equal to any controller type
	//do not match the entire string but match contains
    private boolean checkEqualsControllerType(String string) {
    	for(String type:controllerTypeList) {
    		if(string.contains(type))
    			return true;
    	}
		return false;
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
	
	//start tcp connection
	private void startTcpConnection(){
	   String serverName = "192.168.0.102";
	   int port = 23;
	   System.out.println("Connecting to " + serverName +" on port " + port);
	   try {
		   client = new Socket(serverName, port);
	   } catch (IOException e) {
		   e.printStackTrace();
	   }
	   System.out.println("Just connected to " + client.getRemoteSocketAddress());
	}
	
	//closes tcp connection
	private void closeTcpConnection() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
