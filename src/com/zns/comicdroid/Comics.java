package com.zns.comicdroid;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.zns.comicdroid.data.ComicAdapter;

public class Comics extends BaseFragmentActivity
	implements	LoaderCallbacks<Cursor> {
	
	public static final int VIEWTYPE_GROUP = 1;
	public static final int VIEWTYPE_AUTHOR = 2;
	public static final int VIEWTYPE_PUBLISHER = 3;
	
	private SQLiteCursorLoader loader;
	private ComicAdapter adapter;
	private ListView lvComics;
	private TextView tvHeading;	
	private int viewType;
	private String viewWhereValue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comics);
		
		lvComics = (ListView)findViewById(R.id.comics_lvComics);
		tvHeading = (TextView)findViewById(R.id.comics_txtHeading);
		
		adapter = new ComicAdapter(this);
		lvComics.setAdapter(adapter);
		
		lvComics.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				int comicId = adapter.getComicId(position);
				Intent intent = new Intent(Comics.this, ComicView.class);
				intent.putExtra("com.zns.comic.COMICID", comicId);
				startActivity(intent);
			}
		});
		
		Intent intent = getIntent();
	    viewType = intent.getIntExtra("com.zns.comic.COMICS_TYPE", 0);
	    viewWhereValue = intent.getCharSequenceExtra("com.zns.comic.COMICS_VALUE").toString();
	    String heading = intent.getCharSequenceExtra("com.zns.comic.COMICS_HEADING").toString();
	    tvHeading.setText(heading);
	    
	    getSupportLoaderManager().initLoader(0, null, this);
	}

	//Loader Implementation
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		String sql = "SELECT Id AS _id, Title, Subtitle, Author, Image, Issue, IsBorrowed FROM tblBooks ";
		
		switch (viewType) {
			case VIEWTYPE_GROUP:
				sql += "WHERE GroupId = ? ORDER BY Issue, Title";
				break;
			case VIEWTYPE_AUTHOR:
				sql += "WHERE Author = ? ORDER BY Title, Issue";
				break;
			case VIEWTYPE_PUBLISHER:
				sql += "WHERE Publisher = ? ORDER BY Title, Issue";
				break;
		}
			
		loader = new SQLiteCursorLoader(Comics.this, getDBHelper(), sql, new String[] { viewWhereValue });
		return(loader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);
	}	
}
