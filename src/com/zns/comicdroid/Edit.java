package com.zns.comicdroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.ComicArrayAdapter;
import com.zns.comicdroid.data.DBHelper;

import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.Toast;

public class Edit extends BaseFragmentActivity
	implements OnClickListener {
	
	private static final int CAMERA_REQUEST = 1888; 
	
	private DBHelper comicDB;
	private List<Comic> comics;
	private EditText etTitle;
	private EditText etSubtitle;
	private EditText etIssue;
	private EditText etAuthor;
	private EditText etPublisher;
	private EditText etPublished;
	private EditText etAdded;
	private EditText etPageCount;
	private ImageView ivImage;
	private Bitmap newImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		etTitle = (EditText)findViewById(R.id.comicEdit_etTitle);
		etSubtitle = (EditText)findViewById(R.id.comicEdit_etSubtitle);
		etIssue = (EditText)findViewById(R.id.comicEdit_etIssue);
		etAuthor = (EditText)findViewById(R.id.comicEdit_etAuthor);
		etPublisher = (EditText)findViewById(R.id.comicEdit_etPublisher);
		etPublished = (EditText)findViewById(R.id.comicEdit_etPublished);
		etAdded = (EditText)findViewById(R.id.comicEdit_etAdded);
		etPageCount = (EditText)findViewById(R.id.comicEdit_etPageCount);
		ivImage = (ImageView)findViewById(R.id.comicEdit_ivImage);		
		
		Intent intent = getIntent();
	    int[] comicIds = intent.getIntArrayExtra("com.zns.comic.COMICIDS");
	    if (comicIds != null && comicIds.length > 0)
	    {
	    	comicDB = new DBHelper(this);
	    	comics = comicDB.getComics(comicIds);
	    
	    	ListView lvEdit = (ListView)findViewById(R.id.comicEdit_listView);
			SlidingDrawer drawer = (SlidingDrawer)findViewById(R.id.comicEdit_drawer);
	    	if (comicIds.length > 1)
	    	{
	    		drawer.setVisibility(View.VISIBLE);
		    	ComicArrayAdapter adapter = new ComicArrayAdapter(this, comics);		    	
		    	lvEdit.setAdapter(adapter);		    			    	
	    	}
	    	else {
	    		ivImage.setOnClickListener(this);
	    		drawer.setVisibility(View.GONE);
	    	}
	    	
	    	BindComics();
	    }
	}

	private void BindComics()
	{
		if (comics == null || comics.size() == 0)
			return;
		
		if (comics.size() == 1)
		{
			Comic comic = comics.get(0);
			SetTextField(etTitle, comic.getTitle(), true);
			SetTextField(etSubtitle, comic.getSubTitle(), true);
			SetTextField(etIssue, comic.getIssue() > 0 ? Integer.toString(comic.getIssue()) : "", true);
			SetTextField(etAuthor, comic.getAuthor(), true);
			SetTextField(etPublisher, comic.getPublisher(), true);
			SetTextField(etPublished, new SimpleDateFormat("yyyy-MM-dd").format(comic.getPublishDate()), true);
			SetTextField(etAdded, new SimpleDateFormat("yyyy-MM-dd").format(comic.getAddedDate()), true);
			SetTextField(etPageCount, Integer.toString(comic.getPageCount()), true);
			if (comic.getImage() != null)
				ivImage.setImageBitmap(BitmapFactory.decodeByteArray(comic.getImage(), 0, comic.getImage().length));
		}
		else
		{
			//Multi edit
			Comic comic = comics.get(0);
			SetTextField(etTitle, comic.getTitle(), true);
			SetTextField(etSubtitle, comic.getSubTitle(), true);
			SetTextField(etIssue, "", false);
			SetTextField(etAuthor, comic.getAuthor(), true);
			SetTextField(etPublisher, comic.getPublisher(), true);
			SetTextField(etPublished, "", false);
			SetTextField(etAdded, "", false);
			SetTextField(etPageCount, "", false);			
		}
	}
	
	private void UpdateComics()
	{
		ContentValues values = new ContentValues();
		try
		{			
			values.put("Title", etTitle.getText().toString());
			values.put("SubTitle", etSubtitle.getText().toString());
			values.put("Author", etAuthor.getText().toString());
			values.put("Publisher", etPublisher.getText().toString());
			if (comics.size() == 1)
			{
				if (etIssue.getText().toString() != "")
					values.put("Issue", Integer.parseInt(etIssue.getText().toString()));			
				if (etPageCount.getText().toString() != "")
					values.put("PageCount", Integer.parseInt(etPageCount.getText().toString()));
				values.put("PublishDate", comicDB.GetDateStamp(etPublished.getText().toString()));
				values.put("AddedDate", comicDB.GetDateStamp(etAdded.getText().toString()));
				if (newImage != null) {			
		        	ByteArrayOutputStream stream = new ByteArrayOutputStream();
		        	if (newImage.compress(Bitmap.CompressFormat.PNG, 100, stream))
		        	{
			        	byte[] byteArray = stream.toByteArray();
						values.put("Image", byteArray);	        	
		        	}
		        	stream.close();
				}
			}
		}
		catch (ParseException e) {}
		catch (IOException e) {}
		
		StringBuilder sbWhere = new StringBuilder("Id IN (");
		String[] ids = new String[comics.size()];
		int i = 0;
		for (Comic c : comics) {
			sbWhere.append("?,");
			ids[i] = Integer.toString(c.getId());
			i++;
		}
		sbWhere.setLength(sbWhere.length() - 1);
		sbWhere.append(")");
		
		SQLiteDatabase db = comicDB.getWritableDatabase();	
		db.update("tblBooks", values, sbWhere.toString(), ids);
		db.close();
		
		Toast.makeText(this, "Sparat och klart!", Toast.LENGTH_LONG).show();
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
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
            startActivityForResult(cameraIntent, CAMERA_REQUEST); 			
		}
	}	
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	try
    	{
	        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
	        	Bitmap photo = (Bitmap)data.getExtras().get("data"); 
	        	if (photo != null)
	        	{
		        	int width = photo.getWidth();
		        	int height = photo.getHeight();
		        	double thumbHeight = ((double)height / (double)width) * 128.0d;
		        	newImage = ThumbnailUtils.extractThumbnail(photo, 128, (int)thumbHeight);		        	
		        	photo = null;
	        	}
	        }
    	}
    	catch (Exception e) {}
    }	
    
	private void SetTextField(EditText view, String text, Boolean enabled)
	{
		view.setText(text);
		view.setEnabled(enabled);
		view.setFocusable(enabled);
		if (enabled) {
			view.getBackground().setColorFilter(null);
			view.setHint("");
		}
		else {
			view.getBackground().setColorFilter(0xFFFFDDDD, Mode.MULTIPLY);
			view.setHint("Ej Redigerbar");
		}
	} 
}
