package com.zns.comicdroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.activity.Add;
import com.zns.comicdroid.activity.Borrow;
import com.zns.comicdroid.activity.Borrowed;
import com.zns.comicdroid.activity.Comics;
import com.zns.comicdroid.activity.Settings;
import com.zns.comicdroid.activity.Start;
import com.zns.comicdroid.activity.WatchedGroups;
import com.zns.comicdroid.adapter.DrawerMenuAdapter;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.service.ProgressResult;

import de.greenrobot.event.EventBus;

public class BaseFragmentActivity 
extends com.actionbarsherlock.app.SherlockFragmentActivity
implements ListView.OnItemClickListener {

	private DrawerLayout mDrawer;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private ProgressBar mPbService;
	private TextView mTvProgressService;
	private LinearLayout mLLProgressService;

	public DBHelper getDBHelper() {
		return DBHelper.getHelper(this);
	}

	public String getImagePath(boolean appendSlash) {
		return ((Application)getApplication()).getImagePath(appendSlash);
	}

	public String getImagePath(String imageName) {
		return ((Application)getApplication()).getImagePath(imageName);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {   
		super.onCreate(savedInstanceState);	
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);		
		mDrawer = (DrawerLayout)findViewById(R.id.drawer_layout);
		mDrawerList = (ListView)findViewById(R.id.drawer_left);
		mPbService = (ProgressBar)findViewById(R.id.pbService);		
		mTvProgressService = (TextView)findViewById(R.id.tvProgressService);
		mLLProgressService = (LinearLayout)findViewById(R.id.llProgressService);
		
		String[] titles = new String[] { 
				getString(R.string.menu_start), 
				getString(R.string.menu_borrowed),
				getString(R.string.menu_watched),
				getString(R.string.menu_read),
				getString(R.string.menu_add), 
				getString(R.string.menu_borrow), 
				getString(R.string.menu_settings) };
		String[] subTitles = new String[] { 
				getString(R.string.menu_start_sub), 
				getString(R.string.menu_borrowed_sub),
				getString(R.string.menu_watched_sub),
				getString(R.string.menu_read_sub),
				getString(R.string.menu_add_sub), 
				getString(R.string.menu_borrow_sub), 
				getString(R.string.menu_settings_sub) };
		mDrawerList.setAdapter(new DrawerMenuAdapter(this, titles, subTitles));
		mDrawerList.setOnItemClickListener(this);		
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_launcher, R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerOpened(View drawerView) {
				// TODO Auto-generated method stub
				super.onDrawerOpened(drawerView);
			}
		};
		mDrawer.setDrawerListener(mDrawerToggle);

		//First use check
		if (((Application)getApplication()).isFirstUse) {			
			mDrawer.openDrawer(mDrawerList);
			((Application)getApplication()).isFirstUse = false;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getDefault().register(this, "onServiceProgress", ProgressResult.class);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this, ProgressResult.class);
	}
	
	public void onServiceProgressMainThread(ProgressResult progress) {
		if (mPbService == null)
			return;
		
		if (mLLProgressService.getVisibility() == View.GONE) {
			mPbService.setProgress(0);
			mPbService.setMax(100);
			mLLProgressService.setVisibility(View.VISIBLE);
		}
		mTvProgressService.setText(progress.desc);
		
		if (progress.value < 100) {
			mPbService.setProgress(progress.value);
		}
		else {
			mLLProgressService.setVisibility(View.GONE);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getSupportMenuInflater().inflate(R.menu.actionbar_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:        		 
			if (mDrawer.isDrawerOpen(mDrawerList)) {
				mDrawer.closeDrawer(mDrawerList);
			} 
			else {
				mDrawer.openDrawer(mDrawerList);
			}
			return true;	    
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Intent intent;
		switch (position) {
		case 0:
			intent = new Intent(this, Start.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(this, Borrowed.class);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(this, WatchedGroups.class);
			startActivity(intent);
			break;
		case 3:
			intent = new Intent(this, Comics.class);
			intent.putExtra(Comics.INTENT_COMICS_TYPE, Comics.VIEWTYPE_READ);
			intent.putExtra(Comics.INTENT_COMICS_VALUE, "0");
			intent.putExtra(Comics.INTENT_COMICS_HEADING, getString(R.string.comics_heading_read));
			startActivity(intent);	        	
			break;
		case 4:
			intent = new Intent(this, Add.class);
			startActivity(intent);
			break;
		case 5:
			intent = new Intent(this, Borrow.class);
			startActivity(intent);
			break;
		case 6:
			intent = new Intent(this, Settings.class);
			startActivity(intent);
			break;
		}
		mDrawer.closeDrawer(mDrawerList);
	}	
}
