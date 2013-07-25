package com.zns.comicdroid.adapter;

import com.zns.comicdroid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerMenuAdapter extends BaseAdapter {

    private String[] title;
    private String[] subTitle;
    private LayoutInflater inflater;
    private Context context;
    
    public DrawerMenuAdapter(Context context, String[] title, String[] subtitle) {
    	this.context = context;
        this.title = title;
        this.subTitle = subtitle;
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return title.length;
	}

	@Override
	public Object getItem(int pos) {
		// TODO Auto-generated method stub
		return title[pos];
	}

	@Override
	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
			TextView txtTitle;
	        TextView txtSubTitle;		
    		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);    		
    		View itemView = inflater.inflate(R.layout.navigation_drawer_menu_item, parent, false);
            txtTitle = (TextView) itemView.findViewById(R.id.title);
            txtSubTitle = (TextView) itemView.findViewById(R.id.subtitle);
            txtTitle.setText(title[position]);
            txtSubTitle.setText(subTitle[position]);
            return itemView;
	}
}
