package com.zns.comicdroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class BaseListFragment extends SherlockFragment {
	
	public interface OnFragmentStartedListener {
		public void onStarted();
	}
	
	public OnFragmentStartedListener fragmentStartedCallback = null;
	protected SimpleCursorAdapter adapter;	
	protected ListView listView;

	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);	    
	    if (activity instanceof OnFragmentStartedListener) {
	    	fragmentStartedCallback = (OnFragmentStartedListener)activity;
	    }
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_baselist, container, false);	
		listView = (ListView)view.findViewById(R.id.fragment_baseListView);				
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (fragmentStartedCallback != null) {
			fragmentStartedCallback.onStarted();
		}
	}
	
	public void BindList()
	{
		listView.setAdapter(adapter);
	}
	
	protected String getSQLDefault() {
		return "";
	}
	
	protected String getSQLFilter() {
		return "";
	} 
	
	protected int[] getItemIds() {
		return null;
	}
}
