package com.max.lucas.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.max.lucas.ActiveDownloads;
import com.max.lucas.sync.User;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by imax5 on 29-Nov-15.
 */
public class Handler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "lucas.db";

    public static final String TABLE_USERS = "users";
    public static final String TABLE_USERS_COLUMN_ID = "id";
    public static final String TABLE_USERS_COLUMN_USERNAME = "username";
    public static final String TABLE_USERS_COLUMN_IMAGEURL = "imageurl";

    public static final String TABLE_HISTORY = "downloaded_tracks";
    public static final String TABLE_HISTORY_COLUMN_ID = "id";
    public static final String TABLE_HISTORY_COLUMN_NAME = "name";
    public static final String TABLE_HISTORY_COLUMN_DATE = "date";
    public static final String TABLE_HISTORY_COLUMN_URL = "url";

    public Handler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + "(" +
                        TABLE_USERS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        TABLE_USERS_COLUMN_USERNAME + " NVARCHAR(50) NOT NULL," +
                        TABLE_USERS_COLUMN_IMAGEURL + " TEXT NOT NULL" +
                        ");"
        );
        db.execSQL(
                "CREATE TABLE " + TABLE_HISTORY + "(" +
                        TABLE_HISTORY_COLUMN_ID + " INTEGER, " +
                        TABLE_HISTORY_COLUMN_NAME + " TEXT, " +
                        TABLE_HISTORY_COLUMN_DATE + " TEXT, " +
                        TABLE_HISTORY_COLUMN_URL + " TEXT " +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public void myFix(int numOfTracksTomMakeUndownloaded) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            //String where = "1 LIMIT " + numOfTracksTomMakeUndownloaded;
            String where = DbContract.TrackEntry.COLUMN_API_TITLE + " LIKE 'Monstercat Podcast %'";
            ContentValues cv = new ContentValues(1);
            cv.put(DbContract.TrackEntry.COLUMN_DOWNLOADED, 0);
            Log.i("handler-tag" ,"updated rows: " + db.delete(DbContract.TrackEntry.TABLE_NAME,
                    //cv,
                    where,
                    null));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }

    public void deleteALLHistory() {
        SQLiteDatabase db = getWritableDatabase();
        try { db.delete(TABLE_HISTORY, null, null);
        } finally {
            if (db != null)
                db.close();
        }
    }
}