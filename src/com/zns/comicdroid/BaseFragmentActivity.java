package com.zns.comicdroid;

import android.content.Intent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BaseFragmentActivity extends com.actionbarsherlock.app.SherlockFragmentActivity {
    
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
	        case R.id.menu_add:
	        	intent = new Intent(this, Add.class);
	        	startActivity(intent);	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}	
}
