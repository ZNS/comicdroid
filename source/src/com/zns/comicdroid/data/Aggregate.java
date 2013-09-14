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

public class Aggregate {
	private int mId;
	private String mTitle;
	private int mType;

	public Aggregate(int id, String title, int type) {
		this.mId = id;
		this.mTitle = title;
		this.mType = type;
	}

	public int getId() {
		return mId;
	}
	public void setId(int id) {
		this.mId = id;
	}

	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String title) {
		this.mTitle = title;
	}

	public int getType() {
		return mType;
	}
	public void setType(int type) {
		this.mType = type;
	}
}
