package com.zns.comicdroid.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class DropboxHandler {
	final static public String DROPBOX_KEY = "85qsb7sjbuyljzu";
	final static public String DROPBOX_SECRET = "btadloh29prr9ts";
	final static public AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	
	private AccessTokenPair accessToken;
	
	public DropboxHandler(AccessTokenPair accessToken) {
		this.accessToken = accessToken;
	}
	
	public void uploadFile(File file)
	{
		new UploadFileTask().execute(file);
	}
	
	private DropboxAPI<AndroidAuthSession> getSession()
	{
		AppKeyPair appKeys = new AppKeyPair(DROPBOX_KEY, DROPBOX_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
		DropboxAPI<AndroidAuthSession> dbApi = new DropboxAPI<AndroidAuthSession>(session);
		dbApi.getSession().setAccessTokenPair(accessToken);
		
		return dbApi;
	}
	
	private class UploadFileTask extends AsyncTask<File, Void, Void> {
		protected Void doInBackground(File... param)
		{
			DropboxAPI<AndroidAuthSession> session = getSession();
			FileInputStream inputStream = null;
			try {
			    inputStream = new FileInputStream(param[0]);
			    session.putFile("/index.html", inputStream, param[0].length(), null, null);
			} catch (DropboxUnlinkedException e) {
			    // User has unlinked, ask them to link again here.
			} catch (DropboxException e) {
			    Log.e("DbExampleLog", "Something went wrong while uploading.");
			} catch (FileNotFoundException e) {
			    Log.e("DbExampleLog", "File not found.");
			} finally {
			    if (inputStream != null) {
			        try {
			            inputStream.close();
			        } catch (IOException e) {}
			    }
			}
			
			return null;
		}	
	}	
}
