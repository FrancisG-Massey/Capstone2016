package org.nestnz.app.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Trapline;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

public class AddTrapView extends View {

    private static final Logger LOG = Logger.getLogger(AddTrapView.class.getName());
    
	public static final String NAME = "add_trap";
	
	private final ObjectProperty<Trapline> traplineProperty = new SimpleObjectProperty<>();
	
	protected final Button addTrapButton = new Button("Add Trap");
	
	public AddTrapView() {
		super(NAME);
		addTrapButton.setMaxHeight(1000.0);
		addTrapButton.setMaxWidth(1000.0);
		
		setBottom(addTrapButton);
	}
	
	public void setTrapline (Trapline trapline) {
		traplineProperty.set(trapline);
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setNavIcon(MaterialDesignIcon.MENU.button(evt -> LOG.log(Level.INFO, "Open menu pressed...")));
		appBar.setTitleText("Add Trap");
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> this.getApplication().switchToPreviousView()));
    }	
}
