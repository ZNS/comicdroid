package com.zns.comicdroid.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;

public final class JsonUtil {
	public static <T extends Object> T deserialize(String url, Class<T> type, int timeout)
	{
		return deserialize(url, type, null, timeout);
	}
		
	public static <T extends Object> T deserialize(String url, Class<T> type, String skipToKey, int timeout)
	{
		JsonFactory jsonFactory = new JacksonFactory();
		InputStream stream = null;
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			int statusCode = connection.getResponseCode();
			if (statusCode == HttpURLConnection.HTTP_OK) {
				stream = new BufferedInputStream(connection.getInputStream());
				JsonParser parser = jsonFactory.createJsonParser(stream);
				if (skipToKey != null) {
					parser.skipToKey(skipToKey);
				}
				return parser.parseAndClose(type);
			}
		}
		catch (IOException e) {
			e.printStackTrace();			
		}
		finally {
			if (connection != null)
			{
				connection.disconnect();
				if (stream != null)
				{
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
	public static <T extends Object> Collection<T> deserializeArray(String url, Class<T> type)
	{
		return deserializeArray(url, type, 15000);
	}
	
	public static <T extends Object> Collection<T> deserializeArray(String url, Class<T> type, int timeout)
	{
		JsonFactory jsonFactory = new JacksonFactory();
		InputStream stream = null;
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			int statusCode = connection.getResponseCode();
			if (statusCode == HttpURLConnection.HTTP_OK) {
				stream = new BufferedInputStream(connection.getInputStream());
				JsonParser parser = jsonFactory.createJsonParser(stream);
				return parser.parseArrayAndClose(List.class, type);
			}
		}
		catch (IOException e) {
			e.printStackTrace();			
		}
		finally {
			if (connection != null)
			{
				connection.disconnect();
				if (stream != null)
				{
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}	
}
