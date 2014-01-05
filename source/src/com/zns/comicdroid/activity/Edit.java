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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.AutoCompleteAdapter;
import com.zns.comicdroid.adapter.ComicArrayAdapter;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.Group;
import com.zns.comicdroid.dialog.GroupDialogFragment;
import com.zns.comicdroid.util.ImageHandler;
import com.zns.comicdroid.widget.AndroidSlidingDrawer;

public class Edit extends BaseFragmentActivity
implements	OnClickListener, 
GroupDialogFragment.OnGroupAddDialogListener {

	public static final String INTENT_COMIC_IDS = "com.zns.comic.COMICIDS";
	private static final int CAMERA_REQUEST = 1888; 

	private DateFormat mDateFormat;
	private List<Comic> mComics = null;
	private EditText mEtTitle;
	private EditText mEtSubtitle;
	private EditText mEtIssue;
	private AutoCompleteTextView mEtAuthor;
	private AutoCompleteTextView mEtIllustrator;
	private AutoCompleteTextView mEtPublisher;	
	private EditText mEtPublished;
	private EditText mEtAdded;
	private EditText mEtPageCount;
	private EditText mEtIssues;
	private ImageView mIvImage;
	private String mNewImage = null;
	private Spinner mSpGroup;
	private ImageView mIvGroupAdd;
	private RelativeLayout mRowIssue;
	private RelativeLayout mRowPublishDate;
	private RelativeLayout mRowAdded;
	private RelativeLayout mRowPageCount;
	private RelativeLayout mRowIssues;

	private ArrayAdapter<Group> mAdapterGroups;		
	private AutoCompleteAdapter mAdapterAuthors;
	private AutoCompleteAdapter mAdapterIllustrators;
	private AutoCompleteAdapter mAdapterPublisher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_edit);
		super.onCreate(savedInstanceState);

		mDateFormat = android.text.format.DateFormat.getDateFormat(this);
		mEtTitle = (EditText)findViewById(R.id.comicEdit_etTitle);
		mEtSubtitle = (EditText)findViewById(R.id.comicEdit_etSubtitle);
		mEtIssue = (EditText)findViewById(R.id.comicEdit_etIssue);
		mEtIssues = (EditText)findViewById(R.id.comicEdit_etIssues);
		mEtAuthor = (AutoCompleteTextView)findViewById(R.id.comicEdit_actAuthor);
		mEtIllustrator = (AutoCompleteTextView)findViewById(R.id.comicEdit_actIllustrator);
		mEtPublisher = (AutoCompleteTextView)findViewById(R.id.comicEdit_actPublisher);
		mEtPublished = (EditText)findViewById(R.id.comicEdit_etPublished);
		mEtAdded = (EditText)findViewById(R.id.comicEdit_etAdded);
		mEtPageCount = (EditText)findViewById(R.id.comicEdit_etPageCount);
		mIvImage = (ImageView)findViewById(R.id.comicEdit_ivImage);		
		mSpGroup = (Spinner)findViewById(R.id.comicEdit_spGroup);
		mIvGroupAdd = (ImageView)findViewById(R.id.comicEdit_ivGroupAdd);
		mRowIssue = (RelativeLayout)findViewById(R.id.comicEdit_issue);
		mRowPublishDate = (RelativeLayout)findViewById(R.id.comicEdit_publishDate);
		mRowAdded = (RelativeLayout)findViewById(R.id.comicEdit_added);
		mRowPageCount = (RelativeLayout)findViewById(R.id.comicEdit_pageCount);
		mRowIssues = (RelativeLayout)findViewById(R.id.comicEdit_issues);
		AndroidSlidingDrawer drawer = (AndroidSlidingDrawer)findViewById(R.id.comicEdit_drawer);

		Intent intent = getIntent();
		int[] comicIds = intent.getIntArrayExtra(INTENT_COMIC_IDS);

		//Spinner groups
		List<Group> groups = getDBHelper().getGroups();
		if (groups == null)
			groups = new ArrayList<Group>();
		groups.add(0, new Group(0, getResources().getString(R.string.common_nogroup), null, 0, 0, 0, 0, 0));
		mAdapterGroups = new ArrayAdapter<Group>(this, android.R.layout.simple_spinner_item, groups);
		mAdapterGroups.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpGroup.setAdapter(mAdapterGroups);

		//Dialog
		mIvGroupAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment dialogAddGroup = new GroupDialogFragment();
				dialogAddGroup.show(getSupportFragmentManager(), "GROUPADD");
			}
		});

		//Autocomplete author
		mAdapterAuthors = new AutoCompleteAdapter(this, 
				"SELECT DISTINCT 0 AS _id, Author FROM tblBooks WHERE Author LIKE ? ORDER BY Author", 
				"Author", 
				1); 	    	
		mEtAuthor.setThreshold(3);
		mEtAuthor.setAdapter(mAdapterAuthors);

		//Autocomplete illustrator
		mAdapterIllustrators = new AutoCompleteAdapter(this, 
				"SELECT DISTINCT 0 AS _id, Illustrator FROM tblBooks WHERE Illustrator LIKE ? ORDER BY Illustrator", 
				"Illustrator", 
				1); 	    	
		mEtIllustrator.setThreshold(3);
		mEtIllustrator.setAdapter(mAdapterIllustrators);

		//Autocomplete publisher
		mAdapterPublisher = new AutoCompleteAdapter(this, 
				"SELECT DISTINCT 0 AS _id, Publisher FROM tblBooks WHERE Publisher LIKE ? ORDER BY Publisher", 
				"Publisher", 
				1); 
		mEtPublisher.setThreshold(3);
		mEtPublisher.setAdapter(mAdapterPublisher);

		if (comicIds != null && comicIds.length > 0)
		{
			mComics = getDBHelper().getComics(comicIds);

			ListView lvEdit = (ListView)findViewById(R.id.comicEdit_listView);			
			if (comicIds.length > 1)
			{
				drawer.setVisibility(View.VISIBLE);
				ComicArrayAdapter adapter = new ComicArrayAdapter(this, mComics, getImagePath(true));		    	
				lvEdit.setAdapter(adapter);
			}
			else {
				mIvImage.setOnClickListener(this);
				drawer.setVisibility(View.GONE);
			}

			BindComics();
		}
		else {
			DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
			mEtAdded.setText(dateFormat.format(new Date()));
			mIvImage.setOnClickListener(this);
			drawer.setVisibility(View.GONE);
		}
	}

	private void BindComics()
	{
		if (mComics == null || mComics.size() == 0)
			return;

		if (mComics.size() == 1)
		{
			Comic comic = mComics.get(0);
			mRowIssue.setVisibility(View.VISIBLE);
			mRowPublishDate.setVisibility(View.VISIBLE);
			mRowAdded.setVisibility(View.VISIBLE);
			mRowPageCount.setVisibility(View.VISIBLE);
			mRowIssues.setVisibility(View.VISIBLE);

			setTextField(mEtTitle, comic.getTitle(), false);
			setTextField(mEtSubtitle, comic.getSubTitle(), false);
			setTextField(mEtIssue, comic.getIssue() > 0 ? Integer.toString(comic.getIssue()) : "", false);
			setTextField(mEtAuthor, comic.getAuthor(), false);
			setTextField(mEtIllustrator, comic.getIllustrator(), false);
			setTextField(mEtPublisher, comic.getPublisher(), false);
			setTextField(mEtIssues, comic.getIssues(), false);

			if (comic.getPublishDateTimestamp() > 0)
				setTextField(mEtPublished, mDateFormat.format(comic.getPublishDate()), false);
			else
				setTextField(mEtPublished, "", false);

			if (comic.getAddedDateTimestamp() > 0)
				setTextField(mEtAdded, mDateFormat.format(comic.getAddedDate()), false);
			else
				setTextField(mEtAdded, "", false);

			if (comic.getPageCount() > 0)
				setTextField(mEtPageCount, Integer.toString(comic.getPageCount()), false);
			else
				setTextField(mEtPageCount, "", false);

			if (comic.getGroupId() > 0) {
				int pos = mAdapterGroups.getPosition(new Group(comic.getGroupId()));
				mSpGroup.setSelection(pos);
			}

			mIvImage.setVisibility(View.VISIBLE);
			if (comic.getImage() != null && comic.getImage().length() > 0) {
				final Bitmap bmp = BitmapFactory.decodeFile(getImagePath(comic.getImage()));
				if (bmp != null) {
					mIvImage.setImageBitmap(bmp);
				}
				else {
					mIvImage.setImageDrawable(getResources().getDrawable(R.drawable.camera_icon));
				}
			}
			else {
				mIvImage.setImageDrawable(getResources().getDrawable(R.drawable.camera_icon));
			}
		}
		else
		{
			//Multi edit
			Comic comic = mComics.get(0);
			mRowIssue.setVisibility(View.GONE);
			mRowPublishDate.setVisibility(View.GONE);
			mRowAdded.setVisibility(View.GONE);
			mRowPageCount.setVisibility(View.GONE);
			mRowIssues.setVisibility(View.GONE);
			setTextField(mEtTitle, comic.getTitle(), true);
			setTextField(mEtSubtitle, comic.getSubTitle(), true);			
			setTextField(mEtAuthor, comic.getAuthor(), true);
			setTextField(mEtIllustrator, comic.getIllustrator(), true);
			setTextField(mEtPublisher, comic.getPublisher(), true);
			int groupId = getCommonGroupId(mComics);
			if (groupId > 0) {
				int pos = mAdapterGroups.getPosition(new Group(groupId));
				mSpGroup.setSelection(pos);
			}
			mIvImage.setVisibility(View.GONE);
		}
	}

	private void UpdateComics()
	{
		final ContentValues values = new ContentValues();
		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);		
		String title = mEtTitle.getText().toString().trim();
		if (title.toLowerCase(Locale.ENGLISH).startsWith("the ")) {
			title = title.substring(4) + ", The";
		}

		if (mComics != null && mComics.size() > 1)
		{
			if (!isEmpty(mEtTitle))
				values.put("Title", title);
			if (!isEmpty(mEtSubtitle))
				values.put("SubTitle", mEtSubtitle.getText().toString());
			if (!isEmpty(mEtAuthor))
				values.put("Author", mEtAuthor.getText().toString());
			if (!isEmpty(mEtIllustrator))
				values.put("Illustrator", mEtIllustrator.getText().toString());				
			if (!isEmpty(mEtPublisher))
				values.put("Publisher", mEtPublisher.getText().toString());
			if (mSpGroup.getSelectedItemPosition() > 0) {
				Group g = (Group)mSpGroup.getSelectedItem();
				values.put("GroupId", g.getId());
			}	
		}
		else
		{
			//Strings
			values.put("Title", title);
			values.put("SubTitle", mEtSubtitle.getText().toString());
			values.put("Author", mEtAuthor.getText().toString());
			values.put("Illustrator", mEtIllustrator.getText().toString());
			values.put("Publisher", mEtPublisher.getText().toString());
			values.put("Issues", mEtIssues.getText().toString());
			//Integers
			if (!isEmpty(mEtIssue)) {
				if (isValidInt(mEtIssue.getText().toString())) {
					values.put("Issue", Integer.parseInt(mEtIssue.getText().toString()));
				}
				else {
					Toast.makeText(this, R.string.edit_issueerror, Toast.LENGTH_LONG).show();
					return;
				}
			}
			else {
				values.putNull("Issue");
			}				
			if (!isEmpty(mEtPageCount)) {
				if (isValidInt(mEtPageCount.getText().toString())) {
					values.put("PageCount", Integer.parseInt(mEtPageCount.getText().toString()));
				}
				else {
					Toast.makeText(this, R.string.edit_pagecounterror, Toast.LENGTH_LONG).show();
					return;					
				}
			}
			else {
				values.putNull("PageCount");
			}
			//Dates
			try
			{
				if (!isEmpty(mEtPublished)) {
					
					values.put("PublishDate", getDBHelper().GetDateStamp(mEtPublished.getText().toString(), dateFormat));
				}
				else {
					values.putNull("PublishDate");
				}
				if (!isEmpty(mEtAdded)) {
					values.put("AddedDate", getDBHelper().GetDateStamp(mEtAdded.getText().toString(), dateFormat));
				}
				else {
					values.putNull("AddedDate");
				}
			}
			catch (ParseException e) {
				Toast.makeText(this, getString(R.string.edit_dateerror) + " " + dateFormat.format(new Date()), Toast.LENGTH_LONG).show();
				return;
			}
			//Image
			if (mNewImage != null) {
				values.put("ImageUrl", "");
				values.put("Image", new File(mNewImage).getName());
			}			
			//Group
			if (mSpGroup.getSelectedItemPosition() > 0) {
				Group g = (Group)mSpGroup.getSelectedItem();
				values.put("GroupId", g.getId());
			}
			else {
				values.putNull("GroupId");
			}			
		}

		if (mComics != null)
		{
			//UPDATE
			StringBuilder sbWhere = new StringBuilder("_id IN (");
			String[] ids = new String[mComics.size()];
			int i = 0;
			for (Comic c : mComics) {
				sbWhere.append("?,");
				ids[i] = Integer.toString(c.getId());
				i++;
			}
			sbWhere.setLength(sbWhere.length() - 1);
			sbWhere.append(")");

			getDBHelper().update("tblBooks", values, sbWhere.toString(), ids);
		}
		else
		{
			//INSERT
			if (!values.containsKey("AddedDate") || values.get("AddedDate") == null) {
				values.remove("AddedDate");
				values.put("AddedDate", (int)(System.currentTimeMillis() / 1000L));
			}
			long id = getDBHelper().insert("tblBooks", values);
			Comic comic = getDBHelper().getComic((int)id);
			if (comic != null){
				mComics = new ArrayList<Comic>();
				mComics.add(comic);
			}
		}

		//Backup
		BackupManager m = new BackupManager(this);
		m.dataChanged();

		setResult(RESULT_OK);

		Toast.makeText(this, getResources().getString(R.string.edit_done), Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar_edit, (com.actionbarsherlock.view.Menu) menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_done:
			UpdateComics();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.comicEdit_ivImage) {
			if (mComics == null) {
				Toast.makeText(Edit.this, getResources().getString(R.string.edit_needtosave), Toast.LENGTH_LONG).show();
				return;
			}
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
			String fileName = "thumb" + mComics.get(0).hashCode() + ".jpg";
			mNewImage = getImagePath(fileName);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mNewImage)));
			startActivityForResult(cameraIntent, CAMERA_REQUEST);
		}
	}

	@Override
	public void onGroupDialogPositiveClick(String groupAdded) {
		List<Group> groups = getDBHelper().getGroups();
		groups.add(0, new Group(0, getResources().getString(R.string.common_nogroup), null, 0, 0, 0, 0, 0));
		mAdapterGroups.clear();
		int i = 0;
		int index = 0;
		for (Group g : groups) {
			mAdapterGroups.add(g);
			if (g.getName().equals(groupAdded)) {
				index = i;
			}
			i++;
		}
		mSpGroup.setEnabled(true);
		mSpGroup.setSelection(index);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST) {
			if (resultCode == RESULT_OK) {
				try
				{
					ImageHandler.resizeOnDisk(mNewImage, 4);
				}
				catch (Exception e) {
					Toast.makeText(this, R.string.error_storeimage, Toast.LENGTH_SHORT).show();
				}
				Bitmap bmp = BitmapFactory.decodeFile(mNewImage);
				mIvImage.setImageBitmap(bmp);
			}
			else {
				Toast.makeText(this, R.string.error_storeimage, Toast.LENGTH_SHORT).show();
				mNewImage = null;
			}
		}
	}

	private int getCommonGroupId(List<Comic> comics) {
		int groupId = comics.get(0).getGroupId();
		for (Comic c : comics.subList(1, comics.size() - 1)) {
			if (c.getGroupId() != groupId) {
				groupId = -1;
				break;
			}
		}
		return groupId;
	}

	private boolean isEmpty(EditText et) {
		return et.getText().toString().trim().length() == 0;
	}

	private boolean isValidInt(String val) {
		if (val == null)
			return false;
		return val.matches("^\\d+$");
	}
	
	private void setTextField(EditText view, String text, boolean asHint)
	{
		if (!asHint) {
			view.setText(text);
		}
		else {
			view.setText("");
			view.setHint(text);
		}
	}

	@Override
	protected void onDestroy() {
		mAdapterPublisher.close();
		mAdapterAuthors.close();
		mAdapterIllustrators.close();
		super.onDestroy();	
	}
}
