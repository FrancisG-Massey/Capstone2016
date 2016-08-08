package org.nestnz.app.views;

import static org.nestnz.app.util.NavigationTools.getDistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.NestApplication;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.HeadingService;
import org.nestnz.app.services.NestPlatformFactory;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.down.common.Position;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import eu.hansolo.fx.AirCompass;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class NavigationView extends View {
	
	public static final String NAME = "navigation";

    private static final Logger LOG = Logger.getLogger(NavigationView.class.getName());
    
    private Trapline trapline;
    
    private final Button prev;
    
    private final Button next;
    
    private final List<Trap> orderedTraps = new ArrayList<>();
    
    private ListIterator<Trap> iterator;
    
    private final ObjectProperty<Trap> trapProperty = new SimpleObjectProperty<>();
    
    private final ObjectProperty<Position> targetCoordsProperty = new SimpleObjectProperty<>();
    
    private final StringProperty distanceToTrap = new SimpleStringProperty();

    public NavigationView() {
        super(NAME);
        getStylesheets().add(NavigationView.class.getResource("secondary.css").toExternalForm());
        
        //setShowTransitionFactory(BounceInRightTransition::new);
        
        getLayers().add(new FloatingActionButton(MaterialDesignIcon.INFO.text, 
            e -> System.out.println("Info")));
        
        Label distanceLabel = new Label("0.0");
        distanceLabel.setMaxWidth(1000.0);
        distanceLabel.setId("distance-label");
        distanceLabel.setAlignment(Pos.CENTER);
        
        distanceLabel.textProperty().bind(distanceToTrap);
                
        setTop(distanceLabel);
        
        prev = MaterialDesignIcon.ARROW_BACK.button(evt -> previousTrap());
        next = MaterialDesignIcon.ARROW_FORWARD.button(evt -> nextTrap());
        
        setLeft(prev);
        setRight(next);
        
        initMonitors();
    }
    
    private void initMonitors () {
    	trapProperty.addListener((obs, oldV, newV) -> {
    		LOG.log(Level.INFO, "Selected trap: "+newV);
    		if (newV == null) {
    			targetCoordsProperty.set(null);
    		} else {
    			targetCoordsProperty.set(new Position(newV.getLatitude(), newV.getLongitude()));
    			prev.setVisible(iterator.hasPrevious());//FIXME: This returns "true" when we're looking at the first element in the list
    			next.setVisible(iterator.hasNext());
    		}
    	});
    	
    	PlatformFactory.getPlatform().getPositionService().positionProperty().addListener((obs, oldPos, newPos) -> {
        	if (newPos != null && targetCoordsProperty.get() != null) {
        		double distance = getDistance(newPos, targetCoordsProperty.get());
        		LOG.info(String.format("Found coordinates: %1$.3f, %2$.3f (distance=%3$.2fm)", newPos.getLatitude(), newPos.getLongitude(), distance));
        		distanceToTrap.set(String.format("%1$.0fm", distance));
        	}
        });
        
        HeadingService headingService = NestPlatformFactory.getPlatform().getHeadingService();
        
        if (headingService.isHeadingAvailable()) {
        	AirCompass compass = new AirCompass();
        	headingService.headingProperty().addListener((obs, oldHeading, newHeading) -> {
            	if (newHeading != null) {
            		compass.setBearing(newHeading.doubleValue());
            	}
            });
        	//setCenter(compass);
        }
    }
    
    /**
     * Go back to the previous trap in the trapline
     */
    protected void previousTrap () {
		LOG.log(Level.INFO, "Requested swap to previous trap");
		iterator.previous();//Since iterator.previous() returns the current element, we need to call it twice to get the actual previous one then "next()" to select it
    	setTrap(iterator.previous());
		iterator.next();
    }
    
    /**
     * Jump to the next trap in the trapline
     */
    protected void nextTrap() {
		LOG.log(Level.INFO, "Requested swap to next trap");
    	setTrap(iterator.next());
    }
    
    /**
     * Sets the target trap for the navigation view
     * @param trap The target trap
     */
    public final void setTrap (Trap trap) {
    	trapProperty.set(trap);
    }
    
    /**
     * Sets the trapline for the navigation view & sets the first trap as trap #1 (or whichever is the lowest number)
     * @param trapline
     */
    public final void setTrapline (Trapline trapline) {
    	Objects.requireNonNull(trapline);
    	this.trapline = trapline;
    	
    	//Since Android/iOS don't support Java 8 streams, we have to do it the old way (adding the elements to another list & using Collections.sort()).
    	orderedTraps.clear();
    	orderedTraps.addAll(trapline.getTraps());
    	Collections.sort(orderedTraps, (t1, t2) -> {
    		return t1.getNumber() - t2.getNumber();
    	});
    	
    	iterator = orderedTraps.listIterator();
    	setTrap(iterator.next());    	
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().showLayer(NestApplication.MENU_LAYER)));
        trapProperty.addListener((obs, oldV, newV) -> {
        	if (newV != null) {
                appBar.setTitleText("Trap "+newV.getNumber());        		
        	}
        });
        appBar.setTitleText("Trap "+trapProperty.get().getNumber());
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> MobileApplication.getInstance().switchToPreviousView()));
    }
    
}
