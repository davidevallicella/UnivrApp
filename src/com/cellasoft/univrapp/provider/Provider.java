package com.cellasoft.univrapp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.cellasoft.univrapp.model.Channel.Channels;
import com.cellasoft.univrapp.model.Image.Images;
import com.cellasoft.univrapp.model.Item.Items;
import com.cellasoft.univrapp.model.Lecturer.Lecturers;

import java.util.HashMap;

public class Provider extends ContentProvider {
    public static final String AUTHORITY = "com.cellasoft.univrapp.provider.provider";
    public static final String WHERE_ID = "ID=?";
    private static final UriMatcher URL_MATCHER;
    private static final int CHANNELS = 1;
    private static final int ITEMS = 2;
    private static final int ITEMS_LIMIT = 3;
    private static final int ITEMS_LIMIT_OFFSET = 4;
    private static final int ITEMS_UNREAD_COUNT_ALL_CHANNELS = 5;
    private static final int ITEMS_UNREAD_COUNT_OF_EACH_CHANNEL = 6;
    private static final int LECTURERS = 7;
    private static final int IMAGES = 8;
    private static final int IMAGES_LIMIT = 9;
    private static HashMap<String, String> channelsProjectionMap;
    private static HashMap<String, String> itemsProjectionMap;
    private static HashMap<String, String> lecturerProjectionMap;
    private static HashMap<String, String> imagesProjectionMap;
    private DatabaseHelper dbHelper;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (URL_MATCHER.match(uri)) {
            case CHANNELS:
                count = db.delete(DatabaseHelper.CHANNELS_TABLE_NAME, where,
                        whereArgs);
                break;
            case ITEMS:
                count = db
                        .delete(DatabaseHelper.ITEMS_TABLE_NAME, where, whereArgs);
                break;
            case IMAGES:
                count = db.delete(DatabaseHelper.IMAGES_TABLE_NAME, where,
                        whereArgs);
                break;
            case LECTURERS:
                count = db.delete(DatabaseHelper.LECTURERS_TABLE_NAME, where,
                        whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URL_MATCHER.match(uri)) {
            case CHANNELS:
                return Channels.CONTENT_TYPE;
            case ITEMS:
                return Items.CONTENT_TYPE;
            case LECTURERS:
                return Lecturers.CONTENT_TYPE;
            case IMAGES:
                return Images.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        int matchedUri = URL_MATCHER.match(uri);
        if (matchedUri != CHANNELS && matchedUri != ITEMS
                && matchedUri != LECTURERS && matchedUri != IMAGES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = -1;
        Uri contentUri = null;
        switch (matchedUri) {
            case CHANNELS:
                rowId = db.insert(DatabaseHelper.CHANNELS_TABLE_NAME,
                        Channels.DESCRIPTION, values);
                contentUri = Channels.CONTENT_URI;
                break;
            case ITEMS:
                rowId = db.insert(DatabaseHelper.ITEMS_TABLE_NAME,
                        Items.DESCRIPTION, values);
                contentUri = Channels.CONTENT_URI;
                break;
            case LECTURERS:
                rowId = db
                        .insert(DatabaseHelper.LECTURERS_TABLE_NAME, null, values);
                contentUri = Lecturers.CONTENT_URI;
                break;
            case IMAGES:
                rowId = db.insert(DatabaseHelper.IMAGES_TABLE_NAME, Images.URL,
                        values);
                contentUri = Images.CONTENT_URI;
                break;
            default:
                break;
        }

        if (rowId > 0 && contentUri != null) {
            contentUri = ContentUris.withAppendedId(contentUri, rowId);
            getContext().getContentResolver().notifyChange(contentUri, null);
            return contentUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        dbHelper.getWritableDatabase().rawQuery("PRAGMA synchronous=OFF", null);
        dbHelper.close();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String group = null, having = null, limit = null;
        switch (URL_MATCHER.match(uri)) {
            case CHANNELS:
                qb.setTables(DatabaseHelper.CHANNELS_TABLE_NAME);
                qb.setProjectionMap(channelsProjectionMap);
                break;
            case ITEMS:
                qb.setTables(DatabaseHelper.ITEMS_TABLE_NAME);
                qb.setProjectionMap(itemsProjectionMap);
                break;
            case ITEMS_LIMIT:
                qb.setTables(DatabaseHelper.ITEMS_TABLE_NAME);
                qb.setProjectionMap(itemsProjectionMap);
                limit = uri.getLastPathSegment();
                break;
            case ITEMS_LIMIT_OFFSET:
                qb.setTables(DatabaseHelper.ITEMS_TABLE_NAME);
                qb.setProjectionMap(itemsProjectionMap);
                limit = uri.getLastPathSegment() + ", "
                        + uri.getPathSegments().get(1);
                break;
            case IMAGES:
                qb.setTables(DatabaseHelper.IMAGES_TABLE_NAME);
                qb.setProjectionMap(imagesProjectionMap);
                limit = "50";
                break;
            case IMAGES_LIMIT:
                qb.setTables(DatabaseHelper.IMAGES_TABLE_NAME);
                qb.setProjectionMap(imagesProjectionMap);
                limit = uri.getLastPathSegment();
                break;
            case LECTURERS:
                qb.setTables(DatabaseHelper.LECTURERS_TABLE_NAME);
                qb.setProjectionMap(lecturerProjectionMap);
                break;
            case ITEMS_UNREAD_COUNT_OF_EACH_CHANNEL:
                qb.setTables(DatabaseHelper.ITEMS_TABLE_NAME);
                qb.setProjectionMap(itemsProjectionMap);
                group = Items.CHANNEL_ID;
                break;
            case ITEMS_UNREAD_COUNT_ALL_CHANNELS:
                qb.setTables(DatabaseHelper.ITEMS_TABLE_NAME);
                qb.setProjectionMap(itemsProjectionMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, group,
                having, sortOrder, limit);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (URL_MATCHER.match(uri)) {
            case CHANNELS:
                count = db.update(DatabaseHelper.CHANNELS_TABLE_NAME, values,
                        where, whereArgs);
                break;
            case ITEMS:
                count = db.update(DatabaseHelper.ITEMS_TABLE_NAME, values, where,
                        whereArgs);
                break;
            case LECTURERS:
                count = db.update(DatabaseHelper.LECTURERS_TABLE_NAME, values,
                        where, whereArgs);
                break;
            case IMAGES:
                count = db.update(DatabaseHelper.IMAGES_TABLE_NAME, values, where,
                        whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.CHANNELS_TABLE_NAME,
                CHANNELS);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.ITEMS_TABLE_NAME, ITEMS);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.LECTURERS_TABLE_NAME,
                LECTURERS);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.ITEMS_TABLE_NAME + "/#",
                ITEMS_LIMIT);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.ITEMS_TABLE_NAME + "/#/#",
                ITEMS_LIMIT_OFFSET);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.ITEMS_TABLE_NAME
                + "/unread", ITEMS_UNREAD_COUNT_OF_EACH_CHANNEL);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.ITEMS_TABLE_NAME
                + "/unread/all", ITEMS_UNREAD_COUNT_ALL_CHANNELS);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.IMAGES_TABLE_NAME, IMAGES);
        URL_MATCHER.addURI(AUTHORITY, DatabaseHelper.IMAGES_TABLE_NAME + "/#",
                IMAGES_LIMIT);

        channelsProjectionMap = new HashMap<String, String>();
        channelsProjectionMap.put(Channels.ID, Channels.ID);
        channelsProjectionMap.put(Channels.LECTURER_ID, Channels.LECTURER_ID);
        channelsProjectionMap.put(Channels.TITLE, Channels.TITLE);
        channelsProjectionMap.put(Channels.URL, Channels.URL);
        channelsProjectionMap.put(Channels.DESCRIPTION, Channels.DESCRIPTION);
        channelsProjectionMap.put(Channels.UPDATE_TIME, Channels.UPDATE_TIME);
        channelsProjectionMap.put(Channels.STARRED, Channels.STARRED);
        channelsProjectionMap.put(Channels.MUTE, Channels.MUTE);
        channelsProjectionMap.put(Channels.IMAGE_URL, Channels.IMAGE_URL);
        channelsProjectionMap.put(Channels.UNREAD, "(SELECT COUNT(*) FROM "
                + DatabaseHelper.ITEMS_TABLE_NAME + " WHERE "
                + DatabaseHelper.ITEMS_TABLE_NAME + ".CHANNEL_ID = "
                + DatabaseHelper.CHANNELS_TABLE_NAME + ".ID AND "
                + DatabaseHelper.ITEMS_TABLE_NAME + ".READ = 0) AS UNREAD");

        itemsProjectionMap = new HashMap<String, String>();
        itemsProjectionMap.put(Items.ID, Items.ID);
        itemsProjectionMap.put(Items.TITLE, Items.TITLE);
        itemsProjectionMap.put(Items.DESCRIPTION, Items.DESCRIPTION);
        itemsProjectionMap.put(Items.PUB_DATE, Items.PUB_DATE);
        itemsProjectionMap.put(Items.LINK, Items.LINK);
        itemsProjectionMap.put(Items.READ, Items.READ);
        itemsProjectionMap.put(Items.UNREAD_COUNT, "COUNT(*) AS UNREAD");
        itemsProjectionMap.put(Items.COUNT, "COUNT(*)");
        itemsProjectionMap.put(Items.CHANNEL_ID, Items.CHANNEL_ID);
        itemsProjectionMap.put(Items.UPDATE_TIME, Items.UPDATE_TIME);

        lecturerProjectionMap = new HashMap<String, String>();
        lecturerProjectionMap.put(Lecturers.ID, Lecturers.ID);
        lecturerProjectionMap.put(Lecturers.KEY, Lecturers.KEY);
        lecturerProjectionMap.put(Lecturers.DEST, Lecturers.DEST);
        lecturerProjectionMap.put(Lecturers.NAME, Lecturers.NAME);
        lecturerProjectionMap.put(Lecturers.DEPARTMENT, Lecturers.DEPARTMENT);
        lecturerProjectionMap.put(Lecturers.SECTOR, Lecturers.SECTOR);
        lecturerProjectionMap.put(Lecturers.OFFICE, Lecturers.OFFICE);
        lecturerProjectionMap.put(Lecturers.TELEPHONE, Lecturers.TELEPHONE);
        lecturerProjectionMap.put(Lecturers.EMAIL, Lecturers.EMAIL);
        lecturerProjectionMap.put(Lecturers.THUMBNAIL, Lecturers.THUMBNAIL);

        imagesProjectionMap = new HashMap<String, String>();
        imagesProjectionMap.put(Images.ID, Images.ID);
        imagesProjectionMap.put(Images.URL, Images.URL);
        imagesProjectionMap.put(Images.STATUS, Images.STATUS);
        imagesProjectionMap.put(Images.UPDATE_TIME, Images.UPDATE_TIME);
        imagesProjectionMap.put(Images.RETRIES, Images.RETRIES);
        imagesProjectionMap.put(Images.COUNT, Images.COUNT);
    }
}
