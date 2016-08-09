package org.nestnz.app.model;

import java.net.URL;

public class CatchType {
	
	/**
	 * The internal server ID of the catch type
	 */
	private final int id;
	
	private final String name;
	
	private final URL image;

	public CatchType(int id, String name, URL image) {
		this.id = id;
		this.name = name;
		this.image = image;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public URL getImage() {
		return image;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CatchType [id=" + id + ", name=" + name + ", image=" + image + "]";
	}
}