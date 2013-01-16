package com.zns.comicdroid.task;

import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class DriveUnauthorizeTask extends AsyncTask<GoogleAccountCredential, Void, Boolean> {
	
	public DriveUnauthorizeTask()
	{
	}
	
	@Override
	protected Boolean doInBackground(GoogleAccountCredential... arg0) {
		try {
			String token = arg0[0].getToken();
			GoogleAuthUtil.invalidateToken(arg0[0].getContext(), token);
			return true;
		}	
		catch (Exception e) {
			return false;
		}
	}
}
