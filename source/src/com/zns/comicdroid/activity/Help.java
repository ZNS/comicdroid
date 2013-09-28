package com.zns.comicdroid.activity;

import org.sufficientlysecure.donations.DonationsFragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.zns.comicdroid.Application;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.activity.fragment.HelpAboutFragment;
import com.zns.comicdroid.activity.fragment.HelpMainFragment;
import com.zns.comicdroid.activity.fragment.HelpStatsFragment;

public class Help extends BaseFragmentActivity implements ActionBar.TabListener {
	private static final int TAB_COUNT = Application.DEBUG ? 4 : 3;
	private static final String TAB_HELP = "TABHELP";
	private static final String TAB_DONATE = "TABDONATE";
	private static final String TAB_ABOUT = "TABABOUT";
	private static final String TAB_STATS = "TABSTATS";
	
    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApX60N74KqgXjTljUw477SsjETTouXg16b2ANSY0JwpMx/kmSyYzVkWVQWksv0pxZFGvHPmojI3ezgQ9S6zKpLTfFxRrhJ8qkHB5HvU08kRu6hkLPK6I1dcCniyQRvvkwVEA4THLd0wOGUHE7ZCjI/AzAzI1BtMIxpbC2FZZcq+ZVYPfC8E3xLQS4pS8dao0PfDwfGc5wojO156DQjXVLSRrznFubgSdgwo+eDQ+AtQBZ4QAfhdxPWecreWHJtW7QSsXfM20ob6KLjc7FPanB1Bd4p1DBUPdswqlJuun7RDOC/AFLqYDRwRKBEO5xwyYqAkXNfAxCaRwTECMrlvJzYQIDAQAB";
    private static final String[] GOOGLE_CATALOG = new String[]{"comicdroid.donation.1","comicdroid.donation.3", "comicdroid.donation.5"};
	private static final String FLATTR_PROJECT_URL = "https://github.com/ZNS/comicdroid/";
	
	
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
		
		if (Application.DEBUG)
		{
			ActionBar.Tab tab3 = getSupportActionBar().newTab();
			tab3.setText(res.getString(R.string.help_tab_stats));
			tab3.setTag(TAB_STATS);
			tab3.setTabListener(this);
			getSupportActionBar().addTab(tab3);
		}
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
			if (tag.equals(TAB_STATS)) {
				return HelpStatsFragment.newInstance();
			}			
			if (tag.equals(TAB_DONATE)) {
				DonationsFragment fragment = DonationsFragment.newInstance(Application.DEBUG, 
						true, GOOGLE_PUBKEY, GOOGLE_CATALOG, getResources().getStringArray(R.array.donation_google_catalog_values), false, null, null, null, true, FLATTR_PROJECT_URL, FLATTR_PROJECT_URL);
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
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(makeFragmentName(1));
        if (fragment != null) {
            ((DonationsFragment) fragment).onActivityResult(requestCode, resultCode, data);
        }
    }	
    
    private String makeFragmentName(int index) {
        return "android:switcher:" + mViewPager.getId() + ":" + index;
    }    
}
