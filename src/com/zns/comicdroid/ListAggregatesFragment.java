package com.zns.comicdroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zns.comicdroid.data.AggregateAdapter;

public class ListAggregatesFragment extends BaseListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);	 

		adapter = new AggregateAdapter(getActivity());		
		
		return view;
	}
	
	@Override
	protected String getSQLDefault() {
		return "SELECT Id AS _id, Title, Subtitle, Author, Image, 1 AS ItemType FROM tblBooks WHERE GroupId = 0 OR ifnull(GroupId, '') = '' " +
				"UNION " +
				"SELECT Id AS _id, Name AS Title, '' AS Subtitle, '' AS Author, Image, 2 AS ItemType FROM tblGroups " +
				"ORDER BY Title";
	}
	
	@Override
	protected String getSQLFilter() {
		return getSQLDefault();
	}	
}