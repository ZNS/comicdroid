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
package com.zns.comicdroid.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.zns.comicdroid.service.GoogleDriveService;

public class DriveBackupInitTask  extends AsyncTask<com.zns.comicdroid.task.DriveBackupInitTask.DriveBackupInitTaskArg, Void, com.zns.comicdroid.task.DriveBackupInitTask.DriveBackupInitTaskResult> {

	public static class DriveBackupInitTaskArg {
		public GoogleAccountCredential credentials;
		public String appId;
	}

	public static class DriveBackupInitTaskResult {
		public boolean success = true;
		public boolean backupAllowed = true;
		public Intent intent;
	}

	public DriveBackupInitTask()
	{
	}

	@Override
	protected com.zns.comicdroid.task.DriveBackupInitTask.DriveBackupInitTaskResult doInBackground(com.zns.comicdroid.task.DriveBackupInitTask.DriveBackupInitTaskArg... args) {
		DriveBackupInitTaskResult result = new DriveBackupInitTaskResult();
		try {
			//Try to get token
			args[0].credentials.getToken();			
			if (args[0].appId != null) {
				Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), args[0].credentials).build();
				//Get backup meta file
				FileList files = service.files().list().setQ("'appdata' in parents and title = '" + GoogleDriveService.BACKUP_META_FILENAME + "'").execute();
				if (files.getItems().size() > 0)
				{
					HttpResponse response = null;
					BufferedReader reader = null;
					String backupAppId = "";
					try
					{
						com.google.api.services.drive.model.File f = files.getItems().get(0);
						response = service.getRequestFactory().buildGetRequest(new GenericUrl(f.getDownloadUrl())).execute();
						reader = new BufferedReader(new InputStreamReader(response.getContent()));
						backupAppId = reader.readLine();					
					}
					finally {
						if (reader != null)
							reader.close();
						if (response != null)
							response.disconnect();
					}
					
					if (!backupAppId.equals(args[0].appId)) {
						result.success = false;
						result.backupAllowed = false;
					}
				}
			}
		}	
		catch (UserRecoverableAuthException e) {
			result.intent = e.getIntent();
			result.success = false;
		}		
		catch (Exception e) {
			result.success = false;
		}
		return result;
	}
}
