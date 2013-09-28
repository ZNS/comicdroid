package com.zns.comicdroid.activity.fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zns.comicdroid.BaseFragment;
import com.zns.comicdroid.R;
import com.zns.comicdroid.util.Logger;

public class HelpStatsFragment extends BaseFragment {
	public static HelpStatsFragment newInstance() {
		return new HelpStatsFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_help_stats, container, false);
		TextView tvLog = (TextView)view.findViewById(R.id.help_stats_tvLog);
		
		File logFile = new File(getActivity().getExternalFilesDir(null).toString() + "/log", Logger.LOG_FILENAME);
		if (logFile.exists())
		{
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(logFile));
		        String line = br.readLine();
		        StringBuilder sb = new StringBuilder();
		        while (line != null) {
		        	sb.append(line);
		        	sb.append('\n');
		        	line = br.readLine();
		        }
		        tvLog.setText(sb.toString());
		    }
			catch (IOException e) {
				tvLog.setText("Unable to read log....");
			}
			finally {
		        try {
		        	if (br != null) {
		        		br.close();
		        	}
				} 
		        catch (IOException e) {
					e.printStackTrace();
				}
		    }
		}
		
		return view;		
	}
}
