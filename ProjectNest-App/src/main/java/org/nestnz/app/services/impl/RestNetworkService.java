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
package org.nestnz.app.services.impl;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.Trap;
import org.nestnz.app.net.model.ApiCatch;
import org.nestnz.app.services.LoginService;
import org.nestnz.app.services.NetworkService;

import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.ObjectDataWriter;
import com.gluonhq.connect.provider.RestClient;
import com.gluonhq.connect.source.RestDataSource;
import com.gluonhq.impl.connect.provider.RestObjectDataWriterAndRemover;

/**
 * 
 */
public class RestNetworkService implements NetworkService {

    private static final Logger LOG = Logger.getLogger(RestNetworkService.class.getName());
	
	private final LoginService loginService;
	
	public RestNetworkService (LoginService loginService) {
		this.loginService = loginService;
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#sendLoggedCatch(int, org.nestnz.app.model.Catch)
	 */
	@Override
	public void sendLoggedCatch(int trapId, Catch loggedCatch) {
		RestClient apiClient = RestClient.create().method("POST").host("https://api.nestnz.org")
    			.path("/catch").header("Session-Token", loginService.getSessionToken())
    			.contentType("application/json");
		
		String logged = loggedCatch.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		
		ApiCatch apiCatch = new ApiCatch(trapId, loggedCatch.getCatchType().getId(), null, logged);
		
		RestDataSource dataSource = apiClient.createRestDataSource();
		
		ObjectDataWriter<ApiCatch> writer = new RestObjectDataWriterAndRemover<>(dataSource, ApiCatch.class);
    	
		GluonObservableObject<ApiCatch> result = DataProvider.storeObject(apiCatch, writer);

    	result.stateProperty().addListener((obs, oldValue, newValue) -> {
    		switch (newValue) {
			case FAILED://Successful responses will be marked as 'failed', because the data writer tries to read the response as JSON
	    	case SUCCEEDED:
				if (dataSource.getResponseCode() == 201) {//Created successfully
					List<String> locationHeaders = dataSource.getResponseHeaders().get("Location");
					if (locationHeaders.isEmpty()) {
						LOG.log(Level.WARNING, "Missing 'Location' header in creation response for catch "+apiCatch);
					} else {
						int id;
						try {
							id = extractIdFromRedirect(locationHeaders.get(0));
						} catch (RuntimeException ex) {
							LOG.log(Level.WARNING, "Could not find ID in redirect string: "+locationHeaders.get(0), ex);
							return;
						}
						LOG.log(Level.INFO, "Successfully logged catch: "+apiCatch);
						loggedCatch.setId(id);
					}
				} else {
					LOG.log(Level.WARNING, "Failed to send catch to server. HTTP response: "+dataSource.getResponseMessage(), result.getException());
				}
				break;
			case READY:
			case REMOVED:
			case RUNNING:
			default:
				break;    		
    		}
    	});
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#sendCreatedTrap(int, org.nestnz.app.model.Trap)
	 */
	@Override
	public void sendCreatedTrap(int traplineId, Trap trap) {
		// TODO Auto-generated method stub

	}
	
	public int extractIdFromRedirect (String location) {
		return Integer.parseInt(location.substring(location.lastIndexOf('/')+1));
	}

}