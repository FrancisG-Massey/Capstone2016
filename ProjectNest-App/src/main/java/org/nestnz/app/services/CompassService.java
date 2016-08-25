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

import javafx.beans.property.ReadOnlyDoubleProperty;

/**
 * The CompassService provides details of the direction a device is currently pointing, relative to the earth's magnetic poles.
 * 
 * 
 */
public interface CompassService {
	
	/**
	 * Checks whether the devices has the sensors required to calculate heading
	 * @return True if heading is available
	 */
	boolean isHeadingAvailable();
	
	/**
	 * 
	 * @return
	 */
	ReadOnlyDoubleProperty headingProperty();
	
	/**
	 * The current heading of the device, as a number of degrees between 0 and 360 relative to magnetic North.
	 * If the heading was unable to be determined, returns a negative value
	 * 
	 * @return the current heading of the device
	 */
	double getHeading();
}
