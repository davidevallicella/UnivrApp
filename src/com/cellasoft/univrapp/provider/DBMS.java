package com.cellasoft.univrapp.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cellasoft.univrapp.model.Channel.Channels;
import com.cellasoft.univrapp.model.Image.Images;
import com.cellasoft.univrapp.model.Lecturer.Lecturers;
import com.cellasoft.univrapp.model.RSSItem.Items;
import com.cellasoft.univrapp.model.University.Universitys;

public class DBMS extends SQLiteOpenHelper {
	private static DBMS instance;
	public static final String DATABASE_NAME = "simplestring.db";
	public static final int DATABASE_VERSION = 1;
	public static final String CHANNELS_TABLE_NAME = "channels";
	public static final String ITEMS_TABLE_NAME = "items";
	public static final String LECTURERS_TABLE_NAME = "lecturers";
	public static final String UNIVERSITY_TABLE_NAME = "university";
	public static final String IMAGES_TABLE_NAME = "images";

	private DBMS(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static synchronized SQLiteDatabase getInstance(Context context) {
		if (instance == null) {
			instance = new DBMS(context);
		}

		return instance.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + CHANNELS_TABLE_NAME + " (" 
				+ Channels.ID  			+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ Channels.TITLE 		+ " VARCHAR(255)," 
				+ Channels.URL   		+ " VARCHAR(1000),"
				+ Channels.DESCRIPTION 	+ " VARCHAR(1000),"
				+ Channels.IMAGE_URL	+ " VARCHAR(1000),"
				+ Channels.STARRED		+ " INTEGER );");
		
		db.execSQL("CREATE TABLE " + ITEMS_TABLE_NAME + " (" 
				+ Items.ID 				+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ Items.TITLE 			+ " VARCHAR(255)," 
				+ Items.DESCRIPTION 	+ " LONGTEXT,"
				+ Items.PUB_DATE 		+ " VARCHAR(25)," 
				+ Items.LINK 			+ " VARCHAR(1000)," 
				+ Items.CHANNEL_ID 		+ " INTEGER );");
		
		db.execSQL("CREATE TABLE " + LECTURERS_TABLE_NAME + " (" 
				+ Lecturers.ID 			+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ Lecturers.KEY			+ " INTEGER,"
				+ Lecturers.DEST 		+ " INTEGER," 
				+ Lecturers.THUMBNAIL	+ " VARCHAR(255),"
				+ Lecturers.NAME 		+ " VARCHAR(30) );");
		
		db.execSQL("CREATE TABLE " + UNIVERSITY_TABLE_NAME + " (" 
				+ Universitys.ID 		+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ Universitys.NAME 		+ " VARCHAR(30)," 
				+ Universitys.DEST		+ " INTEGER,"
				+ Universitys.URL 		+ " VARCHAR(255) );");
		
		db.execSQL("CREATE TABLE " + IMAGES_TABLE_NAME + " (" 
				+ Images.ID				+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + Images.URL 			+ " VARCHAR(1000),"
                + Images.RETRIES 		+ " INTEGER,"
                + Images.UPDATE_TIME 	+ " INTEGER,"
                + Images.STATUS 		+ " INTEGER );");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {

		}

		if (oldVersion == 2 && newVersion == 3) {

		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
	}
}
