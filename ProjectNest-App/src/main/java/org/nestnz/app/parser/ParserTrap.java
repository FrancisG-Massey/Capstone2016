package org.nestnz.app.parser;

import javax.xml.bind.annotation.XmlElement;

import org.nestnz.app.model.Trap;

public final class ParserTrap {
	
	private int number;
	
	private double coordLat;
	
	private double coordLong;
	
	private String created;
	
	public ParserTrap() {
		
	}
	
	public ParserTrap(Trap trap) {
		this.number = trap.getNumber();
		this.coordLat = trap.getLatitude();
		this.coordLong = trap.getLongitude();
		this.created = trap.getCreated().toString();
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

	@Override
	public String toString() {
		return "ParserTrap [number=" + number + ", coordLat=" + coordLat + ", coordLong=" + coordLong + ", created="
				+ created + "]";
	}
}
