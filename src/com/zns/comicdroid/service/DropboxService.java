package com.zns.comicdroid.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.DBHelper;

public class DropboxService extends IntentService {
	
	public DropboxService() {
		super("DropboxService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		//Dropbox check		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String tokenKey = prefs.getString("DROPBOX_KEY", null);
		String tokenSecret = prefs.getString("DROPBOX_SECRET", null);

		//If user has not authenticated with dropbox, stop this now 
		if (tokenKey == null || tokenSecret == null) {
			stopSelf();
			return;
		}
		
		//Get some more stuff from context
		DBHelper db = DBHelper.getHelper(getApplicationContext());			
		String outPath = getApplicationContext().getExternalFilesDir(null).toString() + "/html";
		
		//Let's get started
		File fileOut = new File(outPath);
		fileOut.mkdirs();
		fileOut = new File(outPath, "index.html");		
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		Cursor cursor = db.getCursor("SELECT _id, Title, Subtitle, Author, ImageUrl, 1 AS ItemType, 0 AS BookCount, IsBorrowed " +
				"FROM tblBooks WHERE GroupId = 0 OR ifnull(GroupId, '') = '' " +
				"UNION " +
				"SELECT _id, Name AS Title, '' AS Subtitle, '' AS Author, ImageUrl, 2 AS ItemType, BookCount, 0 AS IsBorrowed " +
				"FROM tblGroups " +
				"ORDER BY Title", null);
		
		try 
		{			
			String line;
			
			//Read template
			StringBuilder sbTemplate = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.listtemplate)));
			while ((line = reader.readLine()) != null)
			{
				sbTemplate.append(line);
			}
			reader.close();
			
			//Write HTML
			writer = new BufferedWriter(new FileWriter(fileOut));
			reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.framework)));			
			while ((line = reader.readLine()) != null)
			{
				if (line.trim().equals("#LISTCOMICS#")) {
					while (cursor.moveToNext()) {
						int id = cursor.getInt(0);
						String title = cursor.getString(1);
						String subTitle = cursor.getString(2);
						if (subTitle == null)
							subTitle = "";
						String author = cursor.getString(3);
						if (author == null)
							author = "";
						String imageUrl = cursor.getString(4);
						if (imageUrl == null)
							imageUrl = "";
						int type = cursor.getInt(5);
						StringBuilder sbChildren = new StringBuilder();
						
						String comicLine = sbTemplate.toString();
						comicLine = comicLine.replace("#TITLE#", title + (type == 1 && !subTitle.equals("") ? " - " + subTitle : ""));
						comicLine = comicLine.replace("#AUTHOR#", author);
						comicLine = comicLine.replace("#IMAGEURL#", imageUrl);
						comicLine = comicLine.replace("#ISSUE#", "");
						
						if (type == 2) {
							//Render group children
							List<Comic> comics = db.getComics(id);
							for(Comic comic : comics) {
								String childComic = sbTemplate.toString();
								childComic = childComic.replace("#TITLE#", comic.getTitle() + (!comic.getSubTitle().equals("") ? " - " + comic.getSubTitle() : ""));
								childComic = childComic.replace("#AUTHOR#", comic.getAuthor());
								childComic = childComic.replace("#IMAGEURL#", comic.getImageUrl());
								childComic = childComic.replace("#ISSUE#", comic.getIssue() > 0 ? Integer.toString(comic.getIssue()) : "");
								childComic = childComic.replace("#ISSUE#", "");
								sbChildren.append(childComic);
							}
							comicLine = comicLine.replace("#CHILDREN#", sbChildren.toString());
						}
						else {
							comicLine = comicLine.replace("#CHILDREN#", "");
						}
						
						writer.write(comicLine);
					}
				}
				else {
					writer.write(line);
				}
			}
		}
		catch (IOException e) {
			stopSelf();
			return;
		}
		finally {
			if (cursor != null)
				cursor.close();
			try {
				if (writer != null)
					writer.close();
				if (reader != null)
					reader.close();
			}
			catch (IOException e) {}
		}
		
		//Upload to Dropbox
		AppKeyPair appKeys = new AppKeyPair(Application.DROPBOX_KEY, Application.DROPBOX_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys, Application.DROPBOX_ACCESS_TYPE);
		DropboxAPI<AndroidAuthSession> dbApi = new DropboxAPI<AndroidAuthSession>(session);
		dbApi.getSession().setAccessTokenPair(new AccessTokenPair(tokenKey, tokenSecret));
		
		FileInputStream inputStream = null;
		try {
		    inputStream = new FileInputStream(fileOut);
		    dbApi.putFileOverwrite("/index.html", inputStream, fileOut.length(), null);
		} catch (DropboxUnlinkedException e) {
		    // User has unlinked, ask them to link again here.
		} catch (DropboxException e) {
		    Log.e("DbExampleLog", "Something went wrong while uploading.");
		} catch (FileNotFoundException e) {
		    Log.e("DbExampleLog", "File not found.");
		} finally {
		    if (inputStream != null) {
		        try {
		            inputStream.close();
		        } catch (IOException e) {}
		    }
		    stopSelf();
		}		
	}

}
