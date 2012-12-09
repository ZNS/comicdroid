package com.zns.comicdroid;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.zns.comicdroid.data.DBHelper;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Start extends BaseFragmentActivity
	implements LoaderCallbacks<Cursor>, ActionBar.TabListener, BaseListFragment.OnFragmentStartedListener {
	
	private static final String LISTFRAGMENTTAG = "LISTFRAGMENT";
	
	private DBHelper db;
	private SQLiteCursorLoader loader;
	private EditText etSearch;
	private Button btnClearSearch;	
	private Handler filterHandler;
	private String filterQuery;
	private Runnable filterTask;
	private String currentTab = "TITLES";
	private MenuItem menuEdit;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		db = new DBHelper(this);
		filterHandler = new Handler();
		
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
				getSupportLoaderManager().restartLoader(0, null, Start.this);
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
		
		//Load fragment
        if (findViewById(R.id.start_fragmentcontainer) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create an instance of ExampleFragment
            ListTitlesFragment fragment = new ListTitlesFragment();
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager()
            	.beginTransaction()
                .add(R.id.start_fragmentcontainer, fragment, LISTFRAGMENTTAG)
                .commit();
        }       
	}	

	//List fragment is started
	@Override
	public void onStarted() {
		//Initiate sqlite cursor loader
		if (getSupportLoaderManager().getLoader(0) == null)
			getSupportLoaderManager().initLoader(0,  null, this);
		else
			getSupportLoaderManager().restartLoader(0,  null, this);
	}
	
	public BaseListFragment getCurrentListFragment()
	{
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(LISTFRAGMENTTAG);
		return (BaseListFragment)fragment;
	}
	
	//Menu Implementation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar_view, (com.actionbarsherlock.view.Menu) menu);
		return true;
	}		

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menuEdit = menu.findItem(R.id.menu_edit);
		menuEdit.setVisible(false);		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        	case R.id.menu_edit:
	        	Intent intent = new Intent(this, Edit.class);
	        	int[] ids = getCurrentListFragment().getItemIds();
				intent.putExtra("com.zns.comic.COMICIDS", ids);
	        	startActivity(intent);
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	//Loader Implementation
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (filterQuery != null && filterQuery != "")
			loader= new SQLiteCursorLoader(this, db, getCurrentListFragment().getSQLFilter(), new String[] { filterQuery + "%" });
		else
			loader= new SQLiteCursorLoader(this, db, getCurrentListFragment().getSQLDefault(), null); 
		return(loader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		getCurrentListFragment().adapter.changeCursor(cursor);
		getCurrentListFragment().BindList();
		if (currentTab == "TITLES" && filterQuery != null && filterQuery != "" && cursor.getCount() > 0) {
			menuEdit.setVisible(true);
		}
		else {
			menuEdit.setVisible(false);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		getCurrentListFragment().adapter.changeCursor(null);
		getCurrentListFragment().BindList();
	}
	
	//Tab Implementation
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String tag = (String)tab.getTag();
		if (tag.equals(currentTab))
			return;
		
		BaseListFragment fragment = null;
		if (tag.equals("TITLES"))
			fragment = new ListTitlesFragment();
		else if (tag.equals("AUTHORS"))
			fragment = new ListAuthorsFragment();
		else if (tag.equals("PUBLISHERS"))
			fragment = new ListPublishersFragment();
		
		if (fragment != null)
		{
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	
			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack so the user can navigate back
			transaction.replace(R.id.start_fragmentcontainer, fragment, LISTFRAGMENTTAG);
			transaction.addToBackStack(null);
	
			// Commit the transaction
			transaction.commit();
			
			currentTab = tag;
		}
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