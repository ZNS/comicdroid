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

import java.lang.ref.WeakReference;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

	private String mCurrentTab = TAB_AGGREGATES;
	private MenuItem mMenuEdit;
	private MenuItem mMenuSearch;
	private MenuItem mMenuSort;
	private SearchView mSearchView;
	private ViewPager mViewPager;
	private TabFragmentAdapter mFragmentAdapter;
	private TextView mTvEmpty;
	private boolean mIsCreation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_start);
		super.onCreate(savedInstanceState);

		//View pager
		mFragmentAdapter = new TabFragmentAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager)findViewById(R.id.start_viewPager);		
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getSupportActionBar().setSelectedNavigationItem(position); //TODO: TRACKED NULL EXCEPTION HERE, SOMETHING TO DO WITH RestoreInstanceState
			}
		});	
		mViewPager.setAdapter(mFragmentAdapter);
		
		//Tabs
		Resources res = getResources();
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

		//Layout elements
		mTvEmpty = (TextView)findViewById(R.id.start_tvEmpty);
		
		//Track creation
		mIsCreation = true;
	}	

	@Override
	protected void onResume() {
		super.onResume();		
		//Refresh data
		if (!mIsCreation) {
			BaseListFragment fragment = getCurrentFragment();
			if (fragment != null) {
				fragment.update();
			}
		}
		else {
			mIsCreation = false;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			BaseListFragment fragment = getCurrentFragment();
			if (fragment != null) {
				fragment.setFilter(query);
			}
		}		
	}

	//Menu Implementation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar_start, (com.actionbarsherlock.view.Menu) menu);

		SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
		mSearchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String query = mSearchView.getQuery().toString();
					if (query.trim().length() == 0) {
						mMenuSearch.collapseActionView();
						BaseListFragment fragment = getCurrentFragment();
						if (fragment != null) {
							fragment.clearFilter();
						}
					}
				}
			}
		});

		return true;
	}		

	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) {
		mMenuEdit = menu.findItem(R.id.menu_edit);
		mMenuSearch = menu.findItem(R.id.menu_search);
		mMenuSort = menu.findItem(R.id.menu_sort);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		BaseListFragment fragment = getCurrentFragment();		
		if (fragment != null)
		{
			switch (item.getItemId()) {
			case R.id.menu_edit:
				Intent intent = new Intent(this, Edit.class);
				int[] ids = fragment.getItemIds();
				intent.putExtra(Edit.INTENT_COMIC_IDS, ids);
				startActivity(intent);
				return true;
			case R.id.sort_title:
				fragment.setOrderBy("Title, Issue");
				return true;
			case R.id.sort_added:
				fragment.setOrderBy("AddedDate DESC, Title, Issue");
				return true;
			case R.id.sort_rating:
				fragment.setOrderBy("Rating DESC, Title, Issue");
				return true;        		
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onListLoaded() {
		BaseListFragment fragment = getCurrentFragment();
		if (fragment != null && mMenuEdit != null)
		{
			if (mCurrentTab == TAB_TITLES && fragment.getFilter() != null && !fragment.getFilter().equals("") && fragment.mAdapter.getCount() > 0) {
				mMenuEdit.setVisible(true);
			}
			else {
				mMenuEdit.setVisible(false);
			}
		}

		mTvEmpty.setVisibility(View.GONE);
		if (mViewPager != null && (mViewPager.getCurrentItem() == 0 || mViewPager.getCurrentItem() == 1)) {
			if (!fragment.hasItems() && fragment.getFilter() == null)
			{
				mTvEmpty.setVisibility(View.VISIBLE);
			}
		}
	}

	//Tab Implementation
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String tag = (String)tab.getTag();
		
		//Sorting
		if (mMenuSort != null) {
			if (tag.equals(TAB_TITLES))
				mMenuSort.setVisible(true);
			else
				mMenuSort.setVisible(false);
		}

		//Set current fragment for pager
		if (!tag.equals(mCurrentTab))
			mViewPager.setCurrentItem(tab.getPosition());
		
		//Searchview
		BaseListFragment fragment = getCurrentFragment();
		if (fragment != null && mMenuSearch != null && mSearchView != null)
		{
			if (fragment.getFilter() != null) {
				mMenuSearch.expandActionView();
				mSearchView.setQuery(fragment.getFilter(), false);
				mSearchView.clearFocus();
			}
			else {
				mMenuSearch.collapseActionView(); //TODO:THIS CAN THROW NULL EXCEPTION!!!
				mSearchView.setQuery(null, false);				
			}
		}	
		
		//Set current tab
		mCurrentTab = tag;
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
	}		

	private BaseListFragment getCurrentFragment() 
	{
		if (mViewPager != null)
		{
			int index = mViewPager.getCurrentItem();
			if (index > -1) {			
				return mFragmentAdapter.getFragment(index);			
			}
		}
		return null;
	}

	class TabFragmentAdapter extends FragmentStatePagerAdapter {
		@SuppressWarnings("unchecked")
		final WeakReference<BaseListFragment>[] m_fragments = new WeakReference[TAB_COUNT];

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

			m_fragments[pos] = new WeakReference<BaseListFragment>(fragment);

			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			if (m_fragments[position] != null)
				m_fragments[position].clear();
		}

		@Override
		public int getCount() {
			return TAB_COUNT;
		}

		//We need to keep track of when fragments are restored via state. This is ripped and modified from android source code...
		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
			super.restoreState(state, loader);
			if (state != null)
			{
				for (int i = 0; i < m_fragments.length; i++) {
					if (m_fragments[i] != null)
						m_fragments[i].clear();
				}

				Bundle bundle = (Bundle)state;
				bundle.setClassLoader(loader);
				Iterable<String> keys = bundle.keySet();
				for (String key: keys) {
					if (key.startsWith("f")) {
						int index = Integer.parseInt(key.substring(1));
						Fragment f = getSupportFragmentManager().getFragment(bundle, key);
						if (f != null && f instanceof BaseListFragment) {
							m_fragments[index] = new WeakReference<BaseListFragment>((BaseListFragment)f);
						}
					}
				}
			}
		}

		public BaseListFragment getFragment(final int position) {
			return m_fragments[position] == null ? null :
				m_fragments[position].get();
		}		
	}
}
