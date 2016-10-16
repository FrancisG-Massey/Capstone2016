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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.net.model.ApiCatch;
import org.nestnz.app.net.model.ApiCatchType;
import org.nestnz.app.net.model.ApiRegion;
import org.nestnz.app.net.model.ApiTrap;
import org.nestnz.app.net.model.ApiTrapline;
import org.nestnz.app.net.model.ApiPostTrap;
import org.nestnz.app.services.LoginService;
import org.nestnz.app.services.LoginService.LoginStatus;
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
		RestClient apiClient = RestClient.create().method("POST").host("https://api.nestnz.org")
    			.path("/catch").header("Session-Token", loginService.getSessionToken())
    			.contentType("application/json");
		
		String logged = loggedCatch.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		
		ApiCatch apiCatch = new ApiCatch(trapId, loggedCatch.getCatchType().getId(), null, logged);
		
    	return processCreateRequest(ApiCatch.class, apiCatch, apiClient, id -> {
    		LOG.log(Level.INFO, "Successfully logged catch: "+apiCatch);
    		loggedCatch.setId(id);
    	});
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#sendCreatedTrap(int, org.nestnz.app.model.Trap)
	 */
	@Override
	public ReadOnlyObjectProperty<RequestStatus> sendCreatedTrap(int traplineId, Trap trap) {
		RestClient apiClient = RestClient.create().method("POST").host("https://api.nestnz.org")
    			.path("/trap").header("Session-Token", loginService.getSessionToken())
    			.contentType("application/json");
		
		String created = trap.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		
		ApiPostTrap apiTrap = new ApiPostTrap(traplineId, trap.getNumber(), trap.getLatitude(), trap.getLongitude(), created);

		return processCreateRequest(ApiPostTrap.class, apiTrap, apiClient, id -> {
			LOG.log(Level.INFO, "Successfully created trap: "+apiTrap);
			trap.setId(id);    		
    	});
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#loadRegions(java.util.function.Consumer)
	 */
	@Override
	public ReadOnlyObjectProperty<RequestStatus> loadRegions(Consumer<Region> loadCallback) {
		RestClient regionClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/region");
		
		return processReadRequest(ApiRegion.class, regionClient, apiRegion -> {
			Region region = new Region(apiRegion.getId(), apiRegion.getName());
			loadCallback.accept(region);
		});
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#loadCatchTypes(java.util.function.Consumer)
	 */
	@Override
	public ReadOnlyObjectProperty<RequestStatus> loadCatchTypes(Consumer<CatchType> loadCallback) {
		RestClient catchTypeClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/catch-type");
		return processReadRequest(ApiCatchType.class, catchTypeClient, apiCatchType -> {
			URL imageUrl = null;
			if (apiCatchType.getImageUrl() != null) {
				try {
					imageUrl = new URL(apiCatchType.getImageUrl());
				} catch (MalformedURLException ex) {
					LOG.log(Level.WARNING, "Error decoding image url: "+apiCatchType.getImageUrl(), ex);
				}
			}
			CatchType catchType = new CatchType(apiCatchType.getId(), apiCatchType.getName(), imageUrl);
			loadCallback.accept(catchType);
		});
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#loadTrapline(org.nestnz.app.services.Trapline, java.util.function.Consumer)
	 */
	@Override
	public ReadOnlyObjectProperty<RequestStatus> loadTrapline(Trapline trapline, Consumer<Trap> loadCallback) {
		RestClient trapsClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/trap").queryParam("trapline-id", Integer.toString(trapline.getId()));
		
		return processReadRequest(ApiTrap.class, trapsClient, apiTrap -> {
			if (apiTrap.getTraplineId() != trapline.getId()) {
				LOG.log(Level.WARNING, apiTrap+" was returned in a request for trapline "+trapline.getId());
				return;
			}
			
			LocalDateTime created = LocalDateTime.parse(apiTrap.getCreated().replace(' ', 'T'));
			LocalDateTime lastReset = apiTrap.getLastReset() == null ? null : LocalDateTime.parse(apiTrap.getLastReset().replace(' ', 'T'));
			
			Trap trap  = new Trap(apiTrap.getId(), apiTrap.getNumber(), 
					apiTrap.getLatitude(), apiTrap.getLongitude(), TrapStatus.ACTIVE, created, lastReset);
			
			loadCallback.accept(trap);			
		});
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.NetworkService#loadTraplines(java.util.function.Consumer)
	 */
	@Override
	public ReadOnlyObjectProperty<RequestStatus> loadTraplines(Consumer<Trapline> loadCallback) {
		RestClient traplineClient = RestClient.create().method("GET").host("https://api.nestnz.org")
    			.path("/trapline");
    	
    	return processReadRequest(ApiTrapline.class, traplineClient, apiTrapline -> {
    		Region r = new Region(apiTrapline.getRegionId());
    		Trapline trapline = new Trapline(apiTrapline.getId(), apiTrapline.getName(), r, apiTrapline.getStart(), apiTrapline.getEnd());
    		trapline.getCatchTypes().add(new CatchType(apiTrapline.getCommonCatchType1()));
    		trapline.getCatchTypes().add(new CatchType(apiTrapline.getCommonCatchType2()));
    		trapline.getCatchTypes().add(new CatchType(apiTrapline.getCommonCatchType3()));
    		
    		loadCallback.accept(trapline);
    	});
	}	
	
	private <T> ReadOnlyObjectProperty<RequestStatus> processReadRequest (Class<T> type, RestClient client, Consumer<T> callback) {
		return processReadRequest(type, client, callback, true);
	}
	
	private <T> ReadOnlyObjectProperty<RequestStatus> processReadRequest (Class<T> type, RestClient client, Consumer<T> callback, boolean retry) {
		ReadOnlyObjectWrapper<RequestStatus> status = new ReadOnlyObjectWrapper<>(RequestStatus.PENDING);
		    	
    	RestDataSource dataSource = client.createRestDataSource();
    	
    	dataSource.getHeaders().remove("Session-Token");
    	dataSource.addHeader("Session-Token", loginService.getSessionToken());
		
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
				switch (dataSource.getResponseCode()) {
				case 204://No Content
					status.set(RequestStatus.SUCCESS);//No data received, but the request was still successful
					break;
				case 403://Session timeout
					if (retry) {
						loginService.renewSession().addListener((loginObs, oldLoginStatus, newLoginStatus) -> {
							if (newLoginStatus == LoginStatus.LOGGED_IN) {
								//Successfully renewed session
								LOG.log(Level.INFO, "Renewed session successfully. Resending request using new session token...");
								//Bind this status property to the result of the inner request
								status.bind(processReadRequest(type, client, callback, false));
							} else if (newLoginStatus == LoginStatus.SERVER_UNAVAILABLE) {
								//Problem renewing session due to sever unavailability
								status.set(RequestStatus.FAILED);
							} else if (newLoginStatus == LoginStatus.INVALID_CREDENTIALS) {
								//Old credentials no longer work
								status.set(RequestStatus.FAILED_UNAUTHORISED);
							}
						});
					} else {
						//Already tried once & received a 403 error.
						status.set(RequestStatus.FAILED_UNAUTHORISED);
					}
					break;
				default:
					//Handle failure
					status.set(RequestStatus.FAILED);
					LOG.log(Level.SEVERE, "Problem loading "+type+". HTTP response: "+dataSource.getResponseMessage(), resultList.getException());
					break;
				}				
			} else if (newState == ConnectState.SUCCEEDED) {
				status.set(RequestStatus.SUCCESS);
			}
		});
		return status;
	}
	
	private <T> ReadOnlyObjectProperty<RequestStatus> processCreateRequest (Class<T> type, T data, RestClient client, Consumer<Integer> callback) {
		ReadOnlyObjectWrapper<RequestStatus> status = new ReadOnlyObjectWrapper<>(RequestStatus.PENDING);
		
		RestDataSource dataSource = client.createRestDataSource();
		
		ObjectDataWriter<T> writer = new RestObjectDataWriterAndRemover<>(dataSource, type);
    	
		GluonObservableObject<T> result = DataProvider.storeObject(data, writer);

    	result.stateProperty().addListener((obs, oldValue, newState) -> {
    		if (newState == ConnectState.SUCCEEDED || newState == ConnectState.FAILED) {
    			//Successful responses will be marked as 'failed', because the data writer tries to read the response as JSON
    			
				if (dataSource.getResponseCode() == 201) {//Created successfully
					List<String> locationHeaders = dataSource.getResponseHeaders().get("Location");
					if (locationHeaders.isEmpty()) {
						LOG.log(Level.WARNING, "Missing 'Location' header in creation response for "+data);
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
						callback.accept(id);
						status.set(RequestStatus.SUCCESS);
					}
				} else {
					LOG.log(Level.WARNING, "Failed to send "+data+" to server. HTTP response: "+dataSource.getResponseMessage(), result.getException());
					status.set(RequestStatus.FAILED);
				}  		
    		}
    	});
    	return status.getReadOnlyProperty();
	}
	
	public int extractIdFromRedirect (String location) {
		return Integer.parseInt(location.substring(location.lastIndexOf('/')+1));
	}
}
