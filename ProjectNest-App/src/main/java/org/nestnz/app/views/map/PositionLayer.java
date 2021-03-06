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
package org.nestnz.app.views.map;

import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gluonhq.charm.down.plugins.Position;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import static javafx.beans.binding.Bindings.createDoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

public class PositionLayer extends MapLayer {

    private static final Logger LOG = Logger.getLogger(PositionLayer.class.getName());

    protected final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();
    
    protected final ObjectProperty<Position> currentPositionProperty = new SimpleObjectProperty<>();
	
	/**
	 * The icon indicating the user's current position on the map
	 */
    protected final Node curPosIcon = new Circle(10, Color.YELLOW);
    
    public PositionLayer() {
    	currentPositionProperty.addListener((obs, oldVal, newVal) -> {
    		MapPoint curPoint = new MapPoint(newVal.getLatitude(), newVal.getLongitude());
    		removePoint(curPosIcon);
			addPoint(curPoint, curPosIcon);
    	});
    }
    
    @Override
    protected void initialize() {
    	baseMap.prefCenterLat().bind(createDoubleBinding(() -> 
    		getCurrentPosition() == null ? 0.0 : getCurrentPosition().getLatitude(), currentPositionProperty));
    	baseMap.prefCenterLon().bind(createDoubleBinding(() -> 
    		getCurrentPosition() == null ? 0.0 : getCurrentPosition().getLongitude(), currentPositionProperty));
    }
    
    public final void setCurrentPosition (Position postition) {
    	currentPositionProperty.set(postition);
    }
    
    public final Position getCurrentPosition () {
    	return currentPositionProperty.get();
    }
    
    public final ObjectProperty<Position> currentPositionProperty () {
    	return currentPositionProperty;    	
    }

    public void addPoint(MapPoint p, Node icon) {
    	//Make sure the point & icon aren't null, which will cause issues futher down the track
    	Objects.requireNonNull(p);
    	Objects.requireNonNull(icon);
    	
    	LOG.log(Level.FINE, String.format("Point added at %f, %f (icon=%s)", p.getLatitude(), p.getLongitude(), icon));
        points.add(new Pair<MapPoint, Node>(p, icon));
        this.getChildren().add(icon);
        this.markDirty();
    }
    
    public void removePoint (Node icon) {
    	Iterator<Pair<MapPoint, Node>> iterator = points.iterator(); 
    	while (iterator.hasNext()) {
    		Pair<MapPoint, Node> p = iterator.next();
    		if (p.getValue() == icon) {
    			iterator.remove();
    		}
    	}
    	this.getChildren().removeAll(icon);
    	this.markDirty();
    }
    
    

    @Override
    protected void layoutLayer() {
        for (Pair<MapPoint, Node> candidate : points) {
            MapPoint point = candidate.getKey();
            Node icon = candidate.getValue();
            Point2D mapPoint = baseMap.getMapPoint(point.getLatitude(), point.getLongitude());
            icon.setVisible(true);
            icon.setTranslateX(mapPoint.getX());
            icon.setTranslateY(mapPoint.getY());
        }
    }
}
