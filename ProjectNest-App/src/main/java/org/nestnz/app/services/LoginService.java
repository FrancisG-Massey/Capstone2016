package org.nestnz.app.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

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
    
    public LoginService () {
    	
    }
    
    public void login (String username, String password) {
    	loginStatusProperty.set(LoginStatus.PENDING);
    	LOG.log(Level.INFO, "Attempted to log in with credentials: "+username+", "+password);
    	
    	final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    	executorService.schedule(() -> {
    		Platform.runLater(() -> loginStatusProperty.set(LoginStatus.SERVER_UNAVAILABLE));
    	}, 4, TimeUnit.SECONDS);
    	
    	//RestClient loginClient = RestClient.create().method("POST").host("https://nestnz.org/api/session/").;
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
}
