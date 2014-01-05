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

import android.os.AsyncTask;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.util.JsonUtil;

import de.greenrobot.event.EventBus;

public class BooksQueryTask extends AsyncTask<String, Void, Void> {
	public Exception mException = null;

	protected Void doInBackground(String... param)
	{
		NetHttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		BooksQueryResult result = new BooksQueryResult(false, null);
		
		try 
		{
			Books books = new Books.Builder(httpTransport, jsonFactory, null)
			.setApplicationName("ComicDroid/1.0")
			.setGoogleClientRequestInitializer(new BooksRequestInitializer())
			.build();

			Volumes list = books.volumes().list(param[0]).execute();

			if (list != null && list.getTotalItems() > 0)
			{
				Volume item = list.getItems().get(0);
				Volume.VolumeInfo info = item.getVolumeInfo();				
				result.mComic = Comic.fromVolumeInfo(info, param[1], param[2]);				
				result.mSuccess = true;				
			}
			
			if (!result.mSuccess || !result.mComic.isComplete())
			{
				//Try gcd
				String gcdUrl = Application.GCD_API_BASEURL + "/isbn/" + param[2];
				com.zns.comicdroid.gcd.Issue issue = JsonUtil.deserialize(gcdUrl, com.zns.comicdroid.gcd.Issue.class, 5000);
				if (issue != null)
				{
					if (result.mComic == null) {
						result.mComic = Comic.fromGCDIssue(issue, param[1], param[2]);
					}
					else {
						result.mComic.extendFromGCD(issue, param[1]);
					}
					result.mSuccess = true;
				}
			}
			
			if (!result.mSuccess || !result.mComic.isComplete())
			{
				//Try open library
				String openLibUrl = "https://openlibrary.org/api/books?bibkeys=" + param[0] + "&format=json&jscmd=data";
				com.zns.openlibrary.Book book = JsonUtil.deserialize(openLibUrl, com.zns.openlibrary.Book.class, param[0], 5000);
				if (book != null)
				{
					if (result.mComic == null) {
						result.mComic = Comic.fromOpenLibraryBook(book, param[1], param[2]);
					}
					else {
						result.mComic.extendFromOpenLibrary(book, param[1]);
					}
					result.mSuccess = true;
				}
			}			
		} 
		catch (Exception e) {
			this.mException = e;
		}

		EventBus.getDefault().post(result);
		return null;
	}	
}
