package com.max.lucas.frangments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.max.lucas.R;

/**
 * Created by imax5 on 29-Nov-15.
 */
public class Help extends Fragment {

    View v;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_help, container, false);

        final String help =
                "<h3>How do I view the tracks that were not downloaded?</h3>" +
                "<p>At the home screen, press the <b>search icon</b> and type <b>pending</b>. </p>" +

                "<h3>The app doesn't display a track I favourited!</h3>" +
                "<p>The Soundcloud API... well... it's not the best. For some reason it does not fetch certain tracks. I've been trying to " +
                        "contact the Soundcloud API team in an attempt to ask them to get that fixed but for now it's the best we have. </p>" +

                "<h3>How to url-download a track</h3>" +
                "<p> There are two ways of doing so: <br><br>" +
                "The easy way: <br>" +
                "1) Go to the soundcloud app and find the track you want to download. <br>" +
                "2) Press the <b>share button</b> and then press the Lucas app. <br>" +
                "3) You will enter a screen where you can edit the track preferences and download it. <br><br>" +
                "The other way: <br>" +
                "1) Copy the url you want to download. <br>" +
                "2) At the home screen, press the text entry box at the top that writes 'download url' and enter said url. <br>" +
                "3) The track properties will appear so you can edit them and download the track. </p>" +

                "<h3>Track not downloading?</h3>" +
                "<p>" +
                "1) Try downloading it again (either via syncing or a <b>url-download</b> and look at your downloads. <br>" +
                "2) If the download is consistantly failing or the track doesn't download at all, you should <b>tap the artwork</b> and you " +
                        "will be redirected to the soundcloud app in order to <b>url-download</b> the track from another upload. (For your " +
                        "conveniece, track data will be copied to your clip tray to make your search easier). <br>" +
                "3) Once the download is succesful, <b>long tap its artwork</b> to mark it as downloaded. </p>";
        ((TextView) v.findViewById(R.id.tvHelp)).setText(Html.fromHtml(help));

        return v;
    }

    @Override
    public String toString() {
        return "Help";
    }
}
