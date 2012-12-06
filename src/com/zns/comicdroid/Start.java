package com.zns.comicdroid;

import com.zns.comicdroid.data.ComicAdapter;
import android.os.Bundle;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Start extends BaseListFragmentActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	 

		adapter = new ComicAdapter(this);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				int comicId = getAdapter().getComicId(position);
				Intent intent = new Intent(Start.this, ComicView.class);
				intent.putExtra("com.zns.comic.COMICID", comicId);
				startActivity(intent);
			}			
		});		
		registerForContextMenu(listView);
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
		getMenuInflater().inflate(R.menu.start_context_menu, menu);
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
					db.deleteComic(comicId);
					return true;
			}
		}
		return super.onContextItemSelected(item);
	}	
}