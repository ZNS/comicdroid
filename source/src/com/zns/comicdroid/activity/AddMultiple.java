package com.zns.comicdroid.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.activity.fragment.AddMultipleIssuesFragment;
import com.zns.comicdroid.activity.fragment.AddMultipleSearchFragment;
import com.zns.comicdroid.activity.fragment.AddMultipleSearchFragment.OnSeriesSelectedListener;

public class AddMultiple extends BaseFragmentActivity implements OnSeriesSelectedListener {
	
	private final static String STATE_ACTIVE_FRAGMENT = "ACTIVEFRAGMENT";
	private final static int FRAGMENT_SEARCH = 1;
	private final static int FRAGMENT_ISSUES = 2;
	private int mActiveFragment = FRAGMENT_SEARCH;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_add_multiple);
		super.onCreate(savedInstanceState);
		
		FragmentManager fragMgr = getSupportFragmentManager();
		FragmentTransaction transaction = fragMgr.beginTransaction();
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVE_FRAGMENT) && savedInstanceState.getInt(STATE_ACTIVE_FRAGMENT) == FRAGMENT_ISSUES) {
			Fragment fragmentIssues = fragMgr.findFragmentByTag("FRAGMENT_ISSUES");
			if (fragmentIssues != null) {
				mActiveFragment = FRAGMENT_ISSUES;
				transaction.replace(R.id.add_multiple_fragments, fragmentIssues, "FRAGMENT_ISSUES").commit();
			}
		}
		else
		{
			Fragment fragmentSearch = fragMgr.findFragmentByTag("FRAGMENT_SEARCH");
			if (fragmentSearch == null) {
				fragmentSearch = AddMultipleSearchFragment.newInstance();
			}
			transaction.replace(R.id.add_multiple_fragments, fragmentSearch, "FRAGMENT_SEARCH").commit();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_ACTIVE_FRAGMENT, mActiveFragment);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onSeriesSelected(int seriesId) {
		AddMultipleIssuesFragment fragment = AddMultipleIssuesFragment.newInstance();
		Bundle bundle = new Bundle();
		bundle.putInt(AddMultipleIssuesFragment.ARGUMENT_SERIES_ID, seriesId);
		fragment.setArguments(bundle);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.add_multiple_fragments, fragment, "FRAGMENT_ISSUES")
			.addToBackStack(null)
			.commit();
		mActiveFragment = FRAGMENT_ISSUES;
	}	
}
