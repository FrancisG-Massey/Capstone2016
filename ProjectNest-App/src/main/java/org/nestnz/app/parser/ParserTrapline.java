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

import java.util.ArrayList;
import java.util.List;

import org.nestnz.app.model.Trap;
import org.nestnz.app.model.Trapline;

public final class ParserTrapline {

	private String name;
	private int id;
	private List<ParserTrap> traps;
	private String start;
	private String end;
	
	public ParserTrapline (Trapline trapline) {
		this.name = trapline.getName();
		this.id = trapline.getId();
		this.traps = new ArrayList<>();
		for (Trap trap : trapline.getTraps()) {
			this.traps.add(new ParserTrap(trap));
		}
		this.start = trapline.getStart();
		this.end = trapline.getEnd();
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
	@Override
	public String toString() {
		return "Parser_Trapline [name=" + name + ", id=" + id + ", traps=" + traps + ", start=" + start + ", end=" + end
				+ "]";
	}
}
