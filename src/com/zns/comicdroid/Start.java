package com.zns.comicdroid;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;

public class Start extends BaseFragmentActivity
	implements	LoaderCallbacks<Cursor>, 
				ActionBar.TabListener, 
				BaseListFragment.OnFragmentStartedListener {
	
	private static final String LISTFRAGMENTTAG = "LISTFRAGMENT";
	
	private SQLiteCursorLoader loader;
	private String filterQuery = "";
	private String currentTab = "AGGREGATES";
	private MenuItem menuEdit;
	private MenuItem menuSearch;
	private SearchView searchView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
				
		//Tabs
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		ActionBar.Tab tab0 = getSupportActionBar().newTab();
		tab0.setText("Serier");
		tab0.setTag("AGGREGATES");
		tab0.setTabListener(this);
		getSupportActionBar().addTab(tab0);
		
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
            ListAggregatesFragment fragment = new ListAggregatesFragment();
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager()
            	.beginTransaction()
                .add(R.id.start_fragmentcontainer, fragment, LISTFRAGMENTTAG)
                .commit();
        }
	}	

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}
    
	//SearchView Implementation
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            filterQuery = query;
            if (menuSearch != null)
            	menuSearch.collapseActionView();
            getSupportLoaderManager().restartLoader(0, null, Start.this);
        }
    }
    
	//List fragment is started
	@Override
	public void onStarted() {
		//Initiate sqlite cursor loader
		filterQuery = "";
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
		inflater.inflate(R.menu.actionbar_start, (com.actionbarsherlock.view.Menu) menu);
		
	    SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
	    searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	    searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String query = searchView.getQuery().toString();
					if (query.trim().length() == 0) {
						filterQuery = "";
						menuSearch.collapseActionView();
						getSupportLoaderManager().restartLoader(0, null, Start.this);
					}
				}
				else {
					if (filterQuery != null && filterQuery.length() > 0) {
						searchView.setQuery(filterQuery, false);
					}	
				}
			}
	    });
		    
		return true;
	}		
	
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) {
		menuEdit = menu.findItem(R.id.menu_edit);
		menuSearch = menu.findItem(R.id.menu_search);
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
		if (filterQuery != null && !filterQuery.equals("")) {
			String sql = getCurrentListFragment().getSQLFilter();
			List<String> params = new ArrayList<String>();
			int paramIndex = -1;
			while((paramIndex = sql.indexOf("?", paramIndex + 1)) > -1) {
				params.add(filterQuery + "%");
			}
			loader = new SQLiteCursorLoader(this, getDBHelper(), getCurrentListFragment().getSQLFilter(), params.toArray(new String[params.size()]));
		}
		else {
			loader = new SQLiteCursorLoader(this, getDBHelper(), getCurrentListFragment().getSQLDefault(), null); 
		}
		return(loader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		getCurrentListFragment().adapter.changeCursor(cursor);
		getCurrentListFragment().BindList();
		if (menuEdit != null)
		{
			if (currentTab == "TITLES" && filterQuery != null && !filterQuery.equals("") && cursor.getCount() > 0) {
				menuEdit.setVisible(true);
			}
			else {
				menuEdit.setVisible(false);
			}
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
		if (tag.equals("AGGREGATES"))
			fragment = new ListAggregatesFragment();
		else if (tag.equals("TITLES"))
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