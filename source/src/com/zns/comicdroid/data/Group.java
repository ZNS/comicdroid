/*******************************************************************************
 * Copyright (c) 2013 Ulrik Andersson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Ulrik Andersson - initial API and implementation
 ******************************************************************************/
package com.zns.comicdroid.data;

import java.util.List;

import com.zns.comicdroid.amazon.Book;

public class Group {
	private int mId;
	private String mName;
	private String mImage;
	private int mBookCount;
	private int mTotalBookCount;
	private int mIsWatched;
	private int mIsFinished;
	private int mIsComplete;
	private List<Book> mAmazonBooks;
	
	public Group(int id) {
		this.mId = id;
	}

	public Group(int id, String name, String image, int bookCount, int totalBookCount, int isWatched, int isFinished, int isComplete)
	{
		this.mId = id;
		this.mName = name;
		this.mImage = image;
		this.mBookCount = bookCount;
		this.mTotalBookCount = totalBookCount;
		this.mIsWatched = isWatched;
		this.mIsFinished = isFinished;
		this.mIsComplete = isComplete;
	}

	public int getId() {
		return mId;
	}
	public void setId(int id) {
		this.mId = id;
	}

	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}

	public String getImage() {
		return mImage;
	}
	public void setImage(String image) {
		this.mImage = image;
	}

	public int getBookCount() {
		return this.mBookCount;
	}

	public int getTotalBookCount() {
		return this.mTotalBookCount;
	}
	public void setTotalBookCount(int count) {
		this.mTotalBookCount = count;
	}

	public boolean getIsWatched() {
		return this.mIsWatched == 1;
	}
	public void setIsWatched(boolean watched) {
		this.mIsWatched = watched ? 1 : 0;
	}

	public boolean getIsFinished() {
		return this.mIsFinished == 1;
	}
	public void setIsFinished(boolean finished) {
		this.mIsFinished = finished ? 1 : 0;
	}

	public boolean getIsComplete() {
		return this.mIsComplete == 1;
	}
	public void setIsComplete(boolean complete) {
		this.mIsComplete = complete ? 1 : 0;
	}

	public List<Book> getAmazonBooks() {
		return this.mAmazonBooks;
	}
	public void setAmazonBooks(List<Book> books) {
		this.mAmazonBooks = books;
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
