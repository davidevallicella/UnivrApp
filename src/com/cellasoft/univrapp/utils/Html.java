package com.cellasoft.univrapp.utils;

import java.util.List;
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
			.compile("([^\\s]+(\\.(?i)(txt|rtf|doc|docx|docm|odt|ppt|pptx|xlt|xltx|xltm|pps|ppsx|ods|pdf|xls|zip|rar|tar|7z|R))$)");

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

	public static List<String> getAttachment(String html) {
		List<String> attachment = Lists.newArrayList();
		Document doc = Jsoup.parse(html);

		Elements files = doc.select("a.LinkContent");

		for (Element file : files) {
			if (file != null && validateFileExtn(file.attr("href"))) {
				String path = Settings.getUniversity().domain
						+ file.attr("href");
				String attach = "<a href=\"" + path + "\">"
						+ getFileNameToPath(path) + "</a></div><br/><small>"
						+ decode(file.text()).trim() + "</small></td>";
				attachment.add(attach);

			}
		}

		return attachment;
	}

	public static String getFileNameToPath(String path) {
		int start = path.lastIndexOf("/") + 1;
		return path.substring(start);

	}

	public static String parserPage(String html) {
		String body = getBodyContent(html);

		Document doc = Jsoup.parse(body);

		try {
			Element table = doc.select("body > table").get(1);
			Element article = table.select("table:has(th:contains(Content))")
					.last().select("tr").get(1);

			return article.html();
		} catch (Exception e) {
			return "";
		} finally {
			doc = null;
		}
	}

}