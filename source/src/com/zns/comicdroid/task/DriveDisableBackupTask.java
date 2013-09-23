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

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.zns.comicdroid.service.GoogleDriveService;
import com.zns.comicdroid.util.DriveUtil;

public class DriveDisableBackupTask  extends AsyncTask<GoogleAccountCredential, Void, Boolean> {

	public DriveDisableBackupTask()
	{
	}

	@Override
	protected Boolean doInBackground(GoogleAccountCredential... args) {
		try {
			//Try to get token
			args[0].getToken();			
			Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), args[0]).build();
			//Get backup meta file
			File f = DriveUtil.getFile(service, "appdata", GoogleDriveService.BACKUP_META_FILENAME);
			if (f != null)
			{
				service.files().delete(f.getId()).execute();
			}
		}	
		catch (Exception e) {
			return false;
		}
		return true;
	}
}
