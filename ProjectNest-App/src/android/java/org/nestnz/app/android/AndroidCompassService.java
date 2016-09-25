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
package org.nestnz.app.android;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.CompassService;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafxports.android.FXActivity;

public class AndroidCompassService implements CompassService, SensorEventListener {

    private static final Logger LOG = Logger.getLogger(AndroidCompassService.class.getName());
    
    private final ReadOnlyDoubleWrapper headingProperty = new ReadOnlyDoubleWrapper();
	
	private final Sensor magnetometer;
	private final Sensor accelerometer;
	
    private final SensorManager sensorManager;
    
    private boolean supported = false;
    
    public AndroidCompassService () {
        Context activityContext = FXActivity.getInstance();
        Object systemService = activityContext.getSystemService(FXActivity.SENSOR_SERVICE);
        sensorManager = (SensorManager) systemService;

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
        	LOG.log(Level.WARNING, "This device does not have an accelerometer sensor");
        	magnetometer = null;
        	return;
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometer == null) {
        	LOG.log(Level.WARNING, "This device does not have a magnetic field sensor");
        	return;
        }
        supported = true;
        
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

	@Override
	public ReadOnlyDoubleProperty headingProperty() {
		return headingProperty.getReadOnlyProperty();
	}

	@Override
	public double getHeading() {
		return headingProperty.get();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) { }

	private float[] gravity;
	private float[] magnetic;
	
	@Override
	public void onSensorChanged(SensorEvent evt) {
		//Reference: http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html
		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			gravity = lowPass(evt.values, gravity);
			
        	LOG.log(Level.INFO, String.format("Found accelerometer data: x=%1$f, y=%2$f, z=%3$f", gravity));			
		} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magnetic = lowPass(evt.values, magnetic);
			
        	LOG.log(Level.INFO, String.format("Found magnetic field data: x=%1$f, y=%2$f, z=%3$f", magnetic));	
		}
		
		if (gravity != null && magnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, magnetic);
			if (success) {
				float orientation[] = new float[3];//Orientation contains: azimuth, pitch and roll
				SensorManager.getOrientation(R, orientation);
				float azimuth = (float) ((orientation[0]*180)/Math.PI)+180;//Convert to degrees
				LOG.info(String.format("Found azimuth %f", azimuth));
				Platform.runLater(() -> {
					headingProperty.set(azimuth);
				});
			}
		}
	}

	@Override
	public boolean isHeadingAvailable() {
		return supported;
	}

	private static final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.
	
	/**
	 * Applies a low-pass filter to raw sensor values, to avoid random noise fluctuations.
	 * 
	 * Source: https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
	 * 
	 * @param input The new sensor values
	 * @param output The old sensor values
	 * @return The new sensor values with the filter applied to them
	 */
	private static float[] lowPass(float[] input, float[] output) { 
		if ( output == null ) {
			return input; 
		}
		for (int i=0; i<input.length; i++) {
			output[i] = output[i] + ALPHA * (input[i] - output[i]); 
		} 
		return output;
	} 
}
