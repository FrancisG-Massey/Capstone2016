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
package org.nestnz.app.parser;

import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlElement;

import org.nestnz.app.model.Trap;

public final class ParserTrap {
	
	private int number;
	
	private double coordLat;
	
	private double coordLong;
	
	private String created;
	
	private String lastReset;
	
	public ParserTrap() {
		
	}
	
	public ParserTrap(Trap trap) {
		this.number = trap.getNumber();
		this.coordLat = trap.getLatitude();
		this.coordLong = trap.getLongitude();
		this.created = trap.getCreated() == null ? null : trap.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		this.lastReset = trap.getLastReset() == null ? null : trap.getLastReset().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@XmlElement(name="coord_lat")
	public double getCoordLat() {
		return coordLat;
	}

	public void setCoordLat(double coordLat) {
		this.coordLat = coordLat;
	}

	@XmlElement(name="coord_long")
	public double getCoordLong() {
		return coordLong;
	}

	public void setCoordLong(double coordLong) {
		this.coordLong = coordLong;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
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
		return "ParserTrap [number=" + number + ", coordLat=" + coordLat + ", coordLong=" + coordLong + ", created="
				+ created + "]";
	}
}
