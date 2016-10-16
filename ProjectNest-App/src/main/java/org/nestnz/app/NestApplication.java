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

import org.nestnz.app.services.AudioServiceFactory;
import org.nestnz.app.services.CachingService;
import org.nestnz.app.services.LoginService;
import org.nestnz.app.services.MapLoadingService;
import org.nestnz.app.services.NetworkService;
import org.nestnz.app.services.TrapDataService;
import org.nestnz.app.services.impl.DefaultCachingService;
import org.nestnz.app.services.impl.GluonMapLoadingService;
import org.nestnz.app.services.impl.RestNetworkService;
import org.nestnz.app.views.AddTrapView;
import org.nestnz.app.views.LoginView;
import org.nestnz.app.views.NavigationView;
import org.nestnz.app.views.TraplineInfoView;
import org.nestnz.app.views.TraplineListView;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.license.License;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.StorageService;

import javafx.scene.Scene;

@License(key="482637c8-d766-40fa-942e-f96a11d31da8")
public class NestApplication extends MobileApplication {
	
    public static final String PRIMARY_VIEW = HOME_VIEW;
    public static final String MENU_LAYER = "Side Menu";
    
    private TrapDataService trapDataService;
    private MapLoadingService mapLoadingService;
    private File appStoragePath;

    @Override
    public void init() throws IOException {
    	appStoragePath = Services.get(StorageService.class).orElseThrow(() -> new RuntimeException("Local storage not supported on this device!"))
    		.getPrivateStorage().orElseThrow(() -> new RuntimeException("No local storage found on this device!"));
    	
    	setupServices();
        
        addViewFactory(LoginView.NAME, () -> new LoginView(LoginService.getInstance()));
        addViewFactory(TraplineListView.NAME, () -> new TraplineListView(trapDataService));
        addViewFactory(NavigationView.NAME, () -> new NavigationView());
        addViewFactory(TraplineInfoView.NAME, () -> new TraplineInfoView(trapDataService, mapLoadingService));
        addViewFactory(AddTrapView.NAME, () -> new AddTrapView());
        
    	addLayerFactory("loading", () -> new LoadingLayer());
    }
    
    private void setupServices () throws IOException {
    	Services.registerServiceFactory(new AudioServiceFactory());
    	LoginService loginService = LoginService.getInstance();
        CachingService cachingService = new DefaultCachingService(new File(appStoragePath, "cache"));
        NetworkService networkService = new RestNetworkService(loginService);
        trapDataService = new TrapDataService(cachingService, networkService);
        
        mapLoadingService = new GluonMapLoadingService(appStoragePath);
        
        trapDataService.initialise();
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
