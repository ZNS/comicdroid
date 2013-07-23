package com.zns.comicdroid.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import android.app.IntentService;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.ParentReference;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.DBHelper;

public class UploadService extends IntentService {
	
	public UploadService() {
		super("ComicDroid Upload Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		//Notify backup manager
		BackupManager m = new BackupManager(getApplicationContext());
		m.dataChanged();
		
		//Google drive check		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean driveAuthenticated = prefs.getBoolean(Application.PREF_DRIVE_AUTHENTICATED, false);
		String account = prefs.getString(Application.PREF_DRIVE_ACCOUNT, null);
		String webFolderId = prefs.getString(Application.PREF_DRIVE_WEBFOLDERID, null);

		//If user has not authenticated with google drive, stop this now 
		if (!driveAuthenticated) {
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
		Cursor cursor = db.getCursor("SELECT _id, Title, Subtitle, Author, ImageUrl, 1 AS ItemType, 0 AS BookCount, IsBorrowed, PublishDate " +
				"FROM tblBooks WHERE GroupId = 0 OR ifnull(GroupId, '') = '' " +
				"UNION " +
				"SELECT _id, Name AS Title, '' AS Subtitle, '' AS Author, ImageUrl, 2 AS ItemType, BookCount, 0 AS IsBorrowed, 0 AS PublishDate " +
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
						int date = cursor.getInt(8);
						StringBuilder sbChildren = new StringBuilder();
						
						String comicLine = sbTemplate.toString();
						comicLine = comicLine.replace("#TITLE#", title + (type == 1 && !subTitle.equals("") ? " - " + subTitle : ""));
						comicLine = comicLine.replace("#AUTHOR#", author);
						comicLine = comicLine.replace("#IMAGEURL#", imageUrl);
						comicLine = comicLine.replace("#ISSUE#", "");
						comicLine = comicLine.replace("#ISAGGREGATE#", type == 2 ? " Aggregate" : "");
						comicLine = comicLine.replace("#MARK#", type == 2 ? "<div class=\"Mark\"></div>" : "");
						comicLine = comicLine.replace("#DATE#", type == 1 ? Integer.toString(date) : "");
						
						if (type == 2) {
							//Render group children
							List<Comic> comics = db.getComics(id);
							for(Comic comic : comics) {
								String childComic = sbTemplate.toString();
								childComic = childComic.replace("#TITLE#", comic.getTitle() + (!comic.getSubTitle().equals("") ? " - " + comic.getSubTitle() : ""));
								childComic = childComic.replace("#AUTHOR#", comic.getAuthor());
								childComic = childComic.replace("#IMAGEURL#", comic.getImageUrl());
								childComic = childComic.replace("#ISSUE#", comic.getIssue() > 0 ? Integer.toString(comic.getIssue()) : "");
								childComic = childComic.replace("#DATE#", Integer.toString(comic.getPublishDateTimestamp()));
								childComic = childComic.replace("#ISAGGREGATE#", "");
								childComic = childComic.replace("#MARK#", "");
								childComic = childComic.replace("#CHILDREN#", "");
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
		
		//Upload to google drive
		try 
		{				
			GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE));
			credential.setSelectedAccountName(account);
			credential.getToken();			
			
			Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), credential).build();
			
			//Get current index file
			com.google.api.services.drive.model.File fileIndex = null;
			ChildList list = service.children().list(webFolderId).execute();
			for (ChildReference c : list.getItems()) {
				com.google.api.services.drive.model.File f = service.files().get(c.getId()).execute();
				if (f.getTitle().toLowerCase().equals("index.html")) {
					fileIndex = f;
					break;
				}
			}
			
			//Set content of file
			FileContent content = new FileContent("text/html", fileOut);
								
			//Insert / Update
			if (fileIndex == null) {
				com.google.api.services.drive.model.File driveFile = new com.google.api.services.drive.model.File();			
				driveFile.setTitle("index.html");
				driveFile.setMimeType("text/html");
				driveFile.setParents(Arrays.asList(new ParentReference().setId(webFolderId)));								
				service.files().insert(driveFile, content).execute();
			}
			else {
				service.files().update(fileIndex.getId(), fileIndex, content).execute();
			}
		}
		catch (UserRecoverableAuthException e) {
			//We are not authenticated for some reason, notify user.
			//NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GoogleAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			stopSelf();
		}
	}

}
