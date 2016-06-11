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
    @FXML private Label lblBwManCtrlr;
    @FXML private Label lblBwManComp;
    @FXML private Label lblBwManTggl;
    @FXML private Label lblDnManCtrlr;
    @FXML private Label lblDnManComp;
    @FXML private Label lblDnManTggl;
    @FXML private Label lblUpManCtrlr;
    @FXML private Label lblUpManComp;
    @FXML private Label lblUpManTggl;
    @FXML private Label lblRtAclkManCtrlr;
    @FXML private Label lblRtAclkManComp;
    @FXML private Label lblRtAclkManTggl;
    @FXML private Label lblRtClkManCtrlr;
    @FXML private Label lblRtClkManComp;
    @FXML private Label lblRtClkManTggl;
    @FXML private Label lblLeftRArmCtrlr;
    @FXML private Label lblLeftRArmComp;
    @FXML private Label lblLeftRArmTggl; 
    @FXML private Label lblRightRArmCtrlr;
    @FXML private Label lblRightRArmComp;
    @FXML private Label lblRightRArmTggl;
    @FXML private Label lblFwRArmCtrlr;
    @FXML private Label lblFwRArmComp;
    @FXML private Label lblFwRArmTggl;
    @FXML private Label lblBwRArmCtrlr;
    @FXML private Label lblBwRArmComp;
    @FXML private Label lblBwRArmTggl;
    @FXML private Label lblDnRArmCtrlr;
    @FXML private Label lblDnRArmComp;
    @FXML private Label lblDnRArmTggl;
    @FXML private Label lblUpRArmCtrlr;
    @FXML private Label lblUpRArmComp;
    @FXML private Label lblUpRArmTggl;
    @FXML private Label lblRtLeftRGripperCtrlr;
    @FXML private Label lblRtLeftRGripperComp;
    @FXML private Label lblRtLeftRGripperTggl;
    @FXML private Label lblRtRightRGripperCtrlr;
    @FXML private Label lblRtRightRGripperComp;
    @FXML private Label lblRtRightRGripperTggl;
    @FXML private Label lblTiltDnRGripperCtrlr;
    @FXML private Label lblTiltDnRGripperComp;
    @FXML private Label lblTiltDnRGripperTggl;
    @FXML private Label lblTiltUpRGripperCtrlr;
    @FXML private Label lblTiltUpRGripperComp;
    @FXML private Label lblTiltUpRGripperTggl;
    @FXML private Label lblCloseRGripperCtrlr;
    @FXML private Label lblCloseRGripperComp;
    @FXML private Label lblCloseRGripperTggl;
    @FXML private Label lblOpenRGripperCtrlr;
    @FXML private Label lblOpenRGripperComp;
    @FXML private Label lblOpenRGripperTggl;
    @FXML private Label lblCloseClawCtrlr;
    @FXML private Label lblCloseClawComp;
    @FXML private Label lblCloseClawTggl;
    @FXML private Label lblOpenClawCtrlr;
    @FXML private Label lblOpenClawComp;
    @FXML private Label lblOpenClawTggl;
    @FXML private Label lblC1S1LeftCtrlr;
    @FXML private Label lblC1S1LeftComp;
    @FXML private Label lblC1S1LeftTggl;
    @FXML private Label lblC1S1RightCtrlr;
    @FXML private Label lblC1S1RightComp;
    @FXML private Label lblC1S1RightTggl;
    @FXML private Label lblC1S2DnCtrlr;
    @FXML private Label lblC1S2DnComp;
    @FXML private Label lblC1S2DnTggl;
    @FXML private Label lblC1S2UpCtrlr;
    @FXML private Label lblC1S2UpComp;
    @FXML private Label lblC1S2UpTggl;
    @FXML private Label lblC2S1LeftCtrlr;
    @FXML private Label lblC2S1LeftComp;
    @FXML private Label lblC2S1LeftTggl;
    @FXML private Label lblC2S1RightCtrlr;
    @FXML private Label lblC2S1RightComp;
    @FXML private Label lblC2S1RightTggl;
    @FXML private Label lblC2S2DnCtrlr;
    @FXML private Label lblC2S2DnComp;
    @FXML private Label lblC2S2DnTggl;
    @FXML private Label lblC2S2UpCtrlr;
    @FXML private Label lblC2S2UpComp;
    @FXML private Label lblC2S2UpTggl;
    @FXML private Label lblLed1Ctrlr;
    @FXML private Label lblLed1Comp;
    @FXML private Label lblLed1Tggl;
    @FXML private Label lblLed2Ctrlr;
    @FXML private Label lblLed2Comp;
    @FXML private Label lblLed2Tggl;
    
    
    //buttons with id in fxml file
    @FXML private Button btnLeftManSetCtrlr;
    @FXML private Button btnLeftManSetTggl;
    @FXML private Button btnRightManSetCtrlr;
    @FXML private Button btnRightManSetTggl;
    @FXML private Button btnFwManSetCtrlr;
    @FXML private Button btnFwManSetTggl;
    @FXML private Button btnBwManSetCtrlr;
    @FXML private Button btnBwManSetTggl;
    @FXML private Button btnDnManSetCtrlr;
    @FXML private Button btnDnManSetTggl;
    @FXML private Button btnUpManSetCtrlr;
    @FXML private Button btnUpManSetTggl;
    @FXML private Button btnRtAclkManSetCtrlr;
    @FXML private Button btnRtAclkManSetTggl;
    @FXML private Button btnRtClkManSetCtrlr;
    @FXML private Button btnRtClkManSetTggl;
    @FXML private Button btnLeftRArmSetCtrlr;
    @FXML private Button btnLeftRArmSetTggl;
    @FXML private Button btnRightRArmSetCtrlr;
    @FXML private Button btnRightRArmSetTggl;
    @FXML private Button btnFwRArmSetCtrlr;
    @FXML private Button btnFwRArmSetTggl;
    @FXML private Button btnBwRArmSetCtrlr;
    @FXML private Button btnBwRArmSetTggl;
    @FXML private Button btnDnRArmSetCtrlr;
    @FXML private Button btnDnRArmSetTggl;
    @FXML private Button btnUpRArmSetCtrlr;
    @FXML private Button btnUpRArmSetTggl;
    @FXML private Button btnRtLeftRGripperSetCtrlr;
    @FXML private Button btnRtLeftRGripperSetTggl;
    @FXML private Button btnRtRightRGripperSetCtrlr;
    @FXML private Button btnRtRightRGripperSetTggl;
    @FXML private Button btnTiltDnRGripperSetCtrlr;
    @FXML private Button btnTiltDnRGripperSetTggl;
    @FXML private Button btnTiltUpRGripperSetCtrlr;
    @FXML private Button btnTiltUpRGripperSetTggl;
    @FXML private Button btnCloseRGripperSetCtrlr;
    @FXML private Button btnCloseRGripperSetTggl;
    @FXML private Button btnOpenRGripperSetCtrlr;
    @FXML private Button btnOpenRGripperSetTggl;
    @FXML private Button btnCloseClawSetCtrlr;
    @FXML private Button btnCloseClawSetTggl;
    @FXML private Button btnOpenClawSetCtrlr;
    @FXML private Button btnOpenClawSetTggl;
    @FXML private Button btnC1S1LeftSetCtrlr;
    @FXML private Button btnC1S1LeftSetTggl;
    @FXML private Button btnC1S1RightSetCtrlr;
    @FXML private Button btnC1S1RightSetTggl;
    @FXML private Button btnC1S2DnSetCtrlr;
    @FXML private Button btnC1S2DnSetTggl;
    @FXML private Button btnC1S2UpSetCtrlr;
    @FXML private Button btnC1S2UpSetTggl;
    @FXML private Button btnC2S1LeftSetCtrlr;
    @FXML private Button btnC2S1LeftSetTggl;
    @FXML private Button btnC2S1RightSetCtrlr2;
    @FXML private Button btnC2S1RightSetTggl;
    @FXML private Button btnC2S2DnSetCtrlr;
    @FXML private Button btnC2S2DnSetTggl;
    @FXML private Button btnC2S2UpSetCtrlr;
    @FXML private Button btnC2S2UpSetTggl;
    @FXML private Button btnLed1SetCtrlr;
    @FXML private Button btnLed1SetTggl;
    @FXML private Button btnLed2SetCtrlr;
    @FXML private Button btnLed2SetTggl;
    
    @FXML private Button btnSave;
    @FXML private Button btnReset;
    
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
	
	@FXML
    private void initialize() {
		//setControllerButtonList = new Button[] {btnLeftManSetCtrlr, btnRightManSetCtrlr, btnFwManSetCtrlr};
		//setToggleButtonList = new Button[] {btnLeftManSetTggl, btnRightManSetTggl, btnFwManSetTggl};
		//controllerLabelList = new Label[] {lblLeftManCtrlr, lblRightManCtrlr, lblFwManCtrlr};
		//componentLabelList = new Label[] {lblLeftManComp, lblRightManComp, lblFwManComp};
		//toggleLabelList = new Label[] {lblLeftManTggl, lblRightManTggl, lblFwManTggl};
		setControllerButtonList = new Button[] {btnLeftManSetCtrlr, btnRightManSetCtrlr, btnFwManSetCtrlr, btnBwManSetCtrlr, btnDnManSetCtrlr, btnUpManSetCtrlr, btnRtAclkManSetCtrlr, btnRtClkManSetCtrlr, btnLeftRArmSetCtrlr, btnRightRArmSetCtrlr, btnFwRArmSetCtrlr, btnBwRArmSetCtrlr, btnDnRArmSetCtrlr, btnUpRArmSetCtrlr, btnRtLeftRGripperSetCtrlr, btnRtRightRGripperSetCtrlr, btnTiltDnRGripperSetCtrlr, btnTiltUpRGripperSetCtrlr, btnCloseRGripperSetCtrlr, btnOpenRGripperSetCtrlr, btnCloseClawSetCtrlr, btnOpenClawSetCtrlr, btnC1S1LeftSetCtrlr, btnC1S1RightSetCtrlr, btnC1S2DnSetCtrlr, btnC1S2UpSetCtrlr, btnC2S1LeftSetCtrlr, btnC2S1RightSetCtrlr2, btnC2S2DnSetCtrlr, btnC2S2UpSetCtrlr, btnLed1SetCtrlr, btnLed2SetCtrlr };
		setToggleButtonList = new Button[] {btnLeftManSetTggl, btnRightManSetTggl, btnFwManSetTggl, btnBwManSetTggl, btnDnManSetTggl, btnUpManSetTggl, btnRtAclkManSetTggl, btnRtClkManSetTggl, btnLeftRArmSetTggl, btnRightRArmSetTggl, btnFwRArmSetTggl, btnBwRArmSetTggl, btnDnRArmSetTggl, btnUpRArmSetTggl, btnRtLeftRGripperSetTggl, btnRtRightRGripperSetTggl, btnTiltDnRGripperSetTggl, btnTiltUpRGripperSetTggl, btnCloseRGripperSetTggl, btnOpenRGripperSetTggl, btnCloseClawSetTggl, btnOpenClawSetTggl, btnC1S1LeftSetTggl, btnC1S1RightSetTggl, btnC1S2DnSetTggl, btnC1S2UpSetTggl, btnC2S1LeftSetTggl, btnC2S1RightSetTggl, btnC2S2DnSetTggl, btnC2S2UpSetTggl, btnLed1SetTggl, btnLed2SetTggl };
		controllerLabelList = new Label[] {lblLeftManCtrlr, lblRightManCtrlr, lblFwManCtrlr, lblBwManCtrlr, lblDnManCtrlr, lblUpManCtrlr, lblRtAclkManCtrlr, lblRtClkManCtrlr, lblLeftRArmCtrlr, lblRightRArmCtrlr, lblFwRArmCtrlr, lblBwRArmCtrlr, lblDnRArmCtrlr, lblUpRArmCtrlr, lblRtLeftRGripperCtrlr, lblRtRightRGripperCtrlr, lblTiltDnRGripperCtrlr, lblTiltUpRGripperCtrlr, lblCloseRGripperCtrlr, lblOpenRGripperCtrlr, lblCloseClawCtrlr, lblOpenClawCtrlr, lblC1S1LeftCtrlr, lblC1S1RightCtrlr, lblC1S2DnCtrlr, lblC1S2UpCtrlr, lblC2S1LeftCtrlr, lblC2S1RightCtrlr, lblC2S2DnCtrlr, lblC2S2UpCtrlr, lblLed1Ctrlr, lblLed2Ctrlr };
		componentLabelList = new Label[] { lblLeftManComp, lblRightManComp, lblFwManComp, lblBwManComp, lblDnManComp, lblUpManComp, lblRtAclkManComp, lblRtClkManComp, lblLeftRArmComp, lblRightRArmComp, lblFwRArmComp, lblBwRArmComp, lblDnRArmComp, lblUpRArmComp, lblRtLeftRGripperComp, lblRtRightRGripperComp, lblTiltDnRGripperComp, lblTiltUpRGripperComp, lblCloseRGripperComp, lblOpenRGripperComp, lblCloseClawComp, lblOpenClawComp, lblC1S1LeftComp, lblC1S1RightComp, lblC1S2DnComp, lblC1S2UpComp, lblC2S1LeftComp, lblC2S1RightComp, lblC2S2DnComp, lblC2S2UpComp, lblLed1Comp, lblLed2Comp };
		toggleLabelList = new Label[] { lblLeftManTggl, lblRightManTggl, lblFwManTggl, lblBwManTggl, lblDnManTggl, lblUpManTggl, lblRtAclkManTggl, lblRtClkManTggl, lblLeftRArmTggl,  lblRightRArmTggl, lblFwRArmTggl, lblBwRArmTggl, lblDnRArmTggl, lblUpRArmTggl, lblRtLeftRGripperTggl, lblRtRightRGripperTggl, lblTiltDnRGripperTggl, lblTiltUpRGripperTggl, lblCloseRGripperTggl, lblOpenRGripperTggl, lblCloseClawTggl, lblOpenClawTggl, lblC1S1LeftTggl, lblC1S1RightTggl, lblC1S2DnTggl, lblC1S2UpTggl, lblC2S1LeftTggl, lblC2S1RightTggl, lblC2S2DnTggl,lblC2S2UpTggl, lblLed1Tggl, lblLed2Tggl };
		
		/*System.out.println(setControllerButtonList.length);
		System.out.println(setToggleButtonList.length);
		System.out.println(controllerLabelList.length);
		System.out.println(componentLabelList.length);
		System.out.println(toggleLabelList.length);*/
		
    	setButtonHandler1();
    	setButtonHandler2();
    	setButtonHandler3();
    	menuClose.setOnAction(menuHandler);
    }
	
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
	
	//for setController buttons
	private void setButtonHandler1() {
		btnLeftManSetCtrlr.setOnAction(setControllerHandler);
		btnRightManSetCtrlr.setOnAction(setControllerHandler);
		btnFwManSetCtrlr.setOnAction(setControllerHandler);
		btnBwManSetCtrlr.setOnAction(setControllerHandler);
		btnDnManSetCtrlr.setOnAction(setControllerHandler);
		btnUpManSetCtrlr.setOnAction(setControllerHandler);
		btnRtAclkManSetCtrlr.setOnAction(setControllerHandler);
		btnRtClkManSetCtrlr.setOnAction(setControllerHandler);
		btnLeftRArmSetCtrlr.setOnAction(setControllerHandler);
		btnRightRArmSetCtrlr.setOnAction(setControllerHandler);
		btnFwRArmSetCtrlr.setOnAction(setControllerHandler);
		btnBwRArmSetCtrlr.setOnAction(setControllerHandler);
		btnDnRArmSetCtrlr.setOnAction(setControllerHandler);
		btnUpRArmSetCtrlr.setOnAction(setControllerHandler);
		btnRtLeftRGripperSetCtrlr.setOnAction(setControllerHandler);
		btnRtRightRGripperSetCtrlr.setOnAction(setControllerHandler);
		btnTiltDnRGripperSetCtrlr.setOnAction(setControllerHandler);
		btnTiltUpRGripperSetCtrlr.setOnAction(setControllerHandler);
		btnCloseRGripperSetCtrlr.setOnAction(setControllerHandler);
		btnOpenRGripperSetCtrlr.setOnAction(setControllerHandler);
		btnCloseClawSetCtrlr.setOnAction(setControllerHandler);
		btnOpenClawSetCtrlr.setOnAction(setControllerHandler);
		btnC1S1LeftSetCtrlr.setOnAction(setControllerHandler);
		btnC1S1RightSetCtrlr.setOnAction(setControllerHandler);
		btnC1S2DnSetCtrlr.setOnAction(setControllerHandler);
		btnC1S2UpSetCtrlr.setOnAction(setControllerHandler);
		btnC2S1LeftSetCtrlr.setOnAction(setControllerHandler);
		btnC2S1RightSetCtrlr2.setOnAction(setControllerHandler);
		btnC2S2DnSetCtrlr.setOnAction(setControllerHandler);
		btnC2S2UpSetCtrlr.setOnAction(setControllerHandler);
		btnLed1SetCtrlr.setOnAction(setControllerHandler);
		btnLed2SetCtrlr.setOnAction(setControllerHandler);
	}
	
	//for setToggle buttons
	private void setButtonHandler2() {
		btnLeftManSetTggl.setOnAction(setToggleHandler);
		btnRightManSetTggl.setOnAction(setToggleHandler);
		btnFwManSetTggl.setOnAction(setToggleHandler);
		btnBwManSetTggl.setOnAction(setToggleHandler);
		btnDnManSetTggl.setOnAction(setToggleHandler);
		btnUpManSetTggl.setOnAction(setToggleHandler);
		btnRtAclkManSetTggl.setOnAction(setToggleHandler);
		btnRtClkManSetTggl.setOnAction(setToggleHandler);
		btnLeftRArmSetTggl.setOnAction(setToggleHandler);
		btnRightRArmSetTggl.setOnAction(setToggleHandler);
		btnFwRArmSetTggl.setOnAction(setToggleHandler);
		btnBwRArmSetTggl.setOnAction(setToggleHandler);
		btnDnRArmSetTggl.setOnAction(setToggleHandler);
		btnUpRArmSetTggl.setOnAction(setToggleHandler);
		btnRtLeftRGripperSetTggl.setOnAction(setToggleHandler);
		btnRtRightRGripperSetTggl.setOnAction(setToggleHandler);
		btnTiltDnRGripperSetTggl.setOnAction(setToggleHandler);
		btnTiltUpRGripperSetTggl.setOnAction(setToggleHandler);
		btnCloseRGripperSetTggl.setOnAction(setToggleHandler);
		btnOpenRGripperSetTggl.setOnAction(setToggleHandler);
		btnCloseClawSetTggl.setOnAction(setToggleHandler);
		btnOpenClawSetTggl.setOnAction(setToggleHandler);
		btnC1S1LeftSetTggl.setOnAction(setToggleHandler);
		btnC1S1RightSetTggl.setOnAction(setToggleHandler);
		btnC1S2DnSetTggl.setOnAction(setToggleHandler);
		btnC1S2UpSetTggl.setOnAction(setToggleHandler);
		btnC2S1LeftSetTggl.setOnAction(setToggleHandler);
		btnC2S1RightSetTggl.setOnAction(setToggleHandler);
		btnC2S2DnSetTggl.setOnAction(setToggleHandler);
		btnC2S2UpSetTggl.setOnAction(setToggleHandler);
		btnLed1SetTggl.setOnAction(setToggleHandler);
		btnLed2SetTggl.setOnAction(setToggleHandler);
	}
	
	//for save button
	private void setButtonHandler3() {
		btnSave.setOnAction(extraConfigHandler);
		btnReset.setOnAction(extraConfigHandler);
	}
	
	//handles save button press
	final EventHandler<ActionEvent> extraConfigHandler = new EventHandler<ActionEvent>(){
        @Override
        public void handle(final ActionEvent event) {
        	if((Button) event.getSource()==btnSave) {
            	try {
    				writeControlConfig();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
        	}
        	else if((Button) event.getSource()==btnReset) {
        		resetAll();
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
  	
  	//resets all labels on UI
  	private void resetAll() {
  		for(int i=0;i<controllerLabelList.length;i++) {
  			controllerLabelList[i].setText("Unassigned");
  			componentLabelList[i].setText("Unassigned");
  			toggleLabelList[i].setText("0");		
  		}
  	}
    
    //writes the new configuration to file
    private void writeControlConfig() throws IOException {
    	StringBuilder str = new StringBuilder();
    	if(Files.exists(Paths.get(configFile))) {
    		lines = Files.readAllLines(Paths.get(configFile));
    		for(int i=0; i<lines.size() && i<controllerLabelList.length; i++) {
    			String line = lines.get(i);
    			String parts[] = null;
    			if(line.contains(":")) {
    				parts = line.split(":");
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
    			}
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
    	//match the pressed button with item in list of buttons
    	for(i=0; i<setControllerButtonList.length; i++) {
    		if(setControllerButtonList[i]==source) {
    			name=controllerLabelList[i];
    			comp=componentLabelList[i];
    			break;
    		}
    	}
		String keys[] = new String[connectedControllers.size()];
		connectedControllers.keySet().toArray(keys);
		for(String key:keys) {
			//find the selected controller from the list of controllers on UI
			if(controllerChooser.getValue().toString().equals(key.toString())) {
				//get name of component from selected component
    			String compName = getComponent(connectedControllers.get(key));
				if(compName!="") {
					//check if this button has been attached as toggle button previously
					if(compName.contains("BUTTON") && !checkAssignedToggleButton(i, compName.substring(6), controllerChooser.getValue().toString())) {
						name.setText(controllerChooser.getValue().toString());
						comp.setText(compName);
						break;
					}
					else if(compName.contains("AXIS")) {
						//set the controller name and component name for selected control
						name.setText(controllerChooser.getValue().toString());
						comp.setText(compName);
						//change the component name for paired control
						if(compName.startsWith("-"))
							compName = compName.substring(1);
						else 
							compName = "-" + compName;
						//force combinations on opposite movements of same system
						//eg. left-right, up-down, etc
						if(i%2==0) {
							controllerLabelList[i+1].setText(controllerChooser.getValue().toString());
							componentLabelList[i+1].setText(compName);
							setToggleButtonList[i+1].setText(setToggleButtonList[i].getText());
						}
						else {
							controllerLabelList[i-1].setText(controllerChooser.getValue().toString());
							componentLabelList[i-1].setText(compName);
							setToggleButtonList[i-1].setText(setToggleButtonList[i].getText());
						}
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
    				if(controllerComponents[i].getPollData()== 1.0f) {
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
    				else if(controllerComponents[i].getPollData()== -1.0f) {
    					if(componentIdentifier == Component.Identifier.Axis.X) {
        					return ("-AXIS" + (0));
    					}
    					else if(componentIdentifier == Component.Identifier.Axis.Y) {
    						return ("-AXIS" + (1));
    					}
    					else if(componentIdentifier == Component.Identifier.Axis.Z) {
    						return ("-AXIS" + (2));
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
    			break;
    		}
    	}
    	String keys[] = new String[connectedControllers.size()];
		connectedControllers.keySet().toArray(keys);
		for(String key:keys) {
			if(controllerChooser.getValue().toString().equals(key.toString())) {
    			String toggleButton = getComponent(connectedControllers.get(key));
    			//check if this button has been attached as normal button previously
    			//prevent assignment of toggle button if controller is not assigned before
    			if(toggleButton.contains("BUTTON") && !checkAssignedButton(i, toggleButton.substring(6))
    					&& !controllerLabelList[i].getText().toString().equalsIgnoreCase("")
    					&& !controllerLabelList[i].getText().toString().equalsIgnoreCase("unassigned")) {
					String str = toggleButton.substring(6);
					//System.out.println(str.toString());
					if(tggl!=null) {
						tggl.setText(str.toString());
						if(i%2==0) {
							toggleLabelList[i+1].setText(str.toString());
						}
						else {
							toggleLabelList[i-1].setText(str.toString());
						}
					}
				}
    			//commented since this doesn't seem to be correct
    			//toggle buttton should be a button
    			/*else if(!toggleButton.contains("BUTTON")){
    				String str = toggleButton.substring(6);
					//System.out.println(str.toString());
					if(tggl!=null)
						tggl.setText(str.toString());
				}*/
    		}
		}
	}
	
	//check if selected toggle button is previously assigned as normal button
    private boolean checkAssignedButton(int i, String toggleButton) {
    	Label controller = controllerLabelList[i];
    	for(int j=0;j<controllerLabelList.length;j++) {
    		//check if component is a button
    		if(componentLabelList[j].getText().toString().contains("BUTTON")) {
    			//check if togglebutton is same as button number
        		//and both have the same controller
        		String buttonNo = componentLabelList[j].getText().toString().substring(6);
        		if(toggleButton.equalsIgnoreCase(buttonNo)
        				&& controllerLabelList[j].getText().toString().equalsIgnoreCase(controller.getText().toString())) {
        			return true;
        		}
    		}
    	}
		return false;
	}
    
    //check if selected button is previously assigned as toggle button
    private boolean checkAssignedToggleButton(int i, String button, String controllerName) {
    	for(int j=0;j<toggleLabelList.length;j++) {
    		//check if button is same as toggle button number
    		//and both have the same controller
    		if(toggleLabelList[j].getText().toString().equalsIgnoreCase(button)
    				&& controllerLabelList[j].getText().toString().equalsIgnoreCase(controllerName)) {
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
    			String parts[] = null;
    			if(line.contains(":")) {
    				parts = line.split(":");
        			controllerLabelList[i].setText(parts[2]);
        			componentLabelList[i].setText(parts[3]);
        			toggleLabelList[i].setText(parts[4]);
        			codeList.add(Integer.parseInt(parts[5]));
        			taskNameList.add(parts[6]);
    			}
    		}
    	}
	}
}
