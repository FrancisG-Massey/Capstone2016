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
