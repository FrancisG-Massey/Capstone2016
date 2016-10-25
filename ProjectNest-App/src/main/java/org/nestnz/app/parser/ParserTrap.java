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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.Trap;

public final class ParserTrap {
	
	private int id;
	
	private int number;
	
	private double coordLat;
	
	private double coordLong;
	
	private String status;
	
	private String created;
	
	private String lastReset;
	
	private List<ParserCatch> catches;
	
	public ParserTrap(int id, int number, double coordLat, double coordLong, String status, String created,
			String lastReset, List<ParserCatch> catches) {
		this.id = id;
		this.number = number;
		this.coordLat = coordLat;
		this.coordLong = coordLong;
		this.status = status;
		this.created = created;
		this.lastReset = lastReset;
		this.catches = catches;
	}

	public ParserTrap() {
		
	}
	
	public ParserTrap(Trap trap) {
		this.id = trap.getId();
		this.number = trap.getNumber();
		this.coordLat = trap.getLatitude();
		this.coordLong = trap.getLongitude();
		this.status = trap.getStatus().name();
		this.created = trap.getCreated() == null ? null : trap.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		this.lastReset = trap.getLastReset() == null ? null : trap.getLastReset().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		
		this.catches = new ArrayList<>();
		for (Catch c : trap.getCatches()) {
			ParserCatch pCatch = new ParserCatch();
			pCatch.setId(c.getId().orElse(-1));
			pCatch.setTypeId(c.getCatchType().getId());
			pCatch.setTimestamp(c.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			pCatch.setNote(c.getNote());
			this.catches.add(pCatch);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public List<ParserCatch> getCatches() {
		return catches;
	}

	public void setCatches(List<ParserCatch> catches) {
		this.catches = catches;
	}

	@Override
	public String toString() {
		return "ParserTrap [id=" + id + ", number=" + number + ", coordLat=" + coordLat + ", coordLong=" + coordLong + ", created="
				+ created + ", catches=" + catches + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catches == null) ? 0 : catches.hashCode());
		long temp;
		temp = Double.doubleToLongBits(coordLat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(coordLong);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + id;
		result = prime * result + ((lastReset == null) ? 0 : lastReset.hashCode());
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
		ParserTrap other = (ParserTrap) obj;
		if (catches == null) {
			if (other.catches != null)
				return false;
		} else if (!catches.equals(other.catches))
			return false;
		if (Double.doubleToLongBits(coordLat) != Double.doubleToLongBits(other.coordLat))
			return false;
		if (Double.doubleToLongBits(coordLong) != Double.doubleToLongBits(other.coordLong))
			return false;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (id != other.id)
			return false;
		if (lastReset == null) {
			if (other.lastReset != null)
				return false;
		} else if (!lastReset.equals(other.lastReset))
			return false;
		if (number != other.number)
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}
}
