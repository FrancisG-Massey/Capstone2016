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

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.parser.ParserTrapline;

import com.gluonhq.connect.GluonObservableObject;
import com.gluonhq.connect.converter.InputStreamInputConverter;
import com.gluonhq.connect.converter.JsonInputConverter;
import com.gluonhq.connect.converter.JsonOutputConverter;
import com.gluonhq.connect.converter.OutputStreamOutputConverter;
import com.gluonhq.connect.provider.DataProvider;
import com.gluonhq.connect.provider.FileClient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class TrapDataService {

    private static final Logger LOG = Logger.getLogger(TrapDataService.class.getName());
    
    private final ObservableList<Trapline> traplines = FXCollections.observableArrayList();
    
    private final File trapCachePath;
    
    public TrapDataService (File trapCachePath) throws IOException {
    	Objects.requireNonNull(trapCachePath);
    	trapCachePath.mkdirs();
    	
    	this.trapCachePath = trapCachePath;
    	addSampleTraplines();
    	loadTraplines();
    }
    
    protected void loadTraplines () {
    	//TODO: Fetch trapline list from server
    	int count = 0;
    	for (File traplineFile : trapCachePath.listFiles()) {
        	LOG.log(Level.INFO, String.format("File: %s", traplineFile));
        	
        	if (traplineFile.toString().endsWith(".json")) {
    			FileClient fileClient = FileClient.create(traplineFile);
    			
    			InputStreamInputConverter<ParserTrapline> converter = new JsonInputConverter<>(ParserTrapline.class);

    			GluonObservableObject<ParserTrapline> pTrapline = DataProvider.retrieveObject(fileClient.createObjectDataReader(converter));
    			pTrapline.initializedProperty().addListener((obs, oldValue, newValue) -> {
    				if (newValue) {
    					try {
    						addTrapline(new Trapline(pTrapline.get()));
    					} catch (RuntimeException ex) {
    						LOG.log(Level.WARNING, "Failed to load data from trapline file "+traplineFile, ex);
    					}
    				}
    			});
    			count++;
    		}
    	}
    	LOG.log(Level.INFO, String.format("Found data for %d traplines in file cache", count));
    }
    
    private void addTrapline (Trapline trapline) {
    	Iterator<Trapline> it = traplines.iterator();
    	while (it.hasNext()) {
    		if (it.next().getId() == trapline.getId()) {
    			it.remove();
    		}
    	}
    	traplines.add(trapline);
    }
    
    /**
     * Fetches a trapline based on its ID. 
     * This method only checks the memory cache for traplines - it doesn't check whether a trapline has been cached on the disk or exists on the server.
     * @param id The ID of the trapline to lookup
     * @return The trapline with the matching ID, or null if no trapline could be found
     */
    public final Trapline getTrapline (int id) {
    	for (Trapline t : traplines) {
    		if (t.getId() == id) {
    			return t;
    		}
    	}
    	return null;
    }

	public final ObservableList<Trapline> getTraplines() {
		return traplines;
	}
    
    private void addSampleTraplines () {
    	if (getTrapline(20) == null) {
	    	Trapline t1 = new Trapline(20, "Test trapline", new Region(20, "Test Region"), "Test Start");
	    	t1.getTraps().add(new Trap(1, 1, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()));
	    	t1.getTraps().add(new Trap(2, 2, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()));
	    	addTrapline(t1);
    	}
    	
    	if (getTrapline(21) == null) {
    		Trapline gorge = new Trapline(21, "Manawatu Gorge", new Region(21, "Manawatu"), "Ashhurst", "Woodville");
    		addTrapline(gorge);
    	}
    }
	
	/**
	 * Updates the cached version of the trapline and flags the trapline as dirty
	 * @param trapline The trapline to update
	 * @return a {@link GluonObservableObject} which is set when the object is fully written
	 */
	public GluonObservableObject<ParserTrapline> updateTrapline (Trapline trapline) {
		//TODO: Save the changes to the server
		File savedFile = new File(trapCachePath, Integer.toString(trapline.getId())+".json");
		
		
		FileClient fileClient = FileClient.create(savedFile);
		
		OutputStreamOutputConverter<ParserTrapline> outputConverter = new JsonOutputConverter<>(ParserTrapline.class);

		GluonObservableObject<ParserTrapline> result = DataProvider.storeObject(new ParserTrapline(trapline), fileClient.createObjectDataWriter(outputConverter));

    	LOG.log(Level.INFO, String.format("Saved trapline data for %s to %s", trapline.getName(), savedFile));		
    	return result;
	}
}
