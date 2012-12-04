package com.zns.comicdroid;

import com.zns.comicdroid.data.ComicsAdapter;
import com.zns.comicdroid.data.DB;

import android.os.Bundle;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class Start extends BaseActivity
	implements OnItemSelectedListener
{
	DB comicDB;
	ListView lvComics;
	ComicsAdapter adapter;
	Spinner spComicsOrder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		comicDB = new DB(this);
		lvComics = (ListView)findViewById(R.id.lvComics);
		spComicsOrder = (Spinner)findViewById(R.id.spComicsOrder);
		lvComics.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
				int comicId = adapter.getComicId(position);
				Intent intent = new Intent(Start.this, ComicView.class);
				intent.putExtra("com.zns.comic.COMICID", comicId);
				startActivity(intent);
			}			
		});
		registerForContextMenu(lvComics);
		
		ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(this,
		        R.array.spinner_comics_orderby, android.R.layout.simple_spinner_item);
		spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spComicsOrder.setAdapter(spAdapter);
		spComicsOrder.setOnItemSelectedListener(this);
		
		//BindComics();
	}

	private void BindComics()
	{
		String order = (String)spComicsOrder.getSelectedItem();
		if (adapter == null)
			adapter = new ComicsAdapter(this);
		lvComics.setAdapter(adapter);
		
		/*ArrayList<Comic> comics = comicDB.getComics(order);
		if (comics != null && comics.size() > 0)
		{
			if (adapter == null)
			{
				adapter = new ComicArrayAdapter(this, comics);
			}
			else
			{
				adapter.clear();
		        for (Comic comic : comics) {
		            adapter.insert(comic, adapter.getCount());
		        }
		        adapter.notifyDataSetChanged();
			}
			lvComics.setAdapter(adapter);
		}*/	
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		BindComics();
    }
	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.start_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int comicId = adapter.getComicId((int)info.id);
		if (comicId > 0)
		{
			switch (item.getItemId()) {
				case R.id.start_context_delete:
					lvComics.removeViews((int)info.id, 1);
					comicDB.deleteComic(comicId);					
					return true;
			}
		}
		return super.onContextItemSelected(item);
	}
}