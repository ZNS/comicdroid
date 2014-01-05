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

import java.text.DateFormat;

import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Comic;

public class ComicView extends BaseFragmentActivity {

	public final static String INTENT_COMIC_ID = "com.zns.comic.COMICID";
	public final static int CODE_COMIC_EDITED = 100;
	
	private DateFormat mDateFormat;
	private Comic mCurrentComic;
	private TextView mTvTitle;
	private TextView mTvSubtitle;
	private TextView mTvSubtitleHeading;
	private View mTvSubtitleDivider;
	private TextView mTvAuthor;
	private TextView mTvIllustrator;
	private TextView mTvPublisher;
	private TextView mTvPublished;
	private TextView mTvAdded;
	private TextView mTvPageCount;
	private EditText mEtBorrower;
	private ImageView mIvImage;
	private TextView mTvIssues;
	private CheckBox mCbIsRead;
	private RatingBar mRbRating;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_comic_view);
		super.onCreate(savedInstanceState);

		mDateFormat = android.text.format.DateFormat.getDateFormat(this);
		mTvTitle = (TextView)findViewById(R.id.comicView_txtTitle);
		mTvSubtitle = (TextView)findViewById(R.id.comicView_txtSubtitle);
		mTvSubtitleHeading = (TextView)findViewById(R.id.comicView_tvSubtitleHeading);
		mTvSubtitleDivider = (View)findViewById(R.id.comicView_vSubtitleDivider);
		mTvAuthor = (TextView)findViewById(R.id.comicView_txtAuthor);
		mTvIllustrator = (TextView)findViewById(R.id.comicView_txtIllustrator);
		mTvPublisher = (TextView)findViewById(R.id.comicView_txtPublisher);
		mTvPublished = (TextView)findViewById(R.id.comicView_txtPublished);
		mTvAdded = (TextView)findViewById(R.id.comicView_txtAdded);
		mTvPageCount = (TextView)findViewById(R.id.comicView_txtPageCount);
		mEtBorrower = (EditText)findViewById(R.id.comicView_etBorrower);
		mIvImage = (ImageView)findViewById(R.id.comicView_ivImage);		
		mTvIssues = (TextView)findViewById(R.id.comicView_txtIssues);
		mCbIsRead = (CheckBox)findViewById(R.id.comicView_cbIsRead);
		mRbRating = (RatingBar)findViewById(R.id.rbComicView);

		mCbIsRead.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton btn, boolean checked) {
				getDBHelper().setComicRead(mCurrentComic.getId(), checked);
			}
		});

		mRbRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				if (fromUser) {
					getDBHelper().setComicRating(mCurrentComic.getId(), (int)rating);
				}
			}
		});

		mEtBorrower.setOnEditorActionListener(new TextView.OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

					getDBHelper().setComicBorrowed(mCurrentComic.getId(), mEtBorrower.getText().toString());

					return true;
				}
				return false;
			}
		});

		Intent intent = getIntent();
		int comicId = intent.getIntExtra(INTENT_COMIC_ID, 0);
		if (comicId > 0)
		{
			mCurrentComic = getDBHelper().getComic(comicId);
			BindComic(mCurrentComic);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_COMIC_EDITED && resultCode == RESULT_OK && mCurrentComic != null) {
			//Comic was edited, refresh
			mCurrentComic = getDBHelper().getComic(mCurrentComic.getId());
			BindComic(mCurrentComic);
		}
	}

	@Override
	protected void onPause()
	{
		if (mCurrentComic != null && !mCurrentComic.getBorrower().equals(mEtBorrower.getText().toString()))
		{
			getDBHelper().setComicBorrowed(mCurrentComic.getId(), mEtBorrower.getText().toString());
		}
		super.onPause();
	}

	private void BindComic(Comic comic)
	{
		if (comic == null)
			return;

		mTvTitle.setText(comic.getTitle() + (comic.getIssue() > 0 ? " - " + getResources().getString(R.string.comicview_issueshort) + " " + comic.getIssue() : ""));
		if (comic.getSubTitle() != null)
		{
			mTvSubtitleHeading.setVisibility(View.VISIBLE);
			mTvSubtitleDivider.setVisibility(View.VISIBLE);
			mTvSubtitle.setVisibility(View.VISIBLE);
			mTvSubtitle.setText(comic.getSubTitle());
		}
		else
		{
			mTvSubtitleHeading.setVisibility(View.GONE);
			mTvSubtitleDivider.setVisibility(View.GONE);			
			mTvSubtitle.setVisibility(View.GONE);
		}
		mTvAuthor.setText(comic.getAuthor());
		mTvIllustrator.setText(comic.getIllustrator());
		mTvPublisher.setText(comic.getPublisher());
		if (comic.getPublishDateTimestamp() > 0) {
			mTvPublished.setText(mDateFormat.format(comic.getPublishDate()));
		}
		mTvAdded.setText(mDateFormat.format(comic.getAddedDate()));
		if (comic.getPageCount() > 0) {
			mTvPageCount.setText(Integer.toString(comic.getPageCount()));
		}
		mTvIssues.setText(comic.getIssues());
		mCbIsRead.setChecked(comic.getIsRead());
		if (comic.getRating() > 0) {
			mRbRating.setRating(comic.getRating());
		}
		mEtBorrower.setText(comic.getBorrower());
		if (comic.getImage() != null) {
			mIvImage.setImageBitmap(BitmapFactory.decodeFile(getImagePath(comic.getImage())));
		}		
	}	

	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar_view, (com.actionbarsherlock.view.Menu) menu);
		menu.findItem(R.id.submenu_view).setVisible(true);
		menu.findItem(R.id.menu_editall).setVisible(false);
		return true;
	}		

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_edit:
			Intent intent = new Intent(this, Edit.class);
			intent.putExtra(Edit.INTENT_COMIC_IDS, new int[] { mCurrentComic.getId() });
			startActivityForResult(intent, CODE_COMIC_EDITED);
			return true;
		case R.id.menu_delete:
			new AlertDialog.Builder(this)
			.setTitle(R.string.comicview_delete_title)
			.setMessage(R.string.comicview_delete_body)
			.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					getDBHelper().deleteComic(mCurrentComic.getId());
					//Backup
					BackupManager m = new BackupManager(ComicView.this);
					m.dataChanged();
					//Go back
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
}