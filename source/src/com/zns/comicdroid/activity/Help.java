package com.zns.comicdroid.activity;

import org.sufficientlysecure.donations.DonationsFragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.activity.fragment.HelpAboutFragment;
import com.zns.comicdroid.activity.fragment.HelpMainFragment;

public class Help extends BaseFragmentActivity implements ActionBar.TabListener {
	private static final int TAB_COUNT = 3;
	private static final String TAB_HELP = "TABHELP";
	private static final String TAB_DONATE = "TABDONATE";
	private static final String TAB_ABOUT = "TABABOUT";
	private ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_help);
		super.onCreate(savedInstanceState);
		
		HelpFragmentPagerAdapter adapter = new HelpFragmentPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager)findViewById(R.id.help_viewPager);
		mViewPager.setAdapter(adapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});
		
		final Resources res = getResources();
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		ActionBar.Tab tab0 = getSupportActionBar().newTab();
		tab0.setText(res.getString(R.string.help_tab_help));
		tab0.setTag(TAB_HELP);
		tab0.setTabListener(this);
		getSupportActionBar().addTab(tab0);

		ActionBar.Tab tab1 = getSupportActionBar().newTab();
		tab1.setText(res.getString(R.string.help_tab_donate));
		tab1.setTag(TAB_DONATE);
		tab1.setTabListener(this);
		getSupportActionBar().addTab(tab1);

		ActionBar.Tab tab2 = getSupportActionBar().newTab();
		tab2.setText(res.getString(R.string.help_tab_about));
		tab2.setTag(TAB_ABOUT);
		tab2.setTabListener(this);
		getSupportActionBar().addTab(tab2);	
	}

	class HelpFragmentPagerAdapter extends FragmentPagerAdapter {

		public HelpFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			String tag = (String)getSupportActionBar().getTabAt(pos).getTag();
			if (tag.equals(TAB_HELP)) {
				return HelpMainFragment.newInstance();
			}
			if (tag.equals(TAB_ABOUT)) {
				return HelpAboutFragment.newInstance();
			}
			if (tag.equals(TAB_DONATE)) {
				DonationsFragment fragment = DonationsFragment.newInstance(false, 
						false, null, null, null, false, null, null, null, true, "https://github.com/dschuermann/android-donations-lib/", "flattr.com/thing/712895/dschuermannandroid-donations-lib-on-GitHub");
				return fragment;
			}			
			return null;
		}

		@Override
		public int getCount() {
			return TAB_COUNT;
		}		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (mViewPager != null) {
			mViewPager.setCurrentItem(tab.getPosition());
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
}
