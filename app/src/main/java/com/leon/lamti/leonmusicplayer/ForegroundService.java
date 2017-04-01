package com.leon.lamti.leonmusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import static android.content.ContentValues.TAG;
import static com.leon.lamti.leonmusicplayer.MediaPlaybackService.getActionIntent;

public class ForegroundService extends Service {

    // foreground
    private static final String LOG_TAG = "ForegroundService";

    // music player
    private MediaPlayer mediaPlayer;
    private Context context;
    private String Log_TAG = "Audio_Session";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getBaseContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");

            Uri u = Uri.parse(intent.getStringExtra("curSong"));
            mediaPlayer = MediaPlayer.create(this, u);

            mediaPlayer.start();
            serviceRunning = true;

            // foreground
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent previousIntent = new Intent(this, ForegroundService.class);
            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

            Intent playIntent = new Intent(this, ForegroundService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

            Intent nextIntent = new Intent(this, ForegroundService.class);
            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
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

        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");

        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");

            if ( !mediaPlayer.isPlaying() ) {

                mediaPlayer.start();
                Log.d(LOG_TAG, "start");
            } else {

                mediaPlayer.pause();
                Log.d(LOG_TAG, "pause");
            }

        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");

        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");

            if ( mediaPlayer != null ) {
                mediaPlayer.stop();
            }
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        serviceRunning = false;
    }

    private static boolean serviceRunning = false;
    public static boolean isServiceRunning() {

        return serviceRunning;
    }

    // media player
    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            /*LogHelper.d(TAG, "play");
            if (mPlayingQueue == null || mPlayingQueue.isEmpty()) {
                mPlayingQueue = QueueHelper.getRandomQueue(mMusicProvider);
                mSession.setQueue(mPlayingQueue);
                mSession.setQueueTitle(getString(R.string.random_queue_title));
                // start playing from the beginning of the queue
                mCurrentIndexOnQueue = 0;
            }
            if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
                handlePlayRequest();
            }*/

            // Right after audio focus
            //mMediaSession.setActive(true);
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            /*LogHelper.d(TAG, "OnSkipToQueueItem:" + queueId);
            if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
                // set the current index on queue from the music Id:
                mCurrentIndexOnQueue = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, queueId);
                // play the music
                handlePlayRequest();
            }*/
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            //LogHelper.d(TAG, "playFromMediaId mediaId:", mediaId, "  extras=", extras);
            // The mediaId used here is not the unique musicId. This one comes from the
            // MediaBrowser, and is actually a "hierarchy-aware mediaID": a concatenation of
            // the hierarchy in MediaBrowser and the actual unique musicID. This is necessary
            // so we can build the correct playing queue, based on where the track was
            // selected from.
            /*mPlayingQueue = QueueHelper.getPlayingQueue(mediaId, mMusicProvider);
            mSession.setQueue(mPlayingQueue);
            String queueTitle = getString(R.string.browse_musics_by_genre_subtitle,
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId));
            mSession.setQueueTitle(queueTitle);
            if (mPlayingQueue != null && !mPlayingQueue.isEmpty()) {
                String uniqueMusicID = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
                // set the current index on queue from the music Id:
                mCurrentIndexOnQueue = QueueHelper.getMusicIndexOnQueue(
                        mPlayingQueue, uniqueMusicID);
                // play the music
                handlePlayRequest();
            }*/
        }

        @Override
        public void onPause() {
            //LogHelper.d(TAG, "pause. current state=" + mState);
            //handlePauseRequest();
        }

        @Override
        public void onStop() {
            //LogHelper.d(TAG, "stop. current state=" + mState);
            //handleStopRequest(null);

            //mMediaSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            /*LogHelper.d(TAG, "skipToNext");
            mCurrentIndexOnQueue++;
            if (mPlayingQueue != null && mCurrentIndexOnQueue >= mPlayingQueue.size()) {
                mCurrentIndexOnQueue = 0;
            }
            if (QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
                mState = PlaybackState.STATE_PLAYING;
                handlePlayRequest();
            } else {
                LogHelper.e(TAG, "skipToNext: cannot skip to next. next Index=" +
                        mCurrentIndexOnQueue + " queue length=" +
                        (mPlayingQueue == null ? "null" : mPlayingQueue.size()));
                handleStopRequest("Cannot skip");
            }*/
        }

        @Override
        public void onSkipToPrevious() {
            /*LogHelper.d(TAG, "skipToPrevious");
            mCurrentIndexOnQueue--;
            if (mPlayingQueue != null && mCurrentIndexOnQueue < 0) {
                // This sample's behavior: skipping to previous when in first song restarts the
                // first song.
                mCurrentIndexOnQueue = 0;
            }
            if (QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
                mState = PlaybackState.STATE_PLAYING;
                handlePlayRequest();
            } else {
                LogHelper.e(TAG, "skipToPrevious: cannot skip to previous. previous Index=" +
                        mCurrentIndexOnQueue + " queue length=" +
                        (mPlayingQueue == null ? "null" : mPlayingQueue.size()));
                handleStopRequest("Cannot skip");
            }*/
        }
    }

    public static PendingIntent getActionIntent2 ( Context context, int mediaKeyEvent ) {

        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setPackage(context.getPackageName());
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyEvent));

        return PendingIntent.getBroadcast(context, mediaKeyEvent, intent, 0);
    }
}
