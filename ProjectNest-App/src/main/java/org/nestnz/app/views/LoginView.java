package org.nestnz.app.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.LoginService;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.ProgressIndicator;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView extends View {

    private static final Logger LOG = Logger.getLogger(LoginView.class.getName());
    
	public static final String NAME = "home";
	
	private final TextField emailField = new TextField();

    private final PasswordField passwordField = new PasswordField();
    
	private final StackPane statusPopup = new StackPane();
	private final ProgressIndicator spinner = new ProgressIndicator();
    
    private final LoginService loginService;
	
	public LoginView (LoginService loginService) {
		super(NAME);
		this.loginService = loginService;
		
		loginService.loginStatusProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				LOG.log(Level.INFO, "New login status: "+newVal);
				boolean visible = false;
				switch (newVal) {
				case INVALID_CREDENTIALS:
					break;
				case LOGGED_IN:
					getApplication().switchView(TraplineListView.NAME);
					break;
				case PENDING:
					visible = true;
					break;
				case SERVER_UNAVAILABLE:
					getApplication().switchView(TraplineListView.NAME);
					//For now, we'll go to the trapline view regardless
					break;
				case LOGGED_OUT:
					break;
				}
				statusPopup.setVisible(visible);
			}
		});
		
		StackPane layout = new StackPane();
		
		VBox controls = new VBox(30);
		controls.setAlignment(Pos.CENTER);
		
		//Create & set up the logo icon
		Image icon = new Image(LoginView.class.getResourceAsStream("/icon.png"));        
        ImageView iconView = new ImageView(icon);
        
        emailField.setFloatText("Email Address");
        
        passwordField.setPromptText("Password");
		Button loginButton = new Button("Login");
		loginButton.setMaxWidth(1000);
		loginButton.setMaxHeight(1000);
		
		setCenter(layout);
		controls.getChildren().addAll(spacer(), iconView, spacer(), emailField, passwordField, loginButton);
		
		//Set up the loading panel
		spinner.setRadius(30);
		StackPane.setAlignment(spinner, Pos.CENTER);
	    statusPopup.getChildren().add(spinner);		
		statusPopup.setVisible(false);		
		statusPopup.setStyle("-fx-background-color: rgba(100, 100, 100, 0.5)");
		
		
		layout.getChildren().addAll(controls, statusPopup);				
		
		loginButton.setOnAction(evt -> {
			runLogin();
		});
	}
	
	private Node spacer () {
		Region spacer = new Region();
        spacer.setMinHeight(Region.USE_PREF_SIZE);
        return spacer;
	}
	
	private void runLogin () {
		loginService.login(emailField.getText(), passwordField.getText());
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setTitleText("Nest NZ Login");
    }

}
