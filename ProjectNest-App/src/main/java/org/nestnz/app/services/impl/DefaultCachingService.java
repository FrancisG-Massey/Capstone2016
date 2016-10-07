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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.parser.Cacheable;
import org.nestnz.app.parser.ParserCatchType;
import org.nestnz.app.parser.ParserCatchTypeList;
import org.nestnz.app.parser.ParserTrapline;
import org.nestnz.app.services.CachingService;
import org.nestnz.app.util.BackgroundTasks;

import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.converter.InputStreamInputConverter;
import com.gluonhq.connect.converter.JsonInputConverter;
import com.gluonhq.connect.converter.JsonOutputConverter;
import com.gluonhq.connect.converter.OutputStreamOutputConverter;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.FileClient;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * This service is used to monitor traplines for changes & save them in a disk cache, so if the app crashes/looses power/whatever, the data is not lost.
 * The service also loads the cached data from disk on application startup
 */
public class DefaultCachingService implements CachingService {

    private static final Logger LOG = Logger.getLogger(DefaultCachingService.class.getName());
    
    private final File cachePath;
    
    private final File traplineCachePath;
    
    /**
     * A latch used to make sure all required resources (only catch types at this stage) have loaded before fetching traplines from the cache
     */
    private final CountDownLatch cacheResourceLoading = new CountDownLatch(1);
    
    public DefaultCachingService (File trapCachePath) {
    	trapCachePath.mkdirs();
    	
    	this.cachePath = trapCachePath;
    	this.traplineCachePath = new File(cachePath, "traplines");
    	if (!traplineCachePath.exists()) {
    		traplineCachePath.mkdir();
    	}    	
    }
    
    /* (non-Javadoc)
	 * @see org.nestnz.app.services.CachingService#fetchTraplines(java.util.function.Consumer)
	 */
    @Override
	public void fetchTraplines (Consumer<ParserTrapline> callback) {
    	int count = 0;
    	for (File traplineFile : traplineCachePath.listFiles()) {
        	LOG.log(Level.INFO, String.format("File: %s", traplineFile));
        	
        	if (traplineFile.toString().endsWith(".json")) {
    			FileClient fileClient = FileClient.create(traplineFile);
    			
    			InputStreamInputConverter<ParserTrapline> converter = new JsonInputConverter<>(ParserTrapline.class);

    			GluonObservableObject<ParserTrapline> pTrapline = DataProvider.retrieveObject(fileClient.createObjectDataReader(converter));
    			pTrapline.initializedProperty().addListener((obs, oldValue, newValue) -> {
    				if (newValue) {
    			    	BackgroundTasks.runInBackground(() -> {
							try {
								//Don't block on the UI thread
								cacheResourceLoading.await();
							} catch (InterruptedException e) {
								//Silently ignore the interrupt
							}
							Platform.runLater(() -> {
								try {
			    					callback.accept(pTrapline.get());
			    				} catch (RuntimeException ex) {
			    					LOG.log(Level.WARNING, "Failed to load data from trapline cache file "+traplineFile, ex);
			    					traplineFile.delete();//This error means the cache file must be corrupted, so delete it (we can always get the data back from the server when needed)
			    				}
							});
    			    	});
    				}
    			});
    			count++;
    		}
    	}
    	LOG.log(Level.INFO, String.format("Found data for %d traplines in file cache", count));
    }
	
	/* (non-Javadoc)
	 * @see org.nestnz.app.services.CachingService#storeTrapline(org.nestnz.app.model.Trapline)
	 */
	@Override
	public GluonObservableObject<ParserTrapline> storeTrapline (Trapline trapline) {
		File savedFile = new File(traplineCachePath, Integer.toString(trapline.getId())+".json");
		
		
		FileClient fileClient = FileClient.create(savedFile);
		
		OutputStreamOutputConverter<ParserTrapline> outputConverter = new JsonOutputConverter<>(ParserTrapline.class);

		GluonObservableObject<ParserTrapline> result = DataProvider.storeObject(new ParserTrapline(trapline), fileClient.createObjectDataWriter(outputConverter));

    	LOG.log(Level.INFO, String.format("Saved trapline data for %s to %s", trapline.getName(), savedFile));		
    	return result;
	}
	
	/* (non-Javadoc)
	 * @see org.nestnz.app.services.CachingService#fetchCatchTypes()
	 */
	@Override
	public GluonObservableObject<Cacheable<Map<Integer, CatchType>>> fetchCatchTypes () {
		GluonObservableObject<Cacheable<Map<Integer, CatchType>>> results = new GluonObservableObject<>();
	    
		Cacheable<Map<Integer, CatchType>> catchTypes = new Cacheable<>();    	
    	catchTypes.setData(new HashMap<>());    	

		results.setValue(catchTypes);
    	
		File savedFile = new File(cachePath, "catchTypes.json");
		
		if (!savedFile.exists()) {
            ((SimpleBooleanProperty) results.initializedProperty()).set(true);
			return results;//No cached data exists
		}
		 
		FileClient fileClient = FileClient.create(savedFile);
		
		InputStreamInputConverter<ParserCatchTypeList> converter = new JsonInputConverter<>(ParserCatchTypeList.class);
		
		
		GluonObservableObject<ParserCatchTypeList> cTypes = DataProvider.retrieveObject(fileClient.createObjectDataReader(converter));
		cTypes.initializedProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue) {
				try {
					ParserCatchTypeList storedObject = cTypes.get();
					catchTypes.setLastServerFetch(LocalDateTime.parse(storedObject.getLastServerFetch()));
					if (storedObject.getData() != null) {
						for (ParserCatchType pct : storedObject.getData()) {
							CatchType c = new CatchType(pct.getId());
							c.setName(pct.getName());
							if (pct.getImageUrl() != null) {
								c.setImage(new URL(pct.getImageUrl()));
							}
							catchTypes.getData().put(c.getId(), c);
						}
						results.setState(ConnectState.SUCCEEDED);
						LOG.log(Level.INFO, "Fetched "+catchTypes.getData().size()+" catch types from disk cache.");
					}
				} catch (RuntimeException | MalformedURLException ex) {
					results.setException(ex);
					results.setState(ConnectState.FAILED);
					LOG.log(Level.WARNING, "Failed to load data from saved catch types file "+savedFile, ex);
					savedFile.delete();//This error means the cache file must be corrupted, so delete it (we can always get the data back from the server when needed)
				} finally {
                    ((SimpleBooleanProperty) results.initializedProperty()).set(true);
					cacheResourceLoading.countDown();//Signal the resource loaded regardless of whether an error occurred or not, otherwise the trapline loading process will wait forever
				}
			}
		});
		return results;
	}
	
	/* (non-Javadoc)
	 * @see org.nestnz.app.services.CachingService#storeCatchTypes(org.nestnz.app.parser.Cacheable)
	 */
	@Override
	public GluonObservableObject<ParserCatchTypeList> storeCatchTypes (Cacheable<Map<Integer, CatchType>> catchTypes) {
		File savedFile = new File(cachePath, "catchTypes.json");
		
		ParserCatchTypeList storeObject = new ParserCatchTypeList();
		
		storeObject.setData(new ArrayList<>());
		storeObject.setLastServerFetch(catchTypes.getLastServerFetch().toString());
		for (CatchType c : catchTypes.getData().values()) {
			ParserCatchType pct = new ParserCatchType();
			pct.setId(c.getId());
			pct.setName(c.getName());
			if (c.getImage() != null) {
				pct.setImageUrl(c.getImage().toExternalForm());
			}
			storeObject.getData().add(pct);
		}		
		
		FileClient fileClient = FileClient.create(savedFile);
		
		OutputStreamOutputConverter<ParserCatchTypeList> outputConverter = new JsonOutputConverter<>(ParserCatchTypeList.class);

		GluonObservableObject<ParserCatchTypeList> result = DataProvider.storeObject(storeObject, fileClient.createObjectDataWriter(outputConverter));

    	LOG.log(Level.INFO, String.format("Saved catch type data to %s", savedFile));		
    	return result;
	}

}
