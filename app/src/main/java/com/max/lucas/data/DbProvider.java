package com.max.lucas.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by imax5 on 23-Apr-16.
 */
public class DbProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DbHelper mOpenHelper;

    static final int USERS = 100;
    static final int USERS_WITH_USER_ID = 101;
    static final int USERS_WITH_USERNAME = 102;
    static final int LIKES = 300;
    static final int LIKES_WITH_TRACK_ID = 301;
    static final int TRACKS = 500;
    static final int TRACKS_WITH_TRACK_ID = 501;
    static final int TRACKS_JOIN_USERS = 700;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    // Cool... That is a class static constructor. Very cool......
    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        // This is an inner join which looks like
        // tracks LEFT JOIN likes ON tracks.id = likes.track_id
        sWeatherByLocationSettingQueryBuilder.setTables(
                DbContract.TrackEntry.TABLE_NAME + " LEFT JOIN " +
                        DbContract.LikeEntry.TABLE_NAME +
                        " ON " + DbContract.TrackEntry.TABLE_NAME +
                        "." + DbContract.TrackEntry.COLUMN_ID +
                        " = " + DbContract.LikeEntry.TABLE_NAME +
                        "." + DbContract.LikeEntry.COLUMN_TRACK_ID);
    }

    private static final String sUserIdSelection =
            DbContract.UserEntry.TABLE_NAME +
                    "." + DbContract.UserEntry.COLUMN_ID + " = ? ";
    private static final String sUsernameSelection =
            DbContract.UserEntry.TABLE_NAME +
                    "." + DbContract.UserEntry.COLUMN_USERNAME + " = ? ";
    private static final String sLikesByTrackSelection =
            DbContract.LikeEntry.TABLE_NAME +
                    "." + DbContract.LikeEntry.COLUMN_TRACK_ID + " = ? ";
    private static final String sTrackSelectionById =
            DbContract.TrackEntry.TABLE_NAME +
                    "." + DbContract.TrackEntry.COLUMN_ID + " = ? ";

    private Cursor getUserByUserId (Uri uri, String[] projection, String sortOrder) {
        String userId = DbContract.UserEntry.getIdSettingFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(
                DbContract.UserEntry.TABLE_NAME,
                projection,
                sUserIdSelection,
                new String[] {userId},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getUserByUsername (Uri uri, String[] projection, String sortOrder) {
        String username = DbContract.UserEntry.getUsernameSettingFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(
                DbContract.UserEntry.TABLE_NAME,
                projection,
                sUsernameSelection,
                new String[] {username},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getLikesByTrackId (Uri uri, String[] projection, String sortOrder) {
        String trackId = DbContract.LikeEntry.getTrackIdSettingFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(
                DbContract.LikeEntry.TABLE_NAME,
                projection,
                sLikesByTrackSelection,
                new String[] {trackId},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrackByTrackId (Uri uri, String[] projection, String sortOrder) {
        String trackId = DbContract.TrackEntry.getTrackIdSettingFromUri(uri);

        return mOpenHelper.getReadableDatabase().query(
                DbContract.TrackEntry.TABLE_NAME,
                projection,
                sTrackSelectionById,
                new String[] {trackId},
                null, null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DbContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DbContract.PATH_USERS, USERS);
        matcher.addURI(authority, DbContract.PATH_USERS + "/#", USERS_WITH_USER_ID);
        matcher.addURI(authority, DbContract.PATH_USERS + "/*", USERS_WITH_USERNAME);

        matcher.addURI(authority, DbContract.PATH_LIKES, LIKES);
        matcher.addURI(authority, DbContract.PATH_LIKES + "/#", LIKES_WITH_TRACK_ID);

        matcher.addURI(authority, DbContract.PATH_TRACKS, TRACKS);
        matcher.addURI(authority, DbContract.PATH_TRACKS + "/#", TRACKS_WITH_TRACK_ID);

        matcher.addURI(authority, DbContract.PATH_TRACKS + "/" + DbContract.PATH_USERS, TRACKS_JOIN_USERS);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case USERS:
                return DbContract.UserEntry.CONTENT_TYPE;
            case USERS_WITH_USER_ID:
            case USERS_WITH_USERNAME:
                return DbContract.UserEntry.CONTENT_ITEM_TYPE;

            case LIKES:
            case LIKES_WITH_TRACK_ID:
                return DbContract.LikeEntry.CONTENT_TYPE;

            case TRACKS:
                return DbContract.TrackEntry.CONTENT_TYPE;
            case TRACKS_WITH_TRACK_ID:
                return DbContract.TrackEntry.CONTENT_ITEM_TYPE;

            case TRACKS_JOIN_USERS:
                return DbContract.TrackEntry.CONTENT_TYPE;

        default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case USERS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DbContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case USERS_WITH_USER_ID:
                retCursor = getUserByUserId(uri, projection, sortOrder);
                break;
            case USERS_WITH_USERNAME:
                retCursor = getUserByUsername(uri, projection, sortOrder);
                break;

            case LIKES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DbContract.LikeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case LIKES_WITH_TRACK_ID:
                retCursor = getLikesByTrackId(uri, projection, sortOrder);
                break;

            case TRACKS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DbContract.TrackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRACKS_WITH_TRACK_ID:
                retCursor = getTrackByTrackId(uri, projection, sortOrder);
                break;

            case TRACKS_JOIN_USERS:
                retCursor = sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        new String[] {DbContract.TrackEntry.TABLE_NAME + ".*",
                                "GROUP_CONCAT(" + DbContract.UserEntry.COLUMN_ID + ") AS " + DbContract.UserEntry.COLUMN_ID},
                        selection,
                        selectionArgs,
                        DbContract.TrackEntry.TABLE_NAME + "." + DbContract.TrackEntry.COLUMN_ID,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        long _id;

        switch (match) {
            case USERS:
                _id = db.insert(DbContract.UserEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DbContract.UserEntry.buildUserUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case TRACKS:
                _id = db.insert(DbContract.TrackEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DbContract.TrackEntry.buildTracksUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case LIKES:
                _id = db.insert(DbContract.LikeEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DbContract.LikeEntry.buildLikeUri(String.valueOf(_id));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;

        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";

        switch (match) {
            case USERS:
                rowsDeleted = db.delete(DbContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRACKS:
                rowsDeleted = db.delete(DbContract.TrackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LIKES:
                rowsDeleted = db.delete(DbContract.LikeEntry.TABLE_NAME, selection, selectionArgs);
                break;
        }
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case USERS:
                rowsUpdated = db.update(DbContract.UserEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRACKS:
                rowsUpdated = db.update(DbContract.TrackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LIKES:
                rowsUpdated = db.update(DbContract.LikeEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
        }
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case TRACKS:
                db.beginTransaction();
                try{
                    for (int i = 0; i < values.length; i++) {
                        long _id = db.insert(DbContract.TrackEntry.TABLE_NAME, null, values[i]);
                        if (_id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case LIKES:
                db.beginTransaction();
                try{
                    for (int i = 0; i < values.length; i++) {
                        long _id = db.insert(DbContract.LikeEntry.TABLE_NAME, null, values[i]);
                        if (_id != -1)
                            returnCount++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                throw new android.database.SQLException("Failed to insert rows into " + uri);
        }
    }
}
