package com.zns.comicdroid;

import java.io.IOException;
import java.text.ParseException;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import com.zns.comicdroid.data.Comic;

import android.os.AsyncTask;

public class BooksQueryTask extends AsyncTask<String, Void, Comic> {
	public Exception exception = null;
	
	protected Comic doInBackground(String... param)
	{
    	NetHttpTransport httpTransport = new NetHttpTransport();
    	JsonFactory jsonFactory = new JacksonFactory();
    	Comic comic = null;
    	
        try 
        {
            Books books = new Books.Builder(httpTransport, jsonFactory, null)
        	.setApplicationName("ComicsDroid/1.0")
        	.setGoogleClientRequestInitializer(new BooksRequestInitializer("AIzaSyBEk4GWuXFdmNUA6fJaG_6vJLqcMbNmhXM"))
        	.build();
            
			Volumes list = books.volumes().list(param[0]).execute();

			if (list != null && list.getTotalItems() > 0)
			{
				Volume item = list.getItems().get(0);
				Volume.VolumeInfo info = item.getVolumeInfo();				
				comic = Comic.fromVolumeInfo(info);				
			}
		} 
        catch (Exception e) {
        	this.exception = e;
		}
        
        return comic;
	}	
}
