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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.services.CompassService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.util.Duration;

public class FakeHeadingService implements CompassService {

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
