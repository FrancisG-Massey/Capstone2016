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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;

public class NavigationView extends View {
	
	public static final String NAME = "navigation";

    private static final Logger LOG = Logger.getLogger(NavigationView.class.getName());
    
    private Line directionLine = new Line(0.0, 0.0, 50.0, 50.0);
    
    private final ObjectProperty<Trap> trapProperty = new SimpleObjectProperty<>();
    
    private final ObjectProperty<Position> targetCoordsProperty = new SimpleObjectProperty<>();
    
    private final StringProperty distanceToTrap = new SimpleStringProperty();

    public NavigationView() {
        super(NAME);
        getStylesheets().add(NavigationView.class.getResource("secondary.css").toExternalForm());
        
        VBox controls = new VBox();
        controls.setAlignment(Pos.TOP_CENTER);
        
        setCenter(controls);
        
        //setShowTransitionFactory(BounceInRightTransition::new);
        
        getLayers().add(new FloatingActionButton(MaterialDesignIcon.INFO.text, 
            e -> System.out.println("Info")));
        
        Label distanceLabel = new Label("0.0");
        distanceLabel.setId("distance-label");
        
        distanceLabel.textProperty().bind(distanceToTrap);
        
        AnchorPane pane = new AnchorPane();
        pane.getChildren().add(directionLine);
        
        //Group compass = new Group();
        //compass.getChildren().add(directionLine);
                
        controls.getChildren().addAll(distanceLabel, pane);
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
        	headingService.headingProperty().addListener((obs, oldHeading, newHeading) -> {
            	if (newHeading != null) {
            		
            	}
            });
        } else {
        	//headingLabel.setText("Your device does not support heading!");
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
