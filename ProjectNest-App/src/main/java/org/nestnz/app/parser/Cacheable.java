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

import java.time.LocalDateTime;

public class Cacheable<T> {

	private T data;
	private LocalDateTime lastServerFetch;
	
	public Cacheable() {
		
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public LocalDateTime getLastServerFetch() {
		return lastServerFetch;
	}

	public void setLastServerFetch(LocalDateTime lastServerFetch) {
		this.lastServerFetch = lastServerFetch;
	}

	@Override
	public String toString() {
		return "Cacheable [data=" + data + ", lastServerFetch=" + lastServerFetch + "]";
	}
}
