package com.zns.comicdroid.activity;

import java.util.Arrays;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.task.DriveAuthorizationTask;
import com.zns.comicdroid.task.DriveWebFolderTask;

public class Settings extends BaseFragmentActivity
	implements OnCheckedChangeListener {

	private ToggleButton tbDropbox;
	private String mAccount;
	private GoogleAccountCredential mCredential;
	
	private String getAccount() {
		if (mAccount != null && !mAccount.equals("")) {
			return mAccount;
		}
		else {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			return pref.getString(Application.PREF_DRIVE_ACCOUNT, null);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_settings);
		super.onCreate(savedInstanceState);

		mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(Application.DRIVE_SCOPE));
		
		tbDropbox = (ToggleButton)findViewById(R.id.settings_tbDropbox);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());		
		tbDropbox.setChecked(pref.getBoolean(Application.PREF_DRIVE_AUTHENTICATED, false));
		tbDropbox.setOnCheckedChangeListener(this);
		
		TextView tvLink = (TextView)findViewById(R.id.settings_tvLink);
		if (pref.getString(Application.PREF_DRIVE_WEBFOLDERID, null) != null) {
			tvLink.setText(Html.fromHtml("<a href=\"https://googledrive.com/host/" + pref.getString(Application.PREF_DRIVE_WEBFOLDERID, "") + "/\">" + getResources().getString(R.string.settings_linktext) + "</a>"));
			tvLink.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}	
	
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == 101 && resultCode == RESULT_OK) {			
			mAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			mCredential.setSelectedAccountName(getAccount());
			
			//Store account
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = pref.edit();
			editor.putString(Application.PREF_DRIVE_ACCOUNT, mAccount);
			editor.commit();			
				
			//Authorize google drive
			new DriveAuthorizationTask() {
				@Override
				protected void onPostExecute(Intent result) {
					if (result != null)
					{						
						startActivityForResult(result, 102);
					}
					else {
						//We are already authorized, enable
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor editor = pref.edit();
						editor.putBoolean(Application.PREF_DRIVE_AUTHENTICATED, true);
						editor.commit(); 						
					}
				}
			}.execute(mCredential); 			
		}
		if (requestCode == 102) {
			if (resultCode == RESULT_OK) {
				//Create webfolder
				new DriveWebFolderTask() {
					@Override
					protected void onPostExecute(String fileId) {
						if (fileId != null)
						{
							//Store url
							SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
							SharedPreferences.Editor editor = pref.edit();
							editor.putString(Application.PREF_DRIVE_WEBFOLDERID, fileId);
							editor.commit();
						}
					}					
				}.execute(mCredential);
				//Google drive authorized, store status
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean(Application.PREF_DRIVE_AUTHENTICATED, true);
				editor.commit(); 
			}
			tbDropbox.setChecked(resultCode == RESULT_OK);
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == tbDropbox) {
			if (isChecked)
			{
				startActivityForResult(mCredential.newChooseAccountIntent(), 101);
			}
			else
			{
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean(Application.PREF_DRIVE_AUTHENTICATED, false);
				editor.putString(Application.PREF_DRIVE_ACCOUNT, null);
				editor.commit();
			}
		}
	}	
}
