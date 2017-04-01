package com.leon.lamti.leonmusicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Songs extends Fragment {

    private static RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private static SongsAdapter mAdapter;
    private static List<SongObject> mSongsList = new ArrayList<>();
    //private SongObject song = new SongObject();
    private static Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getBaseContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_songs, container, false);

        mRecyclerView = (RecyclerView) layoutView.findViewById(R.id.songsRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //getSongList();
        loadAudio();

        Collections.sort(mSongsList, new Comparator<SongObject>(){
            public int compare(SongObject a, SongObject b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        mAdapter = new SongsAdapter(mSongsList);
        mRecyclerView.setAdapter(mAdapter);



        return layoutView;
    }

    public static void setAdapterItemClickable ( boolean b ) {

        //mAdapter.setAdapterClickable( b );
        mRecyclerView.setClickable(b);
        Toast.makeText(mContext, "b: " + b, Toast.LENGTH_SHORT).show();
    }

    public static class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> implements View.OnClickListener {

        private static ConstraintLayout songLayout;
        private int rowX, rowY;
        private static int p;
        private static boolean isTrackListClosed, clickListenerFlag = true;
        private List<SongObject> songsList;
        private SongObject song, clickedSong;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public ConstraintLayout rowSongLayout;

            public MyViewHolder(View view) {
                super(view);

                title = (TextView) view.findViewById(R.id.rowSongTV);
                rowSongLayout = (ConstraintLayout) view.findViewById(R.id.rowSongLayout);
            }
        }

        public SongsAdapter () {

        }

        public SongsAdapter(List<SongObject> songsList) {
            this.songsList = songsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_song, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onViewAttachedToWindow(MyViewHolder holder) {
            super.onViewAttachedToWindow(holder);

        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder,  int position) {

            songLayout = holder.rowSongLayout;

            song = songsList.get(position);
            holder.title.setText(song.getTitle());

            if ( song.isClicked() ) {
                //Log.d("OLE","out amber-true");
                holder.rowSongLayout.setBackgroundColor(mContext.getResources().getColor(R.color.colorTealLight));
            } else {
                //Log.d("OLE","out teal-false");
                holder.rowSongLayout.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhiteGray));
            }

            holder.rowSongLayout.setClickable(clickListenerFlag);

            if ( clickListenerFlag ) {
            //if ( true ) {
                //holder.rowSongLayout.setOnClickListener(this);
                holder.rowSongLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int[] locations = new int[2];
                        holder.rowSongLayout.getLocationOnScreen(locations);
                        //x = locations[0];
                        rowY = locations[1];

                        MainActivity.setFabVisible(true);
                        isTrackListClosed = MainActivity.getTrackListClosed();
                        if ( !isTrackListClosed ) {
                            MainActivity.changeFabY(rowY);
                        }
                        //holder.rowSongLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.ripple));

                        p = holder.getAdapterPosition();

                        MainActivity.setCurrentItemPosition(p);
                        //ForegroundMediaPlayerService.setCurrentItemPosition(p);

                        if ( clickedSong != null ) {
                            clickedSong.setClicked(false);
                            mAdapter.notifyDataSetChanged();
                        }
                        clickedSong = songsList.get(p);
                        clickedSong.setClicked(true);
                        mAdapter.notifyDataSetChanged();

                        //MainActivity.setSong(songsList.get(p).getSongUri());
                        //Log.d("URIU", "current song Uri: " + songsList.get(p).getSongUri());

                    }
                });
            } else {
                holder.rowSongLayout.setClickable(clickListenerFlag);
            }
        }

        @Override
        public int getItemCount() {
            return songsList.size();
        }

        @Override
        public void onClick(View view) {

            /*for ( int i=0; i<=songsList.size(); i++) {

                if ( i == pos) {
                    rowSongLayout1.setBackgroundColor(getResources().getColor(R.color.colorAmber));
                } else {
                    rowSongLayout1.setBackgroundColor(getResources().getColor(R.color.colorWhiteGray));
                }
            }*/
        }

        public static void setAdapterClickable( boolean b ) {

            clickListenerFlag = b;
            mAdapter.notifyDataSetChanged();
        }

        public static void setSongClickable( boolean b ) {

            songLayout.setClickable(b);
        }

        public static int getItemPosition() {

            return p;
        }
    }

    private void getSongList() {

        /*SongObject so = new SongObject("Song 0");
        mSongsList.add(so);*/

        // Get songs from the device
        ContentResolver musicResolver = getContext().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if ( musicCursor != null && musicCursor.moveToFirst() ) {

            //get columns
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);
            //int uri = musicCursor.getColumnIndex(String.valueOf(MediaStore.Audio.Media.INTERNAL_CONTENT_URI));

            //add songs to list
            do {

                //Uri songUri = Uri.parse(musicCursor.getString(uri));
                String path = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Uri uriP = Uri.parse(path);
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisAlbum = musicCursor.getString(artistColumn);
                mSongsList.add(new SongObject(thisId, thisTitle, thisAlbum, uriP));

            }
            while (musicCursor.moveToNext());
        }

        //mAdapter.notifyDataSetChanged();
    }

    private void loadAudio() {
        ContentResolver contentResolver = getActivity().getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            //audioList = new ArrayList<>();
            mSongsList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Uri uriP = Uri.parse(data);

                // Save to audioList
                //audioList.add(new SongObject(data, title, album, artist));
                mSongsList.add(new SongObject(uriP, data, title, album, artist));
            }
        }
        cursor.close();

        MainActivity.setSongs( (ArrayList<SongObject>) mSongsList);
    }

    public static ArrayList<SongObject> getSongs() {

        return (ArrayList<SongObject>) mSongsList;
    }
}
