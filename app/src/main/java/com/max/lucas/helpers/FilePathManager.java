package com.max.lucas.helpers;

import android.content.Context;

import java.io.File;

/**
 * Created by imax5 on 23-Apr-16.
 */
public class FilePathManager {

    public static final String ARTWORK_FOLDER = "artwork";
    public static final String AVATAR_FOLDER = "avatar";

    public static final String FILE_TYPE_SURRFIX = ".jpg";

    public static String getPath(Context context, String folder) {
        return context.getFilesDir().getPath() +
                File.separator +
                folder;
    }

    public static File getArtworkFile(Context context, long id) {
        File f = new File(
                getPath(context, ARTWORK_FOLDER) + File.separator +
                id + FILE_TYPE_SURRFIX
        );

        return f;
    }

    public static File getAvatarFile(Context context, long id) {
        File f = new File(
                getPath(context, AVATAR_FOLDER) + File.separator +
                        id + FILE_TYPE_SURRFIX
        );

        return f;
    }

}
