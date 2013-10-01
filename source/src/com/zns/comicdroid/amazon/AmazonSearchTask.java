package com.zns.comicdroid.amazon;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.api.client.xml.Xml;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.data.Comic;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

public class AmazonSearchTask extends AsyncTask<AmazonSearchTask.AmazonSearchTaskRequest, Void, AmazonSearchTask.AmazonSearchTaskResponse> {

    private static final String AWS_ACCESS_KEY_ID = "AKIAIXSWS4FEWXMGINOQ";
    private static final String AWS_SECRET_KEY = "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";
    private static final String ENDPOINT = "ecs.amazonaws.com";
    private static final String ASSOCIATE_TAG = "COMICDROID-1";
    
    public static String getNextIssueQuery(Comic comic) {
		String author = comic.getAuthor();
		if (author != null && author.length() > 0) {
			String[] parts = author.split(" ");
			author = parts[parts.length - 1];
		}
		return (author != null && author.length() > 0 ? "author:" + author + " and " : "") + "title:\"" + comic.getTitle() + "\" " + (comic.getIssue() + 1);    	
    }
    
    public static String getAuthorQuery(String author) {
    	return "author:" + author;
    }
    
    public static class AmazonSearchTaskRequest {
    	public String query;
    	public String orderBy;
    	public int index;
    	public int issue;
    	public String cachePath;
    }
    
    public static class AmazonSearchTaskResponse {
    	public List<Book> books;
    	public int index;
    }
    
	@Override
	protected AmazonSearchTaskResponse doInBackground(AmazonSearchTaskRequest... arg0) {
		List<Book> books = new ArrayList<Book>();
		SignedRequestsHelper helper = null;
		InputStream stream = null;
		try
		{
			helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("Service", "AWSECommerceService");
	        params.put("AssociateTag", ASSOCIATE_TAG);
	        params.put("SearchIndex", "Books");
	        params.put("Operation", "ItemSearch");
	        params.put("Power", arg0[0].query);
	        params.put("ResponseGroup", "Images,ItemAttributes");
	        params.put("IncludeReviewsSummary", "false");	     
	        if (arg0[0].orderBy != null) {
	        	params.put("Sort", arg0[0].orderBy);
	        }
	        final String requestUrl = helper.sign(params);
	        String key = params.get("Power").concat(params.containsKey("Sort") ? params.get("Sort") : "");
	        
	        XmlPullParser parser = Xml.createParser();
	        stream = getXmlStream(requestUrl, key, arg0[0].cachePath);
	        parser.setInput(stream, null);
	        parser.nextTag();
	        parser.require(XmlPullParser.START_TAG, null, "ItemSearchResponse");
	        while (parser.next() != XmlPullParser.END_DOCUMENT) {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String name = parser.getName();
	            if (name.equalsIgnoreCase("Item")) {
	            	Book book = readItem(parser);
	            	if (book != null && !book.Title.contains("#" + arg0[0].issue)) {
	            		books.add(book);
	            	}
	            }
	        }
		}
		catch (Exception e) {
		}
		finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		AmazonSearchTaskResponse response = new AmazonSearchTaskResponse();
		response.index = arg0[0].index;
		response.books = books;
		return response;
	}
	
	@SuppressLint("DefaultLocale")
	private InputStream getXmlStream(String url, String key, String cachePath) throws MalformedURLException, IOException 
	{
		//Create directories
		File cachePathFile = new File(cachePath);
		cachePathFile.mkdirs();
		//Read file
		File cacheFile = new File(cachePath, "itemsearch" + Integer.toString(key.toLowerCase(Locale.ENGLISH).hashCode()) + ".xml");
		long diff = new Date().getTime() - cacheFile.lastModified();		
		if (diff >= Application.CACHE_AMAZONSEARCH_HOURS * 60 * 60 * 1000) {
			//Delete cache
			cacheFile.delete();
			//Save to cache
			BufferedInputStream in = null;
			FileOutputStream out = null;
			try
			{
				in = new BufferedInputStream(new URL(url).openStream());
				out = new FileOutputStream(cacheFile);
				byte[] data = new byte[1024];
				int count = 0;
				while ((count = in.read(data)) != -1) {
					out.write(data, 0, count);
				}
			}
			catch (IOException e) {
				//Failed to write cache, try to web-stream
				return new URL(url).openStream();
			}			
			finally {
				if (out != null) {
					out.flush();
					out.close();
				}
				if (in != null) {
					in.close();
				}
			}
		}
		//Use cache file
		return new BufferedInputStream(new FileInputStream(cacheFile));
	}
	
	private Book readItem(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		String image = "", id = "", author = "", title = "", date = "", price = "";
		parser.require(XmlPullParser.START_TAG, null, "Item");
	    while (parser.next() != XmlPullParser.END_DOCUMENT) {
	        if (parser.getEventType() == XmlPullParser.START_TAG) {
		        String name = parser.getName();
		        if (name.equalsIgnoreCase("MediumImage")) {
		        	image = readImage(parser);
		        }
		        else if (name.equalsIgnoreCase("ASIN")) {
		        	id = readElementText(parser, "ASIN");
		        }
		        else if (name.equalsIgnoreCase("Author")) {
		        	author = readElementText(parser, "Author");
		        }	        
		        else if (name.equalsIgnoreCase("Title")) {
		        	title = readElementText(parser, "Title");
		        }
		        else if (name.equalsIgnoreCase("PublicationDate")) {
		        	date = readElementText(parser, "PublicationDate");
		        }
		        else if (name.equalsIgnoreCase("FormattedPrice")) {
		        	price = readElementText(parser, "FormattedPrice");
		        }
	        }
	        else if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("Item")) {
	        	break;
	        }
	    }
	    parser.require(XmlPullParser.END_TAG, null, "Item");
	    return new Book(id, image, title, author, date, price);
	}
	
	private String readImage(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		String url = "";
		parser.require(XmlPullParser.START_TAG, null, "MediumImage");
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase("URL")) {
				url = readText(parser);
			}
			else if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("MediumImage")) {
				break;
			}
		}
		parser.require(XmlPullParser.END_TAG, null, "MediumImage");
		return url;
	}
	
	private String readElementText(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
	    parser.require(XmlPullParser.START_TAG, null, tag);
	    String text = readText(parser);
	    parser.require(XmlPullParser.END_TAG, null, tag);
	    return text;
	}
	
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}	
}
