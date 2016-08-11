package org.nestnz.app.ios;

import org.nestnz.app.services.CompassService;
import org.nestnz.app.services.NestNativePlatform;

public class IOSNativePlatform extends NestNativePlatform {
	
	private IOSCompassService headingService;

	@Override
	public CompassService getCompassService() {
		if (headingService == null) {
			headingService = new IOSCompassService();
		}
		return headingService;
	}

}
