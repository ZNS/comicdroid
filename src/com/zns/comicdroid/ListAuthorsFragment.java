package com.zns.comicdroid;

import com.zns.comicdroid.data.GroupedItemAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ListAuthorsFragment extends BaseListFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);	 

		adapter = new GroupedItemAdapter(getActivity());		
		
		return view;
	}
	
	private GroupedItemAdapter getAdapter()
	{
		return (GroupedItemAdapter)adapter;
	}
	
	@Override
	protected String getSQLDefault() {
		return "SELECT 0 AS _id, Author AS Name, COUNT(*) AS Count FROM tblBooks GROUP BY Author ORDER BY Author";
	}
	
	@Override
	protected String getSQLFilter() {
		return "SELECT 0 AS _id, Author AS Name, COUNT(*) AS Count FROM tblBooks WHERE Author LIKE ? GROUP BY Author ORDER BY Author";
	}	
}