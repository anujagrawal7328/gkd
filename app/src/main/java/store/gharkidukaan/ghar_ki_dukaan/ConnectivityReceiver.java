package store.gharkidukaan.ghar_ki_dukaan;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.eventbus.EventBus;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class ConnectivityReceiver  extends BroadcastReceiver {


    Dialog dialog;
    TextView nettext;
    String url;

    int downSpeed;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(final Context context, final Intent intent) {

        String status = NetworkUtil.getConnectivityStatusString(context);

        Log.d("network",status);
        if(status.equals("Wifi enabled")){
            context.sendBroadcast(new Intent("INTERNET_Received"));
        }
       else if(status.equals("Mobile data enabled")) {
            context.sendBroadcast(new Intent("INTERNET_Received"));

        }else{
            status="No Internet Connection";
           // dialog.show();
            context.sendBroadcast(new Intent("INTERNET_Gone"));
       }

//        Toast.makeText(context, status, Toast.LENGTH_LONG).show();
    }

}