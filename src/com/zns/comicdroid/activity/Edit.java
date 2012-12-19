package com.zns.comicdroid.activity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.SlidingDrawer;
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
import com.zns.comicdroid.dialog.GroupAddDialogFragment;
import com.zns.comicdroid.util.ImageHandler;
import com.zns.comicdroid.util.ImageHandler.MediaNotReadyException;

public class Edit extends BaseFragmentActivity
	implements	OnClickListener, 
				GroupAddDialogFragment.OnGroupAddDialogListener {
	
	public static final String INTENT_COMIC_IDS = "com.zns.comic.COMICIDS";
	private static final int CAMERA_REQUEST = 1888; 
	
	private List<Comic> comics = null;
	private EditText etTitle;
	private EditText etSubtitle;
	private EditText etIssue;
	private AutoCompleteTextView etAuthor;
	private AutoCompleteTextView etPublisher;
	private EditText etPublished;
	private EditText etAdded;
	private EditText etPageCount;
	private ImageView ivImage;
	private String newImage = null;
	private Spinner spGroup;
	private ImageView ivGroupAdd;
	private RelativeLayout rowIssue;
	private RelativeLayout rowPublishDate;
	private RelativeLayout rowAdded;
	private RelativeLayout rowPageCount;
	
	private ArrayAdapter<Group> adapterGroups;		
	private AutoCompleteAdapter adapterAuthors;
	private AutoCompleteAdapter adapterPublisher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		etTitle = (EditText)findViewById(R.id.comicEdit_etTitle);
		etSubtitle = (EditText)findViewById(R.id.comicEdit_etSubtitle);
		etIssue = (EditText)findViewById(R.id.comicEdit_etIssue);
		etAuthor = (AutoCompleteTextView)findViewById(R.id.comicEdit_actAuthor);
		etPublisher = (AutoCompleteTextView)findViewById(R.id.comicEdit_actPublisher);
		etPublished = (EditText)findViewById(R.id.comicEdit_etPublished);
		etAdded = (EditText)findViewById(R.id.comicEdit_etAdded);
		etPageCount = (EditText)findViewById(R.id.comicEdit_etPageCount);
		ivImage = (ImageView)findViewById(R.id.comicEdit_ivImage);		
		spGroup = (Spinner)findViewById(R.id.comicEdit_spGroup);
		ivGroupAdd = (ImageView)findViewById(R.id.comicEdit_ivGroupAdd);
		rowIssue = (RelativeLayout)findViewById(R.id.comicEdit_issue);
		rowPublishDate = (RelativeLayout)findViewById(R.id.comicEdit_publishDate);
		rowAdded = (RelativeLayout)findViewById(R.id.comicEdit_added);
		rowPageCount = (RelativeLayout)findViewById(R.id.comicEdit_pageCount);	
		SlidingDrawer drawer = (SlidingDrawer)findViewById(R.id.comicEdit_drawer);
		
		Intent intent = getIntent();
	    int[] comicIds = intent.getIntArrayExtra(INTENT_COMIC_IDS);
	    
    	//Spinner groups
    	List<Group> groups = getDBHelper().getGroups();
    	if (groups == null)
    		groups = new ArrayList<Group>();
    	groups.add(0, new Group(0, "Ingen grupp", null));
    	adapterGroups = new ArrayAdapter<Group>(this, android.R.layout.simple_spinner_item, groups);
    	adapterGroups.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spGroup.setAdapter(adapterGroups);

    	//Dialog
    	ivGroupAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment dialogAddGroup = new GroupAddDialogFragment();
				dialogAddGroup.show(getSupportFragmentManager(), "GROUPADD");
			}
		});
    	
		//Autocomplete author
		adapterAuthors = new AutoCompleteAdapter(this, 
				"SELECT DISTINCT 0 AS _id, Author FROM tblBooks WHERE Author LIKE ? ORDER BY Author", 
				"Author", 
				1); 	    	
		etAuthor.setThreshold(3);
		etAuthor.setAdapter(adapterAuthors);
					
		//Autocomplete publisher
		adapterPublisher = new AutoCompleteAdapter(this, 
				"SELECT DISTINCT 0 AS _id, Publisher FROM tblBooks WHERE Publisher LIKE ? ORDER BY Publisher", 
				"Publisher", 
				1); 
		etPublisher.setThreshold(3);
		etPublisher.setAdapter(adapterPublisher);
		
	    if (comicIds != null && comicIds.length > 0)
	    {
	    	comics = getDBHelper().getComics(comicIds);
	    				
	    	ListView lvEdit = (ListView)findViewById(R.id.comicEdit_listView);			
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
	    else {
	    	ivImage.setOnClickListener(this);
	    	drawer.setVisibility(View.GONE);
	    }
	}
	
	private void BindComics()
	{
		if (comics == null || comics.size() == 0)
			return;
		
		if (comics.size() == 1)
		{
			Comic comic = comics.get(0);
			rowIssue.setVisibility(View.VISIBLE);
			rowPublishDate.setVisibility(View.VISIBLE);
			rowAdded.setVisibility(View.VISIBLE);
			rowPageCount.setVisibility(View.VISIBLE);
			
			setTextField(etTitle, comic.getTitle(), false);
			setTextField(etSubtitle, comic.getSubTitle(), false);
			setTextField(etIssue, comic.getIssue() > 0 ? Integer.toString(comic.getIssue()) : "", false);
			setTextField(etAuthor, comic.getAuthor(), false);
			setTextField(etPublisher, comic.getPublisher(), false);
			
			if (comic.getPublishDateTimestamp() > 0)
				setTextField(etPublished, new SimpleDateFormat("yyyy-MM-dd").format(comic.getPublishDate()), false);
			else
				setTextField(etPublished, "", false);
			
			if (comic.getAddedDateTimestamp() > 0)
				setTextField(etAdded, new SimpleDateFormat("yyyy-MM-dd").format(comic.getAddedDate()), false);
			else
				setTextField(etAdded, "", false);
			
			if (comic.getPageCount() > 0)
				setTextField(etPageCount, Integer.toString(comic.getPageCount()), false);
			else
				setTextField(etPageCount, "", false);

			if (comic.getGroupId() > 0) {
				int pos = adapterGroups.getPosition(new Group(comic.getGroupId()));
				spGroup.setSelection(pos);
			}
			
			ivImage.setVisibility(View.VISIBLE);
			if (comic.getImage() != null)
				ivImage.setImageBitmap(BitmapFactory.decodeFile(comic.getImage()));
		}
		else
		{
			//Multi edit
			Comic comic = comics.get(0);
			rowIssue.setVisibility(View.GONE);
			rowPublishDate.setVisibility(View.GONE);
			rowAdded.setVisibility(View.GONE);
			rowPageCount.setVisibility(View.GONE);		
			setTextField(etTitle, comic.getTitle(), true);
			setTextField(etSubtitle, comic.getSubTitle(), true);			
			setTextField(etAuthor, comic.getAuthor(), true);
			setTextField(etPublisher, comic.getPublisher(), true);
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
			String title = etTitle.getText().toString().trim();
			if (title.toLowerCase().startsWith("the ")) {
				title = title.substring(4) + ", The";
			}
			
			if (comics != null && comics.size() > 1)
			{
				if (!isEmpty(etTitle))
					values.put("Title", title);
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
				values.put("Title", title);
				values.put("SubTitle", etSubtitle.getText().toString());
				values.put("Author", etAuthor.getText().toString());
				values.put("Publisher", etPublisher.getText().toString());				
				if (!isEmpty(etIssue))
					values.put("Issue", Integer.parseInt(etIssue.getText().toString()));
				if (!isEmpty(etPageCount))
					values.put("PageCount", Integer.parseInt(etPageCount.getText().toString()));
				if (!isEmpty(etPublished))
					values.put("PublishDate", getDBHelper().GetDateStamp(etPublished.getText().toString()));
				if (!isEmpty(etAdded))
					values.put("AddedDate", getDBHelper().GetDateStamp(etAdded.getText().toString()));
				if (spGroup.getSelectedItemPosition() > 0) {
					Group g = (Group)spGroup.getSelectedItem();
					values.put("GroupId", g.getId());
				}
				if (newImage != null) {
					ImageHandler.resizeOnDisk(newImage);
					values.put("Image", newImage);
				}
			}
		}
		catch (ParseException e) {}
		catch (IOException e) {} 
		catch (MediaNotReadyException e) {}
		
		if (comics != null)
		{
			//UPDATE
			StringBuilder sbWhere = new StringBuilder("_id IN (");
			String[] ids = new String[comics.size()];
			int i = 0;
			for (Comic c : comics) {
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
				comics = new ArrayList<Comic>();
				comics.add(comic);
			}
		}
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
			if (comics == null) {
				Toast.makeText(Edit.this, "Du måste spara innan du kan lägga till en bild.", Toast.LENGTH_LONG).show();
				return;
			}
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
			String fileName = "thumb" + comics.get(0).hashCode() + ".jpg";
			File file = new File(getExternalFilesDir(null), fileName);
			newImage = file.toString();
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
		}
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		List<Group> groups = getDBHelper().getGroups();
		groups.add(0, new Group(0, "Ingen grupp", null));
		adapterGroups.clear();
		for (Group g : groups)
			adapterGroups.add(g);
		spGroup.setEnabled(true);
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode != RESULT_OK) {
        	newImage = null;
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
		adapterPublisher.close();
		adapterAuthors.close();
		super.onDestroy();	
	}
}
