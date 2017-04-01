package com.leon.lamti.leonmusicplayer;

import android.net.Uri;

public class SongObject {
    private String title, artist, album, list, data;
    private long id, duration;
    private boolean clicked;
    private Uri songUri;

    public SongObject() {
    }

    public SongObject(String title) {
        this.title = title;
    }

    public SongObject(long id, String title, String album) {
        this.id = id;
        this.title = title;
        this.album = album;
    }

    public SongObject(String data, String title, String album, String artist) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public SongObject(Uri songUri, String data, String title, String album, String artist) {
        this.songUri = songUri;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public SongObject(long id, String title, String album, Uri songUri) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.songUri = songUri;
    }

    public SongObject(String title, String artist, String album, String list, long id, long duration, boolean clicked, String data) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.list = list;
        this.id = id;
        this.duration = duration;
        this.clicked = clicked;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
