package com.zns.comicdroid.task;

import com.zns.comicdroid.data.Comic;

public class BooksQueryResult {
	public boolean success;
	public Comic comic;
	
	public BooksQueryResult(boolean success, Comic comic) {
		this.success = success;
		this.comic = comic;
	}
}