package com.zns.comicdroid.data;

public class Aggregate {
	private int id;
	private String title;
	private int type;

	public Aggregate(int id, String title, int type) {
		this.id = id;
		this.title = title;
		this.type = type;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
