package com.cellasoft.univrapp.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.cellasoft.univrapp.model.Channel.Channels;
import com.cellasoft.univrapp.model.Image.Images;
import com.cellasoft.univrapp.model.Item.Items;
import com.cellasoft.univrapp.model.Lecturer.Lecturers;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = DatabaseHelper.class.getName();

    public static final String DATABASE_NAME = "univrapp.db";
    public static final int DATABASE_VERSION = 2;
    public static final String CHANNELS_TABLE_NAME = "channels";
    public static final String ITEMS_TABLE_NAME = "items";
    public static final String LECTURERS_TABLE_NAME = "lecturers";
    public static final String IMAGES_TABLE_NAME = "images";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CHANNELS_TABLE_NAME + " (" + Channels.ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + Channels.LECTURER_ID
                + " INTEGER," + Channels.TITLE + " VARCHAR(255),"
                + Channels.URL + " VARCHAR(255)," + Channels.DESCRIPTION
                + " VARCHAR(255)," + Channels.UPDATE_TIME + " BIGINT,"
                + Channels.IMAGE_URL + " VARCHAR(255)," + Channels.MUTE
                + " INTEGER," + Channels.STARRED + " INTEGER );");

        db.execSQL("CREATE TABLE " + ITEMS_TABLE_NAME + " (" + Items.ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + Items.TITLE
                + " VARCHAR(255)," + Items.DESCRIPTION + " LONGTEXT,"
                + Items.PUB_DATE + " INTEGER," + Items.UPDATE_TIME + " BIGINT,"
                + Items.LINK + " VARCHAR(255)," + Items.READ + " INTEGER,"
                + Items.CHANNEL_ID + " INTEGER );");

        db.execSQL("CREATE TABLE " + LECTURERS_TABLE_NAME + " (" + Lecturers.ID
                + " INTEGER PRIMARY KEY," + Lecturers.KEY + " INTEGER,"
                + Lecturers.DEST + " INTEGER," + Lecturers.NAME
                + " VARCHAR(45)," + Lecturers.DEPARTMENT + " VARCHAR(255),"
                + Lecturers.SECTOR + " VARCHAR(255)," + Lecturers.OFFICE
                + " VARCHAR(255)," + Lecturers.TELEPHONE + " VARCHAR(100),"
                + Lecturers.EMAIL + " VARCHAR(100)," + Lecturers.THUMBNAIL
                + " VARCHAR(255) );");

        db.execSQL("CREATE TABLE " + IMAGES_TABLE_NAME + " (" + Images.ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + Images.URL
                + " VARCHAR(255)," + Images.RETRIES + " INTEGER,"
                + Images.UPDATE_TIME + " BIGINT," + Images.STATUS
                + " INTEGER );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            Log.i(TAG, "Database version 1 upgrade to 2 : Add MUTE column");
            final String ALTER_TBL = "ALTER TABLE " + CHANNELS_TABLE_NAME
                    + " ADD COLUMN MUTE INTEGER;";
            db.execSQL(ALTER_TBL);

        }

        if (oldVersion == 2 && newVersion == 3) {

        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
    }
}
