/*******************************************************************************
 * Copyright (c) 2013 Ulrik Andersson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Ulrik Andersson - initial API and implementation
 ******************************************************************************/
package com.zns.comicdroid.adapter;

import com.zns.comicdroid.BaseFragmentActivity;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.FilterQueryProvider;

public class AutoCompleteAdapter extends SimpleCursorAdapter
implements 	FilterQueryProvider,
SimpleCursorAdapter.CursorToStringConverter {

	private Cursor mCursorPublishers;
	private CharSequence mStrPublisherQuery;
	private final Runnable mRunPublisherAC;
	private final Handler mHandlerAutoComplete;
	private final int mColumnIndex;
	private final String mSql;

	public AutoCompleteAdapter(Context context, String sql, String columnName, int columnIndex) {
		super(context,
				android.R.layout.simple_dropdown_item_1line,
				null,
				new String[] { columnName },
				new int[] { android.R.id.text1 }, 0);

		this.mSql = sql;
		this.mColumnIndex = columnIndex;
		this.mHandlerAutoComplete = new Handler();

		mRunPublisherAC = new Runnable() {
			@Override
			public void run() {
				if (AutoCompleteAdapter.this.mContext instanceof BaseFragmentActivity)
				{
					if (mCursorPublishers != null)
						mCursorPublishers.close();
					mCursorPublishers = ((BaseFragmentActivity)AutoCompleteAdapter.this.mContext)
							.getDBHelper()
							.getCursor(AutoCompleteAdapter.this.mSql, new String[] { mStrPublisherQuery + "%" });
					changeCursor(mCursorPublishers);
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
			mStrPublisherQuery = constraint;
			mHandlerAutoComplete.removeCallbacks(mRunPublisherAC);
			mHandlerAutoComplete.postDelayed(mRunPublisherAC, 1000);
		}
		return null;
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(mColumnIndex);
	}

	public void close() {
		if (mCursorPublishers != null && !mCursorPublishers.isClosed())
			mCursorPublishers.close();
	}
}
