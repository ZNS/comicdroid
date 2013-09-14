/*******************************************************************************
 * Copyright (c) 2013 Ulrik Andersson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Ulrik Andersson - initial API and implementation
 ******************************************************************************/
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
	private ComicArrayAdapter mAdapter;
	private Button mBtnScan;
	private EditText mEtBorrower;
	private ListView mLvComics;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_borrow);
		super.onCreate(savedInstanceState);               

		mEtBorrower = (EditText)findViewById(R.id.borrow_etBorrower);
		mBtnScan = (Button)findViewById(R.id.borrow_btnScan);
		mLvComics = (ListView)findViewById(R.id.borrow_lvComics);

		mBtnScan.setEnabled(false);
		mEtBorrower.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				mBtnScan.setEnabled(s.length() > 0);
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
		mAdapter = new ComicArrayAdapter(this, comics, getImagePath(true));
		mLvComics.setAdapter(mAdapter);        
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);		
		state.putParcelableArrayList(STATE_COMICS, new ArrayList<Comic>(mAdapter.getAll()));
	}    

	public void clear_click(View view) {
		mAdapter.clear();
		mEtBorrower.setText("");
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
					getDBHelper().setComicBorrowed(comic.getId(), mEtBorrower.getText().toString());
					Toast.makeText(this, getResources().getString(R.string.borrow_added), Toast.LENGTH_LONG).show();
					//Update adapter
					mAdapter.insert(comic, 0);
					mAdapter.notifyDataSetChanged();
					return;
				}
			}
			Toast.makeText(this, getResources().getString(R.string.borrow_notadded), Toast.LENGTH_LONG).show();
		}    	
	}    
}
