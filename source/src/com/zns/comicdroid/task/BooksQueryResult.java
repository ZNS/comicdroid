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
