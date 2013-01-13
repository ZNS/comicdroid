package com.zns.comicdroid.activity;

import java.util.List;

import android.os.Bundle;
import android.widget.ListView;

import com.zns.comicdroid.BaseFragmentActivity;
import com.zns.comicdroid.R;
import com.zns.comicdroid.adapter.BorrowedAdapter;
import com.zns.comicdroid.data.Comic;

public class Borrowed extends BaseFragmentActivity {
	
	private BorrowedAdapter adapter;
	private ListView lvComics;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_borrowed);
		
		lvComics = (ListView)findViewById(R.id.borrowed_lvComics);
		lvComics.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		List<Comic> comics = getDBHelper().getBorrowed();
		adapter = new BorrowedAdapter(this, comics);
		lvComics.setAdapter(adapter);
	}
}