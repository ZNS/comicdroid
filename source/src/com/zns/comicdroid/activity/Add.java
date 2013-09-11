package com.zns.comicdroid.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.zns.comicdroid.dialog.AuthorIllustratorDialogFragment;
import com.zns.comicdroid.dialog.GroupDialogFragment;
import com.zns.comicdroid.task.BooksQueryResult;
import com.zns.comicdroid.task.BooksQueryTask;

import de.greenrobot.event.EventBus;

public class Add extends BaseFragmentActivity 
implements GroupDialogFragment.OnGroupAddDialogListener,
AuthorIllustratorDialogFragment.OnAuthorIllustratorDialogListener {

	private final static String STATE_COMICS = "COMICS";

	private EditText etISBN;
	private ComicArrayAdapter adapter;
	private Spinner spGroup;
	private boolean isScanning = false;
	private CheckBox cbIsRead;
	private Button btnScan;

	private ArrayAdapter<Group> adapterGroups;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_add);
		super.onCreate(savedInstanceState);               

		etISBN = (EditText)findViewById(R.id.etISBN);        
		spGroup = (Spinner)findViewById(R.id.add_spGroup);
		cbIsRead = (CheckBox)findViewById(R.id.add_cbIsRead);
		ListView lvComics = (ListView)findViewById(R.id.add_lvComics);
		ImageView ivGroupAdd = (ImageView)findViewById(R.id.add_ivGroupAdd);
		btnScan = (Button)findViewById(R.id.btnScan);

		EventBus.getDefault().register(this, "onBookQueryComplete", BooksQueryResult.class);

		//Spinner groups
		List<Group> groups = getDBHelper().getGroups();
		if (groups == null)
			groups = new ArrayList<Group>();
		groups.add(0, new Group(0, getResources().getString(R.string.common_nogroup), null, 0, 0, 0, 0, 0));
		adapterGroups = new ArrayAdapter<Group>(this, android.R.layout.simple_spinner_item, groups);
		adapterGroups.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spGroup.setAdapter(adapterGroups);

		//Dialog
		ivGroupAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment dialogAddGroup = new GroupDialogFragment();
				dialogAddGroup.show(getSupportFragmentManager(), "GROUPADD");
			}
		});

		ArrayList<Comic> comics = new ArrayList<Comic>();
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_COMICS)) {
			comics = savedInstanceState.getParcelableArrayList(STATE_COMICS);
		}
		adapter = new ComicArrayAdapter(this, comics, getImagePath(true));
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
		groups.add(0, new Group(0, getResources().getString(R.string.common_nogroup), null, 0, 0, 0, 0, 0));
		adapterGroups.clear();
		for (Group g : groups)
			adapterGroups.add(g);
	}

	@Override
	public void onStop() {
		if (!isScanning)
		{
			if (adapter != null && adapter.getCount() > 0) {
				//Backup
				BackupManager m = new BackupManager(this);
				m.dataChanged();
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
		if (result.mSuccess)
		{		
			if (spGroup.getSelectedItemPosition() > 0) {
				Group g = (Group)spGroup.getSelectedItem();
				result.mComic.setGroupId(g.getId());
			}
			if (cbIsRead.isChecked()) {
				result.mComic.setIsRead(true);
			}
			int comicId = getDBHelper().storeComic(result.mComic);			
			if (result.mComic.getAuthor().contains(","))
			{
				//Try to figure out author/illustrator automagically
				String[] names = result.mComic.getAuthor().split(",");
				List<String> authors = getDBHelper().getAuthors(names);
				List<String> illustrators = getDBHelper().getIllustrators(names);
				if (authors.size() == 1)
					result.mComic.setAuthor(authors.get(0));
				if (illustrators.size() == 1)
					result.mComic.setIllustrator(illustrators.get(0));
				if (names.length > (authors.size() + illustrators.size()) || authors.size() != 1 || illustrators.size() != 1)
				{
					//Open dialog for author/illustrator
					AuthorIllustratorDialogFragment dialog = AuthorIllustratorDialogFragment.newInstance(comicId, result.mComic.getAuthor());
					dialog.show(getSupportFragmentManager(), "AUTHORILLUSTRATOR");
				}
			}			
			adapter.insert(result.mComic, 0);
			adapter.notifyDataSetChanged();

			Toast.makeText(Add.this, getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
			return;
		}

		btnScan.setEnabled(true);
		etISBN.setText("");
		Toast
		.makeText(Add.this, getResources().getString(R.string.add_search_notfound), Toast.LENGTH_LONG)
		.show();		
	}

	public void queryISBN(View view)
	{
		String isbn = etISBN.getText().toString();

		//Validate ISBN
		if (!isbn.contains(":"))
		{
			isbn = isbn.toUpperCase(Locale.ENGLISH);
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
		}

		//Fire query
		btnScan.setEnabled(false);    	
		String q = "isbn:" + isbn;
		if (isbn.contains(":"))
			q = isbn;
		new BooksQueryTask().execute(q, getImagePath(false), isbn);
	}

	@Override
	public void onDialogPositiveClick(int comicId, String authors, String illustrators) {
		if (comicId > -1 && (authors.length() > 0 || illustrators.length() > 0))
		{
			ContentValues values = new ContentValues();
			if (authors.length() > 0)
				values.put("Author", authors);
			if (illustrators.length() > 0)
				values.put("Illustrator", illustrators);
			getDBHelper().update("tblBooks", values, "_id=?", new String[] { Integer.toString(comicId) });
		}
	}
}