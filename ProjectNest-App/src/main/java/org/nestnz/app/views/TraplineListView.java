/*******************************************************************************
 * Copyright (C) 2016, Nest NZ
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.nestnz.app.views;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.NestApplication;
import org.nestnz.app.model.Region;
import org.nestnz.app.model.Trapline;
import org.nestnz.app.services.TrapDataService;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.layout.layer.MenuSidePopupView;
import com.gluonhq.charm.glisten.layout.layer.SidePopupView;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.util.StringConverter;

public class TraplineListView extends View implements ChangeListener<Boolean> {
	
	/**
	 * Represents the number of hours between automatically fetching the trapline list from the server.
	 * This only occurs if the view is closed & re-opened after at least the frequency has passed since the last refresh.
	 * This 
	 */
	public static final int REFRESH_FREQUENCY = 1;
	
	public static final String NAME = "trapline_list";

    private static final Logger LOG = Logger.getLogger(TraplineListView.class.getName());
	
	private final CharmListView<Trapline, Region> traplineList;
	
	private final SidePopupView menu;
	
	private final TrapDataService dataService;
	
	private LocalDateTime lastTraplineFetch = null;

    public TraplineListView(TrapDataService dataService) {
        super(NAME);
        this.dataService = dataService;
        
        traplineList = new CharmListView<>(dataService.getTraplines());
        traplineList.setHeadersFunction(Trapline::getRegion);
        traplineList.setConverter(new StringConverter <Region>() {
            @Override public String toString(Region r) {
                return r.getName();
            }
			@Override public Region fromString(String string) {
				throw new UnsupportedOperationException("Not supported!");
			}
        });
        traplineList.setId("trapline-list");
        traplineList.setCellFactory(list -> new CharmListCell<Trapline>() {
        	Button button = new Button();
        	Trapline trapline;
        	
        	{
        		this.setGraphic(button);
        		button.setOnAction(evt -> {
        			LOG.log(Level.INFO, "Pressed trapline: "+trapline);
        			TraplineInfoView infoView = ((NestApplication) TraplineListView.this.getApplication()).lookupView(TraplineInfoView.NAME);
        			infoView.setTrapline(trapline);
        			TraplineListView.this.getApplication().switchView(TraplineInfoView.NAME);
        		});
        	}
        	
        	@Override public void updateItem (Trapline item, boolean empty) {
        		super.updateItem(item, empty);
        		trapline = item;
        		if(item!=null && !empty){
        			button.setText(item.getName());
                } else {
                	button.setText(null);
                }
        	}
        });
				
        this.setOnShown(evt -> {
    		dataService.loadingProperty().addListener(this);
    		if (lastTraplineFetch == null || lastTraplineFetch.plusHours(REFRESH_FREQUENCY).isBefore(LocalDateTime.now())) {
    			LOG.log(Level.INFO, "Refreshing trapline list. Last refresh: "+lastTraplineFetch);
    			dataService.refreshTraplines();//Load the traplines if (a) they haven't been loaded yet or (b) at least an hour has passed since their last load  			
    		}
        });
        
        this.setOnHidden(evt -> {
        	dataService.loadingProperty().removeListener(this);
        });
        
        setCenter(traplineList);
		menu = buildMenu();
        getStylesheets().add(TraplineListView.class.getResource("styles.css").toExternalForm());
        
    }
	
	private SidePopupView buildMenu () {
		Menu menu = new Menu();
		final MenuItem logout = new MenuItem("Logout", MaterialDesignIcon.REMOVE.graphic());
		
		logout.setOnAction(evt -> {
			this.menu.hide();
			
		});
		
		menu.getItems().add(logout);
		return new MenuSidePopupView(menu, Side.LEFT);
	}

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> this.menu.show()));
        appBar.setTitleText("Nest NZ");
        appBar.getActionItems().add(MaterialDesignIcon.REFRESH.button(e -> dataService.refreshTraplines()));
    }

	/* (non-Javadoc)
	 * @see javafx.beans.value.ChangeListener#changed(javafx.beans.value.ObservableValue, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		if (newValue) {
			this.getApplication().showLayer("loading");
		} else {
			this.getApplication().hideLayer("loading");	
			lastTraplineFetch = LocalDateTime.now();
		}
	}
    
}
