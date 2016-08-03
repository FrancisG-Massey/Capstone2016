package org.nestnz.app.desktop;

import org.nestnz.app.services.HeadingService;
import org.nestnz.app.services.NestNativePlatform;

public class DesktopNativePlatform extends NestNativePlatform {
	
	private FakeHeadingService headingService;

	@Override
	public HeadingService getHeadingService() {
		if (headingService == null) {
			headingService = new FakeHeadingService();
		}
		return headingService;
	}

}
