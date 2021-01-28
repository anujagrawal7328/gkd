package store.gharkidukaan.ghar_ki_dukaan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static android.content.ContentValues.TAG;

public class FirebaseMessageReceiver extends FirebaseMessagingService {
    Map<String, String> data;
    String imageUrl,  action;
    NotificationCompat.Builder builder;
    @Override
    public void onNewToken(String token) {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.w("refresh","token"+ refreshedToken);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
    }

    @Override
    public void
    onMessageReceived(RemoteMessage remoteMessage) {
        // First case when notifications are received via
        // data event
        // Here, 'title' and 'message' are the assumed names
        // of JSON
        // attributes. Since here we do not have any data
        // payload, This section is commented out. It is
        // here only for reference purposes.
        /*if(remoteMessage.getData().size()>0){
            showNotification(remoteMessage.getData().get("title"),
                          remoteMessage.getData().get("message"));
        }*/

        // Second case when notification payload is
        // received.
        data = remoteMessage.getData();
        if (remoteMessage.getNotification() != null) {
            // Since the notification is received directly from
            // FCM, the title and the body can be fetched
            // directly as below.
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());

        }
    }

    // Method to get the custom Design for the display of
    // notification.
    private RemoteViews getCustomDesign(String title,
                                        String message) {
        RemoteViews remoteViews = new RemoteViews(
                getApplicationContext().getPackageName(),
                R.layout.notification);
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.message, message);
        remoteViews.setImageViewResource(R.id.icon,
                R.drawable.icon);
        return remoteViews;
    }

    // Method to display the notifications
    public void showNotification(String title,
                                 String message) {
        // Pass the intent to switch to the MainActivity

        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(this, MainActivity.class);

        imageUrl = (String) data.get("image");
        action = (String) data.get("action");
        Log.i(TAG, "onMessageReceived: imageUrl : "+imageUrl);
        Log.i(TAG, "onMessageReceived: action : "+action);
        intent.putExtra("action",action);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // Assign channel ID
        String channel_id =data.get("channel_id");
        Log.i(TAG, "onMessageReceived: channelId : "+channel_id);
        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
        // the activities present in the activity stack,
        // on the top of the Activity that is to be launched
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
        // Pass the intent to PendingIntent to start the
        // next Activity
        PendingIntent pendingIntent
                = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_ONE_SHOT);
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.JELLY_BEAN) {
          builder=  new NotificationCompat.Builder(getApplicationContext(), channel_id).setContent(getCustomDesign(title, message)).setSmallIcon(R.drawable.icon)
                  .setSound(soundUri)
                  .setVibrate(new long[]{1000, 1000, 1000,
                          1000, 1000})
                  .setContentIntent(pendingIntent)
                  .setPriority(NotificationCompat.PRIORITY_MAX)
                  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        } // If Android Version is lower than Jelly Beans,
        // customized layout cannot be used and thus the
        // layout is set as follows
        else {
            builder=  new NotificationCompat.Builder(getApplicationContext(), channel_id).setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.icon).setSmallIcon(R.drawable.icon)
                    .setSound(soundUri)
                    .setVibrate(new long[]{1000, 1000, 1000,
                            1000, 1000})
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        }
        // Create an object of NotificationManager class to
        // notify the
        // user of events that happen in the background.
        NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Check if the Android Version is greater than Oreo
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel
                    = new NotificationChannel(
                    channel_id, "@gkd_notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{1000, 1000, 1000,
                    1000, 1000});

            notificationManager.createNotificationChannel(
                    notificationChannel);

        }
        notificationManager.notify(0, builder.build());
    }

}
