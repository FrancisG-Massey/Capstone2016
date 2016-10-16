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
package org.nestnz.app.net.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Represents a trap sent to/received from /trap on the API.
 * This class can be used for GET and PUT requests, but {@link ApiPostTrap} should be used for POST requests as it excludes the ID
 */
public class ApiTrap extends ApiPostTrap {
	
	private int id;
	
	private String lastReset;
	
	public ApiTrap () {
		
	}

	public ApiTrap(int id, int traplineId, int number, double latitude, double longitude, String created) {
		super(traplineId, number, latitude, longitude, created);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@XmlElement(name="last_reset")
	public String getLastReset() {
		return lastReset;
	}

	public void setLastReset(String lastReset) {
		this.lastReset = lastReset;
	}

	@Override
	public String toString() {
		return "ApiTrap [id=" + id + ", traplineId=" + traplineId + ", number=" + number + ", latitude=" + latitude + ", longitude="
				+ longitude + ", created=" + created +", lastReset=" + lastReset + "]";
	}
}
