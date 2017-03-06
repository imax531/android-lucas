package com.max.lucas.data;

import com.max.lucas.data.DbContract.*;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by imax5 on 26-Apr-16.
 */
public class TestProvider extends AndroidTestCase{

    public static final String TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                LikeEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                TrackEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                UserEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                LikeEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                UserEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /**
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                DbProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + DbContract.CONTENT_AUTHORITY,
                    providerInfo.authority, DbContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /**
        This test doesn't touch the database.  It verifies that the ContentProvider returns
        the correct type for each type of URI that it can handle.
     */
    public void testGetType() {
        // content://com.max.lucas/users/
        String type = mContext.getContentResolver().getType(UserEntry.CONTENT_URI);
        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                UserEntry.CONTENT_TYPE, type);

        String testUserId = "41525761";
        // content://com.max.lucas/users/41525761
        type = mContext.getContentResolver().getType(
                UserEntry.buildUserWithUserId(testUserId));
        assertEquals("Error: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE",
                UserEntry.CONTENT_ITEM_TYPE, type);

        String testUsername = "iMax531";
        // content://com.max.lucas/users/iMax531
        type = mContext.getContentResolver().getType(
                UserEntry.buildUserWithUsername(testUsername));
        assertEquals("Error: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE",
                UserEntry.CONTENT_ITEM_TYPE, type);

        // content://com.max.lucas/tracks/
        type = mContext.getContentResolver().getType(TrackEntry.CONTENT_URI);
        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_TYPE, type);

        String testTrackId = "12345";
        // content://com.max.lucas/tracks/12345
        type = mContext.getContentResolver().getType(
                TrackEntry.buildTracksUri(Long.valueOf(testTrackId)));
        assertEquals("Error: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE",
                TrackEntry.CONTENT_ITEM_TYPE, type);

        // content://com.max.lucas/likes/
        type = mContext.getContentResolver().getType(LikeEntry.CONTENT_URI);
        assertEquals("Error: the WeatherEntry CONTENT_URI with location should return WeatherEntry.CONTENT_TYPE",
                LikeEntry.CONTENT_TYPE, type);

        // content://com.max.lucas/likes/12345
        type = mContext.getContentResolver().getType(
                LikeEntry.buildLikeUri(testTrackId));
        assertEquals("Error: the WeatherEntry CONTENT_URI with location and date should return WeatherEntry.CONTENT_ITEM_TYPE",
                LikeEntry.CONTENT_TYPE, type);
    }

    /**
         This test uses the database directly to insert and then uses the ContentProvider to
         read out the data.  Uncomment this test to see if the basic weather query functionality
         given in the ContentProvider is working correctly.
     */
    public void testBasicUsersQuery() {
        // insert our test records into the database
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createUserValues();
        long usersRowId = TestUtilities.insertUsersValues(mContext);

        assertTrue("Unable to Insert UserEntry into the Database", usersRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor usersCursor = mContext.getContentResolver().query(
                UserEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicUsersQuery", usersCursor, testValues);
    }

    /**
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if your location queries are
        performing correctly.
     */
    public void testBasicTracksQueries() {

        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createTracksValues();
        long tracksRowId = TestUtilities.insertTracksValues(mContext);

        assertTrue("Unable to Insert UserEntry into the Database", tracksRowId != -1);

        // Test the basic content provider query
        Cursor locationCursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicTracksQueries, location query", locationCursor, testValues);
    }

    /**
        This test uses the provider to insert and then update the data
     */
    public void testUpdateUsers() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createUserValues();

        Uri locationUri = mContext.getContentResolver().
                insert(UserEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(UserEntry._ID, locationRowId);
        updatedValues.put(UserEntry.COLUMN_USERNAME, "iMax531");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(UserEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                UserEntry.CONTENT_URI, updatedValues, UserEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                UserEntry.CONTENT_URI,
                null,   // projection
                UserEntry._ID + " = " + locationRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateUsers.  Error validating user entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    /**
     * Make sure we can still delete after adding/updating stuff
     *
     * This test relies on insertions with testInsertReadProvider, so insert and
     * query functionality must also be complete before this test can be used.
     */
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createUserValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(UserEntry.CONTENT_URI, true, tco);
        Uri locationUri = mContext.getContentResolver().insert(UserEntry.CONTENT_URI, testValues);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                UserEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating UserEntry insert.",
                cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestUtilities.createTracksValues();
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, tco);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(TrackEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);

        // Did our content observer get called?  If this fails, then insert weather
        // in ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating TrackEntry insert.",
                weatherCursor, weatherValues);

        // Get the users data
        weatherCursor = mContext.getContentResolver().query(
                UserEntry.buildUserUri(TestUtilities.TESTS_USER_ID),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating user Data.",
                weatherCursor, testValues);

        // Get the track data
        weatherCursor = mContext.getContentResolver().query(
                TrackEntry.buildTracksUri(TestUtilities.TESTS_TRACK_ID),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating track date.",
                weatherCursor, weatherValues);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertTracksValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues weatherValues = new ContentValues();
            weatherValues.put(TrackEntry.COLUMN_ID, i);
            weatherValues.put(TrackEntry.COLUMN_URL, "url " + i);
            weatherValues.put(TrackEntry.COLUMN_API_UPLOADER_USERNAME, "uploader " + i);
            weatherValues.put(TrackEntry.COLUMN_API_TITLE, "api title " + i);
            weatherValues.put(TrackEntry.COLUMN_TITLE, "title " +  i);
            weatherValues.put(TrackEntry.COLUMN_ARTIST, "artist " + i);
            weatherValues.put(TrackEntry.COLUMN_ALBUM, "album " + i);
            returnContentValues[i] = weatherValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        ContentValues[] bulkInsertContentValues = createBulkInsertTracksValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TrackEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(TrackEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                TrackEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // value for order by
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating TrackEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}