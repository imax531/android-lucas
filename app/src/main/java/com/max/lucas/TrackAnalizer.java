package com.max.lucas;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;

/**
 * Created by imax5 on 02-Nov-15.
 * Hopefully becomes an elegant mp3 tags constructor
 */
public class TrackAnalizer {

    private static final String[] bracketExceptions = {"ft", "feat", "featuring", "cover", "mix", "bootleg", "remake"};

    public String title = "";
    public String artist = "";
    public String album = "";

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    private String splitChar = null;

    public TrackAnalizer() { }

    public TrackAnalizer(String title, String artist, String album) {
        this.album = album;
        this.artist = artist;
        this.title = title;
    }

    public TrackAnalizer(String downloadDescription) {
        int index = 0;
        for (int i = 0; i < downloadDescription.length(); i++) {
            if (downloadDescription.charAt(i) == ',') {
                index++;
                continue;
            }
            if (downloadDescription.charAt(i) == '\\') i++;
            switch (index) {
                case 0:
                    this.title += downloadDescription.charAt(i);
                    break;
                case 1:
                    this.artist += downloadDescription.charAt(i);
                    break;
                case 2:
                    this.album += downloadDescription.charAt(i);
                    break;
            }
        }
    }

    public TrackAnalizer(String title, String username) {

        title = title.replace("\\", "\\\\");
        username = username.replace("\\", "\\\\");

        int counted = countDashes(title);
        String[] split = title.split(splitChar);

        switch (counted) {
            case 1:
                this.album = username;
                this.artist = split[0];
                this.title = split[1];
                break;
            case 2:
                this. album = split[0];
                this.artist = split[1];
                this.title = split[2];
                break;
            default:
                this.artist = username;
                this.title = title;
                break;
        }

        if (this.title.contains(" vs ")) {
            String temp = this.title;
            this.title = this.artist;
            this.artist = temp;
        }

        removeOfficialFromArtistName();

        if (this.title.contains(this.artist) && !this.title.toLowerCase().contains("(" + this.artist.toLowerCase() + " remix)"))
            this.title = this.title.replaceAll(this.artist, "");

        this.title = stringCleaner(this.title);
        this.artist = stringCleaner(this.artist);
        this.album = stringCleaner(this.album);

        if (this.artist.toLowerCase().contains(this.album.toLowerCase()))
            this.album = "";
        this.artist = this.artist.replace("and", "&");
    }

    private int countDashes(String str) {
        int count = countString(" - ", str);
        splitChar = " - ";
        if (count == 0) {
            count = countString(" | ", str);
            splitChar = " \\| ";
        }
        return count;
    }

    private int countString (String search, String str) {
        int count = 0;
        boolean counting = true;

        for (int i = 0; i < str.length(); i++) {
            if (openbraces.indexOf(str.charAt(i)) >= 0)
                counting = false;
            else if (closebraces.indexOf(str.charAt(i)) >= 0)
                counting = true;
            else if (counting && str.substring(i).startsWith(search))
                count++;
        }

        return count;
    }

    private static final String cleanerString = " \'";

    private String stringCleaner(String string) {
        if (string.contains(" || "))
            string = string.substring(string.indexOf(" || ") + 4);
        string = cleanStringBrackets(string);
        while (string.length() > 0 && cleanerString.indexOf(string.charAt(0)) != -1)
            string = string.substring(1);
        while (string.length() > 0 && cleanerString.indexOf(string.charAt(string.length() - 1)) != -1)
            string = string.substring(0, string.length() - 1);
        string = string.replace("\"", "");
        return string;
    }

    public ID3v2 getTags(byte[] image) {
        ID3v2 id3v2Tags = new ID3v24Tag();
        id3v2Tags.setAlbum(this.album);
        id3v2Tags.setArtist(this.artist);
        id3v2Tags.setTitle(this.title);
        if (image != null)
            id3v2Tags.setAlbumImage(image, "image/jpeg");
        return id3v2Tags;
    }

    @Override
    public String toString() {
        String str = "";
        if (!this.album.equals(""))
            str = this.album + " - ";
        str += this.artist + " - " + this.title;
        return str;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private final String openbraces =   "({[*";
    private final String closebraces =  ")}]*";

    private String cleanStringBrackets(String s) {
        StringBuilder newString = new StringBuilder();
        boolean write = true;
        for (int i = 0; i < s.length(); i++) {

            //Opening bracket
            if (openbraces.indexOf(s.charAt(i)) != -1)
                write = shouldKeepBrackets(s, i);

            //Add character
            if (write)
                newString.append(s.charAt(i));

            //Closing bracket
            if (closebraces.indexOf(s.charAt(i)) != -1)
                write = true;
        }
        return newString.toString();
    }

    private boolean shouldKeepBrackets(String str, int index) {
        char endingChar = closebraces.charAt(openbraces.indexOf(str.charAt(index)));
        str = str.substring(index);
        if (str.indexOf(endingChar) == -1)
            return true;
        str = str.substring(0, str.indexOf(endingChar));
        for (String s : bracketExceptions)
            if (str.toLowerCase().contains(s))
                return true;
        return false;
    }

    private void removeOfficialFromArtistName() {
        if (this.artist.toLowerCase().contains("official")) {
            String newArtist = "";
            for (int i = 0; i < this.artist.length(); i++) {
                newArtist += this.artist.charAt(i);
                if (i < this.artist.length() - 1 && this.artist.substring(i + 1).toLowerCase().startsWith("official")) {
                    i += 8;
                    if (i < this.artist.length() && this.artist.charAt(i) == ' ')
                        i++;
                }
            }
            this.artist = newArtist;
        }
    }
}