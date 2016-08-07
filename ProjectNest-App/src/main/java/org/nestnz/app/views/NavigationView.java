package org.nestnz.app.views;

import static org.nestnz.app.util.NavigationTools.getDistance;

import java.util.logging.Logger;

import org.nestnz.app.NestApplication;
import org.nestnz.app.model.Trap;
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
import javafx.scene.control.Label;

public class NavigationView extends View {
	
	public static final String NAME = "navigation";

    private static final Logger LOG = Logger.getLogger(NavigationView.class.getName());
    
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
        
        initMonitors();
    }
    
    private void initMonitors () {
    	trapProperty.addListener((obs, oldV, newV) -> {
    		if (newV == null) {
    			targetCoordsProperty.set(null);
    		} else {
    			targetCoordsProperty.set(new Position(newV.getLatitude(), newV.getLongitude()));
    		}
    	});
    	
    	PlatformFactory.getPlatform().getPositionService().positionProperty().addListener((obs, oldPos, newPos) -> {
        	if (newPos != null) {
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
        	setCenter(compass);
        }
    }
    
    /**
     * Sets the target trap for the navigation view
     * @param trap The target trap
     */
    public final void setTrap (Trap trap) {
    	trapProperty.set(trap);
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().showLayer(NestApplication.MENU_LAYER)));
        appBar.setTitleText("Trap "+trapProperty.get().getNumber());
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> MobileApplication.getInstance().switchToPreviousView()));
    }
    
}
