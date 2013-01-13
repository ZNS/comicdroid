package com.zns.comicdroid.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.ComicArrayAdapter;
import com.zns.comicdroid.data.Comic;

public class Borrow extends BaseFragmentActivity {
	
	private final static String STATE_COMICS = "COMICS";
	private ComicArrayAdapter adapter;
	private EditText etBorrower;
	private ListView lvComics;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);               
        setContentView(R.layout.activity_borrow);
                
        etBorrower = (EditText)findViewById(R.id.borrow_etBorrower);
        lvComics = (ListView)findViewById(R.id.borrow_lvComics);
        
	    ArrayList<Comic> comics = new ArrayList<Comic>();
	    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_COMICS)) {
	    	comics = savedInstanceState.getParcelableArrayList(STATE_COMICS);
	    }
        adapter = new ComicArrayAdapter(this, comics);
        lvComics.setAdapter(adapter);        
    }
    
    @Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);		
		state.putParcelableArrayList(STATE_COMICS, new ArrayList<Comic>(adapter.getAll()));
	}    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar_edit, (com.actionbarsherlock.view.Menu) menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        	case R.id.menu_done:
        		if (etBorrower.getText().length() > 0) {
	        		int[] ids = new int[adapter.getCount()];
	        		int i = 0;
	        		for (Comic c : adapter.getAll())
	        		{
	        			ids[i] = c.getId();
	        			i++;
	        		}
	        		getDBHelper().setComicBorrowed(ids, etBorrower.getText().toString());
	        		Toast.makeText(this, getResources().getString(R.string.borrow_success), Toast.LENGTH_LONG).show();
        		}
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
    public void clear_click(View view) {
    	adapter.clear();
    	etBorrower.setText("");
	}
    
    public void scanISBN_click(View view)
    {
    	IntentIntegrator integrator = new IntentIntegrator(this);
    	integrator.initiateScan();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
    	if (requestCode == IntentIntegrator.REQUEST_CODE)
    	{
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			if (scanResult != null) 
			{
				String isbn = scanResult.getContents();
				Comic comic = getDBHelper().getComic(isbn);
				if (comic != null) {
					adapter.insert(comic, 0);
					adapter.notifyDataSetChanged();
				}
			}
			else {
				Toast.makeText(this, getResources().getString(R.string.borrow_notadded), Toast.LENGTH_LONG).show();
			}
    	}    	
	}    
}
