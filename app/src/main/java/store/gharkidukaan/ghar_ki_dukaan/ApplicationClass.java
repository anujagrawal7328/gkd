package store.gharkidukaan.ghar_ki_dukaan;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.onesignal.OSInAppMessageAction;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class ApplicationClass extends Application {
    private static final String ONESIGNAL_APP_ID = "831adfdf-6628-4630-a8d3-69db199e66cf";
    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
        OneSignal.setNotificationOpenedHandler(
                new OneSignal.OSNotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(OSNotificationOpenedResult result) {
                        String actionId = result.getAction().getActionId();
                        OSNotificationAction.ActionType type = result.getAction().getType(); // "ActionTaken" | "Opened"
                        String title = result.getNotification().getTitle();
                        JSONObject data = result.getNotification().getAdditionalData();
                        String action;
                        if (data != null) {
                            action = data.optString("action", null);

                            if (action!= null) {
                                Log.i("OneSignalExample", "customkey set with value: " + action);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("action",action);
                                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(intent);
                            }}
                        if (type == OSNotificationAction.ActionType.ActionTaken){
                            Log.i("OneSignalExample", "Button pressed with id: " +  result.getAction().getActionId());


                            if ( result.getAction().getActionId().equals("https://gharkidukaan.store/cart")) {
                                Toast.makeText(getApplicationContext(), actionId, Toast.LENGTH_LONG).show();
                                Intent sharingIntent = new Intent(getApplicationContext(), MainActivity.class);
                                sharingIntent.putExtra("action",actionId);
                                getApplicationContext().startActivity(sharingIntent);

                            }else if ( result.getAction().getActionId().equals("ActionTwo")) {
                                Toast.makeText(getApplicationContext(), "ActionTwo Button was pressed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }


                });
        OneSignal.setNotificationWillShowInForegroundHandler(new OneSignal.OSNotificationWillShowInForegroundHandler() {
            @Override
            public void notificationWillShowInForeground(OSNotificationReceivedEvent notificationReceivedEvent) {

                // Get custom additional data you sent with the notification
                JSONObject data = notificationReceivedEvent.getNotification().getAdditionalData();
                String action;
                if (data != null) {
                    action = data.optString("action", null);

                    if (action != null) {
                        Log.i("OneSignalExample", "customkey set with value: " + action);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("action", action);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }

                }
            }

        });

        OneSignal.setInAppMessageClickHandler(
                new OneSignal.OSInAppMessageClickHandler() {
                    @Override
                    public void inAppMessageClicked(OSInAppMessageAction result) {
                        String clickName = result.getClickName();
                        String clickUrl = result.getClickUrl();

                        boolean closesMessage = result.doesCloseMessage();
                        boolean firstClick = result.isFirstClick();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("action", clickUrl);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        getApplicationContext().startActivity(intent);
                    }}
                    );
    }
    }

