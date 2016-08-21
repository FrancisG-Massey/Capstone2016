package org.nestnz.app.desktop;

import org.nestnz.app.services.CompassService;
import org.nestnz.app.services.NestNativePlatform;

public class DesktopNativePlatform extends NestNativePlatform {
	
	private FakeHeadingService headingService;

	@Override
	public CompassService getCompassService() {
		if (headingService == null) {
			headingService = new FakeHeadingService();
		}
		return headingService;
	}

}
