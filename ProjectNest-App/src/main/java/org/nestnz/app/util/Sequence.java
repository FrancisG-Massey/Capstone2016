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
package org.nestnz.app.util;

public class Sequence {
	private int start;
	private int end;
	private int step;
	
	public Sequence(int start, int end, int step) {
		this.start = start;
		this.end = end;
		this.step = step;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getStep() {
		return step;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
		result = prime * result + step;
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
		Sequence other = (Sequence) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		if (step != other.step)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Sequence [start=" + start + ", end=" + end + ", step=" + step + "]";
	}
}
