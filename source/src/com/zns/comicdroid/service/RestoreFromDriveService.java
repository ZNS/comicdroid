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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

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
import com.zns.comicdroid.Application;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.util.BackupUtil;
import com.zns.comicdroid.util.DriveUtil;

import de.greenrobot.event.EventBus;

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
			gFileData = DriveUtil.getFile(service, "appdata", GoogleDriveService.BACKUP_DATA_FILENAME);
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
		else {
			EventBus.getDefault().post(new ProgressResult(100, ""));
		}
		
		stopSelf();
	}
}
