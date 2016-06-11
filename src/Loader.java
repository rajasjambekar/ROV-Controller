import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Loader extends Application{

	/*
	 * Loader class is the starting point of this controller
	 * Responsible for initializing and launching the main UI 
	 */
	
	public static void main(String[] args) {
		//used as a starting point for launching a UI application
		launch(args);
	}
	
	//runs when launch(args) is called
	@Override
	public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Team Screwdrivers: ROV Controller");
        loadControllerGUI(primaryStage);
	}

	//Load the ControllerGUI Stage
	//Also run the init function of the ControllerGUI class after loading the UI
	private void loadControllerGUI(Stage primaryStage) {
		Parent root;
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(this.getClass().getResource("TestControllerGUI4.fxml"));
			root = loader.load();
			primaryStage.setScene(new Scene(root));
			primaryStage.show();
            ControllerGUI controllerGUI = loader.getController();
            controllerGUI.init(primaryStage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		Platform.exit();
	}
}
