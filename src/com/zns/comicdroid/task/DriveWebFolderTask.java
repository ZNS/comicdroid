package com.zns.comicdroid.task;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.zns.comicdroid.Application;

public class DriveWebFolderTask extends AsyncTask<com.zns.comicdroid.task.DriveWebFolderTask.DriveWebFolderTaskArg, Void, com.zns.comicdroid.task.DriveWebFolderTask.DriveWebFolderTaskResult> {

	public static class DriveWebFolderTaskArg {
		public GoogleAccountCredential credentials;
		public String webFolderId;
	}

	public static class DriveWebFolderTaskResult {
		public String fileId;
		public boolean success;
		public boolean fileExists = false;
		public Intent intent;
	}

	public DriveWebFolderTask()
	{
	}

	@Override
	protected com.zns.comicdroid.task.DriveWebFolderTask.DriveWebFolderTaskResult doInBackground(com.zns.comicdroid.task.DriveWebFolderTask.DriveWebFolderTaskArg... args) {
		DriveWebFolderTaskResult result = new DriveWebFolderTaskResult();
		try {
			//Try to get token
			args[0].credentials.getToken();
			Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), args[0].credentials).build();

			//Check if folder exists
			File webFolder = null;
			try
			{
				if (args[0].webFolderId != null)
					webFolder = service.files().get(args[0].webFolderId).execute();
			}
			catch (Exception e1) {}

			if (webFolder == null || webFolder.getExplicitlyTrashed() == Boolean.TRUE)
			{
				//Check if comicdroid folder exists
				FileList files = service.files().list().setQ("mimeType = 'application/vnd.google-apps.folder' and title = '" + Application.DRIVE_WEBFOLDER_NAME + "' and trashed = false").execute();
				if (files.getItems().size() == 0) {								
					File body = new File();
					body.setTitle(Application.DRIVE_WEBFOLDER_NAME);
					body.setMimeType("application/vnd.google-apps.folder");
					File file = service.files().insert(body).execute();
					Permission permission = new Permission();
					permission.setValue("");
					permission.setType("anyone");
					permission.setRole("reader");			
					service.permissions().insert(file.getId(), permission).execute();	
					result.fileId = file.getId();
				}
				else {
					result.fileExists = true;
					result.success = false;
				}
			}
			else {
				result.fileId = webFolder.getId();
			}
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