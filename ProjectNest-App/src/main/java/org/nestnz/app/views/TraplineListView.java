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

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.util.StringConverter;

public class TraplineListView extends View {
	
	/**
	 * Represents the number of hours between automatically fetching the trapline list from the server.
	 * This only occurs if the view is closed & re-opened after at least the frequency has passed since the last refresh.
	 */
	public static final int REFRESH_FREQUENCY = 1;
	
	public static final String NAME = "trapline_list";

    private static final Logger LOG = Logger.getLogger(TraplineListView.class.getName());
	
	private final CharmListView<Trapline, Region> traplineList;
	
	private final SidePopupView menu;
	
	private final TrapDataService dataService;
	
	/**
	 * The date & time of the last trapline list update from the server. 
	 * If this is null, or more than {@link #REFRESH_FREQUENCY} hours from the current time, the trapline list will be automatically requested from the API again if/when the view is re-opened. 
	 */
	private LocalDateTime lastTraplineFetch = null;

    public TraplineListView(TrapDataService dataService) {
        super(NAME);
        this.dataService = dataService;
        
        ReadOnlyDoubleProperty deviceWidth = this.widthProperty();
        
        //Create an alphabetically sorted view of the trapline list, so traplines appear in alphabetical order
        SortedList<Trapline> sortedList = new SortedList<>(dataService.getTraplines(), (t1, t2) -> {
        	if (t1 == null && t2 == null) {
                return 0;
            } else if (t1 == null) {
                return -1;
            } else if (t2 == null) {
                return 1;
            } else {
            	return t1.getName().compareToIgnoreCase(t2.getName());
            }
        });
        
        traplineList = new CharmListView<>(sortedList);
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
        		
        		//Prevent long trapline names from expanding the size of buttons to cause side scrolling
        		//NOTE: This assumes a total left & right padding of less than 16 for the parent cell. Higher padding values will cause the scroll bar to re-appear
        		double padding = 16;
        		button.maxWidthProperty().bind(Bindings.subtract(deviceWidth, padding));
        		
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
    		if (dataService.isNetworkAvailable() &&
    				(lastTraplineFetch == null || lastTraplineFetch.plusHours(REFRESH_FREQUENCY).isBefore(LocalDateTime.now()))) {
    			LOG.log(Level.INFO, "Refreshing trapline list. Last refresh: "+lastTraplineFetch);
    			refreshTraplines();//Load the traplines if (a) they haven't been loaded yet or (b) at least an hour has passed since their last load  			
    		}
        });
        
        this.setOnHidden(evt -> {

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
        //appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> this.menu.show()));
        appBar.setTitleText("Nest NZ");
        appBar.getActionItems().add(MaterialDesignIcon.REFRESH.button(e -> refreshTraplines()));
    }
    
    private void refreshTraplines () {
    	NestApplication app = (NestApplication) this.getApplication();
    	
    	dataService.refreshTraplines().addListener((obs, oldStatus, newStatus) -> {
    		String message = null;
    		switch (newStatus) {
			case PENDING:
				return;
			case FAILED_NETWORK:
				message = "Unable to reach the NestNZ server. Please make sure you have internet access before trying again.";
				break;
			case FAILED_OTHER:
			case FAILED_UNAUTHORISED://The user should never receive an "unauthorised" response
				message = "There was a problem loading the traplines from the server. Please try again later.";
				break;
			case SUCCESS:
				lastTraplineFetch = LocalDateTime.now();
				break;    		
    		}
    		app.hideLayer("loading");
    		if (message != null) {
    			app.showNotification(message);
    		}
    	});
    	app.showLayer("loading");
    }
    
}
