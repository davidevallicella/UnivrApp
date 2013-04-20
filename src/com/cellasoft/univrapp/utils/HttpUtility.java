package com.cellasoft.univrapp.utils;

import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Locale;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import com.cellasoft.univrapp.Application;
import com.cellasoft.univrapp.exception.UnivrReaderException;
import com.github.droidfu.cachefu.AbstractCache;
import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpRequest;
import com.github.droidfu.http.BetterHttpResponse;

public class HttpUtility {

	// Wait this many milliseconds max for the TCP connection to be established
	private static final int CONNECTION_TIMEOUT = 60 * 1000;

	// Wait this many milliseconds max for the server to send us data once the
	// connection has been established
	private static final int SO_TIMEOUT = 5 * 60 * 1000;

	private static Context context;
	static {
		context = Application.getInstance();
		setHttpClient(false);
	}

	public static BetterHttpResponse get(String uri)
			throws UnivrReaderException, ConnectException, Exception {

		// Get the data
		final BetterHttpRequest request = BetterHttp.get(uri);
		BetterHttpResponse response = request.send();

		// Check the http status code is correct
		final int status = response.getStatusCode();
		if (status != HttpStatus.SC_OK) {
			throw new UnivrReaderException("Unexpected server response "
					+ response.getStatusCode() + " for " + "(" + uri + ")");
		}

		return response;
	}

	public static void setHttpClient(boolean enableCache) {
		BetterHttp.setHttpClient((AbstractHttpClient) getHttpClient());
		enableCache(enableCache);
	}
	
	public static void enableCache(boolean enableCache) {
		System.out.println("---------------------- cache " + enableCache);
		BetterHttp.enableResponseCache(context, 32, 60, 2,
				AbstractCache.DISK_CACHE_INTERNAL);
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

	public static HttpClient getHttpClient() {
		AbstractHttpClient client = new DefaultHttpClient() {
			@Override
			protected ClientConnectionManager createClientConnectionManager() {
				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				registry.register(new Scheme("https", getHttpsSocketFactory(),
						443));
				HttpParams params = getParams();
				HttpConnectionParams.setSocketBufferSize(params, 8192);
				HttpConnectionParams.setConnectionTimeout(params,
						CONNECTION_TIMEOUT);
				HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
				HttpProtocolParams.setUserAgent(params,
						getUserAgent(HttpProtocolParams.getUserAgent(params)));
				return new ThreadSafeClientConnManager(params, registry);
			}

			/**
			 * Gets an HTTPS socket factory with SSL Session Caching if such
			 * support is available, otherwise falls back to a non-caching
			 * factory
			 * 
			 * @return
			 */
		
			protected SocketFactory getHttpsSocketFactory() {
				try {
					Class<?> sslSessionCacheClass = Class.forName("android.net.SSLSessionCache");
					Object sslSessionCache = sslSessionCacheClass.getConstructor(Context.class).newInstance(context);
					Method getHttpSocketFactory = Class.forName(
							"android.net.SSLCertificateSocketFactory")
							.getMethod(
									"getHttpSocketFactory",
									new Class[] { int.class,
											sslSessionCacheClass });
					return (SocketFactory) getHttpSocketFactory.invoke(null,
							CONNECTION_TIMEOUT, sslSessionCache);
				} catch (Exception e) {
					Log.e("HttpClientProvider",
							"Unable to use android.net.SSLCertificateSocketFactory to get a SSL session caching socket factory, falling back to a non-caching socket factory",
							e);
					return SSLSocketFactory.getSocketFactory();
				}

			}
		};
		return client;
	}
}
