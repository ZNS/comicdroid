package com.zns.comicdroid.service;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.common.base.Joiner;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.util.ImageHandler;

public class RestoreFromDriveService extends IntentService {
	
	public RestoreFromDriveService() {
		super("ComicDroid restore service");
	}
	
 	@Override
	protected void onHandleIntent(Intent intent) {
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		String account = prefs.getString(Application.PREF_DRIVE_ACCOUNT, null);

		com.google.api.services.drive.model.File gFilePrefs = null;
		com.google.api.services.drive.model.File gFileData = null;
 		
		//Get Service
		Drive service = null;
		try
		{
			GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE_BACKUP));
			credential.setSelectedAccountName(account);
			credential.getToken();					
			service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), credential).build();
			
			ChildList list = service.children().list("appdata").execute();
			for (ChildReference c : list.getItems()) {
				com.google.api.services.drive.model.File f = service.files().get(c.getId()).execute();
				if (f.getTitle().toLowerCase(Locale.ENGLISH).equals("prefs.dat")) {
					gFilePrefs = f;
				}
				else if (f.getTitle().toLowerCase(Locale.ENGLISH).equals("data.dat")) {
					gFileData = f;
				}				
			}			
		}
		catch (Exception e) {}
				
		//Restore preferences
		if (gFilePrefs != null && gFilePrefs.getDownloadUrl() != null && gFilePrefs.getDownloadUrl().length() > 0)
		{		
			ObjectInputStream input = null;
			Editor editor = prefs.edit();
			try
			{
				HttpResponse response = service.getRequestFactory().buildGetRequest(new GenericUrl(gFilePrefs.getDownloadUrl())).execute();
				input = new ObjectInputStream(response.getContent());
				int size = input.readInt();
				for (int i = 0; i < size; i++) {
					String key = input.readUTF();
					Object val = input.readObject();
                if (val instanceof Boolean)
                    editor.putBoolean(key, ((Boolean) val).booleanValue());
                else if (val instanceof Float)
                	editor.putFloat(key, ((Float) val).floatValue());
                else if (val instanceof Integer)
                	editor.putInt(key, ((Integer) val).intValue());
                else if (val instanceof Long)
                	editor.putLong(key, ((Long) val).longValue());
                else if (val instanceof String)
                	editor.putString(key, ((String) val));
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			finally {
				try {
					input.close();
				} 
				catch (IOException e) {}
			}
			editor.commit();
		}
		
		//-------------------------Restore Data------------------------------------
		if (gFileData != null && gFileData.getDownloadUrl() != null && gFileData.getDownloadUrl().length() > 0)
		{		
			DBHelper db = DBHelper.getHelper(getApplicationContext());
			String imageDirectory = getExternalFilesDir(null).toString();
			
			DataInputStream in = null;		
			try
			{
				HttpResponse response = service.getRequestFactory().buildGetRequest(new GenericUrl(gFileData.getDownloadUrl())).execute();		
				in = new DataInputStream(response.getContent());
				 
				//Comics
				List<Integer> addedComics = new ArrayList<Integer>();
				int rows = in.readInt();
				for (int i = 0; i < rows; i++)
				{
					int id = in.readInt();
					String sql = in.readUTF();
					if (sql != null && !sql.equals("")) {
						try {						
							db.execSQL(sql);
							addedComics.add(id);							
						}
						catch (Exception x) {
							//Failed to restore comic...
						}
					}
					
					//Restore local images, read image size
					int imgSize = in.readInt();
					if (imgSize > 0)
					{
						//Read image path
						String fileName = in.readUTF();
						//Read image data
						byte[] imgData = new byte[imgSize];
						try {
							in.readFully(imgData, 0, imgSize);
							Bitmap bmp = BitmapFactory.decodeByteArray(imgData, 0, imgSize);
							if (bmp != null)
							{
								String filePath = ImageHandler.storeImage(bmp, imageDirectory, fileName, 100);
								ContentValues val = new ContentValues();
								val.put("Image", filePath);
								db.update("tblBooks", val, "_id=?", new String[] { Integer.toString(id) });
							}							
						}
						catch (IOException x) {
							x.printStackTrace();
						}
					}
				}
				
				//Groups
				rows = in.readInt();
				for (int i = 0; i < rows; i++)
				{
					String sql = in.readUTF();
					if (sql != null && !sql.equals("")) {
						try {
							db.execSQL(sql);
						}
						catch (Exception x) {
							//Failed to restore group...
						}
					}
				}
				
				//Fix images with urls
				Cursor cb = null;				
				try 
				{
					String ids = Joiner.on(',').join(addedComics);
					cb = db.getCursor("SELECT _id, Image, ImageUrl FROM tblBooks WHERE _id IN (" + ids  + ") AND ImageUrl <> ''", null);
					while (cb.moveToNext()) {
						String filePath = cb.getString(1);
						if (filePath.length() > 0) {
							File file = new File(filePath);
							if (file.exists())
								continue;
						}
						String url = cb.getString(2);
						if (url.length() > 0) {
							try
							{
								filePath = ImageHandler.storeImage(new URL(url), imageDirectory);
								ContentValues val = new ContentValues();
								val.put("Image", filePath);
								db.update("tblBooks", val, "_id=?", new String[] { Integer.toString(cb.getInt(0)) });
							}
							catch (Exception x)
							{
								//Unable to save image to disk
								x.printStackTrace();
							}
						}
					}
				}
				finally
				{
					cb.close();
				}
				
				//Fix group images
				db.execSQL("UPDATE tblGroups SET Image = (SELECT Image FROM tblBooks WHERE GroupId = tblGroups._id AND Issue = 1 LIMIT 1)");
				
			}
			catch (Exception e) {				
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (IOException e) {}
				}
			}
		}
 	
		stopSelf();
 	}
}
