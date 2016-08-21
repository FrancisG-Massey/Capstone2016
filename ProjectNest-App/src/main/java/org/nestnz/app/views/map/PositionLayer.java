package org.nestnz.app.views.map;

import java.util.Iterator;

import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Pair;

public class PositionLayer extends MapLayer {

    private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();
    
    public PositionLayer() {
    	
    }

    public void addPoint(MapPoint p, Node icon) {
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
