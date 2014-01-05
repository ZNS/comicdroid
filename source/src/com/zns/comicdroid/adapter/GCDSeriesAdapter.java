package com.zns.comicdroid.adapter;

import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zns.comicdroid.R;
import com.zns.comicdroid.gcd.Series;

public class GCDSeriesAdapter extends ArrayAdapter<Series> {

	static class SeriesHolder
	{
		TextView tvName;
		ImageView ivImage;
		TextView tvPublisher;
		TextView tvIssueCount;
	}

	private final List<Series> mSeries;
	
	public GCDSeriesAdapter(Context context, List<Series> series)
	{
		super(context, R.layout.list_gcdseriesrow, series);
		this.mSeries = series;
	}

	public Series getSeries(int position)
	{
		if (position < mSeries.size())
			return mSeries.get(position);
		return null;
	}
	
	public List<Series> getAll(){
		return this.mSeries;  
	}
	
	public void addMany(Collection<Series> series) {
		for (Series s : series) {
			mSeries.add(s);
		}
	}
	
	@Override
	public int getCount() {
		if (mSeries != null) {
			return mSeries.size();
		}
		return 0;
	}
	
	@Override
	public void clear() {
		mSeries.clear();
		super.clear();
	}
	
	@Override
	public long getItemId(int position) {
		return mSeries.get(position).id;
	}	
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{		  
		View row = convertView;
		SeriesHolder holder = null;

		if (row == null)
		{
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			row = inflater.inflate(R.layout.list_gcdseriesrow, parent, false);

			holder = new SeriesHolder();
			holder.tvName = (TextView)row.findViewById(R.id.gcdseries_tvName);
			holder.tvPublisher = (TextView)row.findViewById(R.id.gcdseries_tvPublisher);
			holder.tvIssueCount = (TextView)row.findViewById(R.id.gcdseries_tvIssueCount);
			row.setTag(holder);
		}
		else
		{
			holder = (SeriesHolder)row.getTag();
		}
		
		Series series = mSeries.get(position);
		holder.tvName.setText(series.name);
		holder.tvPublisher.setText(series.publisher);
		holder.tvIssueCount.setText(Integer.toString(series.issue_count));
		
		return row;
	}
}
