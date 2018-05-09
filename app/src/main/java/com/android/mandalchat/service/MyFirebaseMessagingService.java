package com.android.mandalchat.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.mandalchat.R;
import com.android.mandalchat.ui.LoginActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by vishal on 17/4/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "FromTo: " + remoteMessage.getData().get("body"));

        //Calling method to generate notification
        sendNotification(remoteMessage.getData().get("body"));
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {


        /*Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.smartphone)
                .setContentTitle("Callway Buddy")
                .setContentText("New Message : "+" "+messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());*/

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.btn_star);
        builder.setContentTitle("Callway Buddy");
        builder.setContentText("New Message : "+" "+messageBody);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText("New Message : "+" "+messageBody));
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.smartphone));

        Intent intent = new Intent(MyFirebaseMessagingService.this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 113,intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        builder.setFullScreenIntent(pendingIntent, true);


        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(115, builder.build());
    }
}
