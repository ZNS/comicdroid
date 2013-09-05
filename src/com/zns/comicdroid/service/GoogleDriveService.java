package com.zns.comicdroid.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

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
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Revision;
import com.google.api.services.drive.model.RevisionList;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.R;
import com.zns.comicdroid.activity.Settings;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.DBHelper;

public class GoogleDriveService extends IntentService {

	public static final String INTENT_PUBLISH_ONLY = "com.zns.comicdroid.PUBLISH_ONLY";
	public static final String BACKUP_META_FILENAME = "backup.meta";
	private DBHelper mDb;
	private WifiLock mWifiLock = null;
	
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
		boolean lastBackupSuccess = prefs.getBoolean(Application.PREF_BACKUP_SUCCESS, false);
	
		//Wifi?
		if (prefs.getBoolean(Application.PREF_BACKUP_WIFIONLY, false))
		{
			//We only allow backup on wifi connection. Make sure we are connected and if so lock the wifi connection
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifi.isConnected()) {
		        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "com.zns.comicdroid.wifilock");
		        mWifiLock.acquire();				
			}
			else {
				stopAndClean();
				return;
			}
		}

		//If publishOnly is true then we do a publish no matter if it's needed or not
		if (!publishOnly)
		{		
			//Has data changed and was last backup successful?
			mDb = DBHelper.getHelper(getApplicationContext());
			int lastModified = mDb.GetLastModifiedDate();
			File metaFile = new File(getFilesDir(), BACKUP_META_FILENAME);
			DataInputStream data = null;
			try
			{
				data = new DataInputStream(new FileInputStream(metaFile));
				int lastBackupRestore = data.readInt();
				if (lastBackupRestore >= lastModified && lastBackupSuccess) {
					//Data has not been changed since last backup/restore, and last backup was successful. Stop service and return.
					stopAndClean();
					return;
				}
			}
			catch (Exception e) {
				//Unable to read meta data, continue with backup
				e.printStackTrace();
			}
			finally {
				try {
					if (data != null)
						data.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
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
				PublishComics(account, webFolderId);
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
			FileList files = service.files().list().setQ("'appdata' in parents and title = '" + BACKUP_META_FILENAME + "'").execute();
			if (files.getItems().size() > 0)
			{
				com.google.api.services.drive.model.File f = files.getItems().get(0);
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
		
		//Get file path
		String outPath = getApplicationContext().getExternalFilesDir(null).toString() + "/backup";
		//Get image path
		String imagePath = ((Application)getApplication()).getImagePath(true);

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

		//Write data
		DataOutputStream writer = null;		
		try //Destroy writer
		{					
			Cursor cb = null;			
			writer = new DataOutputStream(new FileOutputStream(fileSql));

			try //Destroy cursor
			{
				cb = mDb.getCursor("SELECT _id, GroupId, Title, Subtitle, Publisher, Author, Illustrator, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue, Issues, IsRead, Rating" +
						" FROM tblBooks ORDER BY _id", null);
				int count = cb.getCount();
				writer.writeInt(count);					
				while (cb.moveToNext())
				{
					try //This is just to make sure one failed insert doesn't abort everything
					{
						writer.writeInt(cb.getInt(0));
						writer.writeUTF(String.format("INSERT OR REPLACE INTO tblBooks(_id, GroupId, Title, Subtitle, Publisher, Author, Illustrator, Image, ImageUrl, PublishDate, AddedDate, PageCount, IsBorrowed, Borrower, BorrowedDate, ISBN, Issue, Issues, IsRead, Rating)" +
								" VALUES(%d ,%d, %s, %s, %s, %s, %s, %s, %s, %d, %d, %d, %d, %s, %d, %s, %d, %s, %d, %d);", 
								cb.getInt(0),
								cb.getInt(1),
								dbString(cb.getString(2)),
								dbString(cb.getString(3)),
								dbString(cb.getString(4)),
								dbString(cb.getString(5)),
								dbString(cb.getString(6)),
								dbString(cb.getString(7)),
								dbString(cb.getString(8)),
								cb.getInt(9),
								cb.getInt(10),
								cb.getInt(11),
								cb.getInt(12),
								dbString(cb.getString(13)),
								cb.getInt(14),
								dbString(cb.getString(15)),
								cb.getInt(16),
								dbString(cb.getString(17)),
								cb.getInt(18),
								cb.getInt(19)));
						//Image
						String fileName = cb.getString(6);
						String imgUrl = cb.getString(7);
						if ((imgUrl == null || imgUrl.length() == 0) && (fileName != null && fileName.length() > 0))
						{							
							Bitmap bmp = BitmapFactory.decodeFile(imagePath.concat(fileName));
							if (bmp != null)
							{
								ByteArrayOutputStream stream = new ByteArrayOutputStream();
								bmp.compress(CompressFormat.JPEG, 100, stream);
								byte[] data = stream.toByteArray();
								//Write image size
								writer.writeInt(data.length);
								//Write file name
								writer.writeUTF(fileName);
								//Write image data							
								writer.write(data);
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
					catch (Exception e) {
						sqlSuccess = false; //At least one comic failed
						e.printStackTrace();
					}
				}
			}
			finally {
				cb.close();
			}

			try
			{
				cb = mDb.getCursor("SELECT _id, Name, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete" +
						" FROM tblGroups ORDER BY _id", null);
				writer.writeInt(cb.getCount());
				while (cb.moveToNext())
				{
					writer.writeUTF(String.format("INSERT OR REPLACE INTO tblGroups(_id, Name, BookCount, TotalBookCount, IsWatched, IsFinished, IsComplete)" +
							" VALUES(%d, %s, %d, %d, %d, %d, %d);", 
							cb.getInt(0),
							dbString(cb.getString(1)),
							cb.getInt(2),
							cb.getInt(3),
							cb.getInt(4),
							cb.getInt(5),
							cb.getInt(6)));
				}
			}
			finally {
				cb.close();
			}			
		}
		catch (Exception e) {
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
		int timeStamp = (int)(System.currentTimeMillis() / 1000L);		
		boolean uploadSuccess = true;
		try 
		{						
			com.google.api.services.drive.model.File prefsDriveFile = null;
			com.google.api.services.drive.model.File dataDriveFile = null;
			
			//Manage revisions
			FileList list = service.files().list().setQ("'appdata' in parents").execute();
			for (com.google.api.services.drive.model.File f : list.getItems()) {
				if (f.getTitle().toLowerCase(Locale.ENGLISH).equals(BACKUP_META_FILENAME)) {
					service.files().delete(f.getId()).execute();
				}
				else if (prefSuccess && f.getTitle().toLowerCase(Locale.ENGLISH).equals("prefs_" + appId + ".dat")) {
					prefsDriveFile = f;
					trimDriveFileRevisions(service, f.getId(), 1);
				}
				else if (sqlSuccess && f.getTitle().toLowerCase(Locale.ENGLISH).equals("data.dat")) {
					dataDriveFile = f;
					trimDriveFileRevisions(service, f.getId(), 1);
				}
			}

			//Insert meta			
			ByteArrayContent contentMeta = ByteArrayContent.fromString("text/plain", appId + "\n" + Integer.toString(timeStamp));
			com.google.api.services.drive.model.File fMeta = new com.google.api.services.drive.model.File();			
			fMeta.setTitle(BACKUP_META_FILENAME);
			fMeta.setMimeType("text/plain");
			fMeta.setParents(Arrays.asList(new ParentReference().setId("appdata")));
			service.files().insert(fMeta, contentMeta).execute();
			
			if (prefSuccess)
			{
				//Insert/Update prefs
				FileContent contentPrefs = new FileContent("application/octet-stream", filePrefs);			
				com.google.api.services.drive.model.File fPrefs = new com.google.api.services.drive.model.File();			
				fPrefs.setTitle("prefs_" + appId + ".dat");
				fPrefs.setMimeType("application/octet-stream");
				fPrefs.setParents(Arrays.asList(new ParentReference().setId("appdata")));
				if (prefsDriveFile == null) {
					service.files().insert(fPrefs, contentPrefs).execute();
				}
				else {
					service.files().update(prefsDriveFile.getId(), prefsDriveFile, contentPrefs).execute();
				}
			}

			if (sqlSuccess)
			{
				//Insert/Update data
				FileContent contentData = new FileContent("application/octet-stream", fileSql);			
				com.google.api.services.drive.model.File fData = new com.google.api.services.drive.model.File();			
				fData.setTitle("data.dat");
				fData.setMimeType("application/octet-stream");
				fData.setParents(Arrays.asList(new ParentReference().setId("appdata")));
				if (dataDriveFile == null) {
					service.files().insert(fData, contentData).execute();
				}
				else {
					service.files().update(dataDriveFile.getId(), dataDriveFile, contentData).execute();
				}				
			}
		}
		catch (Exception e) {
			uploadSuccess = false;
			e.printStackTrace();
		}
		
		//Write meta
		File metaFile = new File(getFilesDir(), BACKUP_META_FILENAME);
		DataOutputStream stream = null;
		try
		{
			stream = new DataOutputStream(new FileOutputStream(metaFile));
			stream.writeInt(timeStamp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (stream != null)
					stream.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Success?
		Editor edit = prefs.edit();
		edit.putBoolean(Application.PREF_BACKUP_SUCCESS, (prefSuccess && sqlSuccess && uploadSuccess));
		edit.commit();
		
		//Clean up
		try
		{
			fileSql.delete();
			filePrefs.delete();
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
		String outPath = getApplicationContext().getExternalFilesDir(null).toString() + "/html";

		//Let's get started
		File fileOut = new File(outPath);
		fileOut.mkdirs();
		fileOut = new File(outPath, "index.html");		

		BufferedReader reader = null;
		BufferedWriter writer = null;
		Cursor cursor = null;
		
		try 
		{			
			cursor = mDb.getCursor("SELECT _id, Title, Subtitle, Author, ImageUrl, 1 AS ItemType, 0 AS BookCount, IsBorrowed, PublishDate " +
					"FROM tblBooks WHERE GroupId = 0 OR ifnull(GroupId, '') = '' " +
					"UNION " +
					"SELECT _id, Name AS Title, '' AS Subtitle, '' AS Author, ImageUrl, 2 AS ItemType, BookCount, 0 AS IsBorrowed, 0 AS PublishDate " +
					"FROM tblGroups " +
					"ORDER BY Title", null);
			
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
							List<Comic> comics = mDb.getComics(id);
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
		try 
		{						
			//Get current index file
			com.google.api.services.drive.model.File fileIndex = null;
			FileList list = service.files().list().setQ("'" + webFolderId + "' in parents and title = 'index.html'").execute();
			if (list.getItems().size() > 0)
			{
				fileIndex = list.getItems().get(0);
				trimDriveFileRevisions(service, fileIndex.getId(), 1);
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

	@Override
	public void onDestroy() {
		//Just to make sure we release the wifi lock
		if (mWifiLock != null)
			mWifiLock.release();		
		super.onDestroy();
	}
	
	private void stopAndClean()
	{
		if (mWifiLock != null)
			mWifiLock.release();
		stopSelf();
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
	
	private void trimDriveFileRevisions(Drive service, String fileId, int revisionCount) throws IOException {
		RevisionList revisions = service.revisions().list(fileId).execute();
		if (revisions.getItems().size() > revisionCount)
		{
			List<Revision> items = revisions.getItems();
			Collections.sort(items, new RevisionsByDateComparer());
			for (Revision rev : items.subList(revisionCount, items.size())) {
				service.revisions().delete(fileId, rev.getId()).execute();
			}
		}
	}
}
