package com.zns.comicdroid;

import java.util.UUID;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.api.services.drive.DriveScopes;

public class Application extends android.app.Application {
	public final static String PREF_DRIVE_ACCOUNT = "DRIVE_ACCOUNT";
	public final static String PREF_DRIVE_PUBLISH = "DRIVE_PUBLISH";
	public final static String PREF_DRIVE_WEBFOLDERID = "DRIVE_WEBFOLDERID";
	public final static String PREF_DRIVE_BACKUP = "DRIVE_BACKUP";
	public final static String PREF_FIRST_TIME_USE = "PREF_FIRST_TIME_USE";
	public final static String PREF_APP_ID = "PREF_APP_ID";
	public final static String DRIVE_SCOPE_PUBLISH = DriveScopes.DRIVE_FILE;
	public final static String DRIVE_SCOPE_BACKUP = DriveScopes.DRIVE_APPDATA;
	public final static String DRIVE_WEBFOLDER_NAME = "ComicDroid";
	public boolean isFirstUse;
	private String mImagePath = null;
	
	public String getImagePath(boolean appendSlash) {
		String path = getImagePath(null);
		if (appendSlash) {
			path = path.replaceAll("/+$", "").concat("/");
		}
		return path;
	}
	
	public String getImagePath(String imageName) {
		if (mImagePath == null) {
			mImagePath = getExternalFilesDir(null).toString();
		}
		if (imageName != null) {
			return mImagePath.concat("/").concat(imageName);
		}
		return mImagePath;
	}
	
	@Override
	public void onCreate() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor prefsEdit = null;
		
		String appId = prefs.getString(PREF_APP_ID, null);
		if (appId == null) {
			prefsEdit = prefs.edit();
			appId = UUID.randomUUID().toString();
			prefsEdit.putString(PREF_APP_ID, appId);		
		}
		
		isFirstUse = prefs.getBoolean(PREF_FIRST_TIME_USE, true);
		if (isFirstUse)
		{
			if (prefsEdit == null)
				prefsEdit = prefs.edit();
			prefsEdit.putBoolean(PREF_FIRST_TIME_USE, false);
		}
		
		if (prefsEdit != null)
			prefsEdit.commit();
		
		super.onCreate();
	}
}