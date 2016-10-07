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
 * Represents a trap entry to be sent/received from /trap on the API
 */
public final class ApiTrap {
	
	private int traplineId;
	
	private int number;
	
	private double latitude;
	
	private double longitude;
	
	private String created;
	
	public ApiTrap () {
		
	}

	public ApiTrap(int traplineId, int number, double latitude, double longitude, String created) {
		this.traplineId = traplineId;
		this.number = number;
		this.latitude = latitude;
		this.longitude = longitude;
		this.created = created;
	}

	@XmlElement(name="trapline_id")
	public int getTraplineId() {
		return traplineId;
	}

	public void setTraplineId(int traplineId) {
		this.traplineId = traplineId;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@XmlElement(name="coord_lat")
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@XmlElement(name="coord_long")
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	@Override
	public String toString() {
		return "ApiTrap [traplineId=" + traplineId + ", number=" + number + ", latitude=" + latitude + ", longitude="
				+ longitude + ", created=" + created + "]";
	}
}
