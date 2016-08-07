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

public class TraplineInfoView extends View {

    private static final Logger LOG = Logger.getLogger(TraplineInfoView.class.getName());
	
	public static final String NAME = "trapline_info";
	
	private ObjectProperty<Trapline> traplineProperty = new SimpleObjectProperty<>();
	
	private Button start = new Button("Start");

	public TraplineInfoView() {
		super(NAME);
		start.setMaxHeight(1000.0);
		start.setMaxWidth(1000.0);
		this.setBottom(start);
	}
	
	public void setTrapline (Trapline trapline) {
		traplineProperty.set(trapline);
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setNavIcon(MaterialDesignIcon.MENU.button(evt -> LOG.log(Level.INFO, "Open menu pressed...")));
		appBar.setTitleText(traplineProperty.get().getName());
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> this.getApplication().switchToPreviousView()));
    }

}
