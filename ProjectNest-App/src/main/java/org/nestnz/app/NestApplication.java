package org.nestnz.app;

import java.time.LocalDateTime;

import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.TrapStatus;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.views.AddTrapView;
import org.nestnz.app.views.NavigationView;
import org.nestnz.app.views.TraplineInfoView;
import org.nestnz.app.views.TraplineListView;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.license.License;
import com.gluonhq.charm.glisten.visual.Swatch;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;

@License(key="482637c8-d766-40fa-942e-f96a11d31da8")
public class NestApplication extends MobileApplication {

    public static final String PRIMARY_VIEW = HOME_VIEW;
    public static final String MENU_LAYER = "Side Menu";
    
    private final ObservableList<Trapline> traplines = FXCollections.observableArrayList();

    @Override
    public void init() {
    	Trapline t1 = new Trapline(20, "Test trapline", new Region(20, "Test Region"), "Test Start");
    	t1.getTraps().add(new Trap(1, 1, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now()));
    	t1.getTraps().add(new Trap(2, 2, 0, 0, TrapStatus.ACTIVE, LocalDateTime.now()));
    	traplines.add(t1);
    	traplines.add(new Trapline(21, "Test trapline 2", new Region(20, "Test Region"), "Test Start 2"));
    	
        addViewFactory(TraplineListView.NAME, () -> new TraplineListView(traplines));
        addViewFactory(NavigationView.NAME, () -> new NavigationView());
        addViewFactory(TraplineInfoView.NAME, () -> new TraplineInfoView());
        addViewFactory(AddTrapView.NAME, () -> new AddTrapView());
        
        /*NavigationDrawer drawer = new NavigationDrawer();
        
        NavigationDrawer.Header header = new NavigationDrawer.Header("Gluon Mobile",
                "Multi View Project",
                new Avatar(21, new Image(NestApplication.class.getResourceAsStream("/icon.png"))));
        drawer.setHeader(header);
        
        final Item primaryItem = new Item("Primary", MaterialDesignIcon.HOME.graphic());
        final Item secondaryItem = new Item("Secondary", MaterialDesignIcon.DASHBOARD.graphic());
        drawer.getItems().addAll(primaryItem, secondaryItem);
        
        drawer.selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            hideLayer(MENU_LAYER);
            switchView(newItem.equals(primaryItem) ? PRIMARY_VIEW : NavigationView.NAME);
        });
        
        addLayerFactory(MENU_LAYER, () -> new SidePopupView(drawer));*/
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
