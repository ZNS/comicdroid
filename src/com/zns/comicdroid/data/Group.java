package com.zns.comicdroid.data;

public class Group {
	private int id;
	private String name;
	private byte[] image;

	public Group(int id) {
		this.id = id;
	}
	
	public Group(int id, String name, byte[] image)
	{
		this.id = id;
		this.name = name;
		this.image = image;
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
	
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		if (obj == null || obj.getClass() != this.getClass()) 
			return false;
		Group g = (Group)obj;
		return g.getId() == this.getId();
	}
	
	@Override
	public int hashCode() {
		int hash = 5;
		hash = hash + ((Integer)this.getId()).hashCode();
		return hash;
	}
}
