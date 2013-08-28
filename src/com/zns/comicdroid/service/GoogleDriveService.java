package com.zns.comicdroid.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

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
import com.zns.comicdroid.activity.Settings;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.DBHelper;

public class GoogleDriveService extends IntentService {
	
	private NotificationManager notificationManager;
	
	public GoogleDriveService() {
		super("ComicDroid google drive service");
	}

	private void NotifyAuthentication() {
		//Set prefs
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = pref.edit();
		editor.putBoolean(Application.PREF_DRIVE_PUBLISH, false);
		editor.commit();
		
		Intent settingsIntent = new Intent(getApplicationContext(), Settings.class);
		settingsIntent.putExtra(Settings.INTENT_STOP_UPLOAD, true);		
		
		TaskStackBuilder stack = TaskStackBuilder.create(getApplicationContext());
		stack.addParentStack(Settings.class);
		stack.addNextIntent(settingsIntent);
		
		PendingIntent pendingIntent = stack.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
		.setContentTitle(getString(R.string.nUploaderrorheading))
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentText(getString(R.string.nUploaderrorsub))
		.setAutoCancel(true)
		.setContentIntent(pendingIntent);
		
		notificationManager.notify(1, builder.build());
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
	}
 	
 	@Override
	protected void onHandleIntent(Intent intent) {
		
		//Notify backup manager
		BackupManager m = new BackupManager(getApplicationContext());
		m.dataChanged();
		
		//Google drive check		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean publishEnabled = prefs.getBoolean(Application.PREF_DRIVE_PUBLISH, false);
		boolean backupEnabled = prefs.getBoolean(Application.PREF_DRIVE_BACKUP, false);
		String account = prefs.getString(Application.PREF_DRIVE_ACCOUNT, null);
		String webFolderId = prefs.getString(Application.PREF_DRIVE_WEBFOLDERID, null);

		//Publish  
		if (publishEnabled) {
			//Webfolder id does not exist anymore.... try to recover
			if (account != null && webFolderId != null) {
				PublishComics(account, webFolderId);
			}
			else {
				NotifyAuthentication();
			}
		}
		
		//Backup
		if (backupEnabled) {
			if (account != null) {
				Backup(account);
			}
			else {
				NotifyAuthentication();
			}			
		}
		
		stopSelf();
	}

 	private synchronized void Backup(String account)
 	{
		//Get Service
		Drive service = null;
		try
		{
			GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE_BACKUP));
			credential.setSelectedAccountName(account);
			credential.getToken();					
			service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), credential).build();
		}
		catch (UserRecoverableAuthException e) {
			//We are not authenticated for some reason, notify user.
			NotifyAuthentication();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (GoogleAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} 			
		
		//Get file path
		String outPath = getApplicationContext().getExternalFilesDir(null).toString() + "/backup";
		
		//------------------Backup preferences---------------------------
		boolean prefSuccess = true;
		File filePrefs = new File(outPath);
		filePrefs.mkdirs();
		filePrefs = new File(outPath, "prefs.dat");
		
		//Get preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		//Get a stream for writing
		ObjectOutputStream out = null;		
		try
		{
			out = new ObjectOutputStream(new FileOutputStream(filePrefs));			
			Map<String,?> keyval = prefs.getAll();
			out.writeInt(keyval.size());
			for (String key : keyval.keySet()) {
				out.writeUTF(key);
				out.writeObject(((Object)keyval.get(key)));
			}
		}
		catch (IOException e) {
			prefSuccess = false;
			e.printStackTrace();
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (IOException e) {}
			}
		}
		
		//------------------Backup sql---------------------------
		boolean sqlSuccess = true;
		File fileSql = new File(outPath, "data.dat");
		
		//Get database
		DBHelper db = DBHelper.getHelper(getApplicationContext());
		
		//Write data
		DataOutputStream writer = null;		
		try
		{					
			Cursor cb = null;			
			writer = new DataOutputStream(new FileOutputStream(fileSql));
			
			try
			{
				cb = db.getCursor("SELECT _id, GroupId, Title, Subtitle, Publisher, Author, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue, Issues, IsRead, Rating" +
						" FROM tblBooks ORDER BY _id", null);
				int count = cb.getCount();
				writer.writeInt(count);					
				while (cb.moveToNext())
				{
					writer.writeInt(cb.getInt(0));
					writer.writeUTF(String.format("INSERT INTO tblBooks(_id, GroupId, Title, Subtitle, Publisher, Author, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue, Issues, IsRead, Rating)" +
							" VALUES(%d ,%d, %s, %s, %s, %s, %s, %s, %d, %d, %d, %d, %s, %d, %s, %d, %s, %d, %d);", 
							cb.getInt(0),
							cb.getInt(1),
							dbString(cb.getString(2)),
							dbString(cb.getString(3)),
							dbString(cb.getString(4)),
							dbString(cb.getString(5)),
							dbString(cb.getString(6)),
							dbString(cb.getString(7)),
							cb.getInt(8),
							cb.getInt(9),
							cb.getInt(10),
							cb.getInt(11),
							dbString(cb.getString(12)),
							cb.getInt(13),
							dbString(cb.getString(14)),
							cb.getInt(15),
							dbString(cb.getString(16)),
							cb.getInt(17),
							cb.getInt(18)));
					//Image
					String imgPath = cb.getString(6);
					String imgUrl = cb.getString(7);
					if ((imgUrl == null || imgUrl.length() == 0) && (imgPath != null && imgPath.length() > 0))
					{							
						Bitmap bmp = BitmapFactory.decodeFile(imgPath);
						if (bmp != null)
						{
							int size = bmp.getRowBytes() * bmp.getHeight();
							ByteBuffer buffer = ByteBuffer.allocate(size);
							bmp.copyPixelsToBuffer(buffer);
							//Write image size
							writer.writeInt(size);
							//Write image path
							writer.writeUTF(imgPath);
							//Write image data							
							writer.write(buffer.array());
						}
						else {
							writer.writeInt(0);
						}
					}
					else
					{
						writer.writeInt(0);
					}
				}
			}
			finally {
				cb.close();
			}
			
			try
			{
				cb = db.getCursor("SELECT _id, Name, Image, ImageUrl, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete" +
						" FROM tblGroups ORDER BY _id", null);
				writer.writeInt(cb.getCount());
				while (cb.moveToNext())
				{
					writer.writeUTF(String.format("INSERT INTO tblGroups(_id, Name, Image, ImageUrl, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete)" +
							" VALUES(%d, %s, %s, %s, %d, %d, %d, %d, %d);", 
							cb.getInt(0),
							dbString(cb.getString(1)),
							dbString(cb.getString(2)),
							dbString(cb.getString(3)),
							cb.getInt(4),
							cb.getInt(5),
							cb.getInt(6),
							cb.getInt(7),
							cb.getInt(8)));
				}
			}
			finally {
				cb.close();
			}			
		}
		catch (IOException e) {
			sqlSuccess = false;
			e.printStackTrace();
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				} 
				catch (IOException e) {}
			}
		}
		
		//Upload to google drive
		try 
		{						
			//Delete
			ChildList list = service.children().list("appdata").execute();
			for (ChildReference c : list.getItems()) {
				com.google.api.services.drive.model.File f = service.files().get(c.getId()).execute();
				if (prefSuccess && f.getTitle().toLowerCase(Locale.ENGLISH).equals("prefs.dat")) {
					service.files().delete(f.getId()).execute();
				}
				else if (sqlSuccess && f.getTitle().toLowerCase(Locale.ENGLISH).equals("data.dat")) {
					service.files().delete(f.getId()).execute();
				}				
			}
			
			if (prefSuccess)
			{
				//Insert prefs
				FileContent contentPrefs = new FileContent("application/octet-stream", filePrefs);			
				com.google.api.services.drive.model.File fPrefs = new com.google.api.services.drive.model.File();			
				fPrefs.setTitle("prefs.dat");
				fPrefs.setMimeType("application/octet-stream");
				fPrefs.setParents(Arrays.asList(new ParentReference().setId("appdata")));								
				service.files().insert(fPrefs, contentPrefs).execute();			
			}
			
			if (sqlSuccess)
			{
				//Insert data
				FileContent contentData = new FileContent("application/octet-stream", fileSql);			
				com.google.api.services.drive.model.File fData = new com.google.api.services.drive.model.File();			
				fData.setTitle("data.dat");
				fData.setMimeType("application/octet-stream");
				fData.setParents(Arrays.asList(new ParentReference().setId("appdata")));								
				service.files().insert(fData, contentData).execute();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
 	}
 	
 	private synchronized void PublishComics(String account, String webFolderId)
 	{
		//Get Service
		Drive service = null;
		try
		{
			GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE_PUBLISH));
			credential.setSelectedAccountName(account);
			credential.getToken();					
			service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), credential).build();
		}
		catch (UserRecoverableAuthException e) {
			//We are not authenticated for some reason, notify user.
			NotifyAuthentication();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (GoogleAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} 			
		
		//Make sure webfolder still exists
		try	{
			com.google.api.services.drive.model.File webFolder = service.files().get(webFolderId).execute();
			if (webFolder == null || webFolder.getExplicitlyTrashed() == Boolean.TRUE) {
				NotifyAuthentication();
				return;				
			}
		} 
		catch (IOException e) {
			NotifyAuthentication();
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
						String title = nullToEmpty(cursor.getString(1));
						String subTitle = nullToEmpty(cursor.getString(2));
						String author = nullToEmpty(cursor.getString(3));
						String imageUrl = nullToEmpty(cursor.getString(4));
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
								childComic = childComic.replace("#TITLE#", nullToEmpty(comic.getTitle()) + (!nullToEmpty(comic.getSubTitle()).equals("") ? " - " + comic.getSubTitle() : ""));
								childComic = childComic.replace("#AUTHOR#", nullToEmpty(comic.getAuthor()));
								childComic = childComic.replace("#IMAGEURL#", nullToEmpty(comic.getImageUrl()));
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
			//Get current index file
			com.google.api.services.drive.model.File fileIndex = null;
			ChildList list = service.children().list(webFolderId).execute();
			for (ChildReference c : list.getItems()) {
				com.google.api.services.drive.model.File f = service.files().get(c.getId()).execute();
				if (f.getTitle().toLowerCase(Locale.ENGLISH).equals("index.html")) {
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
		catch (Exception e) {
			e.printStackTrace();
		}
 	}
 	
	private String nullToEmpty(String val)
	{
		if (val == null)
			return "";
		return val;
	}
	
	private String dbString(String val) {
		if (val != null) {
			val = val.replaceAll("'", "''");
			return "'" + val + "'";
		}
		return "null";
	}	
}
