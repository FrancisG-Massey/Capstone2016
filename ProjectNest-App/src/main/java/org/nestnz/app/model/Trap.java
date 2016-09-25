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

import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Trap {

	/**
	 * The internal server ID of the trap, if the trap has been posted to the server.
	 * If the trap has not yet been posted to the server, this will be Optional.empty()
	 */
	private final ReadOnlyIntegerWrapper idProperty = new ReadOnlyIntegerWrapper();
	
	/**
	 * The assigned number of the trap (visible on the yellow tags along the trapline).
	 * Traps are numbered in the sequence they occur from the start of the trapline.
	 */
	private final IntegerProperty numberProperty = new SimpleIntegerProperty();
	
	/**
	 * The longitude coordinate of the trap
	 */
	private final DoubleProperty longitudeProperty = new SimpleDoubleProperty();
	
	/**
	 * The latitude coordinate of the trap
	 */
	private final DoubleProperty latitudeProperty = new SimpleDoubleProperty();
	
	/**
	 * The current status of the trap (whether it's active or not)
	 */
	private final ObjectProperty<TrapStatus> statusProperty = new SimpleObjectProperty<>();
	
	/**
	 * The date & time the trap was created
	 */
	private LocalDateTime created;
	
	/**
	 * The date & time the trap was last checked & reset
	 */
	private final ObjectProperty<LocalDateTime> lastResetProperty = new SimpleObjectProperty<>();
	
	private final ObservableList<Catch> catches = FXCollections.observableArrayList(c -> new Observable[]{ c.catchTypeProperty() });

	public Trap(int number, double latitude, double longitude) {
		this(number, latitude, longitude, TrapStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
	}

	public Trap(int number, double latitude, double longitude, TrapStatus status, LocalDateTime created, LocalDateTime lastReset) {
		this.numberProperty.set(number);
		this.latitudeProperty.set(latitude);
		this.longitudeProperty.set(longitude);
		this.statusProperty.set(status);
		this.created = created;
		this.lastResetProperty.set(lastReset);
	}

	public Trap(int id, int number, double latitude, double longitude, TrapStatus status, LocalDateTime created, LocalDateTime lastReset) {
		this.idProperty.set(id);
		this.numberProperty.set(number);
		this.latitudeProperty.set(latitude);
		this.longitudeProperty.set(longitude);
		this.statusProperty.set(status);
		this.created = created;
		this.lastResetProperty.set(lastReset);
	}

	public int getId() {
		return idProperty.get();
	}
	
	public ReadOnlyIntegerProperty idProperty() {
		return idProperty.getReadOnlyProperty();
	}

	public int getNumber() {
		return numberProperty.get();
	}
	
	public void setNumber(int number) {
		numberProperty.set(number);
	}
	
	public IntegerProperty numberProperty () {
		return numberProperty;
	}

	public double getLatitude() {
		return latitudeProperty.get();
	}
	
	public void setLatitude(double latitude) {
		latitudeProperty.set(latitude);
	}
	
	public DoubleProperty latitudeProperty () {
		return latitudeProperty;
	}
	
	public double getLongitude() {
		return longitudeProperty.get();
	}
	
	public void setLongitude (double longitude) {
		longitudeProperty.set(longitude);
	}
	
	public DoubleProperty longitudeProperty () {
		return longitudeProperty;
	}

	public TrapStatus getStatus() {
		return statusProperty.get();
	}
	
	public void setStatus (TrapStatus status) {
		statusProperty.set(status);
	}
	
	public ObjectProperty<TrapStatus> statusProperty () {
		return statusProperty;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public LocalDateTime getLastReset() {
		return lastResetProperty.get();
	}
	
	public void setLastReset (LocalDateTime lastReset) {
		lastResetProperty.set(lastReset);
	}
	
	public ObjectProperty<LocalDateTime> lastResetProperty () {
		return lastResetProperty;
	}

	public ObservableList<Catch> getCatches() {
		return catches;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catches == null) ? 0 : catches.hashCode());
		result = prime * result + idProperty.get();
		result = prime * result + ((lastResetProperty.get() == null) ? 0 : lastResetProperty.get().hashCode());
		long temp;
		temp = Double.doubleToLongBits(latitudeProperty.get());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitudeProperty.get());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + numberProperty.get();
		result = prime * result + ((statusProperty.get() == null) ? 0 : statusProperty.get().hashCode());
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
		Trap other = (Trap) obj;
		if (catches == null) {
			if (other.catches != null)
				return false;
		} else if (!catches.equals(other.catches))
			return false;
		if (idProperty.get() != other.idProperty.get())
			return false;
		if (lastResetProperty.get() == null) {
			if (other.lastResetProperty.get() != null)
				return false;
		} else if (!lastResetProperty.get().equals(other.lastResetProperty.get()))
			return false;
		if (Double.doubleToLongBits(latitudeProperty.get()) != Double.doubleToLongBits(other.latitudeProperty.get()))
			return false;
		if (Double.doubleToLongBits(longitudeProperty.get()) != Double.doubleToLongBits(other.longitudeProperty.get()))
			return false;
		if (numberProperty.get() != other.numberProperty.get())
			return false;
		if (statusProperty.get() != other.statusProperty.get())
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trap [id=" + getId() +", number=" + getNumber() + ", longitude=" + getLongitude() + ", latitude=" + getLatitude() 
				+ ", status=" + getStatus() + ", lastReset=" + getLastReset() + "]";
	}
}
