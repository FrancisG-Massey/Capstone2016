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
