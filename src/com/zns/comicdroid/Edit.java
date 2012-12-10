package com.zns.comicdroid;

import com.zns.comicdroid.dialogs.GroupAddDialogFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.ComicArrayAdapter;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.data.Group;

import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff.Mode;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.Toast;

public class Edit extends BaseFragmentActivity
	implements	OnClickListener, 
				GroupAddDialogFragment.OnGroupAddDialogListener {
	
	private static final int CAMERA_REQUEST = 1888; 
	
	private DBHelper comicDB;
	private List<Comic> comics;
	private EditText etTitle;
	private EditText etSubtitle;
	private EditText etIssue;
	private AutoCompleteTextView etAuthor;
	private EditText etPublisher;
	private EditText etPublished;
	private EditText etAdded;
	private EditText etPageCount;
	private ImageView ivImage;
	private Bitmap newImage;
	private Spinner spGroup;
	private ImageView ivGroupAdd;
	
	private ArrayAdapter<Group> adapterGroups;
	
	private SQLiteDatabase db;
	private Cursor cursorAuthors;
	private Cursor cursorPublishers;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		etTitle = (EditText)findViewById(R.id.comicEdit_etTitle);
		etSubtitle = (EditText)findViewById(R.id.comicEdit_etSubtitle);
		etIssue = (EditText)findViewById(R.id.comicEdit_etIssue);
		etAuthor = (AutoCompleteTextView)findViewById(R.id.comicEdit_actAuthor);
		etPublisher = (EditText)findViewById(R.id.comicEdit_etPublisher);
		etPublished = (EditText)findViewById(R.id.comicEdit_etPublished);
		etAdded = (EditText)findViewById(R.id.comicEdit_etAdded);
		etPageCount = (EditText)findViewById(R.id.comicEdit_etPageCount);
		ivImage = (ImageView)findViewById(R.id.comicEdit_ivImage);		
		spGroup = (Spinner)findViewById(R.id.comicEdit_spGroup);
		ivGroupAdd = (ImageView)findViewById(R.id.comicEdit_ivGroupAdd);
		
		Intent intent = getIntent();
	    int[] comicIds = intent.getIntArrayExtra("com.zns.comic.COMICIDS");
	    if (comicIds != null && comicIds.length > 0)
	    {
	    	comicDB = new DBHelper(this);	    	
	    	comics = comicDB.getComics(comicIds);
	    	db = comicDB.getReadableDatabase();
	    	
	    	//Spinner groups
	    	List<Group> groups = comicDB.getGroups();
	    	if (groups == null)
	    		groups = new ArrayList<Group>();
	    	groups.add(0, new Group(0, "Ingen grupp", null));
	    	adapterGroups = new ArrayAdapter<Group>(this, android.R.layout.simple_spinner_item, groups);
	    	spGroup.setAdapter(adapterGroups);

	    	//Dialog
	    	ivGroupAdd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogFragment dialogAddGroup = new GroupAddDialogFragment();
					dialogAddGroup.show(getSupportFragmentManager(), "GROUPADD");
				}
			});
	    	
			//Autocomplete
			SimpleCursorAdapter adapterAuthor = new SimpleCursorAdapter(this,
					android.R.layout.simple_dropdown_item_1line,
					null,
					new String[] { "Author" },
					new int[] { android.R.id.text1 }, 0);			
			adapterAuthor.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					return getAuthorCursor(constraint);
				}
			});
			adapterAuthor.setCursorToStringConverter(new CursorToStringConverter() {				
				@Override
				public CharSequence convertToString(Cursor cursor) {
					return cursor.getString(1);
				}
			});
			etAuthor.setThreshold(3);
			etAuthor.setAdapter(adapterAuthor);
			
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
	
	private Cursor getAuthorCursor(CharSequence constraint) {
		if (constraint != null)
		{
			if (cursorAuthors != null)
				cursorAuthors.close();
			cursorAuthors = db.rawQuery("SELECT DISTINCT 0 AS _id, Author FROM tblBooks WHERE Author LIKE ? ORDER BY Author", new String[] { constraint.toString() + "%" });
			return cursorAuthors;
		}
		return null;
	}
	
	private void BindComics()
	{
		if (comics == null || comics.size() == 0)
			return;
		
		if (comics.size() == 1)
		{
			Comic comic = comics.get(0);
			setTextField(etTitle, comic.getTitle(), true);
			setTextField(etSubtitle, comic.getSubTitle(), true);
			setTextField(etIssue, comic.getIssue() > 0 ? Integer.toString(comic.getIssue()) : "", true);
			setTextField(etAuthor, comic.getAuthor(), true);
			setTextField(etPublisher, comic.getPublisher(), true);
			
			if (comic.getPublishDateTimestamp() > 0)
				setTextField(etPublished, new SimpleDateFormat("yyyy-MM-dd").format(comic.getPublishDate()), true);
			else
				setTextField(etPublished, "", true);
			
			if (comic.getAddedDateTimestamp() > 0)
				setTextField(etAdded, new SimpleDateFormat("yyyy-MM-dd").format(comic.getAddedDate()), true);
			else
				setTextField(etAdded, "", true);
			
			if (comic.getPageCount() > 0)
				setTextField(etPageCount, Integer.toString(comic.getPageCount()), true);
			else
				setTextField(etPageCount, "", true);

			if (comic.getGroupId() > 0) {
				int pos = adapterGroups.getPosition(new Group(comic.getGroupId()));
				spGroup.setSelection(pos);
			}
			
			ivImage.setVisibility(View.VISIBLE);
			if (comic.getImage() != null)
				ivImage.setImageBitmap(BitmapFactory.decodeByteArray(comic.getImage(), 0, comic.getImage().length));
		}
		else
		{
			//Multi edit
			Comic comic = comics.get(0);
			setTextField(etTitle, comic.getTitle(), true, true);
			setTextField(etSubtitle, comic.getSubTitle(), true, true);
			setTextField(etIssue, "", false);
			setTextField(etAuthor, comic.getAuthor(), true, true);
			setTextField(etPublisher, comic.getPublisher(), true, true);
			setTextField(etPublished, "", false);
			setTextField(etAdded, "", false);
			setTextField(etPageCount, "", false);
			int groupId = getCommonGroupId(comics);
			if (groupId > 0) {
				int pos = adapterGroups.getPosition(new Group(groupId));
				spGroup.setSelection(pos);
			}
			ivImage.setVisibility(View.GONE);
		}
	}
	
	private void UpdateComics()
	{
		ContentValues values = new ContentValues();
		try
		{
			if (comics.size() > 1)
			{
				if (!isEmpty(etTitle))
					values.put("Title", etTitle.getText().toString());
				if (!isEmpty(etSubtitle))
					values.put("SubTitle", etSubtitle.getText().toString());
				if (!isEmpty(etAuthor))
					values.put("Author", etAuthor.getText().toString());
				if (!isEmpty(etPublisher))
					values.put("Publisher", etPublisher.getText().toString());
				if (spGroup.getSelectedItemPosition() > 0) {
					Group g = (Group)spGroup.getSelectedItem();
					values.put("GroupId", g.getId());
				}	
			}
			else
			{
				values.put("Title", etTitle.getText().toString());
				values.put("SubTitle", etSubtitle.getText().toString());
				values.put("Author", etAuthor.getText().toString());
				values.put("Publisher", etPublisher.getText().toString());				
				if (!isEmpty(etIssue))
					values.put("Issue", Integer.parseInt(etIssue.getText().toString()));
				if (!isEmpty(etPageCount))
					values.put("PageCount", Integer.parseInt(etPageCount.getText().toString()));
				if (!isEmpty(etPublished))
					values.put("PublishDate", comicDB.GetDateStamp(etPublished.getText().toString()));
				if (!isEmpty(etAdded))
					values.put("AddedDate", comicDB.GetDateStamp(etAdded.getText().toString()));
				if (spGroup.getSelectedItemPosition() > 0) {
					Group g = (Group)spGroup.getSelectedItem();
					values.put("GroupId", g.getId());
				}
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
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		List<Group> groups = comicDB.getGroups();
		groups.add(0, new Group(0, "Ingen grupp", null));
		adapterGroups.clear();
		for (Group g : groups)
			adapterGroups.add(g);
		spGroup.setEnabled(true);
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

    private void setTextField(EditText view, String text, boolean enabled)
    {
    	setTextField(view, text, enabled, false);
    }
    
	private void setTextField(EditText view, String text, boolean enabled, boolean asHint)
	{
		if (!asHint) {
			view.setText(text);
		}
		else {
			view.setText("");
			view.setHint(text);
		}
		view.setEnabled(enabled);
		view.setFocusable(enabled);
		if (enabled) {
			view.getBackground().setColorFilter(null);
			if (view.getHint() != null && view.getHint().equals("Ej Redigerbar"))
				view.setHint("");
		}
		else {
			view.getBackground().setColorFilter(0xFFFFDDDD, Mode.MULTIPLY);
			view.setHint("Ej Redigerbar");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		newImage = null;
		if (cursorPublishers != null)
			cursorPublishers.close();		
		if (cursorAuthors != null)
			cursorAuthors.close();
		if (db != null)
			db.close();
	}
}
