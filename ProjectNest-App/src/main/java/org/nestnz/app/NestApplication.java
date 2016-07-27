package org.nestnz.app;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import javafx.scene.Scene;

import javafx.stage.Stage;
import com.gluonhq.charm.glisten.license.License;

@License(key="482637c8-d766-40fa-942e-f96a11d31da8")
public class NestApplication extends MobileApplication {

    public static final String BASIC_VIEW = HOME_VIEW;

    @Override
    public void init() {
        addViewFactory(BASIC_VIEW, () -> new BasicView(BASIC_VIEW));
    }

    @Override
    public void postInit(Scene scene) {
        Swatch.BLUE.assignTo(scene);


    }
}
