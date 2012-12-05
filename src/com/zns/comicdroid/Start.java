package com.zns.comicdroid;

import com.zns.comicdroid.data.ComicsAdapter;
import com.zns.comicdroid.data.DB;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class Start extends BaseActivity
{
	DB comicDB;
	ListView lvComics;
	ComicsAdapter adapter;
	EditText etSearch;
	Button btnClearSearch;
	
	Handler filterHandler;
	Editable filterQuery;
	Runnable filterTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		comicDB = new DB(this);
		lvComics = (ListView)findViewById(R.id.start_lvComics);
		etSearch = (EditText)findViewById(R.id.start_etSearch);
		btnClearSearch = (Button)findViewById(R.id.start_btnClearSearch);
		filterHandler = new Handler();
		
		btnClearSearch.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				etSearch.setText("");
				etSearch.clearFocus();
			}
		});
		
		lvComics.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				int comicId = adapter.getComicId(position);
				Intent intent = new Intent(Start.this, ComicView.class);
				intent.putExtra("com.zns.comic.COMICID", comicId);
				startActivity(intent);
			}			
		});
		registerForContextMenu(lvComics);
				 
		filterTask = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				adapter.getFilter().filter(filterQuery);
			}
		};
		
		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				filterQuery = s;
				filterHandler.removeCallbacks(filterTask);
				filterHandler.postDelayed(filterTask, 1000);
				if (s.length() > 0)
					btnClearSearch.setVisibility(View.VISIBLE);
				else
					btnClearSearch.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}			
		});
		
		BindComics();
	}

	private void BindComics()
	{
		if (adapter == null)
			adapter = new ComicsAdapter(this);
		lvComics.setAdapter(adapter);
	}
		
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.start_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int comicId = adapter.getComicId((int)info.id);
		if (comicId > 0)
		{
			switch (item.getItemId()) {
				case R.id.start_context_delete:
					lvComics.removeViews((int)info.id, 1);
					comicDB.deleteComic(comicId);					
					return true;
			}
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first
	}
}