package com.cellasoft.univrapp.utils;

import java.io.IOException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.exception.UnivrReaderException;

public class HttpUtility {

	// Wait this many milliseconds max for the TCP connection to be established
	private static final int CONNECTION_TIMEOUT = 5 * 1000;

	// Wait this many milliseconds max for the server to send us data once the
	// connection has been established
	private static final int SO_TIMEOUT = 10 * 1000;

	private static CookieStore cookieStore;
	private static HttpContext httpContext;

	private static Context context;

	static {
		context = Application.getInstance();
		cookieStore = new BasicCookieStore();
		httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	public static HttpResponse get(String uri) throws UnivrReaderException {
		// HTTP connection reuse which was buggy pre-froyo
		
		HttpGet get = new HttpGet(uri);
		DefaultHttpClient client = getHttpClient();
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUserAgent(params,
				getUserAgent(HttpProtocolParams.getUserAgent(params)));
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);

		client.setParams(params);
		HttpResponse response = null;
		try {
			response = client.execute(get, httpContext);

			final int status = response.getStatusLine().getStatusCode();

			if (status != HttpStatus.SC_OK) {
				HttpEntity ety = response.getEntity();
				if (ety != null)
					StreamUtils.closeQuietly(ety.getContent());
				throw new UnivrReaderException("Unexpected server response "
						+ status + " for " + "(" + uri + ")");
			}
		} catch (ClientProtocolException e) {
			get.abort();
			e.printStackTrace();
		} catch (IOException e) {
			get.abort();
			e.printStackTrace();
		} finally {
			//client.getConnectionManager().shutdown();
		}

		return response;
	}

	/**
	 * Build and return a user-agent string that can identify this application
	 * to remote servers. Contains the package name and version code.
	 */
	private static String getUserAgent(String defaultHttpClientUserAgent) {
		try {
			final PackageManager manager = context.getPackageManager();
			final PackageInfo info = manager.getPackageInfo(
					context.getPackageName(), 0);
			StringBuilder ret = new StringBuilder();
			ret.append(info.packageName);
			ret.append("/");
			ret.append(info.versionName);
			ret.append(" (");
			ret.append("Linux; U; Android ");
			ret.append(Build.VERSION.RELEASE);
			ret.append("; ");
			ret.append(Locale.getDefault());
			ret.append("; ");
			ret.append(Build.PRODUCT);
			ret.append(")");
			if (defaultHttpClientUserAgent != null) {
				ret.append(" ");
				ret.append(defaultHttpClientUserAgent);
			}
			return ret.toString();
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	/**
	 * Generate and return a {@link HttpClient} configured for general use,
	 * including setting an application-specific user-agent string.
	 */

	public static DefaultHttpClient getHttpClient() {

		DefaultHttpClient client = new DefaultHttpClient();
		ClientConnectionManager mgr = client.getConnectionManager();
		HttpParams params = client.getParams();
		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
				mgr.getSchemeRegistry()), params);
		return client;
	}
}
