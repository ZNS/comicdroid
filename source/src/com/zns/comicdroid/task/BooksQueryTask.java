package com.zns.comicdroid.task;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import com.zns.comicdroid.data.Comic;

import de.greenrobot.event.EventBus;

import android.os.AsyncTask;

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
			.setApplicationName("ComicsDroid/1.0")
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
		} 
		catch (Exception e) {
			this.mException = e;
		}

		EventBus.getDefault().post(result);
		return null;
	}	
}