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
package org.nestnz.app.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.util.BackgroundTasks;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.SettingService;
import com.gluonhq.connect.provider.RestClient;
import com.gluonhq.connect.source.RestDataSource;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

public final class LoginService {
	
	public static enum LoginStatus {
		PENDING_LOGIN,
		PENDING_LOGOUT,
		LOGGED_IN,
		LOGGED_OUT,
		INVALID_CREDENTIALS,
		SERVER_UNAVAILABLE;
	}

    private static final Logger LOG = Logger.getLogger(LoginService.class.getName());
    
    private static LoginService instance;
    
    public static synchronized LoginService getInstance () {
    	if (instance == null) {
    		instance = new LoginService();
    	}
    	return instance;
    }
    
    private final ReadOnlyObjectWrapper<LoginStatus> loginStatusProperty = new ReadOnlyObjectWrapper<>(LoginStatus.LOGGED_OUT);
    
    private final ReadOnlyStringWrapper sessionTokenProperty = new ReadOnlyStringWrapper();
    
    private final SettingService settingService;
    
    private LoginService () {
    	settingService = PlatformFactory.getPlatform().getSettingService();
    }
    
    /**
     * Tries to log in using the username & password saved in the device settings
     * @return true if credentials were found & used, false if no credentials were found
     */
    public boolean checkSavedCredentials () {
    	String email = settingService.retrieve("api.email");
    	String password = settingService.retrieve("api.password");
    	if (email == null || password == null) {
    		return false;
    	}
    	login(email, password);
    	return true;
    }
    
    public void login (String username, String password) {
    	if (getLoginStatus() == LoginStatus.PENDING_LOGIN) {
    		return;//Already logging in
    	}
    	if (username == null || username.trim().length() < 1) {
    		throw new IllegalArgumentException("Invalid username: "+username);
    	}
    	loginStatusProperty.set(LoginStatus.PENDING_LOGIN);
    	
    	String credentials = username+":"+password;
    	String encodedAuth = "Basic "+new String(Base64.getEncoder().encode(credentials.getBytes()), Charset.forName("UTF-8"));
    	
    	RestClient loginClient = RestClient.create().method("POST").host("https://api.nestnz.org")
    			.path("/session/").header("Authorization", encodedAuth);
    	
    	final RestDataSource dataSource = loginClient.createRestDataSource();
    	
    	BackgroundTasks.runInBackground(() -> {
    		try {
    			dataSource.getInputStream();
			    Platform.runLater(() -> {
					switch (dataSource.getResponseCode()) {
					case 201://Created
						List<String> sessionHeaders = dataSource.getResponseHeaders().get("Session-Token");
						if (sessionHeaders == null || sessionHeaders.size() == 0) {
							LOG.log(Level.SEVERE, "Session token missing from server response");
							loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);
						} else {
							sessionTokenProperty.set(sessionHeaders.get(0));
							
							//Save the email & password for future use
							settingService.store("api.email", username);
							settingService.store("api.password", password);
							loginStatusProperty.set(LoginStatus.LOGGED_IN);
						}					
						break;
					case 403://Invalid username/password
						loginStatusProperty.set(LoginStatus.INVALID_CREDENTIALS);
						break;
					default://Some other error occured
						LOG.log(Level.SEVERE, "Problem sending login request. Response="+dataSource.getResponseMessage());
						loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);					
					}
			    });
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, "Problem sending login request", ex);
				Platform.runLater(() -> {
					loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);});
			}
    	}); 
    }
    
    /**
     * Sends a request to expire the current session for the user. 
     * They will be unable to make any more requests to the server until {@link #login(String, String)} is called
     */
    public void logout () {
    	if (getLoginStatus() != LoginStatus.PENDING_LOGOUT) {
    		return;//Already logging out
    	}
    	if (getSessionToken() == null) {
        	loginStatusProperty.set(LoginStatus.LOGGED_OUT);
    		return;//Not logged in		
    	}
    	loginStatusProperty.set(LoginStatus.PENDING_LOGOUT);
    	
    	//Clear the saved email & password
    	settingService.remove("api.email");
    	settingService.remove("api.password");
    	
    	RestClient loginClient = RestClient.create().method("DELETE").host("https://api.nestnz.org")
    			.path("/session/").queryParam("Session-Token", getSessionToken());
    	final RestDataSource dataSource = loginClient.createRestDataSource();
    	BackgroundTasks.runInBackground(() -> {
    		try {
				dataSource.getInputStream();
			    Platform.runLater(() -> {
					switch (dataSource.getResponseCode()) {
					case 200://Success
					case 410://Session already expired
						sessionTokenProperty.set(null);
						loginStatusProperty.set(LoginStatus.LOGGED_OUT);					
						break;
					default://Some other error occured
						LOG.log(Level.SEVERE, "Problem sending logout request. Response="+dataSource.getResponseMessage());
						loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);					
					}
		    	});
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, "Problem sending logout request", ex);
		    	Platform.runLater(() -> loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE));
			}
    	}); 
    }
    
    /**
     * Gets the current login status of the service. 
     * @return
     */
    public LoginStatus getLoginStatus () {
    	return loginStatusProperty.get();
    }

    public ReadOnlyObjectProperty<LoginStatus> loginStatusProperty () {
    	return loginStatusProperty.getReadOnlyProperty();
    }
    
    public String getSessionToken () {
    	return sessionTokenProperty.get();
    }

    public ReadOnlyStringProperty sessionTokenProperty () {
    	return sessionTokenProperty.getReadOnlyProperty();
    }
}
