package com.zns.comicdroid.amazon;

import java.io.File;
import java.util.Date;

import com.zns.comicdroid.Application;

import android.os.AsyncTask;

public class AmazonCacheTask extends AsyncTask<String, Void, Void> {

	@Override
	protected Void doInBackground(String... params) {
		//Clean up old cache files
		File cachePath = new File(params[0]);
		File[] cacheFiles = cachePath.listFiles();
		for (File file : cacheFiles) {
			long diff = new Date().getTime() - file.lastModified();
			if (diff > Application.CACHE_AMAZONSEARCH_HOURS * 60 * 60 * 1000) {
				file.delete();
			}
		}
		return null;
	}

}
