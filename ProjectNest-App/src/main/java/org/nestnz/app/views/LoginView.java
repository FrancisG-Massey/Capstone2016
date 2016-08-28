/*******************************************************************************
 * Copyright (C) 2016, Nest NZ
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.nestnz.app.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.LoginService;
import org.nestnz.app.services.LoginService.LoginStatus;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.ProgressIndicator;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    
    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
	
	public LoginView (LoginService loginService) {
		super(NAME);
		this.loginService = loginService;
        getStylesheets().add(LoginView.class.getResource("login.css").toExternalForm());
		
		loginService.loginStatusProperty().addListener((obs, oldVal, newVal) -> {
			Platform.runLater(() -> updateLogin(newVal));
			
		});
		
		StackPane layout = new StackPane();
		
		VBox controls = new VBox(30);
		controls.setAlignment(Pos.CENTER);
		
		//Create & set up the logo icon
		Image icon = new Image(LoginView.class.getResourceAsStream("/icon.png"));        
        ImageView iconView = new ImageView(icon);
        
        emailField.setPromptText("Email Address");
        
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
		if (emailField.getText().isEmpty()) {
			emailField.pseudoClassStateChanged(errorClass, true);
			emailField.requestFocus();
			getApplication().showMessage("Please enter an email address");
			return;
		}
		emailField.pseudoClassStateChanged(errorClass, false);
		loginService.login(emailField.getText(), passwordField.getText());
	}
	
	private void updateLogin (LoginStatus status) {
		if (status != null) {
			LOG.log(Level.INFO, "New login status: "+status);
			boolean visible = false;
			switch (status) {
			case INVALID_CREDENTIALS:
				break;
			case LOGGED_IN:
				getApplication().switchView(TraplineListView.NAME);
				break;
			case PENDING_LOGIN:
				visible = true;
				break;
			case SERVER_UNAVAILABLE:
				getApplication().switchView(TraplineListView.NAME);
				//For now, we'll go to the trapline view regardless
				break;
			default:
				break;
			}
			statusPopup.setVisible(visible);
		}
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setTitleText("Nest NZ Login");
    }

}
