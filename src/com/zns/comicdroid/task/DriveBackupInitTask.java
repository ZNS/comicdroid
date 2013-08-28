package com.zns.comicdroid.task;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

public class DriveBackupInitTask  extends AsyncTask<com.zns.comicdroid.task.DriveBackupInitTask.DriveBackupInitTaskArg, Void, com.zns.comicdroid.task.DriveBackupInitTask.DriveBackupInitTaskResult> {

	public static class DriveBackupInitTaskArg {
		public GoogleAccountCredential credentials;
	}
	
	public static class DriveBackupInitTaskResult {
		public boolean success;
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
			Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), args[0].credentials).build();
			//Try to get appdata folder 
			service.files().get("appdata").execute();
			result.success = true;
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
