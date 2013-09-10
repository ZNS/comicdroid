package com.zns.comicdroid.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.util.BackupUtil;

public class RestoreFromDriveService extends IntentService {
	
	public RestoreFromDriveService() {
		super("ComicDroid restore service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String account = prefs.getString(Application.PREF_DRIVE_ACCOUNT, null);

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
				if (f.getTitle().toLowerCase(Locale.ENGLISH).equals("data.dat")) {
					gFileData = f;
				}				
			}			
		}
		catch (Exception e) {
			//Failed to get data from google drive
		}

		//-------------------------Restore Data------------------------------------
		if (gFileData != null && gFileData.getDownloadUrl() != null && gFileData.getDownloadUrl().length() > 0)
		{		
			DBHelper db = DBHelper.getHelper(getApplicationContext());
			String imagePath = ((Application)getApplication()).getImagePath(true);
			DataInputStream in = null;
			try
			{
				//Get http response for data file
				HttpResponse response = service.getRequestFactory().buildGetRequest(new GenericUrl(gFileData.getDownloadUrl())).execute();		
				in = new DataInputStream(response.getContent());				
				//Restore data
				BackupUtil.RestoreDataFromStream(in, db, imagePath, getResources());
			}
			catch (Exception e) {
				//Restore failed
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
