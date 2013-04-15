package com.cellasoft.univrapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

public class FileUtils {
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
}