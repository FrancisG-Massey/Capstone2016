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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.nestnz.app.model.Catch;
import org.nestnz.app.model.Trap;
import org.nestnz.app.services.net.messages.CatchLog;

import javafx.collections.ListChangeListener;

/**
 * This service is used to monitor traps in a trapline for changes (such as logged catches & changes to trap coords).
 * Whenever a trap is added or updated on the monitored list, the change is either sent directly to the API (if internet is available) or added to a queue to be sent to the server when it becomes available.
 */
public class TraplineUpdateService implements ListChangeListener<Trap> {

    private static final Logger LOG = Logger.getLogger(TraplineUpdateService.class.getName());
	
	/* (non-Javadoc)
	 * @see javafx.collections.ListChangeListener#onChanged(javafx.collections.ListChangeListener.Change)
	 */
	@Override
	public void onChanged(javafx.collections.ListChangeListener.Change<? extends Trap> change) {
		while (change.next()) {
			if (change.wasUpdated()) {
				for (Trap trap : change.getList().subList(change.getFrom(), change.getTo())) {
					for (Catch c : trap.getCatches()) {
						if (!c.getId().isPresent()) {
							CatchLog cLog = new CatchLog();
							cLog.setTypeId(c.getCatchType().getId());
							cLog.setTrapId(trap.getId());
							LOG.log(Level.INFO, String.format("Detected unsent catch log: %s", cLog));
						}
					}
				}
			}
		}
	}

}
