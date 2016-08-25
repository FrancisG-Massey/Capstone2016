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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    	//loadTraplines();
    }
    
    protected void loadTraplines () {
    	//TODO: Fetch trapline list from server
    	int count = 0;
    	for (File traplineFile : trapCachePath.listFiles()) {
        	LOG.log(Level.INFO, String.format("File: %s", traplineFile));
        	//TODO: Use the proper method in Java 7, rather than just converting to string & checking ends with
    		if (traplineFile.toString().endsWith(".json")) {
    			FileClient fileClient = FileClient.create(traplineFile);
    			
    			InputStreamInputConverter<ParserTrapline> converter = new JsonInputConverter<>(ParserTrapline.class);

    			GluonObservableObject<ParserTrapline> pTrapline = DataProvider.retrieveObject(fileClient.createObjectDataReader(converter));
    			pTrapline.initializedProperty().addListener((obs, oldValue, newValue) -> {
    				if (newValue) {
    	    			traplines.add(new Trapline(pTrapline.get()));	    					
    				}
    			});
    			count++;
    		}
    	}
    	LOG.log(Level.INFO, String.format("Found data for %d traplines in file cache", count));
    }

	public ObservableList<Trapline> getTraplines() {
		return traplines;
	}
	
	/**
	 * Updates the cached version of the trapline and flags the trapline as dirty
	 * @param trapline The trapline to update
	 */
	public void updateTrapline (Trapline trapline) {
		//TODO: Save the changes to the server
		File savedFile = new File(trapCachePath, Integer.toString(trapline.getId())+".json");
		
		
		FileClient fileClient = FileClient.create(savedFile);
		
		OutputStreamOutputConverter<ParserTrapline> outputConverter = new JsonOutputConverter<>(ParserTrapline.class);

		DataProvider.storeObject(new ParserTrapline(trapline), fileClient.createObjectDataWriter(outputConverter));

    	LOG.log(Level.INFO, String.format("Saved trapline data for %s to %s", trapline.getName(), savedFile));		
	}
}
