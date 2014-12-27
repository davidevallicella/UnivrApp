package com.cellasoft.univrapp.exception;

public class UnivrReaderException extends Exception {

    private static final long serialVersionUID = -3258378133968912876L;
    public int code;

    public UnivrReaderException(int code, String detailMessage) {
        super(String.valueOf(code) + ": " + detailMessage);
        this.code = code;
    }

    public UnivrReaderException() {
        super();
    }

    public UnivrReaderException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnivrReaderException(String detailMessage) {
        super(detailMessage);
    }

    public UnivrReaderException(Throwable throwable) {
        super(throwable);
    }
}