package com.zns.comicdroid;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_start, menu);
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
