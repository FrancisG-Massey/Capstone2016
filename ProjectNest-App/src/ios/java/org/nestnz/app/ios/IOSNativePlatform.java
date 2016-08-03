package org.nestnz.app.ios;

import org.nestnz.app.services.HeadingService;
import org.nestnz.app.services.NestNativePlatform;

public class IOSNativePlatform extends NestNativePlatform {
	
	private IOSHeadingService headingService;

	@Override
	public HeadingService getHeadingService() {
		if (headingService == null) {
			headingService = new IOSHeadingService();
		}
		return headingService;
	}

}
