package com.cellasoft.univrapp.widget;

public interface SynchronizationListener {
    void onStart(int id);

    void onProgress(int id, long updateTime);

    void onFinish(int totalNewItems);
}