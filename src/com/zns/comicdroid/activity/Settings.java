package com.zns.comicdroid.activity;

import java.util.Arrays;

import android.accounts.AccountManager;
import android.app.AlertDialog;
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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.service.GoogleDriveService;
import com.zns.comicdroid.service.RestoreFromDriveService;
import com.zns.comicdroid.task.DriveBackupInitTask;
import com.zns.comicdroid.task.DriveBackupInitTask.DriveBackupInitTaskArg;
import com.zns.comicdroid.task.DriveWebFolderTask;
import com.zns.comicdroid.task.DriveWebFolderTask.DriveWebFolderTaskArg;

public class Settings extends BaseFragmentActivity
	implements OnCheckedChangeListener {

	public final static String INTENT_STOP_UPLOAD = "com.zns.comicdroid.SETTINGS_STOP_UPLOAD";
	private ToggleButton tbDropbox;
	private ToggleButton tbDriveBackup;
	private TextView tvLink;
	private String mAccount;
	
	private void pickAccount(int code) {
		Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
				false, null, null, null, null);
		startActivityForResult(intent, code);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_settings);
		super.onCreate(savedInstanceState);	

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		tbDropbox = (ToggleButton)findViewById(R.id.settings_tbDropbox);		
		tbDropbox.setChecked(pref.getBoolean(Application.PREF_DRIVE_PUBLISH, false));
		tbDropbox.setOnCheckedChangeListener(this);
		tbDriveBackup = (ToggleButton)findViewById(R.id.settings_tbDriveBackup);
		tbDriveBackup.setChecked(pref.getBoolean(Application.PREF_DRIVE_BACKUP, false));
		tbDriveBackup.setOnCheckedChangeListener(this);
		
		tvLink = (TextView)findViewById(R.id.settings_tvLink);
		if (pref.getString(Application.PREF_DRIVE_WEBFOLDERID, null) != null) {
			tvLink.setText(Html.fromHtml("<a href=\"https://googledrive.com/host/" + pref.getString(Application.PREF_DRIVE_WEBFOLDERID, "") + "/\">" + getResources().getString(R.string.settings_linktext) + "</a>"));
			tvLink.setMovementMethod(LinkMovementMethod.getInstance());
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
			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());			
			if (requestCode == 101 || requestCode == 201) {
				//Get Account name from result
				mAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);							
				//Store account			
				SharedPreferences.Editor editor = pref.edit();
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
				args.webFolderId = pref.getString(Application.PREF_DRIVE_WEBFOLDERID, null);
				new DriveWebFolderTask() {
					protected void onPostExecute(DriveWebFolderTaskResult result) {
						if (result.intent != null) {
							//Not authorized, prompt for authorization
							startActivityForResult(result.intent, 102);
						}						
						else if (result.success && result.fileId != null)
						{
							//Store url
							SharedPreferences.Editor editor = pref.edit();
							editor.putString(Application.PREF_DRIVE_WEBFOLDERID, result.fileId);
							//Google drive authorized, store status							
							editor.putBoolean(Application.PREF_DRIVE_PUBLISH, true);							
							editor.commit();
							//Upload
							Intent intent = new Intent(Settings.this, GoogleDriveService.class);
							startService(intent);						
							//Update link
							tvLink.setText(Html.fromHtml("<a href=\"https://googledrive.com/host/" + result.fileId + "/\">" + getResources().getString(R.string.settings_linktext) + "</a>"));
							tvLink.setMovementMethod(LinkMovementMethod.getInstance());						
							//Check checkbox
							tbDropbox.setChecked(true);
						}
						else
						{
							Toast.makeText(Settings.this, R.string.error_webfoldercreate, Toast.LENGTH_SHORT).show();
							tbDropbox.setChecked(false);
						}
					}
				}.execute(args);
			}
			else if (requestCode == 201 || requestCode == 202)
			{
				//Make sure we have access to appdata folder
				DriveBackupInitTaskArg args = new DriveBackupInitTaskArg();
				GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE_BACKUP));
				credential.setSelectedAccountName(mAccount);				
				args.credentials = credential;
				new DriveBackupInitTask() {
					protected void onPostExecute(DriveBackupInitTaskResult result) {
						if (result.intent != null) {
							//Not authorized, prompt for authorization
							startActivityForResult(result.intent, 202);
						}
						else if (result.success)
						{
							//Store preferences
							SharedPreferences.Editor editor = pref.edit();
							editor.putBoolean(Application.PREF_DRIVE_BACKUP, true);
							editor.commit();
							//Backup
							Intent intent = new Intent(Settings.this, GoogleDriveService.class);
							startService(intent);							
							//Check checkbox
							tbDriveBackup.setChecked(true);
						}
						else
						{
							Toast.makeText(Settings.this, R.string.error_driveappdataaccess, Toast.LENGTH_SHORT).show();
							tbDriveBackup.setChecked(false);
						}						
					}
				}.execute(args);
			}
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == tbDropbox) {
			if (isChecked)
			{
				pickAccount(101);
			}
			else
			{
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean(Application.PREF_DRIVE_PUBLISH, false);
				editor.commit();
			}
		}
		else if (buttonView == tbDriveBackup) {
			if (isChecked)
			{
				pickAccount(201);
			}
			else
			{
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean(Application.PREF_DRIVE_BACKUP, false);
				editor.commit();				
			}
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
		startService(intent);
		showNotificationDialog(R.string.settings_publish_notification);
	}
	
	public void updateGroupCountClick(View view) {
		getDBHelper().updateGroupBookCount();
		showNotificationDialog(R.string.settings_updateGroupCount_notification);
	}
	
	public void setReadClick(View view) {
		getDBHelper().setAllComicsRead();
	}
	
	public void restoreClick(View view) {
		//Restore
		Intent intent = new Intent(Settings.this, RestoreFromDriveService.class);
		startService(intent);		
	}
}
