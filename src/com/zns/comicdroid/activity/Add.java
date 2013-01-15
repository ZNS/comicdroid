package com.zns.comicdroid.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.ComicArrayAdapter;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.Group;
import com.zns.comicdroid.dialog.GroupAddDialogFragment;
import com.zns.comicdroid.service.UploadService;
import com.zns.comicdroid.task.BooksQueryResult;
import com.zns.comicdroid.task.BooksQueryTask;

import de.greenrobot.event.EventBus;

public class Add extends BaseFragmentActivity 
	implements GroupAddDialogFragment.OnGroupAddDialogListener {

	private final static String STATE_COMICS = "COMICS";
	
	private EditText etISBN;
	private ComicArrayAdapter adapter;
	private Spinner spGroup;
	private boolean isScanning = false;
		
	private ArrayAdapter<Group> adapterGroups;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);               
        setContentView(R.layout.activity_add);
                
        etISBN = (EditText)findViewById(R.id.etISBN);        
        spGroup = (Spinner)findViewById(R.id.add_spGroup);
        ListView lvComics = (ListView)findViewById(R.id.add_lvComics);
        ImageView ivGroupAdd = (ImageView)findViewById(R.id.add_ivGroupAdd);
        
		EventBus.getDefault().register(this, "onBookQueryComplete", BooksQueryResult.class);
		
    	//Spinner groups
    	List<Group> groups = getDBHelper().getGroups();
    	if (groups == null)
    		groups = new ArrayList<Group>();
    	groups.add(0, new Group(0, getResources().getString(R.string.common_nogroup), null));
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
    	
	    ArrayList<Comic> comics = new ArrayList<Comic>();
	    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_COMICS)) {
	    	comics = savedInstanceState.getParcelableArrayList(STATE_COMICS);
	    }
        adapter = new ComicArrayAdapter(this, comics);
        lvComics.setAdapter(adapter);
    }

    @Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);		
		state.putParcelableArrayList(STATE_COMICS, new ArrayList<Comic>(adapter.getAll()));
	}   
    
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		List<Group> groups = getDBHelper().getGroups();
		groups.add(0, new Group(0, getResources().getString(R.string.common_nogroup), null));
		adapterGroups.clear();
		for (Group g : groups)
			adapterGroups.add(g);
	}
		
	@Override
	public void onStop() {
		if (!isScanning)
		{
			if (adapter != null && adapter.getCount() > 0) {
				//Sync with google drive
				Intent intent = new Intent(this, UploadService.class);
				startService(intent);
			}
		}
		super.onStop();
	}
	
	@Override
	public void onDestroy() {		
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
	
	public void create(View view)
	{
    	Intent intent = new Intent(this, Edit.class);
    	startActivity(intent);		
	}
	
    public void scanISBN(View view)
    {
    	isScanning = true;
    	IntentIntegrator integrator = new IntentIntegrator(this);
    	integrator.initiateScan();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
    	if (requestCode == IntentIntegrator.REQUEST_CODE)
    	{
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			if (scanResult != null) 
			{
				etISBN.setText(scanResult.getContents());
				queryISBN(null);
			}
			isScanning = false;
    	}    	
	}
    
    public void onBookQueryCompleteMainThread(BooksQueryResult result) {
		if (result.success)
		{														
			if (spGroup.getSelectedItemPosition() > 0) {
				Group g = (Group)spGroup.getSelectedItem();
				result.comic.setGroupId(g.getId());
			}
			getDBHelper().storeComic(result.comic);
			adapter.insert(result.comic, 0);
			adapter.notifyDataSetChanged();
			return;
		}
		
		Toast
			.makeText(Add.this, getResources().getString(R.string.add_search_notfound), Toast.LENGTH_LONG)
			.show();
    }
    
    public void queryISBN(View view)
    {
    	String isbn = etISBN.getText().toString().toUpperCase(Locale.ENGLISH);
    	
    	//Validate ISBN
    	boolean isValid = false;
    	if (isbn.length() == 10 || isbn.length() == 13) {
    		if (isbn.length() == 10) {
    			int sum = 0;
    			for (int x = 0; x < 10; x++) {
    				int digit = isbn.charAt(x) != 'X' ? ((int)isbn.charAt(x) & 0xF) : 10;
    				sum += x != 9 ? digit * (10 - x) : digit;
    			}
    			isValid = sum % 11 == 0;
    		}
    		else {
    		    int sum = 0;
    		    for (int x = 0; x < 13; x += 2) {
    		        sum += ((int)isbn.charAt(x) & 0xF);
    		    }
    		    for (int x = 1; x < 12; x += 2) {
    		        sum += ((int)isbn.charAt(x) & 0xF) * 3;
    		    }
    		    isValid = sum % 10 == 0;
    		}
    	}
    	if (!isValid) {
    		Toast.makeText(this, getResources().getString(R.string.add_search_invalidisbn), Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	//Duplicate check
    	if (getDBHelper().isDuplicateComic(isbn)) {
    		Toast.makeText(this, getResources().getString(R.string.add_search_isduplicate), Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	//Fire query
		new BooksQueryTask().execute("isbn:" + isbn, getExternalFilesDir(null).toString(), isbn);
    }
}
