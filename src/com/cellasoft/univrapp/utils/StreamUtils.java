package com.cellasoft.univrapp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class StreamUtils {
	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final int BUFFER_SIZE = 1024 * 8;

	/**
	 * Workaround for bug pre-Froyo, see here for more info:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 */
	public static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (!UIUtils.hasFroyo()) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public static String readFromUrl(String url) {
		return readFromUrl(url, DEFAULT_ENCODING);
	}

	/**
	 * Reading content of URL to a string using provided encoding
	 * 
	 * @param url
	 * @param encoding
	 * @return
	 */
	public static String readFromUrl(String url, String encoding) {
		disableConnectionReuseIfNecessary();
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse response = client.execute(get);
			InputStream is = response.getEntity().getContent();
			String result = readAllText(is, encoding);
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String readAllText(InputStream inputStream) {
		return readAllText(inputStream, DEFAULT_ENCODING);
	}

	public static String[] readLines(InputStream inputStream, String encoding) {
		List<String> lines = Lists.newArrayList();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream, encoding), BUFFER_SIZE);
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}

			closeQuietly(reader);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(inputStream);
		}
		return lines.toArray(new String[0]);
	}

	public static String readAllText(InputStream inputStream, String encoding) {
		StringBuilder responseBuilder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream, encoding), BUFFER_SIZE);
			String responseLine;

			while ((responseLine = reader.readLine()) != null) {
				responseBuilder.append(responseLine);
			}

			closeQuietly(reader);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(inputStream);
		}
		return responseBuilder.toString();
	}

	public static void writeStream(InputStream is, OutputStream os)
			throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int readBytes = 0;
		while ((readBytes = is.read(buffer)) > 0) {
			os.write(buffer, 0, readBytes);
		}
	}

	/**
	 * Closes 'closeable', ignoring any checked exceptions. Does nothing if
	 * 'closeable' is null.
	 */
	public static void closeQuietly(java.io.Closeable stream) {
		try {
			if (stream != null)
				stream.close();
		} catch (RuntimeException rethrown) {
			throw rethrown;
		} catch (Exception ignored) {
		}
	}
}