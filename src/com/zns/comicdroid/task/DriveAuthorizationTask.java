package com.zns.comicdroid.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

public class DriveAuthorizationTask extends AsyncTask<String, Void, Intent> {

	private Context mContext;
	
	public DriveAuthorizationTask(Context context)
	{
		mContext = context;
	}
	
	@Override
	protected Intent doInBackground(String... arg0) {
		Intent intent = null;
		try {
			GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, DriveScopes.DRIVE_FILE);
			credential.setSelectedAccountName(arg0[0]);
			// Trying to get a token right away to see if we are authorized
			credential.getToken();
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
