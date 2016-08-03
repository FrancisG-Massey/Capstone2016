package org.nestnz.app.android;

import org.nestnz.app.services.HeadingService;
import org.nestnz.app.services.NestNativePlatform;

public class AndroidNativePlatform extends NestNativePlatform {
	
	private AndroidHeadingService headingService;

	@Override
	public HeadingService getHeadingService() {
		if (headingService == null) {
			headingService = new AndroidHeadingService();
		}
		return headingService;
	}

}
