package com.zns.comicdroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.content.Intent;
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
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.ComicArrayAdapter;
import com.zns.comicdroid.data.Group;
import com.zns.comicdroid.dialogs.GroupAddDialogFragment;
import com.zns.comicdroid.isbn.BooksQueryTask;

public class Add extends BaseFragmentActivity 
	implements GroupAddDialogFragment.OnGroupAddDialogListener {

	private EditText etISBN;
	private ComicArrayAdapter adapter;
	private ListView lvComics;
	private ImageView ivGroupAdd;
	private Spinner spGroup;
	
	
	private ArrayAdapter<Group> adapterGroups;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);               
        setContentView(R.layout.activity_add);
        
        etISBN = (EditText)findViewById(R.id.etISBN);
        lvComics = (ListView)findViewById(R.id.add_lvComics);
        spGroup = (Spinner)findViewById(R.id.add_spGroup);
        ivGroupAdd = (ImageView)findViewById(R.id.add_ivGroupAdd);
        
    	//Spinner groups
    	List<Group> groups = getDBHelper().getGroups();
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
    	
        if (adapter == null)
        {
	        ArrayList<Comic> comics = new ArrayList<Comic>(); 
	        adapter = new ComicArrayAdapter(this, comics);
	        lvComics.setAdapter(adapter);
        }
    }

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		List<Group> groups = getDBHelper().getGroups();
		groups.add(0, new Group(0, "Ingen grupp", null));
		adapterGroups.clear();
		for (Group g : groups)
			adapterGroups.add(g);
	}
	
    public void scanISBN(View view)
    {
    	IntentIntegrator integrator = new IntentIntegrator(this);
    	integrator.initiateScan();
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
	  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
	  if (scanResult != null) 
	  {
		  etISBN.setText(scanResult.getContents());
		  queryISBN(null);
	  }
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
    		Toast.makeText(this, "Invalid ISBN", Toast.LENGTH_LONG).show();
    		return;
    	}
    	
    	//Duplicate check
    	if (getDBHelper().isDuplicateComic(isbn)) {
    		Toast.makeText(this, "Boken är redan registrerad", Toast.LENGTH_LONG).show();
    		return;
    	}
    	
		try 
		{
			new BooksQueryTask() {
				 public void onPostExecute(Comic comic)
				    {
					 	if (this.exception != null)
					 	{
					 		System.out.println(this.exception.getMessage());
					 	}
					 	
						if (comic != null)
						{
							if (spGroup.getSelectedItemPosition() > 0) {
								Group g = (Group)spGroup.getSelectedItem();
								comic.setGroupId(g.getId());
							}
							getDBHelper().storeComic(comic);
							adapter.add(comic);
							adapter.notifyDataSetChanged();
							return;
						}
						
						Toast
							.makeText(Add.this, "Not Found", Toast.LENGTH_SHORT)
							.show();						
				    }				
			}.execute("isbn:" + isbn, getExternalFilesDir(null).toString(), isbn);
		} 
		catch (Exception e) {}
    }
}
