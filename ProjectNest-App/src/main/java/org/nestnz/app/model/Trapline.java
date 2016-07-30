package org.nestnz.app.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Trapline {
	
	private final int id;
	
	/**
	 * The visible name of the trapline
	 */
	private final String name;
	
	private final ObservableList<Trap> traps = FXCollections.observableArrayList();

	public Trapline(int id, String name) {
		this.id = id;
		this.name = name;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trapline [id=" + id + ", name=" + name + ", traps=" + traps + "]";
	}
}
