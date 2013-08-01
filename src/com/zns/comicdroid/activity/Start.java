	package com.zns.comicdroid.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.BaseListFragment;
import com.zns.comicdroid.R;
import com.zns.comicdroid.activity.fragment.ListAggregatesFragment;
import com.zns.comicdroid.activity.fragment.ListAuthorsFragment;
import com.zns.comicdroid.activity.fragment.ListIllustratorsFragment;
import com.zns.comicdroid.activity.fragment.ListPublishersFragment;
import com.zns.comicdroid.activity.fragment.ListTitlesFragment;

public class Start extends BaseFragmentActivity
	implements	BaseListFragment.OnListLoadedListener, 
				ActionBar.TabListener {
	
	private static final int TAB_COUNT = 5;
	private static final String TAB_AGGREGATES = "AGGREGATES";
	private static final String TAB_TITLES= "TITLES";
	private static final String TAB_AUTHORS = "AUTHORS";
	private static final String TAB_PUBLISHERS = "PUBLISHERS";
	private static final String TAB_ILLUSTRATORS = "ILLUSTRATORS";
	
	private String currentTab = TAB_AGGREGATES;
	private MenuItem menuEdit;
	private MenuItem menuSearch;
	private MenuItem menuSort;
	private SearchView searchView;
	private ViewPager viewPager;
	private TabFragmentAdapter fragmentAdapter;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_start);
		super.onCreate(savedInstanceState);
				
		Resources res = getResources();
	             
		//Tabs
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		ActionBar.Tab tab0 = getSupportActionBar().newTab();
		tab0.setText(res.getString(R.string.start_tab_aggregates));
		tab0.setTag(TAB_AGGREGATES);
		tab0.setTabListener(this);
		getSupportActionBar().addTab(tab0);
		
		ActionBar.Tab tab1 = getSupportActionBar().newTab();
		tab1.setText(res.getString(R.string.start_tab_comics));
		tab1.setTag(TAB_TITLES);
		tab1.setTabListener(this);
		getSupportActionBar().addTab(tab1);
		
		ActionBar.Tab tab2 = getSupportActionBar().newTab();
		tab2.setText(res.getString(R.string.start_tab_authors));
		tab2.setTag(TAB_AUTHORS);
		tab2.setTabListener(this);
		getSupportActionBar().addTab(tab2);		
				
		ActionBar.Tab tab3 = getSupportActionBar().newTab();
		tab3.setText(res.getString(R.string.start_tab_illustrators));
		tab3.setTag(TAB_ILLUSTRATORS);
		tab3.setTabListener(this);
		getSupportActionBar().addTab(tab3);		
		
		ActionBar.Tab tab4 = getSupportActionBar().newTab();
		tab4.setText(res.getString(R.string.start_tab_publishers));
		tab4.setTag(TAB_PUBLISHERS);
		tab4.setTabListener(this);
		getSupportActionBar().addTab(tab4);
		
		//View pager
		fragmentAdapter = new TabFragmentAdapter(getSupportFragmentManager());
		viewPager = (ViewPager)findViewById(R.id.start_viewPager);		
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});	
		viewPager.setAdapter(fragmentAdapter);
	}	

	@Override
	protected void onResume() {
		super.onResume();		
		//Refresh data
		BaseListFragment fragment = fragmentAdapter.getFragment(viewPager.getCurrentItem());
		if (fragment != null) {
			fragment.update();
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
            if (menuSearch != null)
            	menuSearch.collapseActionView();
            fragmentAdapter.getFragment(viewPager.getCurrentItem()).setFilter(query);
        }
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
						menuSearch.collapseActionView();
			            fragmentAdapter.getFragment(viewPager.getCurrentItem()).clearFilter();
					}
				}
				else {
					String filter = fragmentAdapter.getFragment(viewPager.getCurrentItem()).getFilter();
					if (filter != null && filter.length() > 0) {
						searchView.setQuery(filter, false);
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
		menuSort = menu.findItem(R.id.menu_sort);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        	case R.id.menu_edit:
	        	Intent intent = new Intent(this, Edit.class);
	        	BaseListFragment fragment = fragmentAdapter.getFragment(viewPager.getCurrentItem());
	        	int[] ids = fragment.getItemIds();
				intent.putExtra(Edit.INTENT_COMIC_IDS, ids);
	        	startActivity(intent);
	            return true;
        	case R.id.sort_title:
        		fragmentAdapter.getFragment(viewPager.getCurrentItem()).setOrderBy("Title, Issue");
        		return true;
        	case R.id.sort_added:
        		fragmentAdapter.getFragment(viewPager.getCurrentItem()).setOrderBy("AddedDate DESC, Title, Issue");
        		return true;        		
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onListLoaded() {
		BaseListFragment fragment = fragmentAdapter.getFragment(viewPager.getCurrentItem());
		if (fragment != null && menuEdit != null)
		{
			if (currentTab == TAB_TITLES && fragment.getFilter() != null && !fragment.getFilter().equals("") && fragment.adapter.getCount() > 0) {
				menuEdit.setVisible(true);
			}
			else {
				menuEdit.setVisible(false);
			}
		}
	}
	
	//Tab Implementation
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String tag = (String)tab.getTag();
		if (menuSort != null) {
			if (tag.equals(TAB_TITLES))
				menuSort.setVisible(true);
			else
				menuSort.setVisible(false);
		}
		if (tag.equals(currentTab))
			return;
		viewPager.setCurrentItem(tab.getPosition());
		currentTab = tag;
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}		
	
	class TabFragmentAdapter extends FragmentStatePagerAdapter {
		private Map<Integer, BaseListFragment> mPageReferenceMap = new HashMap<Integer, BaseListFragment>();
		
		public TabFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			String tag = (String)getSupportActionBar().getTabAt(pos).getTag();
			BaseListFragment fragment = null;
							
			if (tag.equals(TAB_TITLES))
				fragment = ListTitlesFragment.newInstance(pos);
			else if (tag.equals(TAB_AUTHORS))
				fragment = ListAuthorsFragment.newInstance(pos);
			else if (tag.equals(TAB_ILLUSTRATORS))
				fragment = ListIllustratorsFragment.newInstance(pos);			
			else if (tag.equals(TAB_PUBLISHERS))
				fragment = ListPublishersFragment.newInstance(pos);
			else
				fragment = ListAggregatesFragment.newInstance(pos);
			
			mPageReferenceMap.put(Integer.valueOf(pos), fragment);
			
			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			mPageReferenceMap.remove(Integer.valueOf(position));
		}
		
		@Override
		public int getCount() {
			return TAB_COUNT;
		}
		
		public BaseListFragment getFragment(int pos) {
			BaseListFragment fragment = mPageReferenceMap.get(pos);
			return fragment;
		}
	}
}