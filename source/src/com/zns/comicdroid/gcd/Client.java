package com.zns.comicdroid.gcd;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.sufficientlysecure.donations.google.util.Base64;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.zns.comicdroid.R;
import com.zns.comicdroid.util.JsonUtil;

public class Client {
	public final static String BASEURL = "https://comicdroid.zns.se";
	public final static String IMAGEURL = "http://comicdroid.zns.se";
	private final TrustManagerFactory mFactory;
	private final String mAPIUser;
	private final String mAPIPasswd;
	
	public Client(Context context) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, NotFoundException, IOException {
		mAPIUser = context.getString(R.string.key_gcd_api_user);
		mAPIPasswd = context.getString(R.string.key_gcd_api_passwd);
		KeyStore trusted = KeyStore.getInstance("BKS");
		InputStream in = context.getResources().openRawResource(R.raw.comicdroidkeystore);
		try {
			trusted.load(in, context.getResources().getString(R.string.key_gcd_keystore_passwd).toCharArray());
		}
		finally {
			in.close();
		}			
		mFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		mFactory.init(trusted);		
	}
	
	public Issue queryISBN(String isbn) {
		Issue issue = null;
		try {
			HttpsURLConnection connection = getConnection(BASEURL + "/isbn/" + isbn, 5000);
			issue = JsonUtil.deserialize(connection, Issue.class, null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return issue;
	}
	
	public List<Series> searchSeries(String query, int page) {
		List<Series> series = null;
		try {
			HttpsURLConnection connection = getConnection(BASEURL + "/search?q=" + URLEncoder.encode(query, "utf-8") + "&p=" + page, 15000);
			series = new ArrayList<Series>(JsonUtil.deserializeArray(connection, Series.class));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return series;
	}
	
	public List<Issue> getIssuesBySeries(int seriesId, int page) {
		List<Issue> issues = null;
		try {
			HttpsURLConnection connection = getConnection(BASEURL + "/issues/" + Integer.toString(seriesId) + "/?p=" + page, 15000);
			issues = new ArrayList<Issue>(JsonUtil.deserializeArray(connection, Issue.class));
		}
		catch (Exception e) {
			
		}
		return issues;
	}
	
	private HttpsURLConnection getConnection(String url, int timeout)
			throws MalformedURLException, IOException, NoSuchAlgorithmException, KeyManagementException
	{
		HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
		connection.setConnectTimeout(timeout);
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, mFactory.getTrustManagers(), new SecureRandom());			
		connection.setSSLSocketFactory(sslContext.getSocketFactory());			
		String strAuth = mAPIUser + ":" + mAPIPasswd;
		String encAuth = Base64.encode(strAuth.getBytes());
		connection.setRequestProperty("Authorization", "Basic " + encAuth);		
		return connection;
	}	
}