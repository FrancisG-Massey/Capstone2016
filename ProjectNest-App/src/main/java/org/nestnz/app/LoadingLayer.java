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
package org.nestnz.app;

import com.gluonhq.charm.glisten.application.GlassPane;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.ProgressIndicator;
import com.gluonhq.charm.glisten.layout.Layer;

/**
 * 
 */
public class LoadingLayer extends Layer {

	private final ProgressIndicator spinner = new ProgressIndicator();
	private final int radius = 30;
	
    public LoadingLayer() { 
    	setAutoHide(false);//Prevent users from removing the hiding screen by pressing anywhere
    	spinner.setRadius(radius);
    	getChildren().add(spinner);
    	MobileApplication.getInstance().getGlassPane().getLayers().add(this);
    	setBackgroundFade(GlassPane.DEFAULT_BACKGROUND_FADE_LEVEL);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
    }
    
    @Override 
    public void layoutChildren() {
    	spinner.setVisible(isShowing());
        if (!isShowing()) {
            return;
        }
        spinner.resizeRelocate(
        		(MobileApplication.getInstance().getGlassPane().getWidth() - radius*2)/2, 
        		(MobileApplication.getInstance().getGlassPane().getHeight()- radius*2)/2, 
        		radius*2, radius*2);
    }
}
