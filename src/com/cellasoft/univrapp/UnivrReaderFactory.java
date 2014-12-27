package com.cellasoft.univrapp;

import com.cellasoft.univrapp.reader.UnivrReader;

public class UnivrReaderFactory {

    public static UnivrReader getUnivrReader() {
        return new UnivrReader(Application.getInstance()
                .getApplicationContext());
    }
}