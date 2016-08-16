package org.nestnz.app.services;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
    
    private final Path trapCachePath;
    
    public TrapDataService (Path trapCachePath) throws IOException {
    	Objects.requireNonNull(trapCachePath);
    	Files.createDirectories(trapCachePath);
    	
    	this.trapCachePath = trapCachePath;
    	loadTraplines();
    }
    
    protected void loadTraplines () {
    	//TODO: Fetch trapline list from server
    	try (DirectoryStream<Path> stream = Files.newDirectoryStream(trapCachePath)) {
	    	for (Path traplineFile : stream) {
	        	LOG.log(Level.INFO, String.format("File: %s", traplineFile));
	        	//TODO: Use the proper method in Java 7, rather than just converting to string & checking ends with
	    		if (traplineFile.toString().endsWith(".json")) {
	    			FileClient fileClient = FileClient.create(traplineFile.toFile());
	    			
	    			InputStreamInputConverter<ParserTrapline> converter = new JsonInputConverter<>(ParserTrapline.class);
	
	    			GluonObservableObject<ParserTrapline> pTrapline = DataProvider.retrieveObject(fileClient.createObjectDataReader(converter));
	    			pTrapline.initializedProperty().addListener((obs, oldValue, newValue) -> {
	    				if (newValue) {
	    	    			traplines.add(new Trapline(pTrapline.get()));	    					
	    				}
	    			});
	    		}
	    	}
    	} catch (IOException ex) {
			LOG.log(Level.WARNING, "Failed to read trap cache directory content", ex);
		}
    	LOG.log(Level.INFO, String.format("Loaded data for %d traplines from file cache", traplines.size()));
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
		Path savedFile = trapCachePath.resolve(Integer.toString(trapline.getId())+".json");
		
		
		FileClient fileClient = FileClient.create(savedFile.toFile());
		
		OutputStreamOutputConverter<ParserTrapline> outputConverter = new JsonOutputConverter<>(ParserTrapline.class);

		DataProvider.storeObject(new ParserTrapline(trapline), fileClient.createObjectDataWriter(outputConverter));

    	LOG.log(Level.INFO, String.format("Saved trapline data for %s to %s", trapline.getName(), savedFile));		
	}
}
