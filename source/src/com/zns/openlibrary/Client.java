package com.zns.openlibrary;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.zns.comicdroid.util.JsonUtil;

public class Client {
	private final static String BASEURL = "https://openlibrary.org/api";
	
	public Client() {		
	}
	
	public Book queryISBN(String isbn) {
		Book book = null;
		try
		{
			String query = "isbn:" + isbn;
			HttpURLConnection connection = getConnection(BASEURL + "/books?bibkeys=isbn:" + query + "&format=json&jscmd=data", 5000);
			book = JsonUtil.deserialize(connection, Book.class, query);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return book;
	}
	
	private HttpURLConnection getConnection(String url, int timeout) throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		connection.setConnectTimeout(timeout);
		return connection;
	}
}
