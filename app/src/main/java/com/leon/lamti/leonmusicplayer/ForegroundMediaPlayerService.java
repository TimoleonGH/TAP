package com.leon.lamti.leonmusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Timoleon on 2/4/2017.
 */
public class ForegroundMediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "com.leon.lamti.leonmusicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.leon.lamti.leonmusicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.leon.lamti.leonmusicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.leon.lamti.leonmusicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.leon.lamti.leonmusicplayer.ACTION_STOP";

    private static Context mContext;

    // Media Player
    private static MediaPlayer mediaPlayer;
    private String mediaFile;
    private static int resumePosition;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    // Audio Focus
    private AudioManager audioManager;

    //List of available Audio files
    private static ArrayList<SongObject> audioList;
    private static int audioIndex = -1;
    private static SongObject activeAudio;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    private static boolean serviceRunning = false;


    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        registerBecomingNoisyReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if ( intent.getAction().equals("START_SERVICE") ) {
            try {
                //Load data from SharedPreferences
                //StorageUtil storage = new StorageUtil(getApplicationContext());
                audioList = Songs.getSongs();
                //storage.loadAudio();
                audioIndex = Songs.SongsAdapter.getItemPosition();
                //storage.loadAudioIndex();

                Log.d("AudioPlayer", "START_SERVICE" );
                Log.d("AudioPlayer", "AudioList 1: " + audioList.get(0).getTitle());
                Log.d("AudioPlayer", "AudioList 2: " + audioList.get(1).getTitle());
                Log.d("AudioPlayer", "AudioList 3: " + audioList.get(2).getTitle());
                Log.d("AudioPlayer", "AudioList 4: " + audioList.get(3).getTitle());
                Log.d("AudioPlayer", "AudioList position: " + audioIndex);
                Log.d("AudioPlayer", " " );

                if (audioIndex != -1 && audioIndex < audioList.size()) {
                    //index is in a valid range
                    activeAudio = audioList.get(audioIndex);
                    Log.d("AudioPlayer", "activeAudio title: " + activeAudio.getTitle() );
                    Log.d("AudioPlayer", "activeAudio title: " + activeAudio.getSongUri() );
                } else {
                    stopSelf();
                }
            } catch (NullPointerException e) {
                stopSelf();
            }

            //Request audio focus
            if (requestAudioFocus() == false) {
                //Could not gain focus
                stopSelf();
            } else {

                initMediaPlayer();
                foregroundNotification();
            }
        } else if ( intent.getAction().equals("PLAY_AUDIO") ){

            Log.d("AudioPlayer", "PLAY_AUDIO" );
            playMedia();
        } else if ( intent.getAction().equals("PAUSE_AUDIO") ){

            Log.d("AudioPlayer", "PAUSE_AUDIO" );
            pauseMedia();
        } else if ( intent.getAction().equals("STOP_AUDIO") ){

            Log.d("AudioPlayer", "STOP_AUDIO" );
            stopMedia();
        } else if ( intent.getAction().equals("NEXT_TRACK") ){

            Log.d("AudioPlayer", "NEXT_TRACK" );
            skipToNext();
        } else if ( intent.getAction().equals("PREV_TRACK") ){

            Log.d("AudioPlayer", "PREV_TRACK" );
            skipToPrevious();
        } else if ( intent.getAction().equals("STOP_SERVICE") ){

            Log.d("AudioPlayer", "STOP_SERVICE" );
            serviceRunning = false;
            //stopForeground(true);
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("AudioPlayer", "onDestroy");
        Log.d("AudioPlayer", " " );

        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();

        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // Media Player Methods
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

        Log.d("AudioPlayer", "onBufferingUpdate");
        Log.d("AudioPlayer", " " );
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        Log.d("AudioPlayer", "onCompletion");
        Log.d("AudioPlayer", " " );
        //Invoked when playback of a media source has completed.
        stopMedia();
        //stop the service
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("AudioPlayer", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("AudioPlayer", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("AudioPlayer", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d("AudioPlayer", "onInfo");
        Log.d("AudioPlayer", " " );
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        Log.d("AudioPlayer", "onPrepared");
        Log.d("AudioPlayer", " " );

        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

        Log.d("AudioPlayer", "onSeekComplete");
        Log.d("AudioPlayer", " " );

        skipToNext();
    }


    // Audio Methods
    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }


    // Media Player - Actions
    private void initMediaPlayer() {

        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Set the data source to the mediaFile location
        //mediaPlayer = MediaPlayer.create(this, activeAudio.getSongUri());
        try {
            Log.d("AudioPlayer", "uri: " + activeAudio.getSongUri());
            mediaPlayer.setDataSource(this, activeAudio.getSongUri());
            //.setDataSource(String.valueOf(activeAudio.getSongUri()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        serviceRunning = true;
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {

        Log.d("AudioPlayer", "playMedia");
        Log.d("AudioPlayer", " " );

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopMedia() {

        Log.d("AudioPlayer", "stopMedia");
        Log.d("AudioPlayer", " " );

        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {

        Log.d("AudioPlayer", "pauseMedia");
        Log.d("AudioPlayer", " " );

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {

        Log.d("AudioPlayer", "resumeMedia");
        Log.d("AudioPlayer", " " );

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void skipToNext() {

        Log.d("AudioPlayer", "skipToNext");
        Log.d("AudioPlayer", " " );

        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        //Update stored index
        //new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {

        Log.d("AudioPlayer", "skipToPrevious");
        Log.d("AudioPlayer", " " );

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        //new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }


    public static boolean isServiceRunning() {

        return serviceRunning;
    }


    // Build Notification
    private void foregroundNotification() {

        // foreground
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(this, ForegroundService.class);
        previousIntent.setAction("PREV_TRACK");
        //previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent(this, ForegroundService.class);
        playIntent.setAction("PLAY_AUDIO");
        //playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent(this, ForegroundService.class);
        nextIntent.setAction("NEXT_TRACK");
        //nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_key_music);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Leon Music Player")
                .setTicker("Leon Music Player")
                .setContentText("My Music")
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent).build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
    }


    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }


    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            //buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }
}
