package org.nestnz.app.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.gluonhq.charm.down.common.JavaFXPlatform;

public final class NestPlatformFactory {

    private static final Logger LOG = Logger.getLogger(NestPlatformFactory.class.getName());

	public static NestNativePlatform getPlatform() {
        try {
            return (NestNativePlatform) Class.forName(getPlatformClassName()).newInstance();
        } catch (Throwable ex) {
        	LOG.log(Level.SEVERE, null, ex);
            return null;
        }

    }

    private static String getPlatformClassName() {
        switch (JavaFXPlatform.getCurrent()) {
            case ANDROID: return "org.nestnz.app.android.AndroidNativePlatform";
            case IOS: return "org.nestnz.app.ios.IOSNativePlatform";
            default : return "org.nestnz.app.desktop.DesktopNativePlatform";
        }
    }
}
