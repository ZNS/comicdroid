package com.zns.comicdroid;

import java.text.SimpleDateFormat;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.data.Comic;

public class ComicView extends BaseFragmentActivity {
	
	private Comic currentComic;
	private TextView tvTitle;
	private TextView tvSubtitle;
	private TextView tvSubtitleHeading;
	private View tvSubtitleDivider;
	private TextView tvAuthor;
	private TextView tvPublisher;
	private TextView tvPublished;
	private TextView tvAdded;
	private TextView tvPageCount;
	private EditText etBorrower;
	private CheckBox cbIsBorrowed;
	private ImageView ivImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comic_view);

		tvTitle = (TextView)findViewById(R.id.comicView_txtTitle);
		tvSubtitle = (TextView)findViewById(R.id.comicView_txtSubtitle);
		tvSubtitleHeading = (TextView)findViewById(R.id.comicView_tvSubtitleHeading);
		tvSubtitleDivider = (View)findViewById(R.id.comicView_vSubtitleDivider);
		tvAuthor = (TextView)findViewById(R.id.comicView_txtAuthor);
		tvPublisher = (TextView)findViewById(R.id.comicView_txtPublisher);
		tvPublished = (TextView)findViewById(R.id.comicView_txtPublished);
		tvAdded = (TextView)findViewById(R.id.comicView_txtAdded);
		tvPageCount = (TextView)findViewById(R.id.comicView_txtPageCount);
		etBorrower = (EditText)findViewById(R.id.comicView_etBorrower);
		cbIsBorrowed = (CheckBox)findViewById(R.id.comicView_cbIsBorrowed);
		ivImage = (ImageView)findViewById(R.id.comicView_ivImage);		
		
		Intent intent = getIntent();
	    int comicId = intent.getIntExtra("com.zns.comic.COMICID", 0);
	    if (comicId > 0)
	    {
	    	currentComic = getDBHelper().getComic(comicId);
	    	BindComic(currentComic);
	    }
	}

	private void BindComic(Comic comic)
	{
		if (comic == null)
			return;
				
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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.actionbar_view, (com.actionbarsherlock.view.Menu) menu);
		return true;
	}		
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        	case R.id.menu_edit:
	        	Intent intent = new Intent(this, Edit.class);
				intent.putExtra("com.zns.comic.COMICIDS", new int[] { currentComic.getId() });
	        	startActivity(intent);
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
