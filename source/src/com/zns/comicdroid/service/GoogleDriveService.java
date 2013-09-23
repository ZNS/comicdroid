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
package com.zns.comicdroid.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Update;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.R;
import com.zns.comicdroid.activity.Settings;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.util.BackupHelper;
import com.zns.comicdroid.util.DriveUtil;

import de.greenrobot.event.EventBus;

public class GoogleDriveService extends IntentService {

	public static final String INTENT_PUBLISH_ONLY = "com.zns.comicdroid.PUBLISH_ONLY";
	public static final String BACKUP_META_FILENAME = "backup.meta";
	public static final String BACKUP_DATA_FILENAME = "data.dat";
	public static final String PUBLISH_INDEX_FILENAME = "index.html";	
	private DBHelper mDb;
	
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
		boolean publishOnly = intent.getBooleanExtra(INTENT_PUBLISH_ONLY, false);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	
		//Get database connections
		mDb = DBHelper.getHelper(getApplicationContext());
				
		//Google drive check		
		boolean publishEnabled = prefs.getBoolean(Application.PREF_DRIVE_PUBLISH, false);
		boolean backupEnabled = prefs.getBoolean(Application.PREF_DRIVE_BACKUP, false);
		String account = prefs.getString(Application.PREF_DRIVE_ACCOUNT, null);
		String webFolderId = prefs.getString(Application.PREF_DRIVE_WEBFOLDERID, null);
		String appId = prefs.getString(Application.PREF_APP_ID, "");

		//Backup
		if (backupEnabled && !publishOnly) {
			if (account != null) {
				Backup(account, appId);
			}
			else {
				//We do not seem to have access to appdata.... try to recover
				NotifyAuthentication();
			}			
		}

		//Publish  
		if (publishEnabled || publishOnly) {
			if (account != null && webFolderId != null) {
				PublishComics(account, webFolderId, publishOnly);
			}
			else {
				//Webfolder id does not exist anymore.... try to recover				
				NotifyAuthentication();
			}
		}
		
		stopAndClean();
	}

	private synchronized void Backup(String account, String appId)
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

		//Make sure the current backup is made from the same device
		try {		
			com.google.api.services.drive.model.File f = DriveUtil.getFile(service, "appdata", BACKUP_META_FILENAME);
			if (f != null)
			{
				HttpResponse response = service.getRequestFactory().buildGetRequest(new GenericUrl(f.getDownloadUrl())).execute();
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getContent()));
				String backupAppId = reader.readLine();					
				reader.close();
				response.disconnect();
				
				if (!backupAppId.equals(appId)) {
					return;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		//------------------Upload current backup to google drive---------------------------
		//Get file path
		String outPath = getApplicationContext().getExternalFilesDir(null).toString() + "/backup";		
		File fileData = new File(outPath, BACKUP_DATA_FILENAME);

		//Upload to google drive
		int timeStamp = (int)(System.currentTimeMillis() / 1000L);		
		boolean uploadSuccess = true;
		try 
		{									
			//Get files to update/insert
			com.google.api.services.drive.model.File dataDriveFile = null;
			com.google.api.services.drive.model.File metaDriveFile = null;			
			FileList list = service.files().list().setQ("'appdata' in parents").execute();
			for (com.google.api.services.drive.model.File f : list.getItems()) {
				if (f.getTitle().toLowerCase(Locale.ENGLISH).equals(BACKUP_META_FILENAME)) {
					metaDriveFile = f;
				}
				else if (f.getTitle().toLowerCase(Locale.ENGLISH).equals(BACKUP_DATA_FILENAME)) {
					dataDriveFile = f;
				}
			}

			//Insert meta			
			ByteArrayContent contentMeta = ByteArrayContent.fromString("text/plain", appId + "\n" + Integer.toString(timeStamp));
			com.google.api.services.drive.model.File fMeta = new com.google.api.services.drive.model.File();			
			fMeta.setTitle(BACKUP_META_FILENAME);
			fMeta.setMimeType("text/plain");
			fMeta.setParents(Arrays.asList(new ParentReference().setId("appdata")));
			if (metaDriveFile == null) {
				service.files().insert(fMeta, contentMeta).execute();
			}
			else {
				Update update = service.files().update(metaDriveFile.getId(), null, contentMeta);
				update.setNewRevision(false);
				update.execute();
			}
			
			//Insert/Update data
			FileContent contentData = new FileContent("application/octet-stream", fileData);			
			com.google.api.services.drive.model.File fData = new com.google.api.services.drive.model.File();			
			fData.setTitle(BACKUP_DATA_FILENAME);
			fData.setMimeType("application/octet-stream");
			fData.setParents(Arrays.asList(new ParentReference().setId("appdata")));
			if (dataDriveFile == null) {
				service.files().insert(fData, contentData).execute();
			}
			else {
				Update update = service.files().update(dataDriveFile.getId(), null, contentData);
				update.setNewRevision(false);
				update.execute();
			}				
		}
		catch (Exception e) {
			uploadSuccess = false;
			e.printStackTrace();
		}
		
		//Success?
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor edit = prefs.edit();		
		edit.putBoolean(Application.PREF_BACKUP_SUCCESS, uploadSuccess);
		if (uploadSuccess) {
			edit.putInt(Application.PREF_BACKUP_LAST, timeStamp);
		}
		edit.commit();
	}

	private synchronized void PublishComics(String account, String webFolderId, boolean force)
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
		String outPath = getApplicationContext().getExternalFilesDir(null).toString() + "/html";
		String imagePath = ((Application)getApplication()).getImagePath(true);
		
		//Let's get started
		File fileOut = new File(outPath);
		fileOut.mkdirs();
		fileOut = new File(outPath, PUBLISH_INDEX_FILENAME);		

		BufferedReader reader = null;
		BufferedWriter writer = null;
		Cursor cursor = null;
		
		try 
		{			
			cursor = mDb.getCursor("SELECT _id, Title, Subtitle, Author, Image, ImageUrl, 1 AS ItemType, 0 AS BookCount, IsBorrowed, AddedDate " +
					"FROM tblBooks WHERE GroupId = 0 OR ifnull(GroupId, '') = '' " +
					"UNION " +
					"SELECT _id, Name AS Title, '' AS Subtitle, '' AS Author, (SELECT Image FROM tblBooks where GroupId = tblGroups._id) AS Image, ImageUrl, 2 AS ItemType, BookCount, 0 AS IsBorrowed, 0 AS PublishDate " +
					"FROM tblGroups " +
					"ORDER BY Title", null);
			
			int rowCount = cursor.getCount();
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
			int i = 0;
			while ((line = reader.readLine()) != null)
			{
				if (line.trim().equals("#LISTCOMICS#")) {
					while (cursor.moveToNext()) {
						int id = cursor.getInt(0);
						String title = nullToEmpty(cursor.getString(1));
						String subTitle = nullToEmpty(cursor.getString(2));
						String author = nullToEmpty(cursor.getString(3));
						String image = nullToEmpty(cursor.getString(4));
						String imageUrl = nullToEmpty(cursor.getString(5));
						int type = cursor.getInt(6);
						int date = cursor.getInt(9);
												
						StringBuilder sbChildren = new StringBuilder();

						String comicLine = sbTemplate.toString();
						comicLine = comicLine.replace("#TITLE#", title + (type == 1 && !subTitle.equals("") ? " - " + subTitle : ""));
						comicLine = comicLine.replace("#AUTHOR#", author);
						comicLine = comicLine.replace("#IMAGEURL#", getImageSrc(imageUrl, image, imagePath));
						comicLine = comicLine.replace("#ISSUE#", "");
						comicLine = comicLine.replace("#ISAGGREGATE#", type == 2 ? " Aggregate" : "");
						comicLine = comicLine.replace("#MARK#", type == 2 ? "<div class=\"Mark\"></div>" : "");
						comicLine = comicLine.replace("#DATE#", type == 1 ? Integer.toString(date) : "");

						if (type == 2) {
							//Render group children
							List<Comic> comics = mDb.getComics(id);
							for(Comic comic : comics) {
								String childComic = sbTemplate.toString();
								childComic = childComic.replace("#TITLE#", nullToEmpty(comic.getTitle()) + (!nullToEmpty(comic.getSubTitle()).equals("") ? " - " + comic.getSubTitle() : ""));
								childComic = childComic.replace("#AUTHOR#", nullToEmpty(comic.getAuthor()));
								childComic = childComic.replace("#IMAGEURL#", getImageSrc(nullToEmpty(comic.getImageUrl()), nullToEmpty(comic.getImage()), imagePath));
								childComic = childComic.replace("#ISSUE#", comic.getIssue() > 0 ? Integer.toString(comic.getIssue()) : "");
								childComic = childComic.replace("#DATE#", Integer.toString(comic.getAddedDateTimestamp()));
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
						
						if (force) {
							Double part = (((double)i + 1) / (double)rowCount) * 100.0;
							EventBus.getDefault().post(new ProgressResult(part.intValue(), getString(R.string.progress_publish)));
							i++;							
						}
					}
				}
				else {
					writer.write(line);
				}
			}
		}
		catch (Exception e) {
			//Gotta catch'em all!
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
		if (force) {
			EventBus.getDefault().post(new ProgressResult(20, getString(R.string.progress_publishupload)));
		}
		
		try 
		{						
			//Get current index file
			com.google.api.services.drive.model.File fileIndex = DriveUtil.getFile(service, webFolderId, PUBLISH_INDEX_FILENAME);
			
			//Set content of file
			FileContent content = new FileContent("text/html", fileOut);

			//Insert / Update
			if (fileIndex == null) {
				com.google.api.services.drive.model.File driveFile = new com.google.api.services.drive.model.File();			
				driveFile.setTitle(PUBLISH_INDEX_FILENAME);
				driveFile.setMimeType("text/html");
				driveFile.setParents(Arrays.asList(new ParentReference().setId(webFolderId)));								
				service.files().insert(driveFile, content).execute();
			}
			else {
				Update update = service.files().update(fileIndex.getId(), null, content);
				update.setNewRevision(false);
				update.execute();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if  (force) {
			EventBus.getDefault().post(new ProgressResult(100, getString(R.string.progress_publishupload)));
		}
	}

	@Override
	public void onDestroy() {
		//Just to make sure we release the wifi lock
		if (BackupHelper.wifiLock != null && BackupHelper.wifiLock.isHeld())
			BackupHelper.wifiLock.release();
		super.onDestroy();
	}
	
	private void stopAndClean()
	{
		if (BackupHelper.wifiLock != null)
			BackupHelper.wifiLock.release();
		stopSelf();
	}
	
	private String nullToEmpty(String val)
	{
		if (val == null)
			return "";
		return val;
	}
	
	private String getImageSrc(String imageUrl, String image, String imagePath) {
		String imgSrc = imageUrl;
		if (!image.equals("") && imageUrl.equals(""))
		{
			ByteArrayOutputStream stream = null;
			try
			{
				Bitmap bmp = BitmapFactory.decodeFile(imagePath.concat(image));
				stream = new ByteArrayOutputStream();  
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] b = stream.toByteArray();
				imgSrc = "data:image/jpeg;base64,".concat(Base64.encodeToString(b, Base64.DEFAULT));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try
				{
					stream.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}		
		return imgSrc;
	}
}
