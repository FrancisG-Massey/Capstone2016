package org.nestnz.app.model;

import java.time.LocalDateTime;
import java.util.Optional;

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
	private CatchType catchType;	
	

	public Catch(CatchType catchType) {
		this.catchType = catchType;
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
		return catchType;
	}

	public void setCatchType(CatchType catchType) {
		this.catchType = catchType;
	}
	
	
}
