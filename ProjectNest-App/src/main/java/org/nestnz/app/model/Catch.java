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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class Catch {

	/**
	 * The internal server ID of the catch, if the catch has been posted to the server.
	 * If the catch has not yet been posted to the server, this will be Optional.empty()
	 */
	private ObjectProperty<Optional<Integer>> idProperty = new SimpleObjectProperty<>(Optional.empty());
	
	/**
	 * The date & time this catch was recorded
	 */
	private LocalDateTime timestamp;
	
	/**
	 * The type of pest caught in the trap
	 */
	private final ObjectProperty<CatchType> catchTypeProperty = new SimpleObjectProperty<>();
	
	/**
	 * An optional note for the catch log (generally used if {@link CatchType#OTHER} is selected) 
	 */
	private String note;
	

	public Catch(CatchType catchType) {
		this(catchType, LocalDateTime.now(), null);
	}
	

	public Catch(CatchType catchType, LocalDateTime timestamp, String note) {
		this.catchTypeProperty.set(catchType);
		this.timestamp = timestamp;
		this.note = note;
	}

	public Optional<Integer> getId() {
		return idProperty.get();
	}

	public void setId(Optional<Integer> id) {
		idProperty.set(id);
	}

	public void setId(int id) {
		idProperty.set(Optional.of(id));
	}
	
	public ObjectProperty<Optional<Integer>> idProperty () {
		return idProperty;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public CatchType getCatchType() {
		return catchTypeProperty.get();
	}

	public void setCatchType(CatchType catchType) {
		this.catchTypeProperty.set(catchType);
	}
	
	public ObjectProperty<CatchType> catchTypeProperty () {
		return catchTypeProperty;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catchTypeProperty.get() == null) ? 0 : catchTypeProperty.get().hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
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
		Catch other = (Catch) obj;
		if (catchTypeProperty.get() == null) {
			if (other.catchTypeProperty.get() != null)
				return false;
		} else if (!catchTypeProperty.get().equals(other.catchTypeProperty.get()))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (note == null) {
			if (other.note != null)
				return false;
		} else if (!note.equals(other.note))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Catch [id=" + idProperty.get() + ", timestamp=" + timestamp + ", catchType="
				+ catchTypeProperty.get() + ", note=" + note + "]";
	}
	
	
}
