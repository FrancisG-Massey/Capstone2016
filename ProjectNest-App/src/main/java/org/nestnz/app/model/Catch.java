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
	private Optional<Integer> id;
	
	/**
	 * The date & time this catch was recorded
	 */
	private LocalDateTime timestamp;
	
	/**
	 * The type of pest caught in the trap
	 */
	private final ObjectProperty<CatchType> catchTypeProperty = new SimpleObjectProperty<>();	
	

	public Catch(CatchType catchType) {
		this.catchTypeProperty.set(catchType);
		this.timestamp = LocalDateTime.now();
	}

	public Optional<Integer> getId() {
		return id;
	}

	public void setId(Optional<Integer> id) {
		this.id = id;
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
	
	
}
