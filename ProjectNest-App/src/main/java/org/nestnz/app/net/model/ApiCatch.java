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
 * Represents a catch log entry to be sent/received from /catch on the API
 */
public class ApiCatch {
	
	private int trapId;
	
	private int typeId;
	
	private String note;
	
	public ApiCatch() {
		
	}

	public ApiCatch(int trapId, int typeId, String note) {
		this.trapId = trapId;
		this.typeId = typeId;
		this.note = note;
	}

	@XmlElement(name="trap_id")
	public int getTrapId() {
		return trapId;
	}

	public void setTrapId(int trapId) {
		this.trapId = trapId;
	}

	@XmlElement(name="type_id")
	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String toString() {
		return "ApiCatch [trapId=" + trapId + ", typeId=" + typeId + ", note=" + note + "]";
	}
}
