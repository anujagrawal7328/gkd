package store.gharkidukaan.ghar_ki_dukaan;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.eventbus.EventBus;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConnectivityReceiver  extends BroadcastReceiver {


    Dialog dialog;
    TextView nettext;
    String url;


    @Override
    public void onReceive(final Context context, final Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        Log.d("network",status);
        if(status.equals("Wifi enabled")||status.equals("Mobile data enabled")) {
            context.sendBroadcast(new Intent("INTERNET_Received"));

        }else{
            status="No Internet Connection";
           // dialog.show();
            context.sendBroadcast(new Intent("INTERNET_Gone"));


        }

//        Toast.makeText(context, status, Toast.LENGTH_LONG).show();
    }

}