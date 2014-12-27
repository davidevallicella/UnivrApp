package com.cellasoft.univrapp.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActiveList<T> extends ArrayList<T> implements Serializable {

    private static final long serialVersionUID = 870605912858223939L;
    transient private List<ActiveListListener<T>> listeners;

    @Override
    public synchronized void clear() {
        super.clear();
        fireChangedEvent();
    }

    @Override
    public synchronized int size() {
        return super.size();
    }

    @Override
    public synchronized T get(int index) {
        return super.get(index);
    }

    @Override
    public synchronized boolean add(T item) {
        boolean success = super.add(item);
        if (success) {
            fireAddEvent(item);
        }
        return success;
    }

    @Override
    public synchronized void add(int index, T item) {
        super.add(index, item);
        fireInsertEvent(index, item);
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends T> items) {
        boolean success = super.addAll(index, items);
        if (success) {
            fireAddAllEvent(items);
        }
        return success;
    }

    @Override
    public synchronized boolean addAll(Collection<? extends T> items) {
        boolean success = super.addAll(items);
        if (success) {
            fireAddAllEvent(items);
        }
        return success;

    }

    public synchronized void addListener(ActiveListListener<T> listener) {
        if (this.listeners == null) {
            listeners = Lists.newArrayList();
        }
        this.listeners.add(listener);
    }

    public synchronized void removeListener(ActiveListListener<T> listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    private void fireChangedEvent() {
        if (this.listeners == null)
            return;
        for (ActiveListListener<T> listener : listeners) {
            listener.onClear();
        }
    }

    private void fireInsertEvent(int location, T item) {
        if (this.listeners == null)
            return;
        for (ActiveListListener<T> listener : listeners) {
            listener.onInsert(location, item);
        }
    }

    private void fireAddEvent(T item) {
        if (this.listeners == null)
            return;
        for (ActiveListListener<T> listener : listeners) {
            listener.onAdd(item);
        }
    }

    private void fireAddAllEvent(Collection<? extends T> items) {
        if (this.listeners == null)
            return;
        for (ActiveListListener<T> listener : listeners) {
            listener.onAddAll(items);
        }
    }

    public interface ActiveListListener<T> {
        void onAdd(T item);

        void onInsert(int location, T item);

        void onAddAll(Collection<? extends T> items);

        void onClear();
    }
}