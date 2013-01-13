package com.zns.comicdroid;

import android.content.Intent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.activity.Add;
import com.zns.comicdroid.activity.Borrow;
import com.zns.comicdroid.activity.Borrowed;
import com.zns.comicdroid.activity.Settings;
import com.zns.comicdroid.activity.Start;
import com.zns.comicdroid.data.DBHelper;

public class BaseFragmentActivity extends com.actionbarsherlock.app.SherlockFragmentActivity {
          
    public DBHelper getDBHelper() {
    	return DBHelper.getHelper(this);
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.actionbar_main, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Intent intent;
	    switch (item.getItemId()) {
	        case R.id.menu_start:
	        	intent = new Intent(this, Start.class);
	        	startActivity(intent);
	            return true;
	        case R.id.menu_borrowed:
	        	intent = new Intent(this, Borrowed.class);
	        	startActivity(intent);
	            return true;	            
	        case R.id.menu_add:
	        	intent = new Intent(this, Add.class);
	        	startActivity(intent);	        	
	            return true;
	        case R.id.menu_borrow:
	        	intent = new Intent(this, Borrow.class);
	        	startActivity(intent);
	        	return true;
	        case R.id.menu_settings:
	        	intent = new Intent(this, Settings.class);
	        	startActivity(intent);
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}	
}
