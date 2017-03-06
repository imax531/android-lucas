package com.max.lucas.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadedFilesReceiver extends BroadcastReceiver {

    private static final String TAG = "lucas.DownloadCompleted";

    @Override
    public synchronized void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context.getApplicationContext(), DownloadedFilesService.class);
        i.putExtra(DownloadedFilesService.ACTION_KEY, DownloadedFilesService.DOWNLOAD_COMPLETE_ACTION);
        i.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L));
        context.startService(i);
    }
}