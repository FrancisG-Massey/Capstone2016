package org.nestnz.app.views;

import org.nestnz.app.NestApplication;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.layout.layer.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SecondaryView extends View {

    public SecondaryView(String name) {
        super(name);
        
        getStylesheets().add(SecondaryView.class.getResource("secondary.css").toExternalForm());
        
        Label label = new Label("This is Secondary!");

        VBox controls = new VBox(label);
        controls.setAlignment(Pos.CENTER);
        
        setCenter(controls);
        
        setShowTransitionFactory(BounceInRightTransition::new);
        
        getLayers().add(new FloatingActionButton(MaterialDesignIcon.INFO.text, 
            e -> System.out.println("Info")));
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().showLayer(NestApplication.MENU_LAYER)));
        appBar.setTitleText("Secondary");
        appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e -> System.out.println("Favorite")));
    }
    
}
