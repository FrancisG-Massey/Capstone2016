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
package org.nestnz.app.services.net.messages;

import javax.json.JsonObject;
import javax.xml.bind.annotation.XmlElement;

import org.nestnz.app.services.net.APIUpdateMessage;

/**
 * 
 */
public final class CatchLog implements APIUpdateMessage {
	
	private String note;
	
	private int typeId;
	
	private int trapId;
	
	public CatchLog () {
		
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@XmlElement(name="type_id")
	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int id) {
		this.typeId = id;
	}

	@XmlElement(name="trap_id")
	public int getTrapId() {
		return trapId;
	}

	public void setTrapId(int trapId) {
		this.trapId = trapId;
	}

	@Override
	public String toString() {
		return "CatchLog [note=" + note + ", typeId=" + typeId + ", trapId=" + trapId + "]";
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.net.APIUpdateMessage#getMethod()
	 */
	@Override
	public String getMethod() {
		return "POST";
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.net.APIUpdateMessage#getPath()
	 */
	@Override
	public String getPath() {
		return "/catch";
	}

	/* (non-Javadoc)
	 * @see org.nestnz.app.services.net.APIUpdateMessage#onSuccessfulUpdate(javax.json.JsonObject)
	 */
	@Override
	public void onSuccessfulUpdate(JsonObject response) {
		// TODO Auto-generated method stub

	}

}
