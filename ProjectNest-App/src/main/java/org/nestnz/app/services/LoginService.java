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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.util.BackgroundTasks;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.SettingsService;
import com.gluonhq.connect.provider.RestClient;
import com.gluonhq.connect.source.RestDataSource;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

public class LoginService {
	
	public static enum LoginStatus {
		PENDING_LOGIN,
		PENDING_LOGOUT,
		LOGGED_IN,
		LOGGED_OUT,
		INVALID_CREDENTIALS,
		SERVER_UNAVAILABLE,
		UNKNOWN_ERROR;
	}

    private static final Logger LOG = Logger.getLogger(LoginService.class.getName());
	
	/**
	 * The connection timeout for all network requests, in milliseconds
	 */
	private static final int TIMEOUT = 60_000;
    
    private static LoginService instance;
    
    public static synchronized LoginService getInstance () {
    	if (instance == null) {
    		instance = new LoginService();
    	}
    	return instance;
    }
    
    private final ReadOnlyObjectWrapper<LoginStatus> loginStatusProperty = new ReadOnlyObjectWrapper<>(LoginStatus.LOGGED_OUT);
    
    private final ReadOnlyStringWrapper sessionTokenProperty = new ReadOnlyStringWrapper();
    
    private final Optional<SettingsService> settingService;
    
    private String username;
    
    private String password;
    
    private LoginService () {
    	settingService = Services.get(SettingsService.class);
    }
    
    /**
     * Tries to log in using the username & password saved in the device settings
     * @return true if credentials were found & used, false if no credentials were found
     */
    public boolean checkSavedCredentials () {
    	if (settingService.isPresent()) {
	    	String email = settingService.get().retrieve("api.email");
	    	String password = settingService.get().retrieve("api.password");
	    	if (email == null || password == null) {
	    		return false;
	    	}
	    	login(email, password);
	    	return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Tries to renew the {@link #sessionTokenProperty} using the username & password used in the last call to {@link #login(String, String)}.
     * @return The {@link #loginStatusProperty()}, which can be listened to & indicates when the request has completed
     * @throws IllegalStateException if no username & password have been saved for future use (i.e. if no successful call to {@link #login(String, String)} has completed before calling this method)
     */
    public ReadOnlyObjectProperty<LoginStatus> renewSession () {
    	if (username == null || password == null) {
    		throw new IllegalStateException("Username & password not set!");
    	}
    	return login(username, password);
    }
    
    public ReadOnlyObjectProperty<LoginStatus> login (String username, String password) {
    	if (getLoginStatus() == LoginStatus.PENDING_LOGIN) {
    		return loginStatusProperty.getReadOnlyProperty();//Already logging in
    	}
    	if (username == null || username.trim().length() < 1) {
    		throw new IllegalArgumentException("Invalid username: "+username);
    	}
    	loginStatusProperty.set(LoginStatus.PENDING_LOGIN);
    	
    	String credentials = username+":"+password;
    	String encodedAuth = "Basic "+new String(Base64.getEncoder().encode(credentials.getBytes()), Charset.forName("UTF-8"));
    	
    	RestClient loginClient = RestClient.create().method("POST").host("https://api.nestnz.org")
    			.path("/session/").connectTimeout(TIMEOUT).header("Authorization", encodedAuth);
    	
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
							LOG.log(Level.INFO, "Logged in successfully. Session token: "+sessionTokenProperty.get());
							
							//Save the email & password for future use
							settingService.ifPresent(service -> {
								service.store("api.email", username);
								service.store("api.password", password);
							});
							this.username = username;
							this.password = password;
							
							loginStatusProperty.set(LoginStatus.LOGGED_IN);
						}					
						break;
					case 403://Invalid username/password
						loginStatusProperty.set(LoginStatus.INVALID_CREDENTIALS);
						break;
					case -1:
						LOG.log(Level.SEVERE, "Problem sending login request.");
						loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);	
						break;
					default://Some other error occured
						LOG.log(Level.SEVERE, "Problem sending login request. Response="+dataSource.getResponseMessage());
						loginStatusProperty.set(LoginStatus.UNKNOWN_ERROR);					
					}
			    });
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, "Problem sending login request", ex);
				Platform.runLater(() -> loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE));
			} catch (RuntimeException ex) {
				LOG.log(Level.SEVERE, "Problem sending login request", ex);
				Platform.runLater(() -> loginStatusProperty.set(LoginStatus.UNKNOWN_ERROR));
			}
    	}); 
    	return loginStatusProperty.getReadOnlyProperty();
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
    	if (settingService.isPresent()) {
    		settingService.get().remove("api.email");
    		settingService.get().remove("api.password");
    	}
    	
    	RestClient loginClient = RestClient.create().method("DELETE").host("https://api.nestnz.org")
    			.path("/session/").connectTimeout(TIMEOUT).queryParam("Session-Token", getSessionToken());
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
					case -1:
						LOG.log(Level.SEVERE, "Problem sending logout request.");
						loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);	
						break;
					default://Some other error occured
						LOG.log(Level.SEVERE, "Problem sending logout request. Response="+dataSource.getResponseMessage());
						loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);					
					}
		    	});
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, "Problem sending logout request", ex);
		    	Platform.runLater(() -> loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE));
			} catch (RuntimeException ex) {
				LOG.log(Level.SEVERE, "Problem sending logout request", ex);
		    	Platform.runLater(() -> loginStatusProperty.set(LoginStatus.UNKNOWN_ERROR));
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
