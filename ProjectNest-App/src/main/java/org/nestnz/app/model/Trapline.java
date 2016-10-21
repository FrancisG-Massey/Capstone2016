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
package org.nestnz.app.model;

import java.time.LocalDateTime;
import java.util.Optional;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Trapline {
	
	/**
	 * The internal server ID of the trapline
	 */
	private final ReadOnlyIntegerWrapper idProperty = new ReadOnlyIntegerWrapper();
	
	/**
	 * The visible name of the trapline
	 */
	private final StringProperty nameProperty = new SimpleStringProperty();
	
	/**
	 * The traps located within the trapline
	 */
	private final ObservableList<Trap> traps = FXCollections.observableArrayList(trap -> {
		return new Observable[] { trap.numberProperty(), trap.latitudeProperty(), trap.longitudeProperty(),
				trap.lastResetProperty(), trap.getCatches(), trap.statusProperty(), trap.idProperty() };
	});
	
	/**
	 * The region in which this trapline is located (e.g. Auckland, Waikato, Manawatu, etc)
	 */
	private final ObjectProperty<Region> regionProperty = new SimpleObjectProperty<>();
	
	/**
	 * The name of the starting point of the trapline
	 */
	private final StringProperty startProperty = new SimpleStringProperty();
	
	/**
	 * Optionally, the name of the end point of the trapline. For loop traplines, this will be {@link Optional#empty()}
	 */
	private final StringProperty endProperty = new SimpleStringProperty();
	
	private final ObservableList<CatchType> catchTypes = FXCollections.observableArrayList();
	
	/**
	 * Indicates the date & time this trapline was last updated from the server
	 * This will be Optional.empty() if the trap data has not yet been fetched from the server
	 */
	private final ObjectProperty<Optional<LocalDateTime>> lastUpdatedProperty = new SimpleObjectProperty<>(Optional.empty());
	
	public Trapline(int id) {
		this.idProperty.set(id);
	}
	
	public Trapline(int id, String name, Region region, String start) {
		this(id, name, region, start, null);
	}

	public Trapline(int id, String name, Region region, String start, String end) {
		this.idProperty.set(id);
		this.nameProperty.set(name);
		this.regionProperty.set(region);
		this.startProperty.set(start);
		this.endProperty.set(end);
	}

	public int getId() {
		return idProperty.get();
	}
	
	public ReadOnlyIntegerProperty idProperty () {
		return idProperty.getReadOnlyProperty();
	}

	public String getName() {
		return nameProperty.get();
	}
	
	public void setName (String name) {
		nameProperty.set(name);
	}
	
	public StringProperty nameProperty () {
		return nameProperty;
	}

	public ObservableList<Trap> getTraps() {
		return traps;
	}
	
	public Trap getTrap (int id) {
		for (Trap t : traps) {
			if (t.getId() == id) {
				return t;
			}
		}
		return null;
	}

	public Region getRegion() {
		return regionProperty.get();
	}
	
	public void setRegion (Region region) {
		regionProperty.set(region);
	}
	
	public ObjectProperty<Region> regionProperty () {
		return regionProperty;
	}

	public String getStart() {
		return startProperty.get();
	}
	
	public void setStart (String start) {
		startProperty.set(start);
	}
	
	public StringProperty startProperty () {
		return startProperty;
	}

	public String getEnd() {
		return endProperty.get();
	}
	
	public void setEnd (String end) {
		endProperty.set(end);
	}
	
	public StringProperty endProperty () {
		return endProperty;
	}
	
	public Optional<LocalDateTime> getLastUpdated () {
		return lastUpdatedProperty.get();
	}
	
	public void setLastUpdated (Optional<LocalDateTime> lastUpdated) {
		this.lastUpdatedProperty.set(lastUpdated);
	}
	
	public void setLastUpdated (LocalDateTime lastUpdated) {
		this.lastUpdatedProperty.set(Optional.ofNullable(lastUpdated));
	}
	
	public ObjectProperty<Optional<LocalDateTime>> lastUpdatedProperty () {
		return lastUpdatedProperty;
	}
	
	public ObservableList<CatchType> getCatchTypes () {
		return catchTypes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endProperty.get() == null) ? 0 : endProperty.get().hashCode());
		result = prime * result + idProperty.get();
		result = prime * result + ((nameProperty.get() == null) ? 0 : nameProperty.get().hashCode());
		result = prime * result + ((regionProperty.get() == null) ? 0 : regionProperty.get().hashCode());
		result = prime * result + ((startProperty.get() == null) ? 0 : startProperty.get().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trapline other = (Trapline) obj;
		if (endProperty.get() == null) {
			if (other.endProperty.get() != null)
				return false;
		} else if (!endProperty.get().equals(other.endProperty.get()))
			return false;
		if (idProperty.get() != other.idProperty.get())
			return false;
		if (nameProperty.get() == null) {
			if (other.nameProperty.get() != null)
				return false;
		} else if (!nameProperty.get().equals(other.nameProperty.get()))
			return false;
		if (regionProperty.get() == null) {
			if (other.regionProperty.get() != null)
				return false;
		} else if (!regionProperty.get().equals(other.regionProperty.get()))
			return false;
		if (startProperty.get() == null) {
			if (other.startProperty.get() != null)
				return false;
		} else if (!startProperty.get().equals(other.startProperty.get()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trapline [id=" + getId() + ", name=" + getName() + ", region=" + getRegion()
				+ ", start=" + getStart() + ", end=" + getEnd() + ", lastUpdated=" + getLastUpdated() + "]";
	}
}
