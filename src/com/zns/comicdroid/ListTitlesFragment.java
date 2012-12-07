package com.zns.comicdroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.zns.comicdroid.data.ComicAdapter;
import com.zns.comicdroid.data.DBHelper;

public class ListTitlesFragment extends BaseListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		adapter = new ComicAdapter(getActivity());		
		
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				int comicId = getAdapter().getComicId(position);
				Intent intent = new Intent(getActivity(), ComicView.class);
				intent.putExtra("com.zns.comic.COMICID", comicId);
				startActivity(intent);
			}			
		});		
		registerForContextMenu(listView);
		
		return view;
	}

	private ComicAdapter getAdapter()
	{
		return (ComicAdapter)adapter;
	}
	
	@Override
	protected String getSQLDefault() {
		return "SELECT Id AS _id, Title, Subtitle, Author, Image, Issue FROM tblBooks ORDER BY Title";
	}
	
	@Override
	protected String getSQLFilter() {
		return "SELECT Id AS _id, Title, Subtitle, Author, Image, Issue FROM tblBooks WHERE Title LIKE ? ORDER BY Title";
	}
	
	//Create Context Menu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.start_context_menu, menu);
	}
	
	//Handle click on Context Menu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int comicId = getAdapter().getComicId((int)info.id);
		if (comicId > 0)
		{
			switch (item.getItemId()) {
				case R.id.start_context_delete:
					listView.removeViews((int)info.id, 1);
					DBHelper db = new DBHelper(getActivity());
					db.deleteComic(comicId);
					return true;
			}
		}
		return super.onContextItemSelected(item);
	}		
}
