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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.net.model.ApiCatch;
import org.nestnz.app.net.model.ApiRegion;
import org.nestnz.app.net.model.ApiTrap;
import org.nestnz.app.services.LoginService;
import org.nestnz.app.services.NetworkService;

import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.ListDataReader;
import com.gluonhq.connect.provider.ObjectDataWriter;
import com.gluonhq.connect.provider.RestClient;
import com.gluonhq.connect.source.RestDataSource;
import com.gluonhq.impl.connect.provider.RestListDataReader;
import com.gluonhq.impl.connect.provider.RestObjectDataWriterAndRemover;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;

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
	public ReadOnlyObjectProperty<RequestStatus> sendLoggedCatch(int trapId, Catch loggedCatch) {
		ReadOnlyObjectWrapper<RequestStatus> status = new ReadOnlyObjectWrapper<>(RequestStatus.PENDING);
		
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
						status.set(RequestStatus.FAILED);
					} else {
						int id;
						try {
							id = extractIdFromRedirect(locationHeaders.get(0));
						} catch (RuntimeException ex) {
							LOG.log(Level.WARNING, "Could not find ID in redirect string: "+locationHeaders.get(0), ex);
							status.set(RequestStatus.FAILED);
							return;
						}
						LOG.log(Level.INFO, "Successfully logged catch: "+apiCatch);
						loggedCatch.setId(id);
						status.set(RequestStatus.SUCCESS);
					}
				} else {
					LOG.log(Level.WARNING, "Failed to send catch to server. HTTP response: "+dataSource.getResponseMessage(), result.getException());
					status.set(RequestStatus.FAILED);
				}
				break;
			case READY:
			case REMOVED:
			case RUNNING:
			default:
				break;    		
    		}
    	});
    	return status.getReadOnlyProperty();
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#sendCreatedTrap(int, org.nestnz.app.model.Trap)
	 */
	@Override
	public ReadOnlyObjectProperty<RequestStatus> sendCreatedTrap(int traplineId, Trap trap) {
		ReadOnlyObjectWrapper<RequestStatus> status = new ReadOnlyObjectWrapper<>(RequestStatus.PENDING);
		
		RestClient apiClient = RestClient.create().method("POST").host("https://api.nestnz.org")
    			.path("/trap").header("Session-Token", loginService.getSessionToken())
    			.contentType("application/json");
		
		String created = trap.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		
		ApiTrap apiTrap = new ApiTrap(traplineId, trap.getNumber(), trap.getLatitude(), trap.getLongitude(), created);
		
		RestDataSource dataSource = apiClient.createRestDataSource();
		
		ObjectDataWriter<ApiTrap> writer = new RestObjectDataWriterAndRemover<>(dataSource, ApiTrap.class);
    	
		GluonObservableObject<ApiTrap> result = DataProvider.storeObject(apiTrap, writer);

    	result.stateProperty().addListener((obs, oldValue, newValue) -> {
    		switch (newValue) {
			case FAILED://Successful responses will be marked as 'failed', because the data writer tries to read the response as JSON
	    	case SUCCEEDED:
				if (dataSource.getResponseCode() == 201) {//Created successfully
					List<String> locationHeaders = dataSource.getResponseHeaders().get("Location");
					if (locationHeaders.isEmpty()) {
						LOG.log(Level.WARNING, "Missing 'Location' header in creation response for trap "+apiTrap);
						status.set(RequestStatus.FAILED);
					} else {
						int id;
						try {
							id = extractIdFromRedirect(locationHeaders.get(0));
						} catch (RuntimeException ex) {
							LOG.log(Level.WARNING, "Could not find ID in redirect string: "+locationHeaders.get(0), ex);
							status.set(RequestStatus.FAILED);
							return;
						}
						LOG.log(Level.INFO, "Successfully created trap: "+apiTrap);
						trap.setId(id);
						status.set(RequestStatus.SUCCESS);
					}
				} else {
					LOG.log(Level.WARNING, "Failed to send trap to server. HTTP response: "+dataSource.getResponseMessage(), result.getException());
					status.set(RequestStatus.FAILED);
				}
				break;
			case READY:
			case REMOVED:
			case RUNNING:
			default:
				break;    		
    		}
    	});
    	return status.getReadOnlyProperty();
	}
	
	public int extractIdFromRedirect (String location) {
		return Integer.parseInt(location.substring(location.lastIndexOf('/')+1));
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#loadRegions(java.util.function.Consumer)
	 */
	@Override
	public ReadOnlyObjectProperty<RequestStatus> loadRegions(Consumer<Region> loadCallback) {
		RestClient regionClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/region").header("Session-Token", loginService.getSessionToken());
		
		return processReadRequest(ApiRegion.class, regionClient, apiRegion -> {
			Region region = new Region(apiRegion.getId(), apiRegion.getName());
			loadCallback.accept(region);
		});
	}
	
	
	private <T> ReadOnlyObjectProperty<RequestStatus>  processReadRequest (Class<T> type, RestClient client, Consumer<T> callback) {
		ReadOnlyObjectWrapper<RequestStatus> status = new ReadOnlyObjectWrapper<>(RequestStatus.PENDING);
		    	
    	RestDataSource dataSource = client.createRestDataSource();
		
		ListDataReader<T> reader = new RestListDataReader<>(dataSource, type);
		GluonObservableList<T> resultList = DataProvider.retrieveList(reader);
		resultList.addListener((ListChangeListener<T>) c -> {
			while (c.next()) {
				if (c.wasAdded()) {
					for (T item : c.getAddedSubList()) {
						callback.accept(item);
					}
				}
			}
		});
		
		resultList.stateProperty().addListener((obs, oldState, newState) -> {
			if (newState == ConnectState.FAILED) {
				//Handle failure
				status.set(RequestStatus.FAILED);
				LOG.log(Level.SEVERE, "Problem loading "+type+". HTTP response: "+dataSource.getResponseMessage(), resultList.getException());
			} else if (newState == ConnectState.SUCCEEDED) {
				status.set(RequestStatus.SUCCESS);
			}
		});
		return status;
	}

}
