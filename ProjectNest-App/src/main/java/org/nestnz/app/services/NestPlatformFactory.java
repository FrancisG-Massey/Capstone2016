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
