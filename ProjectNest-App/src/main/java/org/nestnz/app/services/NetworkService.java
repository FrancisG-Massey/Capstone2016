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
package org.nestnz.app.services;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.Trap;

import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * 
 */
public interface NetworkService {
	
	public static enum UpdateStatus { PENDING, SUCCESS, FAILED };

	public ReadOnlyObjectProperty<UpdateStatus> sendLoggedCatch(int trapId, Catch loggedCatch);
	
	public ReadOnlyObjectProperty<UpdateStatus> sendCreatedTrap(int traplineId, Trap trap);
}
