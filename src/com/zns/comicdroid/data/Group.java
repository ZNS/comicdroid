package com.zns.comicdroid.data;

public class Group {
	private int id;
	private String name;
	private String image;
	private int bookCount;
	private int totalBookCount;
	private int isWatched;
	private int isFinished;
	private int isComplete;
	
	public Group(int id) {
		this.id = id;
	}
	
	public Group(int id, String name, String image, int bookCount, int totalBookCount, int isWatched, int isFinished, int isComplete)
	{
		this.id = id;
		this.name = name;
		this.image = image;
		this.bookCount = bookCount;
		this.totalBookCount = totalBookCount;
		this.isWatched = isWatched;
		this.isFinished = isFinished;
		this.isComplete = isComplete;
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
	
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	
	public int getBookCount() {
		return this.bookCount;
	}

	public int getTotalBookCount() {
		return this.totalBookCount;
	}
	public void setTotalBookCount(int count) {
		this.totalBookCount = count;
	}
	
	public boolean getIsWatched() {
		return this.isWatched == 1;
	}
	public void setIsWatched(boolean watched) {
		this.isWatched = watched ? 1 : 0;
	}
	
	public boolean getIsFinished() {
		return this.isFinished == 1;
	}
	public void setIsFinished(boolean finished) {
		this.isFinished = finished ? 1 : 0;
	}
	
	public boolean getIsComplete() {
		return this.isComplete == 1;
	}
	public void setIsComplete(boolean complete) {
		this.isComplete = complete ? 1 : 0;
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
