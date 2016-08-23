package org.nestnz.app.model;

import java.time.LocalDateTime;
import java.util.Optional;

import org.nestnz.app.parser.ParserTrap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Trap {

	/**
	 * The internal server ID of the trap, if the trap has been posted to the server.
	 * If the trap has not yet been posted to the server, this will be Optional.empty()
	 */
	private Optional<Integer> id;
	
	private final int number;
	
	/**
	 * The longitude coordinate of the trap
	 */
	private final double longitude;
	
	/**
	 * The latitude coordinate of the trap
	 */
	private final double latitude;
	
	/**
	 * The current status of the trap (whether it's active or not)
	 */
	private TrapStatus status;
	
	/**
	 * The date & time the trap was created
	 */
	private LocalDateTime created;
	
	/**
	 * The date & time the trap was last checked & reset
	 */
	private LocalDateTime lastReset;
	
	private final ObservableList<Catch> catches = FXCollections.observableArrayList();
	
	public Trap(ParserTrap trap) {
		this.id = Optional.empty();
		this.number = trap.getNumber();
		this.latitude = trap.getCoordLat();
		this.longitude = trap.getCoordLong();
		this.created = LocalDateTime.parse(trap.getCreated());
		this.lastReset = trap.getLastReset() == null ? null : LocalDateTime.parse(trap.getLastReset());
	}

	public Trap(int number, double latitude, double longitude) {
		this(number, latitude, longitude, TrapStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
	}

	public Trap(int number, double latitude, double longitude, TrapStatus status, LocalDateTime created, LocalDateTime lastReset) {
		this.id = Optional.empty();
		this.number = number;
		this.latitude = latitude;
		this.longitude = longitude;
		this.status = status;
		this.created = created;
		this.lastReset = lastReset;
	}

	public Trap(int id, int number, double latitude, double longitude, TrapStatus status, LocalDateTime created, LocalDateTime lastReset) {
		this.id = Optional.of(id);
		this.number = number;
		this.latitude = latitude;
		this.longitude = longitude;
		this.status = status;
		this.created = created;
		this.lastReset = lastReset;
	}

	/**
	 * @see #id
	 */
	public Optional<Integer> getId() {
		return id;
	}
	
	public void setId(Optional<Integer> id) {
		this.id = id;
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

	public LocalDateTime getCreated() {
		return created;
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
		result = prime * result + ((catches == null) ? 0 : catches.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastReset == null) ? 0 : lastReset.hashCode());
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + number;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastReset == null) {
			if (other.lastReset != null)
				return false;
		} else if (!lastReset.equals(other.lastReset))
			return false;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		if (number != other.number)
			return false;
		if (status != other.status)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trap [id=" + id + ", longitude=" + longitude + ", latitude=" + latitude + ", status=" + status
				+ ", lastReset=" + lastReset + "]";
	}
}
