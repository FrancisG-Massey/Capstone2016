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
 * Represents a trapline received from GET /trap on the API.
 */
public class ApiTrapline {
	
	private int id;
	
	private String name;
	
	private String start;
	
	private String end;
	
	private int regionId;
	
	private int commonCatchType1;
	private int commonCatchType2;
	private int commonCatchType3;
	
	public ApiTrapline () {
		
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="start_tag")
	public String getStart() {
		return start;
	}
	
	public void setStart(String start) {
		this.start = start;
	}

	@XmlElement(name="end_tag")
	public String getEnd() {
		return end;
	}
	
	public void setEnd(String end) {
		this.end = end;
	}
	
	@XmlElement(name="region_id")
	public int getRegionId() {
		return regionId;
	}
	
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	@XmlElement(name="common_ct_id_1")
	public int getCommonCatchType1() {
		return commonCatchType1;
	}
	
	public void setCommonCatchType1(int commonCatchType1) {
		this.commonCatchType1 = commonCatchType1;
	}

	@XmlElement(name="common_ct_id_2")
	public int getCommonCatchType2() {
		return commonCatchType2;
	}
	
	public void setCommonCatchType2(int commonCatchType2) {
		this.commonCatchType2 = commonCatchType2;
	}

	@XmlElement(name="common_ct_id_3")
	public int getCommonCatchType3() {
		return commonCatchType3;
	}
	
	public void setCommonCatchType3(int commonCatchType3) {
		this.commonCatchType3 = commonCatchType3;
	}

	@Override
	public String toString() {
		return "ApiTrapline [id=" + id + ", name=" + name + ", start=" + start + ", end=" + end + ", regionId="
				+ regionId + ", commonCatchType1=" + commonCatchType1 + ", commonCatchType2=" + commonCatchType2
				+ ", commonCatchType3=" + commonCatchType3 + "]";
	}
}
