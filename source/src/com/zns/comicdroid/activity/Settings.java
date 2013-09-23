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
package com.zns.comicdroid.activity;

import java.util.Arrays;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.service.GoogleDriveService;
import com.zns.comicdroid.service.ProgressResult;
import com.zns.comicdroid.service.RestoreFromDriveService;
import com.zns.comicdroid.task.DriveBackupInitTask;
import com.zns.comicdroid.task.DriveBackupInitTask.DriveBackupInitTaskArg;
import com.zns.comicdroid.task.DriveDisableBackupTask;
import com.zns.comicdroid.task.DriveWebFolderTask;
import com.zns.comicdroid.task.DriveWebFolderTask.DriveWebFolderTaskArg;

import de.greenrobot.event.EventBus;

public class Settings extends BaseFragmentActivity
implements OnCheckedChangeListener {

	public final static String INTENT_STOP_UPLOAD = "com.zns.comicdroid.SETTINGS_STOP_UPLOAD";
	private ToggleButton mTbDrivePublish;
	private ToggleButton mTbDriveBackup;
	private ToggleButton mTbBackupWifi;
	private TextView mTvLink;
	private String mAccount;
	private SharedPreferences mPrefs;
	
	private void pickAccount(int code) {
		//Google play services check
		int serviceStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (serviceStatus == ConnectionResult.SUCCESS) {
			Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
					false, null, null, null, null);
			startActivityForResult(intent, code);
			return;
		}
		else if (serviceStatus == ConnectionResult.SERVICE_MISSING || serviceStatus == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED || serviceStatus == ConnectionResult.SERVICE_DISABLED) {
			GooglePlayServicesUtil.getErrorDialog(serviceStatus, this, 404).show();
		}
		else {
			Toast.makeText(this, R.string.error_playservices, Toast.LENGTH_LONG).show();
		}
		setToggleButtonPref(mTbDriveBackup, false, null);
		setToggleButtonPref(mTbDrivePublish, false, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_settings);
		super.onCreate(savedInstanceState);	

		mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mAccount = mPrefs.getString(Application.PREF_DRIVE_ACCOUNT, null);
		
		mTbDrivePublish = (ToggleButton)findViewById(R.id.settings_tbDropbox);		
		mTbDrivePublish.setChecked(mPrefs.getBoolean(Application.PREF_DRIVE_PUBLISH, false));
		mTbDrivePublish.setOnCheckedChangeListener(this);
		mTbDriveBackup = (ToggleButton)findViewById(R.id.settings_tbDriveBackup);
		mTbDriveBackup.setChecked(mPrefs.getBoolean(Application.PREF_DRIVE_BACKUP, false));
		mTbDriveBackup.setOnCheckedChangeListener(this);
		mTbBackupWifi = (ToggleButton)findViewById(R.id.settings_tbBackupWifi);
		mTbBackupWifi.setChecked(mPrefs.getBoolean(Application.PREF_BACKUP_WIFIONLY, false));
		mTbBackupWifi.setOnCheckedChangeListener(this);
				
		mTvLink = (TextView)findViewById(R.id.settings_tvLink);
		if (mPrefs.getString(Application.PREF_DRIVE_WEBFOLDERID, null) != null) {
			mTvLink.setText(Html.fromHtml("<a href=\"https://googledrive.com/host/" + mPrefs.getString(Application.PREF_DRIVE_WEBFOLDERID, "") + "/\">" + getResources().getString(R.string.settings_linktext) + "</a>"));
			mTvLink.setMovementMethod(LinkMovementMethod.getInstance());
		}

		//If called from notification, stop upload service
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null)
		{
			if (intent.getExtras().getBoolean(INTENT_STOP_UPLOAD, false))
				stopService(new Intent(this, GoogleDriveService.class));
		}		
	}	

	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == RESULT_OK)
		{
			if (requestCode == 101 || requestCode == 201 || requestCode == 301) {
				//Get Account name from result
				mAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);							
				//Store account			
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putString(Application.PREF_DRIVE_ACCOUNT, mAccount);
				editor.commit();			
			}	

			if (requestCode == 101 || requestCode == 102)
			{
				//Try to create folder on drive
				DriveWebFolderTaskArg args = new DriveWebFolderTaskArg();
				GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE_PUBLISH));
				credential.setSelectedAccountName(mAccount);				
				args.credentials = credential;
				args.webFolderId = mPrefs.getString(Application.PREF_DRIVE_WEBFOLDERID, null);
				new DriveWebFolderTask() {
					protected void onPostExecute(DriveWebFolderTaskResult result) {
						if (result.intent != null) {
							//Not authorized, prompt for authorization
							startActivityForResult(result.intent, 102);
						}						
						else if (result.success && result.fileId != null)
						{
							//Store url
							SharedPreferences.Editor editor = mPrefs.edit();
							editor.putString(Application.PREF_DRIVE_WEBFOLDERID, result.fileId);
							//Google drive authorized, store status							
							editor.putBoolean(Application.PREF_DRIVE_PUBLISH, true);							
							editor.commit();
							//Upload
							Intent intent = new Intent(Settings.this, GoogleDriveService.class);
							intent.putExtra(GoogleDriveService.INTENT_PUBLISH_ONLY, true);
							startService(intent);						
							//Update link
							mTvLink.setText(Html.fromHtml("<a href=\"https://googledrive.com/host/" + result.fileId + "/\">" + getResources().getString(R.string.settings_linktext) + "</a>"));
							mTvLink.setMovementMethod(LinkMovementMethod.getInstance());						
							//Check checkbox
							setToggleButtonPref(mTbDrivePublish, true, null);
						}
						else if (result.fileExists) {
							Toast.makeText(Settings.this, R.string.error_webfolderexists, Toast.LENGTH_LONG).show();
							setToggleButtonPref(mTbDrivePublish, false, Application.PREF_DRIVE_PUBLISH);
						}
						else
						{
							Toast.makeText(Settings.this, R.string.error_webfoldercreate, Toast.LENGTH_SHORT).show();
							setToggleButtonPref(mTbDrivePublish, false, Application.PREF_DRIVE_PUBLISH);
						}
					}
				}.execute(args);
			}
			else if (requestCode == 201 || requestCode == 202)
			{
				//Make sure we have access to appdata folder				
				final GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE_BACKUP));
				credential.setSelectedAccountName(mAccount);
				DriveBackupInitTaskArg args = new DriveBackupInitTaskArg();
				args.credentials = credential;
				args.appId = mPrefs.getString(Application.PREF_APP_ID, "");
				new DriveBackupInitTask() {
					protected void onPostExecute(DriveBackupInitTaskResult result) {
						if (result.intent != null) {
							//Not authorized, prompt for authorization
							startActivityForResult(result.intent, 202);
						}
						else if (result.success && result.backupAllowed)
						{
							//Store preferences
							SharedPreferences.Editor editor = mPrefs.edit();
							editor.putBoolean(Application.PREF_DRIVE_BACKUP, true);
							editor.commit();
							//Backup
							BackupManager m = new BackupManager(Settings.this);
							m.dataChanged();							
							//Check checkbox
							setToggleButtonPref(mTbDriveBackup, true, null);
						}
						else if (!result.backupAllowed)
						{
							//User needs to be able to force enabling backup, the appId may be from an uninstalled version
							new AlertDialog.Builder(Settings.this)
							.setMessage(getString(R.string.error_backupdisallowed))
							.setPositiveButton(R.string.settings_forcebackup, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									new DriveDisableBackupTask() {
										@Override
										protected void onPostExecute(Boolean result) {
											if (result == Boolean.TRUE) {												
												setToggleButtonPref(mTbDriveBackup, false, Application.PREF_DRIVE_BACKUP);
											}
										}
									}.execute(credential);									
								}
							})
							.setNegativeButton(R.string.common_close, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									setToggleButtonPref(mTbDriveBackup, false, Application.PREF_DRIVE_BACKUP);
									dialog.cancel();
								}
							}).show();							
						}
						else
						{
							Toast.makeText(Settings.this, getString(R.string.error_driveappdataaccess) + ": " + result.errorMessage, Toast.LENGTH_SHORT).show();
							setToggleButtonPref(mTbDriveBackup, false, Application.PREF_DRIVE_BACKUP);
						}						
					}
				}.execute(args);
			}
			else if (requestCode == 301 || requestCode == 302) 
			{
				//Request access to restore data
				GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE_BACKUP));
				credential.setSelectedAccountName(mAccount);
				DriveBackupInitTaskArg args = new DriveBackupInitTaskArg();
				args.credentials = credential;
				args.appId = null;				
				new DriveBackupInitTask() {
					protected void onPostExecute(DriveBackupInitTaskResult result) {
						if (result.intent != null) {
							//Not authorized, prompt for authorization
							startActivityForResult(result.intent, 302);
						}
						else if (result.success) {
							//Start restore
							Intent intent = new Intent(Settings.this, RestoreFromDriveService.class);
							startService(intent);							
						}
						else
						{
							Toast.makeText(Settings.this, R.string.error_driveappdataaccess, Toast.LENGTH_SHORT).show();
						}
					}
				}.execute(args);
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == mTbDrivePublish) {
			if (isChecked)
			{
				pickAccount(101);
			}
			else {
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putBoolean(Application.PREF_DRIVE_PUBLISH, false);
				editor.commit();				
			}
		}
		else if (buttonView == mTbDriveBackup) {
			if (isChecked)
			{
				pickAccount(201);
			}
			else
			{
				mTbDriveBackup.setEnabled(false);
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putBoolean(Application.PREF_DRIVE_BACKUP, false);
				editor.commit();				
				if (mAccount != null) {
					GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE_BACKUP));
					credential.setSelectedAccountName(mAccount);
					new DriveDisableBackupTask() {
						@Override
						protected void onPostExecute(Boolean result) {
							if (mTbDriveBackup != null) {
								mTbDriveBackup.setEnabled(true);
							}
						}
					}.execute(credential);
				}
			}
		}
		else if (buttonView == mTbBackupWifi) {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putBoolean(Application.PREF_BACKUP_WIFIONLY, isChecked);
			editor.commit();
		}
	}

	private void showNotificationDialog(int text) {
		new AlertDialog.Builder(this)
		.setMessage(text)
		.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();
	}

	public void publishToDriveClick(View view) {
		Intent intent = new Intent(getApplicationContext(), GoogleDriveService.class);
		intent.putExtra(GoogleDriveService.INTENT_PUBLISH_ONLY, true);
		startService(intent);
		EventBus.getDefault().post(new ProgressResult(1, getString(R.string.progress_publishinit)));
	}

	public void updateGroupCountClick(View view) {
		getDBHelper().updateGroupBookCount();
		showNotificationDialog(R.string.settings_updateGroupCount_notification);
	}

	public void setReadClick(View view) {
		new AlertDialog.Builder(this)
		.setMessage(getString(R.string.settings_readconfirm))
		.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				getDBHelper().setAllComicsRead();
			}
		})
		.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();		
	}
	
	public void restoreClick(View view) {
		EventBus.getDefault().post(new ProgressResult(1, getString(R.string.progress_init)));
		//Restore
		pickAccount(301);
	}
	
	private void setToggleButtonPref(ToggleButton button, boolean checked, String pref) {
		if (pref != null) {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putBoolean(pref, checked);
			editor.commit();
		}	
		button.setEnabled(true);
		button.setOnCheckedChangeListener(null);
		button.setChecked(checked);
		button.setOnCheckedChangeListener(this);
	}
}
