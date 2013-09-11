package com.zns.comicdroid.task;

import com.zns.comicdroid.data.Comic;

public class BooksQueryResult {
	public boolean mSuccess;
	public Comic mComic;

	public BooksQueryResult(boolean success, Comic comic) {
		this.mSuccess = success;
		this.mComic = comic;
	}
}