package com.zns.comicdroid;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.data.DBHelper;

public class BaseFragmentActivity extends com.actionbarsherlock.app.SherlockFragmentActivity {
   
	private DBHelper helper;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new DBHelper(this);
    }
    
    @Override 
    protected void onDestroy() {
    	helper.close();
    	super.onDestroy();    	
    }
    
    public DBHelper getDBHelper() {
    	return helper;
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
	        case R.id.menu_add:
	        	intent = new Intent(this, Add.class);
	        	startActivity(intent);	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}	
}
