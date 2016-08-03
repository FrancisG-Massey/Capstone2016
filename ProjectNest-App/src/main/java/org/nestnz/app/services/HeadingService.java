package org.nestnz.app.services;

import javafx.beans.property.ReadOnlyDoubleProperty;

/**
 * The HeadingService provides details of the direction a device is currently pointing, relative to the earth's magnetic poles.
 * 
 * 
 */
public interface HeadingService {
	
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
