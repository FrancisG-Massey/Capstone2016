package org.nestnz.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.JsonObject;

import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.parser.ParserTrap;
import org.nestnz.app.views.AddTrapView;
import org.nestnz.app.views.NavigationView;
import org.nestnz.app.views.TraplineInfoView;
import org.nestnz.app.views.TraplineListView;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.license.License;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.gluonhq.connect.converter.JsonConverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;

@License(key="482637c8-d766-40fa-942e-f96a11d31da8")
public class NestApplication extends MobileApplication {

    private static final Logger LOG = Logger.getLogger(NestApplication.class.getName());

    public static final String PRIMARY_VIEW = HOME_VIEW;
    public static final String MENU_LAYER = "Side Menu";
    
    private final ObservableList<Trapline> traplines = FXCollections.observableArrayList();
    private File appStoragePath;

    @Override
    public void init() throws IOException {    	
        addViewFactory(TraplineListView.NAME, () -> new TraplineListView(traplines));
        addViewFactory(NavigationView.NAME, () -> new NavigationView());
        addViewFactory(TraplineInfoView.NAME, () -> new TraplineInfoView());
        addViewFactory(AddTrapView.NAME, () -> new AddTrapView());
        
        appStoragePath = PlatformFactory.getPlatform().getPrivateStorage();

    	Trapline t1 = new Trapline(20, "Test trapline", new Region(20, "Test Region"), "Test Start");
    	t1.getTraps().add(new Trap(1, 1, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now()));
    	t1.getTraps().add(new Trap(2, 2, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now()));
    	traplines.add(t1);
    	

    	Trapline gorge = new Trapline(20, "Manawatu Gorge", new Region(20, "Manawatu"), "Ashhurst", "Woodville");
    	traplines.add(gorge);
    }
	
	private JsonConverter<ParserTrap> trapConverter = new JsonConverter<>(ParserTrap.class);
    
    public void saveNewTrap (String name, Trap trap) {
    	File newTrapCache = new File(appStoragePath, "/cache/");
    	newTrapCache.mkdir();
    	
    	JsonObject json = trapConverter.writeToJson(new ParserTrap(trap));
    	
    	File cacheData = new File(newTrapCache, name);
    	
    	try (PrintWriter writer = new PrintWriter(new FileWriter(cacheData, true))) {
    		writer.println(json.toString());
    	} catch (IOException ex) {
    		LOG.log(Level.SEVERE, "Failed to write new trap data to file", ex);
		}
    	LOG.log(Level.INFO, "Saved trap "+trap+" to file "+cacheData);
    }
    
    /**
     * Retrieves a view registered on this application by name
     * @param name The name of the view to lookup
     * @return The view
     * @throws IllegalArgumentException if the view could not be found or has not been registered 
     */
    @SuppressWarnings("unchecked")
	public <T> T lookupView (String name) {
    	return retrieveView(name)
				.map(view -> (T) view)
				.orElseThrow(() -> new IllegalArgumentException("View "+name+" not created!"));
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.GREEN.assignTo(scene);

        scene.getStylesheets().add(NestApplication.class.getResource("style.css").toExternalForm());
    }
}
