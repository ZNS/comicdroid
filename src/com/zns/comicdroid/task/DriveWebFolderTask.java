package com.zns.comicdroid.task;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.zns.comicdroid.Application;

public class DriveWebFolderTask extends AsyncTask<GoogleAccountCredential, Void, String> {

	public DriveWebFolderTask()
	{
	}
	
	@Override
	protected String doInBackground(GoogleAccountCredential... arg0) {
		String id = "";
		try {
			Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), arg0[0]).build();			
			File body = new File();
			body.setTitle(Application.DRIVE_WEBFOLDER_NAME);
			body.setMimeType("application/vnd.google-apps.folder");			
			File file = service.files().insert(body).execute();
			Permission permission = new Permission();
			permission.setValue("");
			permission.setType("anyone");
			permission.setRole("reader");			
			service.permissions().insert(file.getId(), permission).execute();	
			id = file.getId();
		}	
		catch (Exception e) {}
		return id;
	}
}
