package com.max.lucas.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.max.lucas.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by imax5 on 04-May-16.
 */
public class TestUtilities extends AndroidTestCase {

    static final long TESTS_USER_ID = 4;
    static final long TESTS_TRACK_ID = 23;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static long insertUsersValues(Context context) {
        // insert our test records into the database
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createUserValues();

        long locationRowId;
        locationRowId = db.insert(DbContract.UserEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to users values", locationRowId != -1);

        return locationRowId;
    }

    static ContentValues createUserValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(DbContract.UserEntry.COLUMN_USERNAME, "User 1");
        testValues.put(DbContract.UserEntry.COLUMN_ID, TESTS_USER_ID);

        return testValues;
    }

    static long insertTracksValues(Context context) {
        // insert our test records into the database
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createTracksValues();

        long locationRowId;
        locationRowId = db.insert(DbContract.TrackEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to tracks Values", locationRowId != -1);

        return locationRowId;
    }

    static ContentValues createTracksValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(DbContract.TrackEntry.COLUMN_ID, TESTS_TRACK_ID);
        testValues.put(DbContract.TrackEntry.COLUMN_URL, "url 1");
        testValues.put(DbContract.TrackEntry.COLUMN_API_TITLE, "api title 1");
        testValues.put(DbContract.TrackEntry.COLUMN_API_UPLOADER_USERNAME, "api username 1");
        testValues.put(DbContract.TrackEntry.COLUMN_TITLE, "title  1");
        testValues.put(DbContract.TrackEntry.COLUMN_ARTIST, "artist 1");
        testValues.put(DbContract.TrackEntry.COLUMN_ALBUM, "album 1");

        return testValues;
    }

    /*
    Students: The functions we provide inside of TestProvider use this utility class to test
    the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
    CTS tests.
    Note that this only tests that the onChange function is called; it does not test that the
    correct Uri is returned.
 */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
