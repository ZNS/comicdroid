package com.zns.comicdroid;

import java.util.ArrayList;

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
			}.execute("isbn:" + etISBN.getText());
		} 
		catch (Exception e) {}
    }
}
