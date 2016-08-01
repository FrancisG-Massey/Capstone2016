package org.nestnz.app.views;

import org.nestnz.app.NestApplication;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Icon;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PrimaryView extends View {

    public PrimaryView(String name) {
        super(name);
        
        getStylesheets().add(PrimaryView.class.getResource("primary.css").toExternalForm());

        Label label = new Label("Hello JavaFX World!");

        Button button = new Button("Change the World!");
        button.setGraphic(new Icon(MaterialDesignIcon.LANGUAGE));
        button.setOnAction(e -> label.setText("Hello JavaFX Universe!"));
        
        VBox controls = new VBox(15.0, label, button);
        controls.setAlignment(Pos.CENTER);
        
        setCenter(controls);
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> MobileApplication.getInstance().showLayer(NestApplication.MENU_LAYER)));
        appBar.setTitleText("Primary");
        appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e -> System.out.println("Search")));
    }
    
}
