import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Component.Identifier;

public class ConfigurationUI {
	
	//choice box is for discovered controllers list
	@FXML private ChoiceBox controllerChooser;
	
	//Labels with id in fxml file
	@FXML private Label lblLeftManCtrlr;
    @FXML private Label lblLeftManComp;
    @FXML private Label lblLeftManTggl;
    @FXML private Label lblRightManCtrlr;
    @FXML private Label lblRightManComp;
    @FXML private Label lblRightManTggl;
    @FXML private Label lblFwManCtrlr;
    @FXML private Label lblFwManComp;
    @FXML private Label lblFwManTggl;
    
    //buttons with id in fxml file
    @FXML private Button btnLeftManSetCtrlr;
    @FXML private Button btnLeftManSetTggl;
    @FXML private Button btnRightManSetCtrlr;
    @FXML private Button btnRightManSetTggl;
    @FXML private Button btnFwManSetCtrlr;
    @FXML private Button btnFwManSetTggl;
    
    @FXML private Button btnSave;
    
    @FXML MenuItem menuClose;
    @FXML MenuBar menuBar;
    
	private HashMap<String,Controller> connectedControllers;
	private List<String> lines;
	private String configFile;
	private Button[] setControllerButtonList;
	private Button[] setToggleButtonList;
	private Label[] controllerLabelList;
	private Label[] componentLabelList;
	private Label[] toggleLabelList;
	private ArrayList<Integer> codeList; //code list for tasks
	private ArrayList<String> taskNameList; //code list for tasks
	
	public void init(HashMap<String,Controller> connectedControllers) {
    	this.connectedControllers = connectedControllers;
    	//(new DiscoverControllers(connectedControllers)).discover();
    	configFile = "Config.txt";
    	codeList = new ArrayList<Integer>();
    	taskNameList = new ArrayList<String>();
    	addControllersToChoiceBox();
    	try {
			readConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	private void addControllersToChoiceBox() {
		String keys[] = new String[connectedControllers.size()];
		connectedControllers.keySet().toArray(keys);
		for(String key:keys) {
			controllerChooser.getItems().add(key);
		}
	}
	
	@FXML
    private void initialize() {
		setControllerButtonList = new Button[] {btnLeftManSetCtrlr, btnRightManSetCtrlr, btnFwManSetCtrlr};
		setToggleButtonList = new Button[] {btnLeftManSetTggl, btnRightManSetTggl, btnFwManSetTggl};
		controllerLabelList = new Label[] {lblLeftManCtrlr, lblRightManCtrlr, lblFwManCtrlr};
		componentLabelList = new Label[] {lblLeftManComp, lblRightManComp, lblFwManComp};
		toggleLabelList = new Label[] {lblLeftManTggl, lblRightManTggl, lblFwManTggl};
    	setButtonHandler1();
    	setButtonHandler2();
    	setButtonHandler3();
    	menuClose.setOnAction(menuHandler);
    }
	
	//for setController buttons
	private void setButtonHandler1() {
		btnLeftManSetCtrlr.setOnAction(setControllerHandler);
		btnRightManSetCtrlr.setOnAction(setControllerHandler);
		btnFwManSetCtrlr.setOnAction(setControllerHandler);
	}
	
	//for setToggle buttons
	private void setButtonHandler2() {
		btnLeftManSetTggl.setOnAction(setToggleHandler);
		btnRightManSetTggl.setOnAction(setToggleHandler);
		btnFwManSetTggl.setOnAction(setToggleHandler);
	}
	
	//for save button
	private void setButtonHandler3() {
		btnSave.setOnAction(saveConfigHandler);
	}
	
	//handles save button press
	final EventHandler<ActionEvent> saveConfigHandler = new EventHandler<ActionEvent>(){
        @Override
        public void handle(final ActionEvent event) {
        	try {
				writeControlConfig();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    };
    
    //handles setToggle button press
    final EventHandler<ActionEvent> setToggleHandler = new EventHandler<ActionEvent>(){
        @Override
        public void handle(final ActionEvent event) {
        	if(controllerChooser.getValue()!=null)
        		handleSetToggleButtonPress((Button) event.getSource());
        }
    };
    
    //handles setController button press
    final EventHandler<ActionEvent> setControllerHandler = new EventHandler<ActionEvent>(){
        @Override
        public void handle(final ActionEvent event) {
        	if(controllerChooser.getValue()!=null)
        		handleSetControllerButtonPress((Button) event.getSource());
        }
    };
    
    //handles close menu item press
  	final EventHandler<ActionEvent> menuHandler = new EventHandler<ActionEvent>(){
  		@Override
  		public void handle(final ActionEvent event) {
  			MenuItem item = (MenuItem) event.getSource();
  			if(item==menuClose) {
  				Stage stage = (Stage) menuBar.getScene().getWindow();
  				stage.close();
  			}
  		}
  	};
    
    //writes the new configuration to file
    private void writeControlConfig() throws IOException {
    	StringBuilder str = new StringBuilder();
    	if(Files.exists(Paths.get(configFile))) {
    		lines = Files.readAllLines(Paths.get(configFile));
    		for(int i=0; i<lines.size() && i<controllerLabelList.length; i++) {
    			String line = lines.get(i);
    			String parts[] = line.split(":");
    			str.append(parts[0]);
    			str.append(":");
    			str.append(parts[1]);
    			str.append(":");
    			str.append(controllerLabelList[i].getText());
    			str.append(":");
    			str.append(componentLabelList[i].getText());
    			str.append(":");
    			str.append(toggleLabelList[i].getText());
    			str.append(":");
    			str.append(parts[5]);
    			str.append(":");
    			str.append(parts[6]);
    			str.append("\n");
    			//System.out.println(str.toString());
    		}
    	}
    	Files.deleteIfExists(Paths.get(configFile));
    	Files.write(Paths.get(configFile), str.toString().getBytes(), StandardOpenOption.CREATE);
    }

	
    private void handleSetControllerButtonPress(Button source) {
    	Label name=null;
    	Label comp=null;
    	//get list of labels to change
    	int i=0;
    	for(i=0; i<setControllerButtonList.length; i++) {
    		if(setControllerButtonList[i]==source) {
    			name=controllerLabelList[i];
    			comp=componentLabelList[i];
    		}
    	}
		String keys[] = new String[connectedControllers.size()];
		connectedControllers.keySet().toArray(keys);
		for(String key:keys) {
			if(controllerChooser.getValue().toString().equals(key.toString())) {
    			String compName = getComponent(connectedControllers.get(key));
				if(compName!="") {
					//check if this button has been attached as toggle button previously
					if(compName.contains("BUTTON") && !checkAssignedToggleButton(i, compName.substring(6))) {
						name.setText(controllerChooser.getValue().toString());
						comp.setText(compName);
						break;
					}
				}
    		}
		}
	}

    private String getComponent(Controller c) {
    	Component[] controllerComponents = c.getComponents();
    	long time = System.currentTimeMillis();
    	//give the user 5 secs to configure the button
    	while(System.currentTimeMillis()-time<5000) {
        	int button = 0;
        	int hatSwitch = 0;
        	//read all the controller components one by one by their type and return if 
        	//any of the components have been pressed
    		for(int i=0;i<controllerComponents.length && c.poll();i++) {
        		Identifier componentIdentifier = controllerComponents[i].getIdentifier();
        		if(componentIdentifier.getName().matches("^[0-9]*$")) {
    				//Component is a button
    				button++;
    				if(controllerComponents[i].getPollData() != 0.0f)
    					return ("BUTTON" + (button));
    			}
        		else if(componentIdentifier == Component.Identifier.Axis.POV) {
        			//Component is a button
        			if(controllerComponents[i].getPollData() != 0.0f)
    					return ("HATSWITCH" + (hatSwitch));
        		}
        		else if(controllerComponents[i].isAnalog()){
    				//Component is an axis
    				if(controllerComponents[i].getPollData()== 1.0f || controllerComponents[i].getPollData()== -1.0f) {
    					if(componentIdentifier == Component.Identifier.Axis.X) {
        					return ("AXIS" + (0));
    					}
    					else if(componentIdentifier == Component.Identifier.Axis.Y) {
    						return ("AXIS" + (1));
    					}
    					else if(componentIdentifier == Component.Identifier.Axis.Z) {
    						return ("AXIS" + (2));
    					}
    				}
    			}
        	}
    	}
    	return "";
	}

    //handles toggle button setting for any task
	private void handleSetToggleButtonPress(Button source) {
    	Label tggl=null;
    	//get list of labels to change
    	int i = 0;
    	for(i=0; i<setControllerButtonList.length; i++) {
    		if(setToggleButtonList[i]==source) {
    			tggl=toggleLabelList[i];
    		}
    	}
    	String keys[] = new String[connectedControllers.size()];
		connectedControllers.keySet().toArray(keys);
		for(String key:keys) {
			if(controllerChooser.getValue().toString().equals(connectedControllers.get(key).getName().toString())) {
    			String toggleButton = getComponent(connectedControllers.get(key));
    			//check if this button has been attached as normal button previously
    			if(toggleButton.contains("BUTTON") && !checkAssignedButton(i, toggleButton.substring(6))) {
					String str = toggleButton.substring(6);
					System.out.println(str.toString());
					if(tggl!=null)
						tggl.setText(str.toString());
				}
    		}
		}
	}
	
	//check if selected toggle button is previously assigned as normal button
    private boolean checkAssignedButton(int i, String toggleButton) {
    	Label controller = controllerLabelList[i];
    	for(int j=0;j<controllerLabelList.length;j++) {
    		if(componentLabelList[j].getText().contains("BUTTON")) {
        		String buttonNo = componentLabelList[j].getText().substring(6);
        		if(controllerLabelList[j].getText().toString() == controller.getText().toString()
        				&& buttonNo==toggleButton)
        			return true;
    			
    		}
    	}
		return false;
	}
    
    //check if selected button is previously assigned as toggle button
    private boolean checkAssignedToggleButton(int i, String button) {
    	Label controller = controllerLabelList[i];
    	for(int j=0;j<controllerLabelList.length;j++) {
    		if(componentLabelList[j].getText().contains("BUTTON")) {
        		String toggleButtonNo = toggleLabelList[j].getText().toString();
        		if(controllerLabelList[j].getText().toString() == controller.getText().toString()
        				&& toggleButtonNo==button)
        			return true;
    			
    		}
    	}
		return false;
	}

	//read config file and set content on the scene
	private void readConfiguration() throws Exception {
    	if(Files.exists(Paths.get(configFile))) {
    		lines = Files.readAllLines(Paths.get(configFile));
    		for(int i=0; i<lines.size() && i<controllerLabelList.length; i++) {
    			String line = lines.get(i);
    			String parts[] = line.split(":");
    			controllerLabelList[i].setText(parts[2]);
    			componentLabelList[i].setText(parts[3]);
    			toggleLabelList[i].setText(parts[4]);
    			codeList.add(Integer.parseInt(parts[5]));
    			taskNameList.add(parts[6]);
    		}
    	}
	}
}
