package com.zns.comicdroid;

import com.google.api.services.drive.DriveScopes;


public class Application extends android.app.Application {
	public final static String PREF_DRIVE_ACCOUNT = "DRIVE_ACCOUNT";
	public final static String PREF_DRIVE_AUTHENTICATED = "DRIVE_AUTHENTICATED";
	public final static String PREF_DRIVE_WEBFOLDERID = "DRIVE_WEBFOLDERID";
	public final static String PREF_FIRST_TIME_USE = "PREF_FIRST_TIME_USE";
	public final static String DRIVE_SCOPE = DriveScopes.DRIVE_FILE;
	public final static String DRIVE_WEBFOLDER_NAME = "ComicDroid";
	
}