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

import java.util.Objects;
import java.util.Optional;

import org.nestnz.app.parser.ParserTrap;
import org.nestnz.app.parser.ParserTrapline;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
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
	private final ObservableList<Trap> traps = FXCollections.observableArrayList();
	
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
	
	/**
	 * Signals some of the data has changed since it was last synchronized with the server
	 */
	private final BooleanProperty dirtyProperty = new SimpleBooleanProperty(false);
	
	public Trapline (ParserTrapline pTrapline) {
		Objects.requireNonNull(pTrapline);
		
		this.idProperty.set(pTrapline.getId());
		this.nameProperty.set(pTrapline.getName());
		//this.region = null;//TODO: Get the region data here
		this.startProperty.set(pTrapline.getStart());
		this.endProperty.set(pTrapline.getEnd());
		for (ParserTrap pTrap : pTrapline.getTraps()) {
			traps.add(new Trap(pTrap));
		}
	}
	
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

	public Region getRegion() {
		return regionProperty.get();
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
	
	public boolean isDirty () {
		return dirtyProperty.get();
	}
	
	public void setDirty (boolean dirty) {
		dirtyProperty.set(dirty);
	}
	
	public BooleanProperty dirtyProperty () {
		return dirtyProperty;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endProperty == null) ? 0 : endProperty.hashCode());
		result = prime * result + ((idProperty == null) ? 0 : idProperty.hashCode());
		result = prime * result + ((nameProperty == null) ? 0 : nameProperty.hashCode());
		result = prime * result + ((regionProperty == null) ? 0 : regionProperty.hashCode());
		result = prime * result + ((startProperty == null) ? 0 : startProperty.hashCode());
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
		if (endProperty == null) {
			if (other.endProperty != null)
				return false;
		} else if (!endProperty.equals(other.endProperty))
			return false;
		if (idProperty == null) {
			if (other.idProperty != null)
				return false;
		} else if (!idProperty.equals(other.idProperty))
			return false;
		if (nameProperty == null) {
			if (other.nameProperty != null)
				return false;
		} else if (!nameProperty.equals(other.nameProperty))
			return false;
		if (regionProperty == null) {
			if (other.regionProperty != null)
				return false;
		} else if (!regionProperty.equals(other.regionProperty))
			return false;
		if (startProperty == null) {
			if (other.startProperty != null)
				return false;
		} else if (!startProperty.equals(other.startProperty))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trapline [id=" + getId() + ", name=" + getName() + ", region=" + getRegion()
				+ ", start=" + getStart() + ", end=" + getEnd() + ", dirty=" + isDirty() + "]";
	}
}
