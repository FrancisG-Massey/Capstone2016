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

import java.util.IdentityHashMap;
import java.util.Map;

import org.nestnz.app.model.Trap;

import com.gluonhq.maps.MapPoint;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class TrapPositionLayer extends PositionLayer implements ListChangeListener<Trap> {
	
	private final ObservableList<Trap> traps = FXCollections.observableArrayList(trap -> 
		new Observable[]{ trap.latitudeProperty(), trap.longitudeProperty() });
	
	/**
	 * A lookup map for the standard marker icons used for each trap on the map.
	 * An IdentityHashMap is used here, as it removes the possibility of changing trap coordinates or IDs (such as when the trap is created on the server) causing future lookups to fail.
	 */
	protected final Map<Trap, Node> trapIcons = new IdentityHashMap<>();
	
	private final ObjectProperty<Trap> activeTrapProperty = new SimpleObjectProperty<>();
	
    protected final Shape activeTrapIcon = new Polygon(12, 12, -12, 12, 0, -12);
	
	public TrapPositionLayer () {
		activeTrapIcon.setFill(Color.RED);
		
		this.traps.addListener(this);
		activeTrapProperty.addListener((obs, oldValue, newValue) -> {
			removePoint(activeTrapIcon);
			if (oldValue != null) {
				MapPoint pos = new MapPoint(oldValue.getLatitude(), oldValue.getLongitude());
				Node icon = trapIcons.get(oldValue);
				if (icon != null) {
					addPoint(pos, icon);
				}
			}
			if (newValue != null) {
				removePoint(trapIcons.get(newValue));
				MapPoint pos = new MapPoint(newValue.getLatitude(), newValue.getLongitude());
				addPoint(pos, activeTrapIcon);
			}
		});
	}
	
	/**
	 * Gets the list of traps displayed as icons in this map layer
	 * @return The underlying {@link ObserableList} used to identify traps on this layer
	 */
	public final ObservableList<Trap> getTraps () {
		return traps;
	}
	
	public final Trap getActiveTrap () {
		return activeTrapProperty.get();
	}
	
	public final void setActiveTrap (Trap trap) {
		activeTrapProperty.set(trap);
	}
	
	/**
	 * The trap which is currently emphasised on the map.
	 * This can be used to identify the target trap on the navigation view, the most recently added trap in the "add trap" view, or for whatever other purpose is desired.
	 * @return The active trap property used to set or change the active trap
	 */
	public final ObjectProperty<Trap> activeTrapProperty () {
		return activeTrapProperty;
	}

	/* (non-Javadoc)
	 * @see javafx.collections.ListChangeListener#onChanged(javafx.collections.ListChangeListener.Change)
	 */
	@Override
	public void onChanged(javafx.collections.ListChangeListener.Change<? extends Trap> c) {
		while (c.next()) {
			if (c.wasAdded()) {
				for (Trap t : c.getAddedSubList()) {
					MapPoint pos = new MapPoint(t.getLatitude(), t.getLongitude());
					Node icon = makeTrapIcon();
					trapIcons.put(t, icon);
					if (t != getActiveTrap()) {
						addPoint(pos, icon);
					}					
				}
			} else if (c.wasUpdated()) {
				for (Trap t : c.getList().subList(c.getFrom(), c.getTo())) {
					Node icon = trapIcons.get(t);
					removePoint(icon);
					MapPoint pos = new MapPoint(t.getLatitude(), t.getLongitude());
					addPoint(pos, icon);					
				}
			} else if (c.wasRemoved()) {
				for (Trap t : c.getRemoved()) {
					Node icon = trapIcons.remove(t);
					removePoint(icon);
				}
			}
		}
	}
	
	private Shape makeTrapIcon () {
		Shape icon = new Polygon(8, 8, -8, 8, 0, -8);
		icon.setFill(Color.BLUE);
		return icon;
	}
}
