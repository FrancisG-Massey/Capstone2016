package org.nestnz.app.android;

import org.nestnz.app.services.CompassService;
import org.nestnz.app.services.NestNativePlatform;

public class AndroidNativePlatform extends NestNativePlatform {
	
	private AndroidCompassService headingService;

	@Override
	public CompassService getCompassService() {
		if (headingService == null) {
			headingService = new AndroidCompassService();
		}
		return headingService;
	}

}
