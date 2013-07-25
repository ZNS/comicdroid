package com.zns.comicdroid.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.ComicAdapter;
import com.zns.comicdroid.dialog.RenameDialogFragment;
import com.zns.comicdroid.service.UploadService;

public class Comics extends BaseFragmentActivity
	implements	LoaderCallbacks<Cursor>, RenameDialogFragment.OnRenameDialogListener {
	
	public static final String INTENT_COMICS_TYPE = "com.zns.comic.COMICS_TYPE";
	public static final String INTENT_COMICS_VALUE = "com.zns.comic.COMICS_VALUE";
	public static final String INTENT_COMICS_HEADING = "com.zns.comic.COMICS_HEADING";
	public static final String INTENT_COMICS_ID = "com.zns.comics.COMICS_ID";
	
	public static final int VIEWTYPE_GROUP = 1;
	public static final int VIEWTYPE_AUTHOR = 2;
	public static final int VIEWTYPE_PUBLISHER = 3;
	
	private SQLiteCursorLoader loader;
	private ComicAdapter adapter;
	private ListView lvComics;
	private TextView tvHeading;	
	private int viewType;
	private String viewWhereValue;
	private boolean deleteComics = false;
	private int groupId;
	private String heading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_comics);
		super.onCreate(savedInstanceState);
		
		lvComics = (ListView)findViewById(R.id.comics_lvComics);
		tvHeading = (TextView)findViewById(R.id.comics_txtHeading);
		
		adapter = new ComicAdapter(this);
		lvComics.setAdapter(adapter);
		
		lvComics.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				int comicId = adapter.getComicId(position);
				Intent intent = new Intent(Comics.this, ComicView.class);
				intent.putExtra(ComicView.INTENT_COMIC_ID, comicId);
				startActivity(intent);
			}
		});
		
		Intent intent = getIntent();
	    viewType = intent.getIntExtra(INTENT_COMICS_TYPE, 0);
	    viewWhereValue = intent.getCharSequenceExtra(INTENT_COMICS_VALUE).toString();
	    heading = intent.getCharSequenceExtra(INTENT_COMICS_HEADING).toString();
	    if (viewType == VIEWTYPE_GROUP) {
	    	groupId = intent.getIntExtra(INTENT_COMICS_ID, 0);
	    }	    
	    tvHeading.setText(heading);
	    
	    getSupportLoaderManager().initLoader(0, null, this);
	}

	//Loader Implementation
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		String sql = "SELECT _id AS _id, Title, Subtitle, Author, Image, Issue, IsBorrowed FROM tblBooks ";
		
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar_view, (com.actionbarsherlock.view.Menu) menu);
		if (viewType != VIEWTYPE_GROUP) {
			//Hide delete
			menu.getItem(1).setVisible(false);
		}
		return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        	case R.id.menu_edit:
				RenameDialogFragment dialogRename = new RenameDialogFragment();
				dialogRename.setName(heading);
				dialogRename.show(getSupportFragmentManager(), "RENAMEDIALOG");
	            return true;
        	case R.id.menu_delete:
        		new AlertDialog.Builder(this)
        	    .setTitle(R.string.group_delete_title)
        	    .setMultiChoiceItems(new String[] {getString(R.string.group_delete_alt)}, null, new DialogInterface.OnMultiChoiceClickListener() {					
					@Override
					public void onClick(DialogInterface arg0, int pos, boolean checked) {
						deleteComics = checked;						
					}
				})
        	    .setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) { 
        	        	getDBHelper().deleteGroup(groupId, deleteComics);
        				//Sync with google drive
        				Intent intent = new Intent(Comics.this, UploadService.class);
        				startService(intent);
        				//Back to start
        	        	Intent intent2 = new Intent(Comics.this, Start.class);
        	        	startActivity(intent2);
        	        	finish();
        	        }
        	     })
        	    .setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
        	        public void onClick(DialogInterface dialog, int which) {
        	        	dialog.cancel();
        	        }
        	     })
        	     .show();        		
        		return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDialogPositiveClick(String oldName, String newName) {
		switch (viewType) {
			case VIEWTYPE_GROUP:
				getDBHelper().renameGroup(groupId, newName);
				break;
			case VIEWTYPE_AUTHOR:
				getDBHelper().renameAuthor(oldName, newName);
				break;
			case VIEWTYPE_PUBLISHER:
				getDBHelper().renamePublisher(oldName, newName);
				break;				
		}
		//Sync with google drive
		Intent intent = new Intent(Comics.this, UploadService.class);
		startService(intent);
	}
}
