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
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class LoginView extends View implements ChangeListener<LoginStatus> {

    private static final Logger LOG = Logger.getLogger(LoginView.class.getName());
    
	public static final String NAME = "home";
	
	/**
	 * The field used to input the user's email address
	 */
	private final TextField emailField = new TextField();

	/**
	 * The field used to input the user's password
	 */
    private final PasswordField passwordField = new PasswordField();
    
	/**
	 * The app service which handles the login system
	 */
    private final LoginService loginService;
    
    private boolean checkSavedCredentials;
	
	public LoginView (LoginService loginService) {
		super(NAME);
		this.loginService = loginService;
		
		checkSavedCredentials = loginService.checkSavedCredentials();
		
		if (checkSavedCredentials) {
			this.getApplication().showLayer("loading");
		}
				
        this.setOnShown(evt -> {
    		loginService.loginStatusProperty().addListener(this);        	
        });
        
        this.setOnHidden(evt -> {
        	loginService.loginStatusProperty().removeListener(this);
        });
        
		setupControls();
	}
	
	private void setupControls () {
		getStylesheets().add(LoginView.class.getResource("login.css").toExternalForm());
		
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
		
		setCenter(controls);
		controls.getChildren().addAll(spacer(), iconView, spacer(), emailField, passwordField, loginButton);
		
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
			emailField.requestFocus();
			getApplication().showMessage("Please enter an email address");
		} else {
			loginService.login(emailField.getText(), passwordField.getText());
		}
	}

	/* (non-Javadoc)
	 * @see javafx.beans.value.ChangeListener#changed(javafx.beans.value.ObservableValue, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void changed(ObservableValue<? extends LoginStatus> observable, LoginStatus oldValue, LoginStatus newValue) {
		Platform.runLater(() -> {
			if (newValue != null) {
				LOG.log(Level.INFO, "New login status: "+newValue);
				if (checkSavedCredentials) {
					switch (newValue) {
					case INVALID_CREDENTIALS:
						checkSavedCredentials = false;
						this.getApplication().hideLayer("loading");
						showResponse("Please enter your email address & password again to continue.");
						break;
					case LOGGED_IN:
						checkSavedCredentials = false;
						this.getApplication().hideLayer("loading");
						getApplication().switchView(TraplineListView.NAME);
						break;
					case PENDING_LOGIN:
						this.getApplication().showLayer("loading");
						break;
					case SERVER_UNAVAILABLE:
						checkSavedCredentials = false;
						this.getApplication().hideLayer("loading");
						getApplication().switchView(TraplineListView.NAME);
						showResponse("The server is currently unavailable, so the traplines listed may be out of date.\n"
								+ "To update the traplines listed, please turn on your internet and refresh this page using the buttom above.");
						break;
					default:
						break;
					}
				} else {
					switch (newValue) {
					case INVALID_CREDENTIALS:
						this.getApplication().hideLayer("loading");
						showResponse("The email address and password you entered is incorrect.");
						break;
					case LOGGED_IN:
						this.getApplication().hideLayer("loading");
						getApplication().switchView(TraplineListView.NAME);
						break;
					case PENDING_LOGIN:
						this.getApplication().showLayer("loading");
						break;
					case SERVER_UNAVAILABLE:
						this.getApplication().hideLayer("loading");
						showResponse("We can't reach the Nest NZ server at the moment. \n"
								+ "Make sure your internet connection is available and try again later.");				
						break;
					default:
						break;
					}
				}
			}
		});
	}
	
	/**
	 * Displays the provided login response message as a dialog box
	 * @param message The login response message to display
	 */
	private void showResponse (String message) {
		Dialog<Button> dialog = new Dialog<>();
		dialog.setContent(new Label(message));
		Button okButton = new Button("OK");
		okButton.setOnAction(e -> {
			dialog.hide();
		});
		dialog.getButtons().add(okButton);
		dialog.showAndWait();
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setTitleText("Nest NZ Login");
    }

}
