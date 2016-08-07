package org.nestnz.app;

import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.views.TraplineListView;
import org.nestnz.app.views.NavigationView;
import org.nestnz.app.views.TraplineInfoView;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.NavigationDrawer.Item;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.visual.Swatch;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import com.gluonhq.charm.glisten.license.License;

@License(key="482637c8-d766-40fa-942e-f96a11d31da8")
public class NestApplication extends MobileApplication {

    public static final String PRIMARY_VIEW = HOME_VIEW;
    public static final String SECONDARY_VIEW = "Secondary View";
    public static final String MENU_LAYER = "Side Menu";
    
    private final ObservableList<Trapline> traplines = FXCollections.observableArrayList();

    @Override
    public void init() {
    	traplines.add(new Trapline(20, "Test trapline", new Region(20, "Test Region"), "Test Start"));
    	traplines.add(new Trapline(21, "Test trapline 2", new Region(20, "Test Region"), "Test Start 2"));
    	
        addViewFactory(TraplineListView.NAME, () -> new TraplineListView(traplines));
        addViewFactory(SECONDARY_VIEW, () -> new NavigationView(SECONDARY_VIEW));
        addViewFactory(TraplineInfoView.NAME, () -> new TraplineInfoView());
        
        NavigationDrawer drawer = new NavigationDrawer();
        
        NavigationDrawer.Header header = new NavigationDrawer.Header("Gluon Mobile",
                "Multi View Project",
                new Avatar(21, new Image(NestApplication.class.getResourceAsStream("/icon.png"))));
        drawer.setHeader(header);
        
        final Item primaryItem = new Item("Primary", MaterialDesignIcon.HOME.graphic());
        final Item secondaryItem = new Item("Secondary", MaterialDesignIcon.DASHBOARD.graphic());
        drawer.getItems().addAll(primaryItem, secondaryItem);
        
        drawer.selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            hideLayer(MENU_LAYER);
            switchView(newItem.equals(primaryItem) ? PRIMARY_VIEW : SECONDARY_VIEW);
        });
        
        addLayerFactory(MENU_LAYER, () -> new SidePopupView(drawer));
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.BLUE.assignTo(scene);

        scene.getStylesheets().add(NestApplication.class.getResource("style.css").toExternalForm());
    }
}
