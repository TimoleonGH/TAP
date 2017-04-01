package com.leon.lamti.leonmusicplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

public class MediaPlaybackService extends Service {

    private MediaSessionCompat mMediaSession;
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

        // Create Media Session
        mMediaSession = new MediaSessionCompat( context, Log_TAG );
        mMediaSession.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        //mMediaSession.setCallback( new ExampleCallbacks() );

        // Right after audio focus
        mMediaSession.setActive(true);

        // On stop
        mMediaSession.setActive(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MediaButtonReceiver.handleIntent( mMediaSession, intent );


        return super.onStartCommand(intent, flags, startId);
    }

    public static NotificationCompat.Builder from ( Context context, MediaSessionCompat mediaSession ){

        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle( description.getTitle() )
                .setContentText( description.getSubtitle() )
                .setSubText( description.getDescription() )
                .setLargeIcon( description.getIconBitmap() )
                .setContentIntent( controller.getSessionActivity() )
                .setVisibility( NotificationCompat.VISIBILITY_PUBLIC )
                .setDeleteIntent( getActionIntent( context, KeyEvent.KEYCODE_MEDIA_STOP) );

        //NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSession);
        return builder;
    }

    public static PendingIntent getActionIntent ( Context context, int mediaKeyEvent ) {

        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setPackage(context.getPackageName());
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, mediaKeyEvent));

        return PendingIntent.getBroadcast(context, mediaKeyEvent, intent, 0);
    }

}
