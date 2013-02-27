package com.cellasoft.univrapp.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.R.string;

public class HtmlParser {

	public static String url = Constants.UNIVERSITY.URL.get(
			Settings.getUniversity()).replace("&rss=1", "");
	public static Document doc;

	public static String get(String url) throws ClientProtocolException,
			IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response = httpClient.execute(get);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return Utils.inputStreamToString(entity.getContent());

			}
		}

		return null;
	}

	public static String getThumbnailUrl(String html)
			throws ClientProtocolException, IOException {
		String thumbnailUrl = "";

		doc = Jsoup.parse(html);
		Element img = doc.select("img[src*=/Persona/]").first();
		if (img != null) {
			System.out.println("ok");
			thumbnailUrl = url.split("\\.it")[0] + ".it" + img.attr("src");
		}

		return thumbnailUrl;
	}

	public static String getEmail(String html) throws ClientProtocolException,
			IOException {
		String email = "null";

		doc = Jsoup.parse(html);
		Pattern patt = Pattern.compile("http://www.sci.univr.it/~[a-z,A-Z]*");
		Matcher m = patt.matcher(doc.text());
		if (m.matches()) {
			System.out.println("Email: " + email);
			email = m.group(1);
		}

		return email;
	}

	public static Elements getLecturerElements()
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);

		HttpResponse response = httpClient.execute(get);
		String html;
		html = Utils.inputStreamToString(response.getEntity().getContent());
		Document doc = Jsoup.parse(html);
		return doc.select("select[name=personeMittente]>option");

	}

	public static final String DOC_PATH = "/documenti/Avviso/all/([0-1][a-z])\\.[a-z]";
	public static final String file_pattern = "[0-9a-z]+\\.[a-z]+";

	public static ArrayList<String> getDocuments(String html) {
		ArrayList<String> documents = new ArrayList<String>();
		Pattern fp = Pattern.compile(file_pattern);
		Matcher fm = fp.matcher(html);
		if (fm.find()) {
			System.out.println("find");
			String file = fm.group(0);
			System.out.println(file);
			documents.add(file);
		}

		return documents;
	}
}
