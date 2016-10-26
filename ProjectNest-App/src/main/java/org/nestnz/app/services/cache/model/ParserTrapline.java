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
package org.nestnz.app.services.cache.model;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.nestnz.app.model.CatchType;
import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;

public final class ParserTrapline {

	private int id;
	private String name;
	private List<ParserTrap> traps = new ArrayList<>();
	private String start;
	private String end;	
	private ParserRegion region;
	
	private List<Long> catchTypes = new ArrayList<>();
	
	private String lastUpdated;
	
	private boolean canEdit;
	
	public ParserTrapline (Trapline trapline) {
		this.name = trapline.getName();
		this.id = trapline.getId();
		this.traps = new ArrayList<>();
		for (Trap trap : trapline.getTraps()) {
			this.traps.add(new ParserTrap(trap));
		}
		this.start = trapline.getStart();
		this.end = trapline.getEnd();
		if (trapline.getRegion() != null) {
			this.region = new ParserRegion();
			this.region.setId(trapline.getRegion().getId());
			this.region.setName(trapline.getRegion().getName());
		}
		this.catchTypes = new ArrayList<>();
		for (CatchType t : trapline.getCatchTypes()) {
			this.catchTypes.add(Long.valueOf(t.getId()));
		}
		trapline.getLastUpdated().ifPresent(timestamp -> 
			this.lastUpdated = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		this.canEdit = trapline.canEdit();
	}
	
	public ParserTrapline(int id, String name, List<ParserTrap> traps, String start, String end, ParserRegion region,
			List<Long> catchTypes, String lastUpdated, boolean canEdit) {
		this.id = id;
		this.name = name;
		this.traps = traps;
		this.start = start;
		this.end = end;
		this.region = region;
		this.catchTypes = catchTypes;
		this.lastUpdated = lastUpdated;
		this.canEdit = canEdit;
	}

	public ParserTrapline () {
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public List<ParserTrap> getTraps() {
		return traps;
	}
	
	public void setTraps(List<ParserTrap> traps) {
		this.traps = traps;
	}
	
	public String getStart() {
		return start;
	}
	
	public void setStart(String start) {
		this.start = start;
	}
	
	public String getEnd() {
		return end;
	}
	
	public void setEnd(String end) {
		this.end = end;
	}
	
	@XmlElement(name="region")
	public ParserRegion getRegion() {
		return region;
	}

	public void setRegion(ParserRegion region) {
		this.region = region;
	}
	
	@XmlElement(name="catch_type_ids")
	public List<Long> getCatchTypes() {
		return catchTypes;
	}

	public void setCatchTypes(List<Long> catchTypes) {
		this.catchTypes = catchTypes;
	}

	@XmlElement(name="last_updated")
	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@XmlElement(name="can_edit")
	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	@Override
	public String toString() {
		return "ParserTrapline [name=" + name + ", id=" + id + ", traps=" + traps + ", start=" + start + ", end=" + end
				+ ", region=" + region + ", lastUpdated=" + lastUpdated + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catchTypes == null) ? 0 : catchTypes.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((traps == null) ? 0 : traps.hashCode());
		result = prime * result + ((lastUpdated == null) ? 0 : lastUpdated.hashCode());
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
		ParserTrapline other = (ParserTrapline) obj;
		if (catchTypes == null) {
			if (other.catchTypes != null)
				return false;
		} else if (!catchTypes.equals(other.catchTypes))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (traps == null) {
			if (other.traps != null)
				return false;
		} else if (!traps.equals(other.traps))
			return false;
		if (lastUpdated == null) {
			if (other.lastUpdated != null)
				return false;
		} else if (!lastUpdated.equals(other.lastUpdated))
			return false;
		return true;
	}
}
