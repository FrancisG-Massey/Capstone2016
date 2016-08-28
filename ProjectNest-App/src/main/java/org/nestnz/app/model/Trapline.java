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

import java.util.Objects;
import java.util.Optional;

import org.nestnz.app.parser.ParserTrap;
import org.nestnz.app.parser.ParserTrapline;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Trapline {
	
	/**
	 * The internal server ID of the trapline
	 */
	private final int id;
	
	/**
	 * The visible name of the trapline
	 */
	private final String name;
	
	private final ObservableList<Trap> traps = FXCollections.observableArrayList();
	
	/**
	 * The region in which this trapline is located (e.g. Auckland, Waikato, Manawatu, etc)
	 */
	private final Region region;
	
	/**
	 * The name of the starting point of the trapline
	 */
	private final String start;
	
	/**
	 * Optionally, the name of the end point of the trapline. For loop traplines, this will be {@link Optional#empty()}
	 */
	private final Optional<String> end;
	
	public Trapline (ParserTrapline pTrapline) {
		Objects.requireNonNull(pTrapline);
		
		this.id = pTrapline.getId();
		this.name = pTrapline.getName();
		this.region = null;//TODO: Get the region data here
		this.start = pTrapline.getStart();
		this.end = Optional.ofNullable(pTrapline.getEnd());
		for (ParserTrap pTrap : pTrapline.getTraps()) {
			traps.add(new Trap(pTrap));
		}
	}
	
	public Trapline(int id, String name, Region region, String start) {
		this(id, name, region, start, null);
	}

	public Trapline(int id, String name, Region region, String start, String end) {
		this.id = id;
		this.name = name;
		this.region = region;
		this.start = start;
		this.end = Optional.ofNullable(end);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ObservableList<Trap> getTraps() {
		return traps;
	}

	public Region getRegion() {
		return region;
	}	

	public String getStart() {
		return start;
	}

	public Optional<String> getEnd() {
		return end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
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
		Trapline other = (Trapline) obj;
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
		return true;
	}

	@Override
	public String toString() {
		return "Trapline [id=" + id + ", name=" + name + ", traps=" + traps + ", region=" + region + ", start=" + start
				+ ", end=" + end + "]";
	}
}
