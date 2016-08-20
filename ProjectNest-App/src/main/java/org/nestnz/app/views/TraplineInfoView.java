package org.nestnz.app.views;

import org.nestnz.app.NestApplication;
import org.nestnz.app.model.Trapline;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.layout.layer.MenuSidePopupView;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class TraplineInfoView extends View {
	
	public static final String NAME = "trapline_info";
	
	private final ObjectProperty<Trapline> traplineProperty = new SimpleObjectProperty<>();
	
	private Button start = new Button("Start");
	
	private final SidePopupView menu;

	public TraplineInfoView() {
		super(NAME);
		start.setMaxHeight(1000.0);
		start.setMaxWidth(1000.0);
		start.setOnAction(evt -> {
			NavigationView navView = ((NestApplication) getApplication()).lookupView(NavigationView.NAME);
			navView.setTrapline(traplineProperty.get());
			getApplication().switchView(NavigationView.NAME);
		});
		this.setBottom(start);
		menu = buildMenu();
	}
	
	public void setTrapline (Trapline trapline) {
		traplineProperty.set(trapline);
		start.setVisible(!trapline.getTraps().isEmpty());
	}
	
	private SidePopupView buildMenu () {
		Menu menu = new Menu();
		final MenuItem addTraps = new MenuItem("Add Traps", MaterialDesignIcon.ADD.graphic());
		
		addTraps.setOnAction(evt -> {
			this.menu.hide();
			AddTrapView addTrapView = ((NestApplication) getApplication()).lookupView(AddTrapView.NAME);
			addTrapView.setTrapline(traplineProperty.get());
			getApplication().switchView(AddTrapView.NAME);
		});
		
		menu.getItems().add(addTraps);
		return new MenuSidePopupView(menu, Side.LEFT);
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
		appBar.setNavIcon(MaterialDesignIcon.MENU.button(evt -> menu.show()));
		appBar.setTitleText(traplineProperty.get().getName());
        appBar.getActionItems().add(MaterialDesignIcon.ARROW_BACK.button(evt -> this.getApplication().switchToPreviousView()));
    }

}