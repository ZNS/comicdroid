package com.zns.comicdroid.util;

import java.io.BufferedInputStream;
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
import java.util.Arrays;
import java.util.Map;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.Intent;
import android.content.SharedPreferences;
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
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		ByteArrayOutputStream bufStream = null;
		ObjectOutputStream out = null;		
		try
		{
			bufStream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bufStream);
			
			Map<String,?> keyval = prefs.getAll();
			out.writeInt(keyval.size());
			for (String key : keyval.keySet()) {
				if (!Arrays.asList(PREFS_BLACKLIST).contains(key)) {
					out.writeUTF(key);
					out.writeObject(((Object)keyval.get(key)));
				}
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
			String outPath = getExternalFilesDir(null).toString() + "/backup";
			//Get image path
			String imagePath = getExternalFilesDir(null).toString().replaceAll("/+$", "").concat("/");
			//Create data file
			File fileSql = new File(outPath, "data.dat");

			//Backup data
			int byteCount = BackupUtil.BackupDataToFile(db, fileSql, imagePath);
			
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
				Intent intent = new Intent(getApplicationContext(), GoogleDriveService.class);
				startService(intent);							
			}				
		}
		
		//Write newstate
		DataOutputStream stateOut = null;
		try
		{
			stateOut = new DataOutputStream(new FileOutputStream(newState.getFileDescriptor()));
			stateOut.writeInt(dataModifed);
		}
		finally {
			if (stateOut != null)
				stateOut.close();
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