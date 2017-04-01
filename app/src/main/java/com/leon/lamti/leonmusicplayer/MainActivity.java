package com.leon.lamti.leonmusicplayer;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    // Views
    private ConstraintLayout mHomeLayout;
    private AwesomeView mAwesomeView, mBottomAwesomeView;
    private AwesomeProgressBar mAwesomeProgressBar;
    private static FloatingActionButton mPlayFAB;
    private ImageButton mStop, mNext, mPrevious, mShuffle, mRepeat, mList;
    private ImageView mTrackImage, mCenterIV;
    private Button albumsB, songsB, listsB;

    // Animations Variables
    private int proBarY;
    private static int pixelDensity;
    private float startedX, startedY;
    private boolean fabMoved, fabPlayed;
    private Handler mHandler;
    private Animation scaleAppear, scaleDisappear, fadeIn, fadeOut;
    private static boolean trackListClosed;

    // StatePager
    //private ViewPager statePager;
    private SelectiveViewPager statePager;
    private StatePagerAdapter statePagerAdapter;
//    private Albums fragmentAlbums;
//    private Songs fragmentSongs;
//    private Lists fragmentLists;
//    private static int chapterPosition;

    // Tab
    private AwesomeTab mAwesomeTab;
    private View mAwesomeTAbView;

    // PlayFab move var
    private static int rowY = 0;
    private static int playX;
    private static int playY;

    // Media Player Service
    private MediaPlayerService player;
    boolean serviceBound = false;

    // Audio
    private static ArrayList<SongObject> audioList;
    private static int audioPosition = 0;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.leon.lamti.leonmusicplayer.PlayNewAudio";


    // Activity Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mHomeLayout = (ConstraintLayout) findViewById(R.id.activity_main);
        mAwesomeView = (AwesomeView) findViewById(R.id.awesomeView);
        mAwesomeTab = (AwesomeTab) findViewById(R.id.awesomeTab);
        mAwesomeTAbView = (View) findViewById(R.id.awesomeTabView);
        mCenterIV = (ImageView) findViewById(R.id.albumsIV);
        albumsB = (Button) findViewById(R.id.albumsB);
        songsB = (Button) findViewById(R.id.songsB);
        listsB = (Button) findViewById(R.id.listsB);
        mBottomAwesomeView = (AwesomeView) findViewById(R.id.awesomeViewBottom);
        mBottomAwesomeView.rotateView();
        mAwesomeProgressBar = (AwesomeProgressBar) findViewById(R.id.awesomeProgressBar);
        mTrackImage = (ImageView) findViewById(R.id.trackIV);
        mPlayFAB = (FloatingActionButton) findViewById(R.id.playFab);
        mStop = (ImageButton) findViewById(R.id.stopIB);
        mNext = (ImageButton) findViewById(R.id.nextTrackIB);
        mPrevious = (ImageButton) findViewById(R.id.previousTrackIB);
        mShuffle = (ImageButton) findViewById(R.id.shuffleIB);
        mRepeat = (ImageButton) findViewById(R.id.repeatIB);
        mList = (ImageButton) findViewById(R.id.listIB);

        fabMoved = false;
        fabPlayed = false;
        trackListClosed = false;

        mHomeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                mHomeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                /*int[] locations = new int[2];
                //mPlayFAB.getLocationOnScreen(locations);
                int x = locations[0];
                int y = locations[1];*/

                startedX = mPlayFAB.getX();

                int tabWidth = mHomeLayout.getWidth() / 3;

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mHomeLayout);
                constraintSet.constrainWidth(R.id.awesomeTabView, tabWidth);
                //constraintSet.constrainHeight(R.id.awesomeProgressBar, (mPlayFAB.getHeight() + 60) );
                constraintSet.applyTo(mHomeLayout);

            }
        });

        context = this.getApplicationContext();
        mPlayFAB.setVisibility(View.INVISIBLE);
        initPager();
        tabButtons();
        playView();
        trackList();
        stopView();
        animations();

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent startIntent = new Intent(MainActivity.this, ForegroundMediaPlayerService.class);
                startIntent.setAction("NEXT_TRACK");
                startService(startIntent);
            }
        });

        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent startIntent = new Intent(MainActivity.this, ForegroundMediaPlayerService.class);
                startIntent.setAction("PREV_TRACK");
                startService(startIntent);
            }
        });

        //audioList = Songs.getSongs();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAwesomeTAbView.setX( (mHomeLayout.getWidth() / 2) - (mAwesomeTAbView.getWidth() / 2) );
    }

    @Override
    protected void onStop() {
        super.onStop();

        Songs.SongsAdapter.setAdapterClickable(true);

        /*Intent startIntent = new Intent(MainActivity.this, ForegroundMediaPlayerService.class);
        startIntent.setAction("STOP_SERVICE");
        startService(startIntent);*/
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Songs.SongsAdapter.setAdapterClickable(true);

        /*if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }*/
    }

    public static void setSongs ( ArrayList<SongObject> list ) {

        audioList = list;
    }

    // Service Connection
    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    static Context mContext;
    public static final String ACTION_PLAY = "com.leon.lamti.leonmusicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.leon.lamti.leonmusicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.leon.lamti.leonmusicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.leon.lamti.leonmusicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.leon.lamti.leonmusicplayer.ACTION_STOP";

    // Play Audio - NOT USED
    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    private void pauseAudio() {
        //Check is service is active

        Intent playerIntent = new Intent(mContext, MediaPlayerService.class);
        playerIntent.setAction(ACTION_PAUSE);
        mContext.startService(playerIntent);
    }

    private void stopAudio() {
        //Check is service is active

        Intent playerIntent = new Intent(mContext, MediaPlayerService.class);
        playerIntent.setAction(ACTION_STOP);
        mContext.startService(playerIntent);
    }


    public static void setCurrentItemPosition ( int i ) {

        audioPosition = i;
    }

    private boolean serviceStarted = false;

    // Play - Stop Animation
    private void playView() {

        mPlayFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if ( !fabPlayed ) {

                        fabPlayed = true;
                        avdPlayToPause();
                        statePager.stopPager();
                        //Songs.setAdapterItemClickable(false);
                        //Songs.SongsAdapter.setSongClickable(false);
                        Songs.SongsAdapter.setAdapterClickable(false);

                        /*if ( !ForegroundService.isServiceRunning() ) {

                            // Start service to play music
                            Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                            startIntent.putExtra("curSong", curSong);
                            startService(startIntent);
                        } else {

                            // Start service to play music
                            Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                            startIntent.setAction(Constants.ACTION.PLAY_ACTION);
                            startIntent.putExtra("curSong", curSong );
                            startService(startIntent);
                        }*/

                        //pauseAudio();
                        //playAudio(audioPosition);

                        if ( !serviceStarted ) {

                            serviceStarted = true;

                            Intent startIntent = new Intent(MainActivity.this, ForegroundMediaPlayerService.class);
                            startIntent.setAction("START_SERVICE");
                            startService(startIntent);

                        } else {

                            Intent startIntent = new Intent(MainActivity.this, ForegroundMediaPlayerService.class);
                            startIntent.setAction("PLAY_AUDIO");
                            startService(startIntent);
                        }

                    } else {

                        Log.d("PLAY_AUDIO", "pos: " + audioPosition);
                        //playAudio(audioPosition);
                        //pauseAudio();
                        Intent startIntent = new Intent(MainActivity.this, ForegroundMediaPlayerService.class);
                        startIntent.setAction("PAUSE_AUDIO");
                        startService(startIntent);

                        // Start service to play music
                        /*Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                        startIntent.setAction(Constants.ACTION.PLAY_ACTION);
                        startIntent.putExtra("curSong", curSong );
                        startService(startIntent);*/

                        fabPlayed = false;
                        avdPauseToPlay();
                    }
                    moveFab();


                }
        });
    }

    private void moveFab() {

        mPlayFAB.setElevation(8);
        mAwesomeProgressBar.setElevation(6);

        playX = mHomeLayout.getWidth() / 2;
        playX = playX - mPlayFAB.getWidth() / 2;
        //playX = (int) ( playX - ( (16 * pixelDensity) + (28 * pixelDensity) ) );

        playY = mPlayFAB.getHeight() / 2;
        proBarY = mAwesomeProgressBar.getHeight() / 2;

        proBarY = (int) ( mBottomAwesomeView.getY() - proBarY);
        playY = (int) ( mBottomAwesomeView.getY() - playY);

        mHandler = new Handler();

        if ( !fabMoved ) {

            fabMoved = true;

            //ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(mPlayFAB, "x", mPlayFAB.getX(), x);
            ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(mPlayFAB, "x", startedX, playX);
            objectAnimatorX.setInterpolator(new DecelerateInterpolator());

            //ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(mPlayFAB, "y", mPlayFAB.getY(), y);
            ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(mPlayFAB, "y", rowY, playY);
            objectAnimatorY.setInterpolator(new AccelerateDecelerateInterpolator());

            mAwesomeProgressBar.setY(proBarY);

            objectAnimatorX.setDuration(300);
            objectAnimatorY.setDuration(300);

            objectAnimatorX.start();
            objectAnimatorY.start();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mAwesomeView.closeViews("top");
                    mBottomAwesomeView.closeViews("bot");
                    mBottomAwesomeView.setVisibility(View.VISIBLE);

                    mStop.setVisibility(View.VISIBLE);
                    mStop.startAnimation(scaleAppear);

                    mNext.setVisibility(View.VISIBLE);
                    mNext.startAnimation(scaleAppear);

                    mPrevious.startAnimation(scaleAppear);
                    mPrevious.setVisibility(View.VISIBLE);

                    mList.startAnimation(scaleAppear);
                    mList.setVisibility(View.VISIBLE);

                    mShuffle.startAnimation(scaleAppear);
                    mShuffle.setVisibility(View.VISIBLE);

                    mRepeat.startAnimation(scaleAppear);
                    mRepeat.setVisibility(View.VISIBLE);


                    Animation fadeOut1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);

                    mAwesomeTAbView.startAnimation(fadeOut1);
                    mAwesomeTAbView.setVisibility(View.GONE);

                    mCenterIV.startAnimation(scaleDisappear);
                    mCenterIV.setVisibility(View.GONE);

                    albumsB.startAnimation(fadeOut1);
                    albumsB.setVisibility(View.GONE);

                    songsB.startAnimation(fadeOut1);
                    songsB.setVisibility(View.GONE);

                    listsB.startAnimation(fadeOut1);
                    listsB.setVisibility(View.GONE);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Animation fadeIn2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                            fadeIn2.setDuration(600);
                            mTrackImage.startAnimation(fadeIn2);
                            mTrackImage.setVisibility(View.VISIBLE);

                            mAwesomeProgressBar.startAnimation(fadeIn);
                            mAwesomeProgressBar.setProgress(0f);
                            mAwesomeProgressBar.setVisibility(View.VISIBLE);
                        }
                    }, 300);
                }
            }, 100);
        }
    }

    private void stopView() {

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Stop service to stop music
                /*Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                startIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                startService(startIntent);*/

                //stopAudio();

                serviceStarted = false;
                Intent startIntent = new Intent(MainActivity.this, ForegroundMediaPlayerService.class);
                startIntent.setAction("STOP_SERVICE");
                startService(startIntent);

                avdPauseToPlay();
                statePager.startPager();
                //Songs.setAdapterItemClickable(true);
                //Songs.SongsAdapter.setSongClickable(true);
                Songs.SongsAdapter.setAdapterClickable(true);
                fabMoved = false;
                fabPlayed = false;

                mAwesomeProgressBar.setVisibility(View.GONE);
                mStop.setVisibility(View.GONE);
                mNext.setVisibility(View.GONE);
                mPrevious.setVisibility(View.GONE);
                mList.setVisibility(View.GONE);

                mAwesomeProgressBar.startAnimation(fadeOut);
                mStop.startAnimation(scaleDisappear);
                mNext.startAnimation(scaleDisappear);
                mPrevious.startAnimation(scaleDisappear);
                mList.startAnimation(scaleDisappear);

                mShuffle.startAnimation(scaleDisappear);
                mShuffle.setVisibility(View.GONE);

                mRepeat.startAnimation(scaleDisappear);
                mRepeat.setVisibility(View.GONE);


                Animation fadeIn1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);

                mAwesomeTAbView.startAnimation(fadeIn1);
                mAwesomeTAbView.setVisibility(View.VISIBLE);

                albumsB.startAnimation(fadeIn1);
                albumsB.setVisibility(View.VISIBLE);

                songsB.startAnimation(fadeIn1);
                songsB.setVisibility(View.VISIBLE);

                listsB.startAnimation(fadeIn1);
                listsB.setVisibility(View.VISIBLE);

                Animation fadeOut2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                mTrackImage.startAnimation(fadeOut2);
                mTrackImage.setVisibility(View.GONE);

                mAwesomeView.closeViews("top");
                mBottomAwesomeView.closeViews("bot");

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(mPlayFAB, "x", playX, startedX);
                        objectAnimatorX.setInterpolator(new AccelerateInterpolator());

                        //ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(mPlayFAB, "y", mPlayFAB.getY(), startedY);
                        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(mPlayFAB, "y", playY, rowY);
                        objectAnimatorY.setInterpolator(new AccelerateDecelerateInterpolator());

                        objectAnimatorX.setDuration(300);
                        objectAnimatorY.setDuration(300);

                        objectAnimatorX.start();
                        objectAnimatorY.start();

                        mCenterIV.startAnimation(scaleAppear);
                        mCenterIV.setVisibility(View.VISIBLE);
                    }
                }, 100);
            }
        });
    }

    private static String curSong;
    public static void setSong ( Uri s ) {

        //Toast.makeText(context, "" + s, Toast.LENGTH_SHORT).show();
        curSong = String.valueOf(s);
    }

    public static void changeFabY( int y ) {

        rowY = y - (mPlayFAB.getHeight() / 2);
        rowY = rowY + (mPlayFAB.getHeight() / 9);
        //rowY = pxToDp(rowY);
        //rowY = (int) ( rowY - ( (16 * pixelDensity) + (28 * pixelDensity) ) );
        mPlayFAB.setY(rowY);
    }

    public static void setFabVisible( boolean b ) {

        if ( b ) {
            mPlayFAB.setVisibility(View.VISIBLE);
        } else {
            mPlayFAB.setVisibility(View.INVISIBLE);
        }
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    static Context context;
    public static int pxToDp(int px) {

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void trackList() {

        mList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ( !trackListClosed ) {

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(mHomeLayout);
                    constraintSet.center(R.id.listIB, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0, R.id.nextTrackIB, ConstraintSet.BOTTOM, 32, 0.91f);
                    constraintSet.center(R.id.shuffleIB, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0, R.id.nextTrackIB, ConstraintSet.BOTTOM, 32, 0.91f);
                    constraintSet.center(R.id.repeatIB, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0, R.id.nextTrackIB, ConstraintSet.BOTTOM, 32, 0.91f);
                    constraintSet.applyTo(mHomeLayout);
                } else {

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(mHomeLayout);
                    constraintSet.center(R.id.listIB, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0, R.id.nextTrackIB, ConstraintSet.BOTTOM, 32, 0.001f);
                    constraintSet.center(R.id.shuffleIB, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0, R.id.nextTrackIB, ConstraintSet.BOTTOM, 32, 0.001f);
                    constraintSet.center(R.id.repeatIB, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0, R.id.nextTrackIB, ConstraintSet.BOTTOM, 32, 0.001f);
                    constraintSet.applyTo(mHomeLayout);
                }
                Transition transition = TransitionInflater.from(MainActivity.this).inflateTransition(R.transition.changebounds_with_arcmotion);
                TransitionManager.beginDelayedTransition(mHomeLayout, transition);

                if ( !trackListClosed ) {

                    statePager.startPager();
                    Songs.SongsAdapter.setAdapterClickable(true);
                    trackListClosed = true;

                    Animation fadeOut2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                    mTrackImage.startAnimation(fadeOut2);
                    mTrackImage.setVisibility(View.GONE);

                    Animation fadeIn1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);

                    mAwesomeTAbView.startAnimation(fadeIn1);
                    mAwesomeTAbView.setVisibility(View.VISIBLE);

                    mCenterIV.startAnimation(scaleAppear);
                    mCenterIV.setVisibility(View.VISIBLE);

                    albumsB.startAnimation(fadeIn1);
                    albumsB.setVisibility(View.VISIBLE);

                    songsB.startAnimation(fadeIn1);
                    songsB.setVisibility(View.VISIBLE);

                    listsB.startAnimation(fadeIn1);
                    listsB.setVisibility(View.VISIBLE);

                } else {

                    statePager.stopPager();
                    Songs.SongsAdapter.setAdapterClickable(false);
                    trackListClosed = false;

                    Animation fadeIn2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                    fadeIn2.setDuration(800);
                    mTrackImage.startAnimation(fadeIn2);
                    mTrackImage.setVisibility(View.VISIBLE);

                    mCenterIV.startAnimation(scaleDisappear);
                    mCenterIV.setVisibility(View.GONE);

                    Animation fadeOut1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);

                    mAwesomeTAbView.startAnimation(fadeOut1);
                    mAwesomeTAbView.setVisibility(View.GONE);

                    albumsB.startAnimation(fadeOut1);
                    albumsB.setVisibility(View.GONE);

                    songsB.startAnimation(fadeOut1);
                    songsB.setVisibility(View.GONE);

                    listsB.startAnimation(fadeOut1);
                    listsB.setVisibility(View.GONE);
                }

                mAwesomeView.closeViews("top");
            }
        });
    }

    public static boolean getTrackListClosed() {

        return trackListClosed;
    }


    //  viewpager change listener
    private void initPager() {

        // Fragment State Pager
        //statePager = (ViewPager) findViewById(R.id.statePagerVP);
        statePager = (SelectiveViewPager) findViewById(R.id.statePagerVP);
        statePagerAdapter = new StatePagerAdapter(getSupportFragmentManager());
        statePager.setAdapter(statePagerAdapter);
        statePager.addOnPageChangeListener(viewPagerPageChangeListener);
        //statePager.removeOnPageChangeListener();
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            if (position == 0) {

                ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(mAwesomeTAbView, "x", mAwesomeTAbView.getX(), 0 );
                objectAnimatorX.setInterpolator(new AccelerateInterpolator());
                objectAnimatorX.setDuration(150);
                objectAnimatorX.start();

                mCenterIV.setImageDrawable(getDrawable(R.drawable.ic_library_music_black_24px));

            } else if ( position == 1 ) {

                ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(mAwesomeTAbView, "x", mAwesomeTAbView.getX(), (mHomeLayout.getWidth() / 2) - (mAwesomeTAbView.getWidth() / 2) );
                objectAnimatorX.setInterpolator(new AccelerateInterpolator());
                objectAnimatorX.setDuration(150);
                objectAnimatorX.start();

                mCenterIV.setImageDrawable(getDrawable(R.drawable.ic_music_note_black_24px));

            } else {

                ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(mAwesomeTAbView, "x", mAwesomeTAbView.getX(), mHomeLayout.getWidth() - mAwesomeTAbView.getWidth() );
                objectAnimatorX.setInterpolator(new AccelerateInterpolator());
                objectAnimatorX.setDuration(150);
                objectAnimatorX.start();

                mCenterIV.setImageDrawable(getDrawable(R.drawable.ic_favorite_black_24px));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

            Log.d("Scroll", "ps: " + arg0 + ", " + arg1 + ", " + arg2);

            //mAwesomeTAbView.setX( mAwesomeTAbView.getX() * arg2);
            //mAwesomeTAbView.setY( mAwesomeTAbView.getY() * arg2);

            /*ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(mAwesomeTAbView, "translationX", mAwesomeTAbView.getX(), mAwesomeTAbView.getX() + (mAwesomeTAbView.getX() * arg1) );
            objectAnimatorX.setInterpolator(new LinearInterpolator());
            objectAnimatorX.start();*/

            //proPrice = proPrice + 10;
            //goalProgressBar.setProgress(proPrice);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private static final int ITEMS = 3;

    // FragmentStatePager
    public static class StatePagerAdapter extends FragmentStatePagerAdapter {

        public StatePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    //return Albums.init(position, chapterPosition);
                    return new Albums();
                case 1:
                    return new Songs();
                case 2:
                    return new Lists();
                default:
                    return new Songs();
            }
        }
    }

    // Tab Buttons
    private void tabButtons() {

        albumsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                statePager.setCurrentItem(0);
            }
        });

        songsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                statePager.setCurrentItem(1);
            }
        });

        listsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                statePager.setCurrentItem(2);
            }
        });
    }


    // Play - Pause Animation
    private void avdPlayToPause() {

        AnimatedVectorDrawable playToPause = (AnimatedVectorDrawable) getDrawable(R.drawable.play_to_pause);
        mPlayFAB.setImageDrawable(playToPause);
        playToPause.start();
    }

    private void avdPauseToPlay() {

        AnimatedVectorDrawable pauseToPlay = (AnimatedVectorDrawable) getDrawable(R.drawable.pause_to_play);
        mPlayFAB.setImageDrawable(pauseToPlay);
        pauseToPlay.start();
    }


    // Animations
    private void animations() {

        scaleAppear = AnimationUtils.loadAnimation(this, R.anim.scale_appear);
        scaleDisappear = AnimationUtils.loadAnimation(this, R.anim.scale_disappear);

        scaleAppear.setDuration(300);
        scaleDisappear.setDuration(150);

        scaleAppear.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleDisappear.setInterpolator(new DecelerateInterpolator());

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
    }
}
