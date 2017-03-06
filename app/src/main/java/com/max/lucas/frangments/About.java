package com.max.lucas.frangments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.max.lucas.R;

/**
 * Created by imax5 on 29-Nov-15.
 */
public class About extends Fragment {
    View v;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_about, container, false);

        final String about = "Lucas is an app designed to download all the liked tracks of a set of SoundCloud users. That's it. <br><br>" +
                "Open source projects that are used in this app:<br>" +
                "- <a href='https://github.com/mpatric/mp3agic'>mp3agic by mpatric</a><br>" +
                "- <a href='https://github.com/google/material-design-icons/'>Material Design Icons by Google</a><br>" +
                "- <a href='http://daniellitech.com/WebPages/DisplayEng.aspx'>Guy Danieli's brilliant beautiful blond brain</a><br>" +
                "- <a href='https://github.com/Pkmmte/CircularImageView'>Pkmmte's CircularImageView</a><br><br>" +
                "All bugs are welcome! Just tell you-know-who. Together, we can make the code great again!";
        TextView tv = ((TextView) v.findViewById(R.id.tvAbout));
        tv.setText(Html.fromHtml(about));
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

    @Override
    public String toString() { return "About"; }
}
