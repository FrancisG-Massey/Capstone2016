package org.nestnz.app.android;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.HeadingService;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafxports.android.FXActivity;

public class AndroidHeadingService implements HeadingService, SensorEventListener {

    private static final Logger LOG = Logger.getLogger(AndroidHeadingService.class.getName());
	
	private final ReadOnlyDoubleWrapper headingProperty = new ReadOnlyDoubleWrapper();
	
	private final Sensor magnetometer;
	private final Sensor accelerometer;
	
    private final SensorManager sensorManager;
    
    private boolean supported = false;
    
    public AndroidHeadingService () {
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
			gravity = evt.values;
		} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magnetic = evt.values;
		}
		
		if (gravity != null && magnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, magnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
				LOG.info(String.format("Found azimut %f", azimut));
				Platform.runLater(() -> {
					headingProperty.set(azimut);
				});
			}
		}
	}

	@Override
	public boolean isHeadingAvailable() {
		return supported;
	}

}
