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
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.NestApplication;

import com.gluonhq.connect.provider.RestClient;
import com.gluonhq.connect.source.RestDataSource;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

public final class LoginService {
	
	public static enum LoginStatus {
		PENDING,
		LOGGED_IN,
		LOGGED_OUT,
		INVALID_CREDENTIALS,
		SERVER_UNAVAILABLE;
	}

    private static final Logger LOG = Logger.getLogger(LoginService.class.getName());
    
    private final ReadOnlyObjectWrapper<LoginStatus> loginStatusProperty = new ReadOnlyObjectWrapper<>();
    
    private final ReadOnlyStringWrapper sessionTokenProperty = new ReadOnlyStringWrapper();
    
    public LoginService () {
    	
    }
    
    public void login (String username, String password) {
    	loginStatusProperty.set(LoginStatus.PENDING);
    	LOG.log(Level.INFO, "Attempted to log in with credentials: "+username+", "+password);
    	
    	String encodedAuth = "Basic "+new String(Base64.getEncoder().encode((username+":"+password).getBytes()));
        
    	RestClient loginClient = RestClient.create().method("POST").host("https://nestnz.org")
    			.path("/api/session/").queryParam("Authorization", encodedAuth);
    	
    	final RestDataSource dataSource = loginClient.createRestDataSource();
    	
    	NestApplication.runInBackground(() -> {
    		try {
				dataSource.getInputStream();
				switch (dataSource.getResponseCode()) {
				case 200://Success
					List<String> sessionHeaders = dataSource.getResponseHeaders().get("Session-Token");
					if (sessionHeaders == null || sessionHeaders.size() == 0) {
						LOG.log(Level.SEVERE, "Session token missing from server response");
						loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);
					} else {
						sessionTokenProperty.set(sessionHeaders.get(0));
						loginStatusProperty.set(LoginStatus.LOGGED_IN);
					}					
					break;
				case 401://Invalid username/password
					loginStatusProperty.set(LoginStatus.INVALID_CREDENTIALS);
					break;
				default://Some other error occured
					LOG.log(Level.SEVERE, "Problem sending login request. Response="+dataSource.getResponseMessage());
					loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);					
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, "Problem sending login request", ex);
				loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE);
			}
    	}); 
    }
    
    /**
     * Gets the current login status of the service. 
     * @return
     */
    public final LoginStatus getLoginStatus () {
    	return loginStatusProperty.get();
    }

    public final ReadOnlyObjectProperty<LoginStatus> loginStatusProperty () {
    	return loginStatusProperty.getReadOnlyProperty();
    }
    
    public final String getSessionToken () {
    	return sessionTokenProperty.get();
    }

    public final ReadOnlyStringProperty sessionTokenProperty () {
    	return sessionTokenProperty.getReadOnlyProperty();
    }
}
