package com.cellasoft.univrapp.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cellasoft.univrapp.Settings;

public class Html {
	private static final Pattern bodyPattern = Pattern.compile(
			"<body[^>]*?>([\\s\\S]*?)</body>", Pattern.CASE_INSENSITIVE);

	private static final Pattern fileExtnPtrn = Pattern
			.compile("([^\\s]+(\\.(?i)(txt|doc|docx|odt|pdf|xls|zip|rar|R))$)");

	public static String getBodyContent(String dirtyHtml) {
		Matcher matcher = bodyPattern.matcher(dirtyHtml);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public static String toText(String html) {
		if (html == null)
			return html;
		return html.replaceAll("</?[^>]+>", "").trim();
	}

	public static String decode(String html) {
		if (html == null)
			return html;
		return html.replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'")
				.replace("&nbsp;", " ").replace("&amp;", "&")
				.replace("&mdash;", "").replace("&#39;", "'");
	}

	public static boolean validateFileExtn(String html) {
		Matcher matcher = fileExtnPtrn.matcher(html);
		return matcher.matches();
	}

	public static String getAttachment(String html) {
		Document doc = Jsoup.parse(html);

		Element file = doc.select("a.LinkContent").first();

		if (file != null && validateFileExtn(file.attr("href"))) {
			return Settings.getUniversity().domain + file.attr("href");

		}

		return null;
	}

	public static String getFileNameToPath(String path) {
		int start = path.lastIndexOf("/") + 1;
		return path.substring(start);

	}

	public static String parserPage(String html) {
		String body = getBodyContent(html);
		Document doc = Jsoup.parse(body);

		Element table = doc.select("body > table").get(1);
		
		Element article = table.select("tbody > tr > td:not(:has(a.LinkContent))").last();
		
		return article.html();
	}
}