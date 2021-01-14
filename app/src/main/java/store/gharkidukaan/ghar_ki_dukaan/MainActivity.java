package store.gharkidukaan.ghar_ki_dukaan;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;

import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<AppUpdateInfo> {

    WebView webshow;
    private BroadcastReceiver MyReceiver = null;
    Dialog dialog;
    Button restartapp;
    RelativeLayout layout_error;
    ImageView imageView,splashImage;
    private static final int REQ_CODE_VERSION_UPDATE = 530;
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;

    String url;
    @Override
    public void onBackPressed()
    {
        if(webshow.canGoBack()){
            webshow.goBack();
        }else{
            super.onBackPressed();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.purple_700));

        MyReceiver = new ConnectivityReceiver();
        broadcastIntent();

        webshow =findViewById(R.id.webshow);
        restartapp = findViewById(R.id.restartapp);
        layout_error = findViewById(R.id.layout_error);
         imageView = (ImageView) findViewById(R.id.imageView);
        splashImage = (ImageView) findViewById(R.id.SplashScreenImage);
        Glide.with(this).load(R.drawable.loader).into(imageView);
        splashImage.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        webshow.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT >= 19) {
            webshow.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            webshow.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        restartapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Log.d("clickedbutton","yes");
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
               startActivity(i);
            }
        });





    }
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("urls").document("webUrl");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            url = document.getString("value");
                            Intent appLinkIntent = getIntent();
                            String appLinkAction = appLinkIntent.getAction();
                            Uri appLinkData = appLinkIntent.getData();
                            if(appLinkData!=null){
                                Log.d("appLinks", appLinkData.toString());
                               checkForAppUpdate();
                                GetPaymentWebView( appLinkData.toString());
                                layout_error.setVisibility(View.GONE);
                                webshow.setVisibility(View.VISIBLE );
                                splashImage.setVisibility(View.GONE);
                                imageView.setVisibility(View.GONE);
                            }else{
                          checkForAppUpdate();
                                GetPaymentWebView(url);
                                layout_error.setVisibility(View.GONE);
                                webshow.setVisibility(View.VISIBLE );
                                splashImage.setVisibility(View.GONE);
                                imageView.setVisibility(View.GONE);
                            }
                            Log.d("value", document.getString("value")); //Print the name
                        } else {
                            Log.d("value", "No such document");
                        }
                    } else {
                        Log.d("value", "get failed with ", task.getException());
                    }
                }
            });




        }
    };
    BroadcastReceiver Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            webshow.setVisibility(View.GONE );
            layout_error.setVisibility(View.VISIBLE);
        }
    };
    public void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_Received"));
        registerReceiver(Receiver, new IntentFilter("INTERNET_Gone"));

    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(MyReceiver);
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(Receiver);
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void GetPaymentWebView(String url) {
        webshow.clearFormData();
        webshow.clearHistory();
        webshow.clearCache(true);
        webshow.getSettings().setAppCacheEnabled(true);
        webshow.getSettings().setDatabaseEnabled(true);
        webshow.getSettings().setDomStorageEnabled(true);
       // webshow.setWebChromeClient(new WebChromeClient());
        webshow.setWebViewClient(new myWebClient());
        webshow.getSettings().setLoadsImagesAutomatically(true);
        webshow.getSettings().setJavaScriptEnabled(true);
        webshow.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webshow.loadUrl(url);


    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {

            case REQ_CODE_VERSION_UPDATE:
                if (resultCode != RESULT_OK) { //RESULT_OK / RESULT_CANCELED / RESULT_IN_APP_UPDATE_FAILED
            //       L.d("Update flow failed! Result code: " + resultCode);
                    // If the update is cancelled or fails,
                    // you can request to start the update again.
                    unregisterInstallStateUpdListener();
                }

                break;
        }
    }

    private void checkForAppUpdate() {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(getApplication());
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(this);
        // Returns an intent object that you use to check for an update.
 // Create a listener to track request state updates.
        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState installState) {
                // Show module progress, log state, or install the update.
                if (installState.installStatus() == InstallStatus.DOWNLOADED)
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdateAndUnregister();
            }
        };

    }

    private void startAppUpdateImmediate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                   REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MainActivity.REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }


    private void popupSnackbarForCompleteUpdateAndUnregister() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.layout_error),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.purple_200));
        snackbar.show();

        unregisterInstallStateUpdListener();
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */

    private void unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && installStateUpdatedListener != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
    }

    @Override
    public void onSuccess(AppUpdateInfo result) {
        if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            // Request the update.
            if (result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                // Before starting an update, register a listener for updates.
                appUpdateManager.registerListener(installStateUpdatedListener);
                // Start an update.
                startAppUpdateFlexible(result);
            } else if (result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Start an update.
                startAppUpdateImmediate(result);
            }
        }
    }


/**
 * Needed only for FLEXIBLE update
 */


}






class myWebClient extends WebViewClient
{
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // TODO Auto-generated method stub
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO Auto-generated method stub
        if(url.startsWith("https://gharkidukaan.store/")||url.startsWith("https://m.gharkidukaan.store/")) {

            view.loadUrl(url);

        }else {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(i);
        }

        return true;
    }
}

