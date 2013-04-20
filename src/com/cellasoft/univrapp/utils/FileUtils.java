package com.cellasoft.univrapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

import android.content.Context;

import com.cellasoft.univrapp.Application;

public class FileUtils {
	private static Context context;
	static {
		context = Application.getInstance();
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			StreamUtils.closeQuietly(source);
			StreamUtils.closeQuietly(destination);
		}
	}

	public static void save(String content, String fileName) throws IOException {
		FileOutputStream os = new FileOutputStream(fileName);
		OutputStreamWriter writer = new OutputStreamWriter(os);
		writer.write(content);
		os.flush();
		StreamUtils.closeQuietly(os);
	}

	public static String getFileFromAssets(String fileName) {
		try {
			return StreamUtils.readAllText(context.getAssets().open(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
}