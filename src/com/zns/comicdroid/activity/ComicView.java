package com.zns.comicdroid.activity;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.data.Comic;

@SuppressLint("SimpleDateFormat")
public class ComicView extends BaseFragmentActivity {
	
	public final static String INTENT_COMIC_ID = "com.zns.comic.COMICID";
	
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
		ivImage = (ImageView)findViewById(R.id.comicView_ivImage);		
		
		etBorrower.setOnEditorActionListener(new TextView.OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
		            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		            
		            getDBHelper().setComicBorrowed(currentComic.getId(), etBorrower.getText().toString());
		            
		            return true;
		        }
		        return false;
			}
		});
	    
		Intent intent = getIntent();
	    int comicId = intent.getIntExtra(INTENT_COMIC_ID, 0);
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
		if (comic.getImage() != null)
			ivImage.setImageBitmap(BitmapFactory.decodeFile(comic.getImage()));		
	}	
		
	//Menu
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
				intent.putExtra(Edit.INTENT_COMIC_IDS, new int[] { currentComic.getId() });
	        	startActivity(intent);
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
