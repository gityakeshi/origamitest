package origamify.com.origamitest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;

import java.util.Map;

//import android.app.Service;
//import android.content.Context;
//import android.os.IBinder;
//import android.support.v4.app.NotificationManagerCompat;
//import android.support.v7.app.NotificationCompat;
//import android.support.v4.app.NotificationManagerCompat;
//import com.google.firebase.messaging.RemoteMessage.Notification;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";


    /*
    *   Diese Methode wird bei eingehender Firebase Cloud Message aufgerufen und differenziert dabei
    *   zwischen den Übertragungsarten data und notification, wobei letztere weniger Anpassungsmöglichkeiten
    *   bietet jedoch etwas leichter zu handhaben ist. Alle von unserem Server stammenden Push-Nachrichten
    *   entsprechen dem Typ data.
    */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

                Map<String, String> receivedMap = remoteMessage.getData();
                String title = receivedMap.get("title");
                String body = receivedMap.get("body");
                Log.d("title", title);
                Log.d("body", body);
                sendNotification(title, body);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Notification receivedMap = remoteMessage.getNotification();
            String title = receivedMap.getTitle();
            String body = receivedMap.getBody();
            Log.d("title", title);
            Log.d("body", body);
            sendNotification(title, body);

            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        }

    }

    /*
    *   Bei Verwendung des Übertragungstyps data notwendige Methode, die die automatische Erstellung von Push-
    *   Nachrichten auf dem Gerät selber übernimmt, u.a. mithilfe von Benachrichtigungskanälen. Die Benachrichtigungen
    *   sind dadurch stets verfügbar (können empfangen und angezeigt werden), solange die App mindestens im Hintergrund
    *   läuft.
    */
    public void sendNotification(String messageTitle, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "1324";
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("1324", "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("haha konrad amirite");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }


        if (notificationManager != null) {
            notificationManager.notify(1324 /* ID of notification */, notificationBuilder.build());
        }


    }

}
