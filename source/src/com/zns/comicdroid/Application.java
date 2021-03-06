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
package com.zns.comicdroid;

import java.util.UUID;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.api.services.drive.DriveScopes;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zns.comicdroid.amazon.AmazonCacheTask;
import com.zns.comicdroid.task.BackupCheckTask;
import com.zns.comicdroid.util.Logger;

public class Application extends android.app.Application {
	public final static String PREF_DRIVE_ACCOUNT = "DRIVE_ACCOUNT";
	public final static String PREF_DRIVE_PUBLISH = "DRIVE_PUBLISH";
	public final static String PREF_DRIVE_WEBFOLDERID = "DRIVE_WEBFOLDERID";
	public final static String PREF_DRIVE_BACKUP = "DRIVE_BACKUP";
	public final static String PREF_FIRST_TIME_USE = "PREF_FIRST_TIME_USE";
	public final static String PREF_APP_ID = "PREF_APP_ID";
	public final static String PREF_BACKUP_SUCCESS = "PREF_BACKUP_SUCCESS";
	public final static String PREF_BACKUP_WIFIONLY = "PREF_BACKUP_WIFIONLY";
	public final static String PREF_BACKUP_LAST = "PREF_BACKUP_LAST";
	public final static String DRIVE_SCOPE_PUBLISH = DriveScopes.DRIVE_FILE;
	public final static String DRIVE_SCOPE_BACKUP = DriveScopes.DRIVE_APPDATA;
	public final static String DRIVE_WEBFOLDER_NAME = "ComicDroid";
	public final static int CACHE_AMAZONSEARCH_HOURS = 4;
	public final static boolean DEBUG = false;	
	
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
		
		//Create unique id for this installation
		String appId = prefs.getString(PREF_APP_ID, null);
		if (appId == null) {
			prefsEdit = prefs.edit();
			appId = UUID.randomUUID().toString();
			prefsEdit.putString(PREF_APP_ID, appId);		
		}
		
		//Check first user
		isFirstUse = prefs.getBoolean(PREF_FIRST_TIME_USE, true);
		if (isFirstUse)
		{
			if (prefsEdit == null)
				prefsEdit = prefs.edit();
			prefsEdit.putBoolean(PREF_FIRST_TIME_USE, false);
		}
		
		if (prefsEdit != null)
			prefsEdit.commit();

		//Set up image loader
		ImageLoaderConfiguration imgConfig = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.defaultDisplayImageOptions(new DisplayImageOptions.Builder()
					.cacheInMemory(true)
					.showImageForEmptyUri(R.drawable.image_placeholder)
					.showImageOnFail(R.drawable.image_placeholder)
					.build())
			.threadPoolSize(3)
			.build();
		ImageLoader.getInstance().init(imgConfig);
		
		//Clean cache
		new AmazonCacheTask().execute(getExternalFilesDir(null).toString() + "/amazoncache");
		
		//Backup check
		if (prefs.getBoolean(PREF_DRIVE_BACKUP, false)) {
			int lastBackup = prefs.getInt(PREF_BACKUP_LAST, -1);
			//Fire and forget
			new BackupCheckTask(getApplicationContext()).execute(lastBackup);
		}
		
		Logger log = new Logger(getExternalFilesDir(null).toString() + "/log");
		log.appendLog("Starting comicdroid", "LIFECYCLE");
		
		super.onCreate();
	}
}
