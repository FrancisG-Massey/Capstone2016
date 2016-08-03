package org.nestnz.app.desktop;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.HeadingService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.util.Duration;

public class FakeHeadingService implements HeadingService {

    private static final Logger LOG = Logger.getLogger(FakeHeadingService.class.getName());
    	
	private final ReadOnlyDoubleWrapper headingProperty = new ReadOnlyDoubleWrapper();
	
	public FakeHeadingService () {
		LOG.log(Level.INFO, "Started DesktopHeadingService");
		Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(10), evt -> {
			newHeading();
		}));
		fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
		fiveSecondsWonder.play();
		Platform.runLater(() -> {
			newHeading();
		});
	}
	
	private void newHeading () {
		double heading = Math.random()*360;
		LOG.log(Level.INFO, String.format("Generated new heading: %f", heading));
		headingProperty.set(heading);
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
		return true;
	}

	
}
