package com.max.lucas.frangments;

import android.app.Fragment;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.max.lucas.Auxiliary;
import com.max.lucas.R;
import com.max.lucas.TrackAnalizer;
import com.max.lucas.data.DbContract;
import com.max.lucas.sync.Accessor;
import com.max.lucas.sync.LoadAvatarTask;
import com.max.lucas.sync.RedirectTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by imax5 on 29-Nov-15.
 */
public class DownloadUrl extends Fragment {

    private final String TAG = "lucas.DownloadUrlAct";

    private ClipboardManager clipboard;
    private URL downloadURL = null;
    private JSONObject track = null;
    private String soundcloudUrl = "";
    private Bitmap artwork = null;
    private MediaPlayer mPlayer = null;
    private int state = 0; // 0=null, 1=fetching, 2=waiting to download
    private long trackId;
    private String artworkUrl;

    private LinearLayout llTagsViews;
    private EditText etUrl;
    private EditText etTitle;
    private EditText etArtist;
    private EditText etAlbum;
    private ImageView imgArtwork;
    private ImageButton imgbtnPlay;
    private ImageButton imgUrlClear;
    private SeekBar sbProgress;
    private TextView tvTimer;
    private FloatingActionButton fab;

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_download_url, container, false);

        initializeViewAccessors();
        initializeOnClickListeners();

        resetView();

        if (savedInstanceState != null) {
            trackId = savedInstanceState.getLong(getString(R.string.track_id));
            artworkUrl = savedInstanceState.getString(getString(R.string.artwork_url));
            etUrl.setText(savedInstanceState.getString(getString(R.string.download_url)));
            if (savedInstanceState.getInt(getString(R.string.tags_visible)) == View.VISIBLE) {
                llTagsViews.setVisibility(View.VISIBLE);
                fab.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { btnDownloadClick(); } });
                fab.setImageResource(R.drawable.ic_file_download_white_24dp);
            }
            etTitle.setText(savedInstanceState.getString(getString(R.string.track_title)));
            etArtist.setText(savedInstanceState.getString(getString(R.string.track_artist)));
            etAlbum.setText(savedInstanceState.getString(getString(R.string.track_album)));
            try {
                URL url = new URL(savedInstanceState.getString(getString(R.string.stream_url)));
                setMediaPlayer(url);
            } catch (Exception ex) {
                // idk.... shouldn't really happen...
            }

            byte[] arr = savedInstanceState.getByteArray(getString(R.string.track_artwork));
            if (arr != null) {
                artwork = BitmapFactory.decodeByteArray(arr, 0, arr.length);
                imgArtwork.setImageBitmap(artwork);
            }
        }

        return v;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(getString(R.string.track_id), trackId);
        outState.putString(getString(R.string.artwork_url), artworkUrl);
        outState.putString(getString(R.string.download_url), etUrl.getText().toString());
        outState.putInt(getString(R.string.tags_visible), llTagsViews.getVisibility());
        outState.putString(getString(R.string.track_title), etTitle.getText().toString());
        outState.putString(getString(R.string.track_artist), etArtist.getText().toString());
        outState.putString(getString(R.string.track_album), etAlbum.getText().toString());
        outState.putByteArray(getString(R.string.track_artwork), artworkToByteArray());
        if (downloadURL != null)
            outState.putString(getString(R.string.stream_url), downloadURL.toString());
    }

    private byte[] artworkToByteArray() {
        if (artwork == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        artwork.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private void resetView() {
        artwork = null;
        etUrl.setText("");
        llTagsViews.setVisibility(View.GONE);
        fab.setImageResource(R.drawable.ic_file_upload_white_24dp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLoadClick();
            }
        });
        if (mPlayer != null)
            mPlayer.reset();
        tvTimer.setText("00:00/00:00");
        imgArtwork.setImageResource(R.drawable.border);
        sbProgress.setProgress(0);
        state = 0;
    }

    public boolean onBackPressed() {
        if (state != 0) {
            resetView();
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getArguments() != null) {
            soundcloudUrl = getArguments().getString(getString(R.string.download_url));
            etUrl.setText(soundcloudUrl);
            btnLoadClick();
        }
    }

    @Override
    public void onPause() {
        super.onResume();

        if (mPlayer != null)
            mPlayer.stop();
    }

    private void initializeViewAccessors() {
        llTagsViews = (LinearLayout) v.findViewById((R.id.llTagsViews));
        llTagsViews.setVisibility(View.GONE);

        etUrl = (EditText) v.findViewById(R.id.etUrl);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        etTitle = (EditText) v.findViewById(R.id.etTitle);
        etArtist = (EditText) v.findViewById(R.id.etArtist);
        etAlbum = (EditText) v.findViewById(R.id.etAlbum);
        imgArtwork = (ImageView) v.findViewById(R.id.imgArtwork);
        imgbtnPlay = (ImageButton) v.findViewById(R.id.btnPlay);
        imgUrlClear = (ImageButton) v.findViewById(R.id.imgbtnClearUrl);
        sbProgress = (SeekBar) v.findViewById(R.id.skProgress);
        tvTimer = (TextView) v.findViewById(R.id.tvTimer);
        fab = (FloatingActionButton) v.findViewById(R.id.fab);

        if (soundcloudUrl != null && !soundcloudUrl.equals("")) {
            etUrl.setText(soundcloudUrl);
            btnLoadClick();
        }
    }

    private void initializeOnClickListeners() {
        imgUrlClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etUrl.getText().length() == 0) paste();
                else clear();
            }
        });

        etUrl.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) imgUrlClear.setImageResource(R.drawable.ic_content_paste_black_24dp);
                else imgUrlClear.setImageResource(R.drawable.ic_close_black_24dp);
            }
        });

        imgArtwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (artworkPref = !artworkPref) imgArtwork.setImageBitmap(artwork);
                else imgArtwork.setImageResource(R.drawable.border);
            }
        });
    }

    private void btnLoadClick() {
        if (mPlayer != null)
            mPlayer.stop();
        // Calls the method that verifies the track exists and redirects to the download details screen
        if (Auxiliary.checkConnectivity(getActivity())) {
            soundcloudUrl = etUrl.getText().toString();
            Uri downloadUrl = Accessor.resolveUrl(soundcloudUrl);
            DownloadURLCheck downloadURLCheck = new DownloadURLCheck(getActivity(), downloadUrl.toString());
            downloadURLCheck.execute();
            state = 1;
        } else {
            Toast.makeText(v.getContext(), "No internet. How do you expect this to work?!", Toast.LENGTH_SHORT).show();
        }
    }

    public void paste() {
        ((android.widget.EditText) v.findViewById(R.id.etUrl)).setText(clipboard.getText());
    }

    public void clear() {
        ((android.widget.EditText) v.findViewById(R.id.etUrl)).setText("");
    }

    private class DownloadURLCheck extends AsyncTask<Void, Void, String> {
        private Context mContext;
        private String mUrl;

        public DownloadURLCheck(Context context, String url) {
            mContext = context;
            mUrl = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            return Accessor.getJSON(mUrl);
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            if (strings == null) {
                Toast.makeText(mContext, "Can't find track", Toast.LENGTH_SHORT).show();
                llTagsViews.setVisibility(View.GONE);
                state = 0;
            } else {
                try {
                    track = new JSONObject(strings);
                    if (!track.has("stream_url"))
                        Toast.makeText(getActivity(), "Can't download track :( sry...", Toast.LENGTH_SHORT).show();
                    else {
                        trackId = track.getLong("id");
                        artworkUrl = track.getString("artwork_url");
                        artworkUrl = artworkUrl.replace("-large.jpg?", "-t500x500.jpg?");
                        artworkPref = true;
                        TrackAnalizer trackAnalizer = new TrackAnalizer(track.getString("title"), track.getJSONObject("user").getString("username"));
                        fab.setImageResource(R.drawable.ic_file_download_white_24dp);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                btnDownloadClick();
                            }
                        });
                        etTitle.setText(trackAnalizer.title);
                        etArtist.setText(trackAnalizer.artist);
                        etAlbum.setText(trackAnalizer.album);
                        new MyLoadAvatarTask(artworkUrl, imgArtwork).execute();
                        llTagsViews.setVisibility(View.VISIBLE);

                        String streamUrl;
                        if (track.getBoolean("downloadable")) {
                            streamUrl = track.getString("download_url");
                        } else {
                            streamUrl = track.getString("stream_url");
                        }

                        MyRedirectTask testAsyncTask = new MyRedirectTask(getActivity(), streamUrl + "?" + Accessor.cid);
                        testAsyncTask.execute();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Intent data error", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.toString());
                }
                state = 2;
            }
        }
    }

    private class MyLoadAvatarTask extends LoadAvatarTask {

        MyLoadAvatarTask(String url, ImageView imageView) {
            super(url, imageView);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            artwork = result;
        }
    }

    private boolean artworkPref;

    //Download the track to the device
    private void btnDownloadClick() {
        if (mPlayer != null)
            mPlayer.stop();
        if (downloadURL != null) {
            TrackAnalizer trackAnalizer = new TrackAnalizer();
            trackAnalizer.setAlbum(etAlbum.getText().toString());
            trackAnalizer.setArtist(etArtist.getText().toString());
            trackAnalizer.setTitle(etTitle.getText().toString());

            try {
                byte[] image = null;
                if (artwork != null && artworkPref) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    artwork.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    image = stream.toByteArray();
                }
                boolean success = Accessor.downloadUrl(getActivity(), downloadURL.toString(), trackAnalizer, trackId, soundcloudUrl, artworkUrl, image);

                if (success) {
                    insertTrackToDb(trackAnalizer);
                    resetView();
                    Toast.makeText(getActivity(), "Downloading", Toast.LENGTH_SHORT).show();
                    llTagsViews.setVisibility(View.GONE);
                    etUrl.setText("");
                } else Toast.makeText(getActivity(), "Enqueuement error 1", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Enqueuement error 2", Toast.LENGTH_SHORT).show();
            }
        } else
            Log.e(TAG, "track or downloadUrl are null!!");
    }

    private void insertTrackToDb(TrackAnalizer trackAnalizer) throws JSONException {
        ContentValues cvTrack = new ContentValues(7);
        cvTrack.put(DbContract.TrackEntry.COLUMN_ID, track.getLong("id"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_API_UPLOADER_USERNAME, track.getJSONObject("user").getString("username"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_API_TITLE, track.getString("title"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_URL, track.getString("permalink_url"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_ARTWORK_URL, track.getString("artwork_url"));
        cvTrack.put(DbContract.TrackEntry.COLUMN_TITLE, trackAnalizer.title);
        cvTrack.put(DbContract.TrackEntry.COLUMN_ARTIST, trackAnalizer.artist);
        cvTrack.put(DbContract.TrackEntry.COLUMN_ALBUM, trackAnalizer.album);
        getActivity().getContentResolver().insert(DbContract.TrackEntry.CONTENT_URI, cvTrack);
    }

    private class MyRedirectTask extends RedirectTask {

        public MyRedirectTask(Context context, String url) {
            super(context, url);
        }

        @Override
        protected void onPostExecute(URL url) {
            setMediaPlayer(url);
        }
    }

    private void setMediaPlayer(URL url) {
        downloadURL = url;
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(downloadURL.toString());
            mPlayer.prepare();
            imgbtnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.pause();
                        imgbtnPlay.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                    } else {
                        mPlayer.start();
                        imgbtnPlay.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                    }
                }
            });
            mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    tvTimer.setText(msToString(mp.getCurrentPosition()) + "/" +
                            msToString(mp.getDuration()));
                    sbProgress.setProgress((int) (((double) mp.getCurrentPosition() / (double) mp.getDuration()) * 100));
                }
            });
            sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        tvTimer.setText(msToString(mPlayer.getDuration() * progress / 100) + "/" +
                                msToString(mPlayer.getDuration()));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mPlayer.pause();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mPlayer.seekTo(mPlayer.getDuration() * seekBar.getProgress() / 100);
                    mPlayer.start();
                }
            });
        } catch (Exception e) {
            // something fucked up
            // I don't care...
        }
    }

    private String msToString(int millis) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}