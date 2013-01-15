package com.zns.comicdroid.activity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.google.android.gms.common.AccountPicker;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.task.DriveAuthorizationTask;

public class Settings extends BaseFragmentActivity
	implements OnCheckedChangeListener {

	private ToggleButton tbDropbox;
	private String mAccount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		tbDropbox = (ToggleButton)findViewById(R.id.settings_tbDropbox);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);		
		tbDropbox.setChecked(pref.getBoolean("DRIVE_AUTHENTICATED", false));
		tbDropbox.setOnCheckedChangeListener(this);
	}	
	
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == 101 && resultCode == RESULT_OK) {			
			mAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			
			//Store account
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(Settings.this);
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("DRIVE_ACCOUNT", mAccount);
			editor.putBoolean("DRIVE_AUTHENTICATED", true);
			editor.commit();			
			
			//Authorize google drive
			DriveAuthorizationTask task = new DriveAuthorizationTask(this) {
				@Override
				protected void onPostExecute(Intent result) {
					if (result != null)
					{						
						startActivityForResult(result, 102);
					}
				}
			};
			task.execute(mAccount);				
		}
		if (requestCode == 102) {
			if (resultCode == RESULT_OK) {
				//Google drive authorized, store status
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean("DRIVE_AUTHENTICATED", true);
				editor.commit();
			}
			tbDropbox.setChecked(resultCode == RESULT_OK);
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == tbDropbox && isChecked) {
			Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
			startActivityForResult(intent, 101);			
		}
	}	
}
