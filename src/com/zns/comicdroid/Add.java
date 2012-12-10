package com.zns.comicdroid;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.zns.comicdroid.data.Comic;
import com.zns.comicdroid.data.ComicArrayAdapter;
import com.zns.comicdroid.data.DBHelper;
import com.zns.comicdroid.isbn.BooksQueryTask;

public class Add extends BaseFragmentActivity {

	DBHelper comicDB;
	EditText etISBN;
	ComicArrayAdapter adapter;
	ListView lvComics;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);               
        setContentView(R.layout.activity_add);
        
        comicDB = new DBHelper(this);
        etISBN = (EditText)findViewById(R.id.etISBN);
        lvComics = (ListView)findViewById(R.id.add_lvComics);
        
        if (adapter == null)
        {
	        ArrayList<Comic> comics = new ArrayList<Comic>(); 
	        adapter = new ComicArrayAdapter(this, comics);
	        lvComics.setAdapter(adapter);
        }
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
    	DBHelper db = new DBHelper(this);
    	if (db.IsDuplicate(isbn)) {
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
							comicDB.storeComic(comic);
							adapter.add(comic);
							adapter.notifyDataSetChanged();
							return;
						}
						
						Toast
							.makeText(Add.this, "Not Found", Toast.LENGTH_SHORT)
							.show();						
				    }				
			}.execute("isbn:" + isbn);
		} 
		catch (Exception e) {}
    }
}
