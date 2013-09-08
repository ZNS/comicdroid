package com.zns.comicdroid.util;

import java.io.IOException;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

import com.zns.comicdroid.service.GoogleDriveService;

public class BackupHelper extends BackupAgent {
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
			//Upload to google drive
			Intent intent = new Intent(getApplicationContext(), GoogleDriveService.class);
			startService(intent);
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {		
	}
}