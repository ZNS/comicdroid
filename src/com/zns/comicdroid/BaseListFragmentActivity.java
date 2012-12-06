package com.zns.comicdroid;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.zns.comicdroid.data.DBHelper;

public class BaseListFragmentActivity extends BaseFragmentActivity
	implements LoaderCallbacks<Cursor>, ActionBar.TabListener {
	
	protected DBHelper db;
	protected SimpleCursorAdapter adapter;
	protected SQLiteCursorLoader loader;
	protected ListView listView;	

	private EditText etSearch;
	private Button btnClearSearch;	
	private Handler filterHandler;
	private String filterQuery;
	private Runnable filterTask;
	private Boolean tabsInitialized;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	 
		setContentView(R.layout.activity_list);
		tabsInitialized = false;
		
		db = new DBHelper(this);
		filterHandler = new Handler();
		listView = (ListView)findViewById(R.id.start_lvComics);
		
		etSearch = (EditText)findViewById(R.id.start_etSearch);
		btnClearSearch = (Button)findViewById(R.id.start_btnClearSearch);					
		
		btnClearSearch.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				etSearch.setText("");
				etSearch.clearFocus();
			}
		});
		
		filterTask = new Runnable() {
			@Override
			public void run() {
				getSupportLoaderManager().restartLoader(0, null, BaseListFragmentActivity.this);
			}
		};
		
		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				filterQuery = s.toString();
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
		
		//Tabs
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		ActionBar.Tab tab1 = getSupportActionBar().newTab();
		tab1.setText("Titlar");
		tab1.setTag("TITLES");
		tab1.setTabListener(this);
		getSupportActionBar().addTab(tab1);
		
		ActionBar.Tab tab2 = getSupportActionBar().newTab();
		tab2.setText("Författare");
		tab2.setTag("AUTHORS");
		tab2.setTabListener(this);
		getSupportActionBar().addTab(tab2);		
				
		ActionBar.Tab tab3 = getSupportActionBar().newTab();
		tab3.setText("Förlag");
		tab3.setTag("PUBLISHERS");
		tab3.setTabListener(this);
		getSupportActionBar().addTab(tab3);
				
		//Initiate sqlite cursor loader
		getSupportLoaderManager().initLoader(0,  null, this);		
	}
	
	protected String getSQLDefault()
	{
		return null;
	}
	
	protected String getSQLFilter()
	{
		return null;
	}
	
	//Loader Implementation
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (filterQuery != null && filterQuery != "")
			loader= new SQLiteCursorLoader(this, db, getSQLFilter(), new String[] { filterQuery + "%" });
		else
			loader= new SQLiteCursorLoader(this, db, getSQLDefault(), null); 
		return(loader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);
	}

	//Tab Implementation
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (!tabsInitialized)
		{
			tabsInitialized = true;
			return;
		}
		
		String tag = (String)tab.getTag();	
		Intent intent = null;
		if (tag.equals("TITLES"))
			intent = new Intent(BaseListFragmentActivity.this, Start.class);
		else if (tag.equals("AUTHORS"))
			intent = new Intent(BaseListFragmentActivity.this, Authors.class);
		if (intent != null)
			startActivity(intent);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}	
}
