package com.cellasoft.univrapp.criteria;

import android.net.Uri;

public interface ItemCriteria {
	Uri getContentUri();

	String getSelection();

	String[] getSelectionArgs();

	String getOrderBy();
}