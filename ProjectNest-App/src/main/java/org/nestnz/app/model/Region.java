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

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class Region implements Comparable<Region> {

	private final ReadOnlyIntegerWrapper idProperty = new ReadOnlyIntegerWrapper();
	
	private final StringProperty nameProperty = new SimpleStringProperty();
	
	public Region (int id) {
		this.idProperty.set(id);
	}
	
	public Region (int id, String name) {
		idProperty.set(id);
		nameProperty.set(name);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * idProperty.get();
		result = prime * result + ((nameProperty.get() == null) ? 0 : nameProperty.get().hashCode());
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
		Region other = (Region) obj;
		if (idProperty.get() == other.idProperty.get())
			return false;
			
		if (nameProperty.get() == null) {
			if (other.nameProperty.get() != null)
				return false;
		} else if (!nameProperty.get().equals(other.nameProperty.get()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Region [id=" + getId() + ", name=" + getName() + "]";
	}	

	@Override
	public int compareTo(Region r) {
		if (nameProperty.get() == null) {
			if (r.nameProperty.get() == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (r.nameProperty.get() == null) {
			return 1;
		}		
		return nameProperty.get().compareTo(r.nameProperty.get());
	}
}
