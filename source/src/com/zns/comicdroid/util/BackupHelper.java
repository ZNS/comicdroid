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
package com.zns.comicdroid.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;

import com.zns.comicdroid.Application;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.service.GoogleDriveService;

public class BackupHelper extends BackupAgent {
	private final static String BACKUP_KEY_PREFS = "comicdroid.prefs";
	private final static String BACKUP_KEY_DB = "comicdroid.db";
	private final static String[] PREFS_BLACKLIST = new String[] { 
		Application.PREF_APP_ID, 
		Application.PREF_BACKUP_SUCCESS, 
		Application.PREF_BACKUP_WIFIONLY, 
		Application.PREF_DRIVE_BACKUP, 
		Application.PREF_DRIVE_PUBLISH, 
		Application.PREF_FIRST_TIME_USE 
	};	
	public static WifiLock wifiLock = null;
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		//Wifi check
		if (prefs.getBoolean(Application.PREF_BACKUP_WIFIONLY, false))
		{
			//We only allow backup on wifi connection. Make sure we are connected and if so lock the wifi connection
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifi.isConnected()) {
		        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL , "com.zns.comicdroid.wifilock");
		        wifiLock.setReferenceCounted(true);
		        wifiLock.acquire();
			}
			else {
				return;
			}
		}

		DBHelper db = DBHelper.getHelper(getApplicationContext());
		int dataModifed = db.GetLastModifiedDate();
		boolean performBackup = true;
		
		//Check if backup is necessary
		DataInputStream in = null;
		try
		{
			in = new DataInputStream(new FileInputStream(oldState.getFileDescriptor()));
			int lastBackup = in.readInt();						
			if (lastBackup >= dataModifed)
				performBackup = false;
			}
		catch (Exception x) {
			//Failed to read state, fall through to backup
			x.printStackTrace();
		}
		finally {
			if (in != null)
				in.close();
		}

		//-----------------------Shared Preferences--------------------------	
		ByteArrayOutputStream bufStream = null;
		ObjectOutputStream out = null;		
		try
		{
			bufStream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bufStream);
			
			Map<String,?> keyval = prefs.getAll();

			//Get keys that are to be backed up
			List<String> keys = new ArrayList<String>();
			for (String key : keyval.keySet()) {
				if (!Arrays.asList(PREFS_BLACKLIST).contains(key)) {
					keys.add(key);
				}
			}
			
			//Write key size
			out.writeInt(keys.size());			
			//Backup values
			for (String key : keys) {
				out.writeUTF(key);
				out.writeObject(((Object)keyval.get(key)));
			}

			//Write to backup manager
			byte[] buffer = bufStream.toByteArray();
			int len = buffer.length;
			data.writeEntityHeader(BACKUP_KEY_PREFS, len);
			data.writeEntityData(buffer, len);
		}
		finally {
			if (out != null)
				out.close();
		}		
		
		//-----------------------Database------------------------------------						
		if (performBackup)
		{
			//Get file path
			File outPath = new File(getExternalFilesDir(null).toString() + "/backup");
			//Make sure folder exists
			outPath.mkdirs();
			//Get image path
			String imagePath = getExternalFilesDir(null).toString().replaceAll("/+$", "").concat("/");
			//Create data file
			File fileSql = new File(outPath, "data.dat");

			//Backup data
			int byteCount = BackupUtil.BackupDataToFile(db, fileSql, imagePath);
			
			if (byteCount > 0)
			{
				//Write file to backup manager
				FileInputStream fileStreamData = null;
				try
				{
					data.writeEntityHeader(BACKUP_KEY_DB, byteCount);
					byte[] buffer = new byte[1024];
					fileStreamData = new FileInputStream(fileSql);
					int chunk = 0;
					while (chunk > -1) {
						chunk = fileStreamData.read(buffer);
						if (chunk > -1) {
							data.writeEntityData(buffer, chunk);
						}
					}
				}
				finally {
					fileStreamData.close();
					//Upload to google drive (even if writing to manager fails)
					if (wifiLock != null) {
						//Acquire again for service, will be released by service
						wifiLock.acquire();
					}
					Intent intent = new Intent(getApplicationContext(), GoogleDriveService.class);
					startService(intent);
				}
			}
		}
		
		//Write newstate
		DataOutputStream stateOut = null;
		try
		{
			stateOut = new DataOutputStream(new FileOutputStream(newState.getFileDescriptor()));
			stateOut.writeInt((int)(System.currentTimeMillis() / 1000L));
		}
		finally {
			if (stateOut != null)
				stateOut.close();
		}
		
		if (wifiLock != null) {
			wifiLock.release(); //Release lock for agent
		}
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		
		DBHelper db = DBHelper.getHelper(getApplicationContext());
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		while (data.readNextHeader()) {
			String key = data.getKey();
			int dataSize = data.getDataSize();
		
			//-----------------------Shared Preferences--------------------------
			if (key.equals(BACKUP_KEY_PREFS) && dataSize > 0) {
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				
				ByteArrayInputStream baStream = null;
				ObjectInputStream in = null;			
				try
				{
					baStream = new ByteArrayInputStream(dataBuf);
					in = new ObjectInputStream(baStream);
					BackupUtil.RestoreSharedPrefencesFromStream(in, prefs.edit());
				}
				catch (Exception e) {
					//Failed to restore preferences
					e.printStackTrace();
				}
				finally {
					in.close();
				}
			}
			
			//-------------------------------Database---------------------------------------
			else if (key.equals(BACKUP_KEY_DB) && dataSize > 0) {
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				
				ByteArrayInputStream baStream = null;
				DataInputStream in = null;
				String imagePath = getExternalFilesDir(null).toString().replaceAll("/+$", "").concat("/");		
				try
				{
					baStream = new ByteArrayInputStream(dataBuf);
					in = new DataInputStream(baStream);
					BackupUtil.RestoreDataFromStream(in, db, imagePath, getResources());
				}
				finally {
					in.close();
				}
			}
		}
		
		//Write newstate
		FileOutputStream outstream = null;
		DataOutputStream out = null;
		try
		{
			outstream = new FileOutputStream(newState.getFileDescriptor());
			out = new DataOutputStream(outstream);	
			out.writeInt((int)(System.currentTimeMillis() / 1000L));
		}
		finally {
			if (out != null)
				out.close();
		}		
	}
}
