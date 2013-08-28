package com.zns.comicdroid.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Locale;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.zns.comicdroid.Application;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

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
			//editor.commit();
		}
		
		stopSelf();
 	}
}
