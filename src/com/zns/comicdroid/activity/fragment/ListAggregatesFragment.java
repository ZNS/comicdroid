package com.zns.comicdroid.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.zns.comicdroid.BaseListFragment;
import com.zns.comicdroid.activity.ComicView;
import com.zns.comicdroid.activity.Comics;
import com.zns.comicdroid.adapter.AggregateAdapter;
import com.zns.comicdroid.data.Aggregate;

public class ListAggregatesFragment extends BaseListFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);	 

		adapter = new AggregateAdapter(getActivity());		
		
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				Aggregate aggregate = getAdapter().getAggregate(position);
				if (aggregate != null)
				{
					if (aggregate.getType() == 1)
					{
						Intent intent = new Intent(getActivity(), ComicView.class);
						intent.putExtra(ComicView.INTENT_COMIC_ID, aggregate.getId());
						startActivity(intent);
					}
					else if (aggregate.getType() == 2)
					{
						Intent intent = new Intent(getActivity(), Comics.class);
						intent.putExtra(Comics.INTENT_COMICS_TYPE, Comics.VIEWTYPE_GROUP);
						intent.putExtra(Comics.INTENT_COMICS_VALUE, Integer.toString(aggregate.getId()));
						intent.putExtra(Comics.INTENT_COMICS_HEADING, aggregate.getTitle());
						startActivity(intent);
					}
				}
			}			
		});
		
		return view;
	}
	
	private AggregateAdapter getAdapter() {
		return (AggregateAdapter)adapter;
	}
	
	@Override
	public String getSQLDefault() {
		return "SELECT _id, Title, Subtitle, Author, Image, 1 AS ItemType, 0 AS BookCount, IsBorrowed FROM tblBooks WHERE GroupId = 0 OR ifnull(GroupId, '') = '' " +
				"UNION " +
				"SELECT _id, Name AS Title, '' AS Subtitle, '' AS Author, Image, 2 AS ItemType, BookCount, 0 AS IsBorrowed FROM tblGroups " +
				"ORDER BY Title";
	}
	
	@Override
	public String getSQLFilter() {
		return "SELECT _id, Title, Subtitle, Author, Image, 1 AS ItemType, 0 AS BookCount, IsBorrowed FROM tblBooks WHERE (GroupId = 0 OR ifnull(GroupId, '') = '') AND Title LIKE ? " +
				"UNION " +
				"SELECT _id AS _id, Name AS Title, '' AS Subtitle, '' AS Author, Image, 2 AS ItemType, BookCount, 0 AS IsBorrowed FROM tblGroups WHERE Title LIKE ? " +
				"ORDER BY Title";
	}	
}