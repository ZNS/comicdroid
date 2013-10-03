/*******************************************************************************
 * Copyright (c) 2013 Ulrik Andersson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Ulrik Andersson - initial API and implementation
 ******************************************************************************/
package com.zns.comicdroid.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.ComicAdapter;
import com.zns.comicdroid.adapter.ExpandableAmazonAdapter;
import com.zns.comicdroid.amazon.AmazonSearchTask;
import com.zns.comicdroid.data.Group;
import com.zns.comicdroid.dialog.GroupDialogFragment;
import com.zns.comicdroid.dialog.RenameDialogFragment;

public class Comics extends BaseFragmentActivity
implements	LoaderCallbacks<Cursor>,
RenameDialogFragment.OnRenameDialogListener,
OnCheckedChangeListener {

	public static final String INTENT_COMICS_TYPE = "com.zns.comic.COMICS_TYPE";
	public static final String INTENT_COMICS_VALUE = "com.zns.comic.COMICS_VALUE";
	public static final String INTENT_COMICS_HEADING = "com.zns.comic.COMICS_HEADING";
	public static final String INTENT_COMICS_ID = "com.zns.comics.COMICS_ID";

	public static final int VIEWTYPE_GROUP = 1;
	public static final int VIEWTYPE_AUTHOR = 2;
	public static final int VIEWTYPE_PUBLISHER = 3;
	public static final int VIEWTYPE_ILLUSTRATOR = 4;
	public static final int VIEWTYPE_READ = 5;

	private SQLiteCursorLoader mLoader;
	private ComicAdapter mAdapter;
	private ListView mLvComics;
	private TextView mTvHeading;	
	private int mViewType;
	private String mViewWhereValue;
	private boolean mDeleteComics = false;
	private int mGroupId;
	private String mHeading;
	private Group mCurrentGroup;
	private CheckBox mCbIsWatched;
	private CheckBox mCbIsFinished;
	private CheckBox mCbIsComplete;
	private TextView mTvEmpty;
	private ExpandableAmazonAdapter mAmazonAdapter;
	private ExpandableListView mElvAmazon;
	private ProgressDialog mProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_comics);
		super.onCreate(savedInstanceState);

		mLvComics = (ListView)findViewById(R.id.comics_lvComics);
		mTvHeading = (TextView)findViewById(R.id.comics_txtHeading);
		mTvEmpty = (TextView)findViewById(R.id.comics_tvEmpty);
		mElvAmazon = (ExpandableListView)findViewById(R.id.comics_elvBooks);
		
		mAdapter = new ComicAdapter(this, getImagePath(true));
		mLvComics.setAdapter(mAdapter);
		mLvComics.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				int comicId = mAdapter.getComicId(position);
				Intent intent = new Intent(Comics.this, ComicView.class);
				intent.putExtra(ComicView.INTENT_COMIC_ID, comicId);
				startActivity(intent);
			}
		});

		Intent intent = getIntent();
		mViewType = intent.getIntExtra(INTENT_COMICS_TYPE, 0);
		mViewWhereValue = intent.getCharSequenceExtra(INTENT_COMICS_VALUE).toString();
		mHeading = intent.getCharSequenceExtra(INTENT_COMICS_HEADING).toString();
		if (mHeading.length() == 0) {
			mHeading = getString(R.string.list_name_na);
		}
		mTvHeading.setText(mHeading);

		if (mViewType == VIEWTYPE_GROUP) {
			mGroupId = intent.getIntExtra(INTENT_COMICS_ID, 0);
			mCurrentGroup = getDBHelper().getGroup(mGroupId);
			mCbIsWatched = (CheckBox)findViewById(R.id.comics_cbWatched);
			mCbIsWatched.setChecked(mCurrentGroup.getIsWatched());
			mCbIsWatched.setOnCheckedChangeListener(this);
			mCbIsFinished = (CheckBox)findViewById(R.id.comics_cbFinished);
			mCbIsFinished.setChecked(mCurrentGroup.getIsFinished());
			mCbIsFinished.setOnCheckedChangeListener(this);
			mCbIsComplete = (CheckBox)findViewById(R.id.comics_cbComplete);
			mCbIsComplete.setChecked(mCurrentGroup.getIsComplete());
			mCbIsComplete.setOnCheckedChangeListener(this);
			mAdapter.mRenderTitle = false;
			findViewById(R.id.comics_group_alts).setVisibility(View.VISIBLE);
			registerForContextMenu(mLvComics);
		}

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdapter != null)
			mAdapter.notifyDataSetChanged();
	}

	@SuppressLint("NewApi")
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    //Set indicator on the right side, it gets distorted but I have no idea why
	    int margin = dpToPx(5);
	    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
	    	mElvAmazon.setIndicatorBounds(mElvAmazon.getRight() - (dpToPx(48) + margin), mElvAmazon.getRight() - margin);
	    } 
	    else {
	    	mElvAmazon.setIndicatorBoundsRelative(mElvAmazon.getRight() - (dpToPx(48) + margin), mElvAmazon.getRight() - margin);
	    }
	}
	
	public int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}    
	
	//Loader Implementation
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		String sql = "SELECT _id AS _id, Title, Subtitle, Author, Image, Issue, IsBorrowed, IsRead, Rating FROM tblBooks ";

		switch (mViewType) {
		case VIEWTYPE_GROUP:
			sql += "WHERE GroupId = ? ORDER BY Issue, Title";
			break;
		case VIEWTYPE_AUTHOR:
			sql += "WHERE Author = ? ORDER BY Title, Issue";
			break;
		case VIEWTYPE_PUBLISHER:
			sql += "WHERE Publisher = ? ORDER BY Title, Issue";
			break;
		case VIEWTYPE_ILLUSTRATOR:
			sql += "WHERE Illustrator = ? ORDER BY Title, Issue";
			break;
		case VIEWTYPE_READ:
			sql += " WHERE IsRead = ? ORDER BY Title, Issue";
			break;
		}

		mLoader = new SQLiteCursorLoader(Comics.this, getDBHelper(), sql, new String[] { mViewWhereValue });
		return(mLoader);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		if (cursor != null && cursor.getCount() <= 0) {
			if (mViewType == VIEWTYPE_READ) {
				mTvEmpty.setText(R.string.read_empty);
				mTvEmpty.setVisibility(View.VISIBLE);
			}
		}
		else {
			mTvEmpty.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar_view, (com.actionbarsherlock.view.Menu) menu);
		if (mViewType == VIEWTYPE_READ) {
			menu.findItem(R.id.menu_edit).setVisible(false);
		}
		if (mViewType == VIEWTYPE_GROUP) {
			//Show edit all
			menu.findItem(R.id.submenu_view).setVisible(true);
		}
		return true;
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_edit:
			if (mViewType == VIEWTYPE_GROUP)
			{
				GroupDialogFragment dialogGroup = GroupDialogFragment.newInstance(mGroupId, mCurrentGroup.getName(), mCurrentGroup.getTotalBookCount());
				dialogGroup.show(getSupportFragmentManager(), "GROUPDIALOG");
			}
			else
			{
				RenameDialogFragment dialogRename = new RenameDialogFragment();
				dialogRename.setName(mHeading);
				dialogRename.show(getSupportFragmentManager(), "RENAMEDIALOG");
			}
			return true;
		case R.id.menu_delete:
			new AlertDialog.Builder(this)
			.setTitle(R.string.group_delete_title)
			.setMultiChoiceItems(new String[] {getString(R.string.group_delete_alt)}, null, new DialogInterface.OnMultiChoiceClickListener() {					
				@Override
				public void onClick(DialogInterface arg0, int pos, boolean checked) {
					mDeleteComics = checked;						
				}
			})
			.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					getDBHelper().deleteGroup(mGroupId, mDeleteComics);
					//Backup
					BackupManager m = new BackupManager(Comics.this);
					m.dataChanged();
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
		case R.id.menu_editall:
			Intent intent = new Intent(this, Edit.class);
			int[] ids = mAdapter.getComicIds();
			intent.putExtra(Edit.INTENT_COMIC_IDS, ids);
			startActivity(intent);        		
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDialogPositiveClick(String oldName, String newName) {
		switch (mViewType) {
		case VIEWTYPE_AUTHOR:
			getDBHelper().renameAuthor(oldName, newName);
			break;
		case VIEWTYPE_PUBLISHER:
			getDBHelper().renamePublisher(oldName, newName);
			break;				
		case VIEWTYPE_ILLUSTRATOR:
			getDBHelper().renameIllustrator(oldName, newName);
			break;
		}
		mTvHeading.setText(newName);
		//Backup
		BackupManager m = new BackupManager(this);
		m.dataChanged();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (mViewType == VIEWTYPE_GROUP) {
			getMenuInflater().inflate(R.menu.collection_context_menu, menu);
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (item.getItemId() == R.id.menu_collection_setimage) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			if (info.position >= 0) {
				int comicId = mAdapter.getComicId(info.position);
				getDBHelper().setGroupImage(comicId);
			}
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == mCbIsFinished)
			getDBHelper().setGroupIsFinished(mGroupId, isChecked);
		else if (buttonView == mCbIsWatched)
			getDBHelper().setGroupIsWatched(mGroupId, isChecked);
		else if (buttonView == mCbIsComplete)
			getDBHelper().setGroupIsComplete(mGroupId, isChecked);		
	}
	
	public void searchAmazon(View view) {
		String cachePath = getExternalFilesDir(null).toString() + "/amazoncache";
		AmazonSearchTask.AmazonSearchTaskRequest req = new AmazonSearchTask.AmazonSearchTaskRequest();
		if (mViewType == VIEWTYPE_AUTHOR) {
			req.query = AmazonSearchTask.getAuthorQuery(mHeading);
		}
		else if (mViewType == VIEWTYPE_GROUP) {
			req.query = AmazonSearchTask.getGroupQuery(mHeading);
		}
		else if (mViewType == VIEWTYPE_ILLUSTRATOR) {
			req.query = AmazonSearchTask.getIllustratorQuery(mHeading);
		}		
		else if (mViewType == VIEWTYPE_PUBLISHER) {
			req.query = AmazonSearchTask.getPublisherQuery(mHeading);
		}		
		req.orderBy = "daterank";		
		req.cachePath = cachePath;
		new AmazonSearchTask() {
			@Override
			protected void onPreExecute() {
				mProgress = new ProgressDialog(Comics.this);
				mProgress.setTitle(R.string.amazon_search_progress);
				mProgress.setCancelable(false);
				mProgress.setIndeterminate(true);
				mProgress.show();
			}
			
			@Override
			protected void onPostExecute(AmazonSearchTask.AmazonSearchTaskResponse result) {
				if (mAmazonAdapter == null) {
					mAmazonAdapter = new ExpandableAmazonAdapter(Comics.this, result.books);
					mElvAmazon.setAdapter(mAmazonAdapter);
					mElvAmazon.setVisibility(View.VISIBLE);
				}
				else {
					mAmazonAdapter.notifyDataSetChanged();
				}
				
				if (mProgress!= null) {
					mProgress.dismiss();
				}
			}
		}.execute(req);		
	}
}
