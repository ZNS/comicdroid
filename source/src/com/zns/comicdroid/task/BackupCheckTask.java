package com.zns.comicdroid.task;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import com.zns.comicdroid.Application;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.service.GoogleDriveService;
import com.zns.comicdroid.util.BackupUtil;
import com.zns.comicdroid.util.Logger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class BackupCheckTask extends AsyncTask<Integer, Void, Void> {
	private final WeakReference<Context> mContext;
	
	public BackupCheckTask(Context appContext)  {
		mContext = new WeakReference<Context>(appContext);
	}
	
	@Override
	protected Void doInBackground(Integer... arg0) {
		int timestamp = arg0[0];

		//Add a week to last date
		Date dateLast = new Date((long)timestamp * 1000L);
		Calendar c = Calendar.getInstance();
		c.setTime(dateLast);
		c.add(Calendar.DATE, 3);
		
		Date now = new Date();
		
		//If todays date is later than last date + a week, check if backup should have been done
		if (timestamp < 0 || now.after(c.getTime())) {
			Context context = mContext.get();			
			if (context != null) {
				Logger log = new Logger(context.getExternalFilesDir(null).toString() + "/log");
				int lastModStamp = DBHelper.getHelper(context).GetLastModifiedDate();
				if (lastModStamp > timestamp) {
					log.appendLog("Backup check task is running", Logger.TAG_BACKUP);
					//Backup should have been made, perhaps backup manager is disabled or just unreliable!
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
					//Wifi check
					if (prefs.getBoolean(Application.PREF_BACKUP_WIFIONLY, false))
					{
						if (!BackupUtil.acquireWifiLock(context))
						{
							log.appendLog("Backup stopped, no wifi connection", Logger.TAG_BACKUP);
							mContext.clear();
							return null;							
						}
					}
					
					//Get file path
					File outPath = new File(context.getExternalFilesDir(null).toString() + "/backup");
					//Make sure folder exists
					outPath.mkdirs();
					//Get image path
					String imagePath = context.getExternalFilesDir(null).toString().replaceAll("/+$", "").concat("/");
					//Create data file
					File fileSql = new File(outPath, GoogleDriveService.BACKUP_DATA_FILENAME);

					//Backup data
					try
					{
						int byteCount = BackupUtil.BackupDataToFile(DBHelper.getHelper(context), fileSql, imagePath);
						if (byteCount > 0) {
							//Run
							Intent intent = new Intent(context, GoogleDriveService.class);
							context.startService(intent);
						}						
					}
					catch (IOException e) {
						//Too bad
						log.appendLog("Backup check task failed to complete backup", Logger.TAG_BACKUP);
						e.printStackTrace();
					}
				}
			}
		}
		
		mContext.clear();
		return null;
	}
}
