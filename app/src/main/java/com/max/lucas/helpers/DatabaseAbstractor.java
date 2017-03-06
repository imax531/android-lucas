package com.max.lucas.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.max.lucas.sync.User;
import com.max.lucas.data.DbContract;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by imax5 on 24-Apr-16.
 */
public class DatabaseAbstractor {

    public static void addTrack (Context context, long id, String uploader, String title, String url) {
        ContentValues values = new ContentValues(4);
        values.put(DbContract.TrackEntry.COLUMN_ID, id);
        values.put(DbContract.TrackEntry.COLUMN_API_UPLOADER_USERNAME, uploader);
        values.put(DbContract.TrackEntry.COLUMN_API_TITLE, title);
        values.put(DbContract.TrackEntry.COLUMN_URL, url);

        context.getContentResolver().insert(DbContract.TrackEntry.CONTENT_URI, values);
    }

    public static void addLike (Context context, long trackId, long userId) {
        ContentValues values = new ContentValues(2);
        values.put(DbContract.LikeEntry.COLUMN_TRACK_ID, trackId);
        values.put(DbContract.LikeEntry.COLUMN_USER_ID, userId);

        context.getContentResolver().insert(DbContract.LikeEntry.CONTENT_URI, values);
    }

    public static ArrayList<Long> getDownloadedTracksIds (Context context) {
        Cursor traksCursor = context.getContentResolver().query(
                DbContract.TrackEntry.CONTENT_URI,
                new String[]{DbContract.TrackEntry.COLUMN_ID},
                DbContract.TrackEntry.COLUMN_DOWNLOADED + " = ? ",
                new String[] {"1"},
                null
        );

        try {
            ArrayList<Long> arr = new ArrayList<>();
            int columnIdex = traksCursor.getColumnIndex(DbContract.TrackEntry.COLUMN_ID);
            while (traksCursor.moveToNext())
                arr.add(traksCursor.getLong(columnIdex));
            return arr;
        } finally {
            traksCursor.close();
        }
    }

    public static User[] getUsers (Context context) {
        Cursor usersCursor = context.getContentResolver().query(
                DbContract.UserEntry.CONTENT_URI,
                null,
                null,
                null,
                "ASC"
        );

        try {
            User[] arr = new User[usersCursor.getCount()];
            int userIdColumnIndex = usersCursor.getColumnIndex(DbContract.UserEntry.COLUMN_ID);
            int usernameColumnIndex = usersCursor.getColumnIndex(DbContract.UserEntry.COLUMN_USERNAME);
            int permalinkColumnIndex = usersCursor.getColumnIndex(DbContract.UserEntry.COLUMN_PERMALINK_URL);;
            for (int i = 0; usersCursor.moveToNext(); i++) {
                arr[i] = new User(usersCursor.getLong(userIdColumnIndex), usersCursor.getString(usernameColumnIndex), usersCursor.getString(permalinkColumnIndex));
            }
            return arr;
        } finally {
            usersCursor.close();
        }
    }

    public static long[] getUsersIds (Context context) {
        Cursor usersCursor = context.getContentResolver().query(
                DbContract.UserEntry.CONTENT_URI,
                new String[]{DbContract.UserEntry.COLUMN_ID},
                null,
                null,
                DbContract.UserEntry.COLUMN_ID + " ASC"
        );

        try {
            long[] arr = new long[usersCursor.getCount()];
            int columnIdex = usersCursor.getColumnIndex(DbContract.UserEntry.COLUMN_ID);
            for (int i = 0; usersCursor.moveToNext(); i++)
                arr[i] = usersCursor.getLong(columnIdex);
            return arr;
        } finally {
            usersCursor.close();
        }
    }

}

