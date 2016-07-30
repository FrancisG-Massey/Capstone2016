package org.nestnz.app.model;

/**
 * Used to identify the current activity status of a trap (i.e. whether it is active or inactive for whatever reason).
 * 
 */
public enum TrapStatus {
	/**
	 * The trap is currently being used to catch pests.
	 */
	ACTIVE,
	
	/**
	 * The trap is currently inactive (damaged or missing) and cannot catch pests
	 */
	INACTIVE;
}
