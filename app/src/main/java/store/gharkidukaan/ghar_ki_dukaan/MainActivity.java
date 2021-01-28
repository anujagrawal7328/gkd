package store.gharkidukaan.ghar_ki_dukaan;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<AppUpdateInfo> {

    WebView webshow;
    private BroadcastReceiver MyReceiver = null;
    Button restartapp;
    RelativeLayout layout_error, main_layout;
    LinearLayout layout_msg;
    ImageView imageView, splashImage;
    private static final int REQ_CODE_VERSION_UPDATE = 530;
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    String url,notification_url,cookies;
    TextView close_msg;
    AsyncTask runningTask;
    Bundle extras;
    ProgressBar clciked_url;
    View transparent_layer;
    CookieManager cookieManager;
    CookieSyncManager cookieSyncManager;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (runningTask != null){
            runningTask.cancel(true);}
       /* LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);*/

    }


    @Override
    public void onBackPressed() {
        if (webshow.canGoBack()) {
            webshow.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        MyReceiver = new ConnectivityReceiver();
        broadcastIntent();
        webshow = findViewById(R.id.webshow);
        main_layout = findViewById(R.id.layoutMain);
        restartapp = findViewById(R.id.restartapp);
        layout_error = findViewById(R.id.layout_error);
        imageView = (ImageView) findViewById(R.id.imageView);
        splashImage = (ImageView) findViewById(R.id.SplashScreenImage);
        layout_msg = (LinearLayout) findViewById(R.id.layout_msg);
        close_msg = (TextView) findViewById(R.id.close_msg);
        clciked_url=(ProgressBar)findViewById(R.id.clicked_url);
        transparent_layer=(View)findViewById(R.id.transparent_layer);
        Glide.with(this).load(R.drawable.s1).into(splashImage);
        Glide.with(this).load(R.drawable.loader).into(imageView);
        splashImage.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        webshow.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        extras = getIntent().getExtras();
        if (Build.VERSION.SDK_INT >= 19) {
            webshow.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webshow.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        restartapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Log.d("clickedbutton", "yes");
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

    }
    class isInternetAvailable extends AsyncTask<Object, Void, Boolean> {

            private Exception exception;

            @Override
            protected Boolean doInBackground(Object... objects) {
                try {
                    Socket sock = new Socket();
                    SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

                    sock.connect(sockaddr, 1000); // This will block no more than timeoutMs
                    sock.close();

                    return true;

                } catch (IOException e) {
                    return false;
                }
            }

            protected void onPostExecute(Boolean Availibility) {
                if (Availibility == true) {

                } else {
                    layout_msg.setVisibility(View.VISIBLE);
                    close_msg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            layout_msg.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                runningTask = new isInternetAvailable();
                runningTask.execute();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("urls").document("webUrl");
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                url = document.getString("value");
                                Intent appLinkIntent = getIntent();
                                String appLinkAction = appLinkIntent.getAction();
                                Uri appLinkData = appLinkIntent.getData();

                                if (appLinkData != null) {
                                    checkForAppUpdate();
                                    GetPaymentWebView(appLinkData.toString());

                                }else if (extras != null) {
                                    if (extras.containsKey("action")) {
                                        notification_url = extras.getString("action");
                                         if (notification_url != null) {
                                            checkForAppUpdate();
                                            GetPaymentWebView(notification_url);

                                        }
                                    }
                                }
                            else if(url!=""||url!=null){
                                checkForAppUpdate();
                                GetPaymentWebView(url);
                            }
                                else {
                                    checkForAppUpdate();
                                    GetPaymentWebView("https://gharkidukaan.store");
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
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceive(Context context, Intent intent) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                webshow.setVisibility(View.GONE);
                splashImage.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                layout_error.setVisibility(View.VISIBLE);

            }
        };

        public void broadcastIntent () {
            registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_Received"));
            registerReceiver(Receiver, new IntentFilter("INTERNET_Gone"));
        }
        @Override
        protected void onPause () {
            super.onPause();
//        unregisterReceiver(MyReceiver);
//        unregisterReceiver(broadcastReceiver);
//        unregisterReceiver(Receiver);
        }
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @SuppressLint("SetJavaScriptEnabled")
        private void GetPaymentWebView (String url){
            webshow.clearFormData();
            webshow.clearHistory();
            webshow.clearCache(false);
            webshow.getSettings().setAppCacheEnabled(true);
            webshow.getSettings().setDatabaseEnabled(true);
            webshow.getSettings().setDomStorageEnabled(true);
            webshow.getSettings().getCacheMode();
            cookieManager = CookieManager.getInstance();
            webshow.setWebViewClient(new myWebClient());
            webshow.getSettings().setLoadsImagesAutomatically(true);
            webshow.getSettings().setJavaScriptEnabled(true);
            webshow.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webshow.loadUrl(url);

        }

        @Override
        public void onActivityResult ( int requestCode, final int resultCode, Intent intent){
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

        private void checkForAppUpdate () {
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

        private void startAppUpdateImmediate (AppUpdateInfo appUpdateInfo){
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

        private void startAppUpdateFlexible (AppUpdateInfo appUpdateInfo){
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


        private void popupSnackbarForCompleteUpdateAndUnregister () {
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

        @Override
        protected void onResume () {
            super.onResume();
            //registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_Received"));
//        registerReceiver(Receiver, new IntentFilter("INTERNET_Gone"));
        }

        /**
         * Checks that the update is not stalled during 'onResume()'.
         * However, you should execute this check at all app entry points.
         */

        private void unregisterInstallStateUpdListener () {
            if (appUpdateManager != null && installStateUpdatedListener != null)
                appUpdateManager.unregisterListener(installStateUpdatedListener);
        }

        @Override
        public void onSuccess (AppUpdateInfo result){
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
//            if (result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
//
//                // Before starting an update, register a listener for updates.
//                appUpdateManager.registerListener(installStateUpdatedListener);
//                // Start an update.
//                startAppUpdateFlexible(result);
//            } else if (result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Start an update.
                startAppUpdateImmediate(result);
                //   }
            }
        }


/**
 * Needed only for FLEXIBLE update
 */


private class myWebClient extends WebViewClient {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onLoadResource(WebView view, String url) {
        transparent_layer.setVisibility(View.GONE);
        clciked_url.setVisibility(View.GONE);
        cookieManager.setAcceptThirdPartyCookies(webshow,true);
        cookieSyncManager.getInstance().sync();
        Log.d(TAG, "onPageFinished: "+cookies);
        super.onLoadResource(view, url);
    }

    @Override
    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {

        return super.onRenderProcessGone(view, detail);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                splashImage.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                webshow.setVisibility(View.VISIBLE);
                layout_error.setVisibility(View.GONE);
                cookieManager.setAcceptCookie(true);
                cookieSyncManager=CookieSyncManager.createInstance(getBaseContext());
                cookies=cookieManager.getInstance().getCookie(url);
                cookieSyncManager.getInstance().startSync();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_700));
                super.onPageStarted(view, url, favicon);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageFinished(WebView view, String url) {

                view.loadUrl("javascript:(function() { " +
                        "document.getElementsByTagName('footer')[0].style.display=\"none\"; " +
                        "})()");

                super.onPageFinished(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                transparent_layer.setVisibility(View.VISIBLE);
                clciked_url.setVisibility(View.VISIBLE);
                if (url.startsWith("http://gharkidukaan.store/") || url.startsWith("http://m.gharkidukaan.store/") || url.startsWith("https://gharkidukaan.store/") || url.startsWith("https://m.gharkidukaan.store/")) {
                    view.loadUrl(url);
                    Log.d("url", url);
                } else {
                    Log.d("url", url);
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(i);
                    imageView.setVisibility(View.GONE);
                }
                return true;
            }


}

}







