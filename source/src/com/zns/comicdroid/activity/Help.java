package com.zns.comicdroid.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;

public class Help extends BaseFragmentActivity implements ActionBar.TabListener {
	private static final int TAB_HELP = 1;
	private static final int TAB_STATS = 2;
	private static final int TAB_ABOUT = 3;
	
	private Fragment mFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_help);
		super.onCreate(savedInstanceState);
		
		final Resources res = getResources();
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		/*ActionBar.Tab tab0 = getSupportActionBar().newTab();
		tab0.setText(res.getString(R.string.help_tab_help));
		tab0.setTag(TAB_HELP);
		tab0.setTabListener(this);
		getSupportActionBar().addTab(tab0);

		ActionBar.Tab tab1 = getSupportActionBar().newTab();
		tab1.setText(res.getString(R.string.help_tab_stats));
		tab1.setTag(TAB_STATS);
		tab1.setTabListener(this);
		getSupportActionBar().addTab(tab1);

		ActionBar.Tab tab2 = getSupportActionBar().newTab();
		tab2.setText(res.getString(R.string.help_tab_about));
		tab2.setTag(TAB_ABOUT);
		tab2.setTabListener(this);
		getSupportActionBar().addTab(tab2);	*/	
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int currentTab = (Integer)tab.getTag();
		if (mFragment == null) {
			switch (currentTab) {
			case TAB_HELP:
				break;
			case TAB_STATS:
				break;
			case TAB_ABOUT:
				break;
			}
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (mFragment != null) {
			ft.detach(mFragment);
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
}
