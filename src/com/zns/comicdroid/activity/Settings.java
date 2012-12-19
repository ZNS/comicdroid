package com.zns.comicdroid.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;

public class Settings extends BaseFragmentActivity
	implements OnCheckedChangeListener {

	private DropboxAPI<AndroidAuthSession> dbApi = null;
	
	private ToggleButton tbDropbox;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		AppKeyPair appKeys = new AppKeyPair(Application.DROPBOX_KEY, Application.DROPBOX_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys, Application.DROPBOX_ACCESS_TYPE);
		dbApi = new DropboxAPI<AndroidAuthSession>(session);
	
		tbDropbox = (ToggleButton)findViewById(R.id.settings_tbDropbox);		
		tbDropbox.setOnCheckedChangeListener(this);				
	}
	
	protected void onResume() {
	    super.onResume();
	    
	    if (dbApi != null && dbApi.getSession().authenticationSuccessful()) {
	        try {
	            dbApi.getSession().finishAuthentication();

	            AccessTokenPair tokens = dbApi.getSession().getAccessTokenPair();
	            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
	            SharedPreferences.Editor editor = pref.edit();
	            editor.putString("DROPBOX_KEY", tokens.key);
	            editor.putString("DROPBOX_SECRET", tokens.secret);
	            editor.commit();
	            
	        } catch (IllegalStateException e) {
	            Log.i("DbAuthLog", "Error authenticating", e);
	        }
	    }

	    // ...
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == tbDropbox && isChecked) {
			dbApi.getSession().startAuthentication(this);			
		}
	}	
}
