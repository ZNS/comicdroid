package com.zns.comicdroid.task;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class DriveAuthorizationTask extends AsyncTask<GoogleAccountCredential, Void, Intent> {
	
	public DriveAuthorizationTask()
	{
	}
	
	@Override
	protected Intent doInBackground(GoogleAccountCredential... arg0) {
		Intent intent = null;
		try {
			arg0[0].getToken();
		}
		catch (UserRecoverableAuthException e) {
			UserRecoverableAuthException exception = (UserRecoverableAuthException) e;			
			intent = exception.getIntent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_FROM_BACKGROUND);			
		}		
		catch (Exception e) {}
		return intent;
	}
}
