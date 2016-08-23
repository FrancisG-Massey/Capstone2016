package org.nestnz.app.ios;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.CompassService;
import org.robovm.apple.corelocation.CLHeading;
import org.robovm.apple.corelocation.CLLocationAccuracy;
import org.robovm.apple.corelocation.CLLocationManager;
import org.robovm.apple.corelocation.CLLocationManagerDelegateAdapter;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

public class IOSCompassService implements CompassService {

    private static final Logger LOG = Logger.getLogger(IOSCompassService.class.getName());

    private static final double DESIRED_ACCURACY = CLLocationAccuracy.Kilometer;
    private static final double DISTANCE_FILTER = 1000.0;
	
	private final ReadOnlyDoubleWrapper headingProperty = new ReadOnlyDoubleWrapper();
	
	private boolean supported = false;
    
    public IOSCompassService () {
        CLLocationManager locationManager = new CLLocationManager();
        
        locationManager.setDelegate(new CLLocationManagerDelegateAdapter() {

            @Override
            public void didUpdateHeading(CLLocationManager manager, CLHeading heading) {
                LOG.log(Level.INFO, String.format("iOS heading update: %f", heading.getMagneticHeading()));

                if (heading.getHeadingAccuracy() < 0) {
                    LOG.log(Level.INFO, String.format("iOS heading update, accuracy too small: %.2f < 0", heading.getHeadingAccuracy()));
                    return;
                }

                double locationAge = -heading.getTimestamp().getTimeIntervalSinceNow();
                if (locationAge > 5.0) {
                    LOG.log(Level.INFO, String.format("iOS location update, time interval to large (probably cached): %.2f > 5.0", locationAge));
                    return;
                }

                Platform.runLater(() -> {
                    headingProperty.set(heading.getMagneticHeading());
                });
            }
        });


        LOG.log(Level.INFO, String.format("Heading Service configured with desiredAccuracy %.2f and distanceFilter %.2f", DESIRED_ACCURACY, DISTANCE_FILTER));
        locationManager.setDesiredAccuracy(DESIRED_ACCURACY);
        locationManager.setDistanceFilter(DISTANCE_FILTER);
        
        if (CLLocationManager.isHeadingAvailable()) {
        	supported = true;
        	LOG.log(Level.INFO, "Telling HeadingService to start updating heading.");        
            locationManager.startUpdatingHeading();
        }       
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
	public boolean isHeadingAvailable() {
		return supported;
	}

}
