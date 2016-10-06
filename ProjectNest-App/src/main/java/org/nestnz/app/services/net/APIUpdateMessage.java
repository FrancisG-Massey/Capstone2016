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
package org.nestnz.app.services.net;

import javax.json.JsonObject;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Represents an update request to the NestNZ API.
 */
public interface APIUpdateMessage {
	
	/**
	 * Gets the HTTP method used to send the request to the server (eg POST, PUT, DELETE, etc
	 * @return
	 */
	@XmlTransient
	public String getMethod();
	
	/**
	 * Gets the API path used to update the data on the server (eg /trap, /trapline, etc)
	 * @return
	 */
	@XmlTransient
	public String getPath();
	
	/**
	 * Called if & when the update is sent to & accepted by the server
	 * @param response The Json response object received by the server
	 */
	public void onSuccessfulUpdate(JsonObject response);
}
