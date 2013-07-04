package com.cellasoft.univrapp;

import com.cellasoft.univrapp.reader.UnivrReader;

public class UnivrReaderFactory {

	public static UnivrReader getUnivrReader() {
		UnivrReader reader = new UnivrReader(Application.getInstance()
				.getApplicationContext());
		return reader;
	}
}