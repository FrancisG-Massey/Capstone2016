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
package org.nestnz.app.model;

import java.net.URL;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class CatchType {
	
	public static final CatchType EMPTY = new CatchType(-1, "Empty", null);
	public static final CatchType OTHER = new CatchType(10000, "Other", null);
	
	/**
	 * The internal server ID of the catch type
	 */
	private final int id;
	
	private final StringProperty nameProperty = new SimpleStringProperty();
	
	private URL imageUrl;
	
	private Image image;
	
	public CatchType(int id) {
		this.id = id;
	}

	public CatchType(int id, String name, URL imageUrl) {
		this.id = id;
		this.nameProperty.set(name);
		this.imageUrl = imageUrl;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return nameProperty.get();
	}
	
	public void setName (String name) {
		nameProperty.set(name);
	}
	
	public StringProperty nameProperty () {
		return nameProperty;
	}

	public URL getImageUrl() {
		return imageUrl;
	}
	
	public void setImageUrl (URL image) {
		this.imageUrl = image;
	}
	
	public Image getImage () {
		if (image == null && imageUrl != null) {
			image = new Image(imageUrl.toExternalForm(), true);
		}
		return image;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((nameProperty.get() == null) ? 0 : nameProperty.get().hashCode());
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
		CatchType other = (CatchType) obj;
		if (id != other.id)
			return false;
		if (nameProperty.get() == null) {
			if (other.nameProperty.get() != null)
				return false;
		} else if (!nameProperty.get().equals(other.nameProperty.get()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CatchType [id=" + id + ", name=" + nameProperty.get() + ", imageUrl=" + imageUrl + "]";
	}
}
