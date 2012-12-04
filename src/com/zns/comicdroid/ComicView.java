package com.zns.comicdroid;

import java.text.SimpleDateFormat;

import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.DB;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ComicView extends BaseActivity {
	
	Comic currentComic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic_view);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
	    int comicId = intent.getIntExtra("com.zns.comic.COMICID", 0);
	    if (comicId > 0)
	    {
	    	DB comicDB = new DB(this);
	    	currentComic = comicDB.getComic(comicId);
	    	BindComic(currentComic);
	    }
	}

	private void BindComic(Comic comic)
	{
		if (comic == null)
			return;
		
		TextView tvTitle = (TextView)findViewById(R.id.comicView_txtTitle);
		TextView tvSubtitle = (TextView)findViewById(R.id.comicView_txtSubtitle);
		TextView tvSubtitleHeading = (TextView)findViewById(R.id.comicView_tvSubtitleHeading);
		View tvSubtitleDivider = (View)findViewById(R.id.comicView_vSubtitleDivider);
		TextView tvAuthor = (TextView)findViewById(R.id.comicView_txtAuthor);
		TextView tvPublisher = (TextView)findViewById(R.id.comicView_txtPublisher);
		TextView tvPublished = (TextView)findViewById(R.id.comicView_txtPublished);
		TextView tvAdded = (TextView)findViewById(R.id.comicView_txtAdded);
		TextView tvPageCount = (TextView)findViewById(R.id.comicView_txtPageCount);
		EditText etBorrower = (EditText)findViewById(R.id.comicView_etBorrower);
		CheckBox cbIsBorrowed = (CheckBox)findViewById(R.id.comicView_cbIsBorrowed);
		ImageView ivImage = (ImageView)findViewById(R.id.comicView_ivImage);
		
		tvTitle.setText(comic.getTitle() + (comic.getIssue() > 0 ? " - Vol. " + comic.getIssue() : ""));
		if (comic.getSubTitle() != null)
		{
			tvSubtitleHeading.setVisibility(View.VISIBLE);
			tvSubtitleDivider.setVisibility(View.VISIBLE);
			tvSubtitle.setVisibility(View.VISIBLE);
			tvSubtitle.setText(comic.getSubTitle());
		}
		else
		{
			tvSubtitleHeading.setVisibility(View.GONE);
			tvSubtitleDivider.setVisibility(View.GONE);			
			tvSubtitle.setVisibility(View.GONE);
		}
		tvAuthor.setText(comic.getAuthor());
		tvPublisher.setText(comic.getPublisher());
		tvPublished.setText(new SimpleDateFormat("yyyy-MM-dd").format(comic.getPublishDate()));
		tvAdded.setText(new SimpleDateFormat("yyyy-MM-dd").format(comic.getAddedDate()));
		tvPageCount.setText(Integer.toString(comic.getPageCount()));
		etBorrower.setText(comic.getBorrower());
		cbIsBorrowed.setChecked(comic.getIsBorrowed());
		if (comic.getImage() != null)
			ivImage.setImageBitmap(BitmapFactory.decodeByteArray(comic.getImage(), 0, comic.getImage().length));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
