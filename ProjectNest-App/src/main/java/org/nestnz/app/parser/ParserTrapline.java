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
		trapline.getEnd().ifPresent(end -> this.end = end);
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
