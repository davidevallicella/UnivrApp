package com.cellasoft.univrapp.loader;

import android.database.Cursor;
import com.cellasoft.univrapp.model.Item;

public interface ItemLoader {
    String[] getProjection();

    Item load(Cursor cursor);
}