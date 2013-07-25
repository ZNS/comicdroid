package com.zns.comicdroid.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.ComicArrayAdapter;
import com.zns.comicdroid.data.Comic;

public class Borrow extends BaseFragmentActivity {
	
	private final static String STATE_COMICS = "COMICS";
	private ComicArrayAdapter adapter;
	private Button btnScan;
	private EditText etBorrower;
	private ListView lvComics;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_borrow);
    	super.onCreate(savedInstanceState);               
                
        etBorrower = (EditText)findViewById(R.id.borrow_etBorrower);
        btnScan = (Button)findViewById(R.id.borrow_btnScan);
        lvComics = (ListView)findViewById(R.id.borrow_lvComics);
        
        btnScan.setEnabled(false);
        etBorrower.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				btnScan.setEnabled(s.length() > 0);
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}        	
        });
        
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
					//Mark as borrowed and notify
					getDBHelper().setComicBorrowed(comic.getId(), etBorrower.getText().toString());
					Toast.makeText(this, getResources().getString(R.string.borrow_added), Toast.LENGTH_SHORT).show();
					//Update adapter
					adapter.insert(comic, 0);
					adapter.notifyDataSetChanged();
					return;
				}
			}
			Toast.makeText(this, getResources().getString(R.string.borrow_notadded), Toast.LENGTH_LONG).show();
    	}    	
	}    
}
