package com.max.lucas.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.max.lucas.data.DbContract.*;
/**
 * Created by imax5 on 21-Apr-16.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;

    private static final String DATABASE_NAME = "lucas.db";

    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static class TableCreationStrings {
        static final String SQL_CREATE_USERS_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY, " +
                UserEntry.COLUMN_ID + " INTEGER UNIQUE NOT NULL, " +
                UserEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                UserEntry.COLUMN_PERMALINK_URL + " TEXT NOT NULL, " +
                UserEntry.COLUMN_DOWNLOAD_PLAYLISTS + " NUMERIC NOT NULL DEFAULT 1" +
                " );";

        static final String SQL_CREATE_TRACKS_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                TrackEntry._ID + " INTEGER PRIMARY KEY, " +
                TrackEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                TrackEntry.COLUMN_API_UPLOADER_USERNAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_API_TITLE + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_URL + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_STREAM_URL + " TEXT, " +
                TrackEntry.COLUMN_ARTWORK_URL + " TEXT, " +
                TrackEntry.COLUMN_DOWNLOADED + " NUMERIC NOT NULL DEFAULT 0," +
                TrackEntry.COLUMN_TITLE + " TEXT, " +
                TrackEntry.COLUMN_ARTIST + " TEXT, " +
                TrackEntry.COLUMN_ALBUM + " TEXT," +

                "UNIQUE (" + TrackEntry.COLUMN_ID + ") ON CONFLICT IGNORE" +
                " );";

        static final String SQL_CREATE_LIKES_TABLE = "CREATE TABLE " + LikeEntry.TABLE_NAME + " (" +
                LikeEntry._ID + " INTEGER PRIMARY KEY, " +
                LikeEntry.COLUMN_TRACK_ID + " INTEGER NOT NULL, " +
                LikeEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + LikeEntry.COLUMN_TRACK_ID + ") REFERENCES " +
                TrackEntry.TABLE_NAME + " (" + TrackEntry.COLUMN_ID + "), " +
                " FOREIGN KEY (" + LikeEntry.COLUMN_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_ID + "), " +

                "UNIQUE (" + LikeEntry.COLUMN_TRACK_ID + ", " + LikeEntry.COLUMN_USER_ID + ") ON CONFLICT IGNORE" +
                " );";
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TableCreationStrings.SQL_CREATE_USERS_TABLE);
        sqLiteDatabase.execSQL(TableCreationStrings.SQL_CREATE_TRACKS_TABLE);
        sqLiteDatabase.execSQL(TableCreationStrings.SQL_CREATE_LIKES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 5:
                break;
        }
    }




    public void addTrack(long id, String uploaderUsername, String apiTitle, boolean downloaded, String title, String artist, String album) {
        ContentValues values = new ContentValues(7);
        values.put(TrackEntry.COLUMN_ID, id);
        values.put(TrackEntry.COLUMN_API_UPLOADER_USERNAME, uploaderUsername);
        values.put(TrackEntry.COLUMN_API_TITLE, apiTitle);
        values.put(TrackEntry.COLUMN_DOWNLOADED, downloaded);
        values.put(TrackEntry.COLUMN_TITLE, title);
        values.put(TrackEntry.COLUMN_ARTIST, artist);
        values.put(TrackEntry.COLUMN_ALBUM, album);

        getWritableDatabase().insert(
                TrackEntry.TABLE_NAME,
                null,
                values
        );
    }

    public void addUser(long id, String username) {
        ContentValues values = new ContentValues(2);
        values.put(UserEntry.COLUMN_ID, id);
        values.put(UserEntry.COLUMN_USERNAME, username);

        getWritableDatabase().insert(
                UserEntry.TABLE_NAME,
                null,
                values
        );
    }

    // Unique policy is replace so no need for alter
    public void addLike(long trackId, long userId) {
        ContentValues values = new ContentValues(2);
        values.put(LikeEntry.COLUMN_TRACK_ID, trackId);
        values.put(LikeEntry.COLUMN_USER_ID, userId);

        getWritableDatabase().insert(
                LikeEntry.TABLE_NAME,
                null,
                values
        );
    }

    public Cursor getTracksJoinUserIds() {
        final String QUERY =
                        "SELECT * " +
                        "FROM " + TrackEntry.TABLE_NAME + " INNER JOIN " + LikeEntry.TABLE_NAME + " INNER JOIN " + UserEntry.TABLE_NAME;

        final String[] VALUES = { };

        return getReadableDatabase().rawQuery(QUERY, VALUES);
    }

    public Cursor getUsers() {
        final String QUERY =
                        "SELECT * " +
                        "FROM " + UserEntry.TABLE_NAME;

        final String[] VALUES = { };

        return getReadableDatabase().rawQuery(QUERY, VALUES);
    }

    public void alterUserSettings(long userId, boolean downloadPlaylists) {
        ContentValues values = new ContentValues(1);
        values.put(UserEntry.COLUMN_DOWNLOAD_PLAYLISTS, downloadPlaylists ? 1 : 0);

        final String WHERE_CLAUSE = UserEntry.COLUMN_ID + " = ?";
        final String[] WHERE_VALUES = { String.valueOf(userId) };

        getWritableDatabase().update(
                UserEntry.TABLE_NAME,
                values,
                WHERE_CLAUSE,
                WHERE_VALUES
        );
    }

    public void deleteUser(long id) {
        final String WHERE_CLAUSE = UserEntry.COLUMN_ID + " = ?";
        final String[] WHERE_VALUES = { String.valueOf(id) };

        getWritableDatabase().delete(
                UserEntry.TABLE_NAME,
                WHERE_CLAUSE,
                WHERE_VALUES
        );
    }
}
