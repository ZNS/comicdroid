package com.zns.comicdroid.data;

import com.zns.comicdroid.BaseFragmentActivity;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.FilterQueryProvider;

public class AutoCompleteAdapter extends SimpleCursorAdapter
	implements 	FilterQueryProvider,
				SimpleCursorAdapter.CursorToStringConverter {
	
	private Cursor cursorPublishers;
	private CharSequence strPublisherQuery;
	private Runnable runPublisherAC;
	private Handler handlerAutoComplete;
	private int columnIndex;
	private String sql;
	
	public AutoCompleteAdapter(Context context, String sql, String columnName, int columnIndex) {
		super(context,
				android.R.layout.simple_dropdown_item_1line,
				null,
				new String[] { columnName },
				new int[] { android.R.id.text1 }, 0);
		
		this.sql = sql;
		this.columnIndex = columnIndex;
		this.handlerAutoComplete = new Handler();
		
		runPublisherAC = new Runnable() {
			@Override
			public void run() {
				if (AutoCompleteAdapter.this.mContext instanceof BaseFragmentActivity)
				{
					if (cursorPublishers != null)
						cursorPublishers.close();
					cursorPublishers = ((BaseFragmentActivity)AutoCompleteAdapter.this.mContext)
							.getDBHelper()
							.getCursor(AutoCompleteAdapter.this.sql, new String[] { strPublisherQuery + "%" });
					changeCursor(cursorPublishers);
				}
			}
		};
		
		setFilterQueryProvider(this);		
		setCursorToStringConverter(this);
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		if (constraint.length() > 0)
		{
			strPublisherQuery = constraint;
			handlerAutoComplete.removeCallbacks(runPublisherAC);
			handlerAutoComplete.postDelayed(runPublisherAC, 1000);
		}
		return null;
	}
	
	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(columnIndex);
	}
	
	public void close() {
		if (cursorPublishers != null && !cursorPublishers.isClosed())
			cursorPublishers.close();
	}	
}