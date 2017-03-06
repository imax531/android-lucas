package com.max.lucas.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by imax5 on 19-Apr-16.
 */
public class DbContract {

    public static final String CONTENT_AUTHORITY = "com.max.lucas";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TRACKS = "tracks";
    public static final String PATH_USERS = "users";
    public static final String PATH_LIKES = "likes";
    public static final String PATH_PLAYLISTS = "playlists";

    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;

        // Table name
        public static final String TABLE_NAME = "users";

        public static final String COLUMN_ID = "user_id";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PERMALINK_URL = "permalink_url";
        public static final String COLUMN_DOWNLOAD_PLAYLISTS = "download_playlist";

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUserWithUserId(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static Uri buildUserWithUsername(String username) {
            return CONTENT_URI.buildUpon().appendPath(username).build();
        }

        public static String getIdSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getUsernameSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static final class TrackEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACKS;

        // Table name
        public static final String TABLE_NAME = "tracks";

        // The api data
        public static final String COLUMN_ID = "track_id";
        public static final String COLUMN_API_UPLOADER_USERNAME = "uploader_username";
        public static final String COLUMN_API_TITLE = "api_title";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_STREAM_URL = "stream_url";
        public static final String COLUMN_ARTWORK_URL = "artwork_url";

        // A flag to distinguish whether the track was downloaded
        public static final String COLUMN_DOWNLOADED = "downloaded";

        // The TrackAnalizer data
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_ALBUM = "album";


        public static Uri buildTracksUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTracksWithUsers() {
            return CONTENT_URI.withAppendedPath(CONTENT_URI, PATH_USERS);
        }

        public static String getTrackIdSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static final class LikeEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIKES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIKES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIKES;

        public static final String TABLE_NAME = "likes";

        public static final String COLUMN_TRACK_ID = "track_id";
        public static final String COLUMN_USER_ID = "user_id";


        public static Uri buildLikeUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static String getTrackIdSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

}
