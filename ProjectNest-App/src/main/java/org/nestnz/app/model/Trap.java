package org.nestnz.app.model;

import java.time.LocalDateTime;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Trap {

	/**
	 * The internal server ID of the trap
	 */
	private final int id;
	
	private final int number;
	
	/**
	 * The longitude (x) coordinate of the trap
	 */
	private final double longitude;
	
	/**
	 * The latitude (y) coordinate of the trap
	 */
	private final double latitude;
	
	/**
	 * The current status of the trap (whether it's active or not)
	 */
	private TrapStatus status;
	
	/**
	 * The date & time the trap was last checked & reset
	 */
	private LocalDateTime lastReset;
	
	private final ObservableList<Catch> catches = FXCollections.observableArrayList();

	public Trap(int id, int number, double longitude, double latitude, TrapStatus status, LocalDateTime lastReset) {
		this.id = id;
		this.number = number;
		this.longitude = longitude;
		this.latitude = latitude;
		this.status = status;
		this.lastReset = lastReset;
	}

	/**
	 * @see #id
	 */
	public int getId() {
		return id;
	}

	public int getNumber() {
		return number;
	}

	/**
	 * @see #longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public TrapStatus getStatus() {
		return status;
	}

	public LocalDateTime getLastReset() {
		return lastReset;
	}

	public ObservableList<Catch> getCatches() {
		return catches;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lastReset == null) ? 0 : lastReset.hashCode());
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + id;
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
		if (lastReset == null) {
			if (other.lastReset != null)
				return false;
		} else if (!lastReset.equals(other.lastReset))
			return false;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		if (status != other.status)
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trap [id=" + id + ", longitude=" + longitude + ", latitude=" + latitude + ", status=" + status
				+ ", lastReset=" + lastReset + "]";
	}
}
