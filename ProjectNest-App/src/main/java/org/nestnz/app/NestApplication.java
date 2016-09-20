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
package org.nestnz.app;

import java.io.File;
import java.io.IOException;

import org.nestnz.app.services.LoginService;
import org.nestnz.app.services.TrapDataService;
import org.nestnz.app.views.AddTrapView;
import org.nestnz.app.views.LoginView;
import org.nestnz.app.views.NavigationView;
import org.nestnz.app.views.TraplineInfoView;
import org.nestnz.app.views.TraplineListView;

import com.gluonhq.charm.down.common.PlatformFactory;
import com.gluonhq.charm.glisten.application.GlassPane;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.ProgressIndicator;
import com.gluonhq.charm.glisten.layout.Layer;
import com.gluonhq.charm.glisten.license.License;
import com.gluonhq.charm.glisten.visual.Swatch;

import javafx.scene.Scene;

@License(key="482637c8-d766-40fa-942e-f96a11d31da8")
public class NestApplication extends MobileApplication {
	
    public static final String PRIMARY_VIEW = HOME_VIEW;
    public static final String MENU_LAYER = "Side Menu";
    
    private TrapDataService trapDataService;
    private File appStoragePath;

    @Override
    public void init() throws IOException {
        appStoragePath = PlatformFactory.getPlatform().getPrivateStorage();
        trapDataService = new TrapDataService(new File(appStoragePath, "cache"));
        
        addViewFactory(LoginView.NAME, () -> new LoginView(LoginService.getInstance()));
        addViewFactory(TraplineListView.NAME, () -> new TraplineListView(trapDataService));
        addViewFactory(NavigationView.NAME, () -> new NavigationView());
        addViewFactory(TraplineInfoView.NAME, () -> new TraplineInfoView());
        addViewFactory(AddTrapView.NAME, () -> new AddTrapView());
        
    	addLayerFactory("loading", () -> new Layer() {
    		private final ProgressIndicator spinner = new ProgressIndicator();
    		private final int radius = 30;
    		
		    { 
		    	spinner.setRadius(radius);
		    	getChildren().add(spinner);
		    	getGlassPane().getLayers().add(this);
		    }

            @Override
            public void show() {
                getGlassPane().setBackgroundFade(GlassPane.DEFAULT_BACKGROUND_FADE_LEVEL);
                super.show();
            }

            @Override
            public void hide() {
                getGlassPane().setBackgroundFade(0.0);
                super.hide();
            }
		    
		    @Override 
		    public void layoutChildren() {
		    	spinner.setVisible(isShowing());
                if (!isShowing()) {
                    return;
                }
		        spinner.resizeRelocate(
		        		(getGlassPane().getWidth() - (radius*2))/2, 
		        		(getGlassPane().getHeight()- (radius*2))/2, 
		        		radius*2, radius*2);
		    }
		});
    }
	
	public TrapDataService getTrapDataService() {
		return trapDataService;
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
