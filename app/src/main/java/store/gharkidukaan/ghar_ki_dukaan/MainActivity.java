package store.gharkidukaan.ghar_ki_dukaan;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<AppUpdateInfo> {
    int progress;
    WebView webshow;
    private BroadcastReceiver MyReceiver = null;
    Button restartapp;
    RelativeLayout layout_error, main_layout;
    LinearLayout layout_msg;
    ImageView splashscreen,info1,info2;
    FrameLayout info_layout,progressbar_layout;
    private static final int REQ_CODE_VERSION_UPDATE = 530;
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    String url,notification_url,cookies;
    TextView close_msg;
    AsyncTask runningTask;
    Bundle extras;
    CookieManager cookieManager;
    CookieSyncManager cookieSyncManager;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS= 7;
    // the same for Android 5.0 methods only
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    String loadingUrl;

    // error handling
    private static final String TAG = MainActivity.class.getSimpleName();



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (runningTask != null){
            runningTask.cancel(true);}
       /* LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);*/

    }


    @Override
    public void onBackPressed() {
    if(!loadingUrl.equals("https://gharkidukaan.store/")) {

            webshow.goBack();
        }
        else{

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                      MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        MyReceiver = new ConnectivityReceiver();
        broadcastIntent();
        webshow = findViewById(R.id.webshow);
        main_layout = findViewById(R.id.layoutMain);
        restartapp = findViewById(R.id.restartapp);
        layout_error = findViewById(R.id.layout_error);
        layout_msg = (LinearLayout) findViewById(R.id.layout_msg);
        close_msg = (TextView) findViewById(R.id.close_msg);

        splashscreen = (ImageView) findViewById(R.id.SplashScreen);
        info1 = (ImageView) findViewById(R.id.info_Image1);
        info2 = (ImageView) findViewById(R.id.info_Image2);
        Glide.with(this).load(R.drawable.s1).into(splashscreen);
        Glide.with(this).load(R.drawable.sad).into(info1);
        Glide.with(this).load(R.drawable.safe).into(info2);
        info_layout = (FrameLayout) findViewById(R.id.info_layout_main);
        progressbar_layout=(FrameLayout) findViewById(R.id.progressbar_layer);
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
        checkNewAppVersionState();

 checkAndroidVersion();


    }
    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();

        } else {
            // code for lollipop and pre-lollipop devices
        }

    }
    private boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(getApplicationContext(),
               CAMERA);
        int wtite = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (wtite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE);
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("in fragment on request", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(CAMERA) == PackageManager.PERMISSION_GRANTED && perms.get(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("in fragment on request", "CAMERA & WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("in fragment on request", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Camera and Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(getApplicationContext(), "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
    private void checkNewAppVersionState() {
        appUpdateManager = AppUpdateManagerFactory.create(getApplication());
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            //FLEXIBLE:
                            // If the update is downloaded but not installed,
                            // notify the user to complete the update.
                            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                popupSnackbarForCompleteUpdateAndUnregister();
                            }

                            //IMMEDIATE:
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                startAppUpdateImmediate(appUpdateInfo);
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
                layout_error.setVisibility(View.GONE);
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
                                         if (notification_url != null && notification_url != "update") {
                                            checkForAppUpdate();
                                            GetPaymentWebView(notification_url);


                                        }else if(notification_url == "update"){
                                            appUpdateManager = AppUpdateManagerFactory.create(getApplication());
                                            appUpdateManager
                                                    .getAppUpdateInfo()
                                                    .addOnSuccessListener(
                                                            appUpdateInfo -> {
                                                                //FLEXIBLE:
                                                                // If the update is downloaded but not installed,
                                                                // notify the user to complete the update.
                                                                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                                                    popupSnackbarForCompleteUpdateAndUnregister();
                                                                }

                                                                //IMMEDIATE:
                                                                if (appUpdateInfo.updateAvailability()
                                                                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                                                    // If an in-app update is already running, resume the update.
                                                                    startAppUpdateImmediate(appUpdateInfo);
                                                                }
                                                            });
                                        }
                                    }
                                }
                            else if(url!="" && url!=null){

                                checkForAppUpdate();
                                GetPaymentWebView(url);
                            }
                            else {  webshow.setVisibility(View.VISIBLE);
                                    GetPaymentWebView("https://gharkidukaan.store");
                                    checkForAppUpdate();
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
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_200));
                webshow.setVisibility(View.GONE);
                layout_error.setVisibility(View.VISIBLE);
                info_layout.setVisibility(View.GONE);
                splashscreen.setVisibility(View.GONE);
                progressbar_layout.setVisibility(View.GONE);

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
            splashscreen.setVisibility(View.GONE);
            info_layout.setVisibility(View.GONE);
            progressbar_layout.setVisibility(View.VISIBLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.purple_200));
            webshow.clearFormData();
            webshow.clearHistory();
            webshow.clearCache(false);
            webshow.getSettings().setAppCacheEnabled(true);
            webshow.getSettings().setDatabaseEnabled(true);
            webshow.getSettings().setDomStorageEnabled(true);
            webshow.getSettings().getCacheMode();
            cookieManager = CookieManager.getInstance();
            webshow.getSettings().setPluginState(WebSettings.PluginState.ON);
            webshow.setWebViewClient(new myWebClient());
            webshow.setWebChromeClient(new WebChromeClient()
            {

                // openFileChooser for Android 3.0+
                public boolean onShowFileChooser(
                        WebView webView, ValueCallback<Uri[]> filePathCallback,
                        FileChooserParams fileChooserParams) {
                  if(checkAndRequestPermissions()) {
                      if (mFilePathCallback != null) {
                          mFilePathCallback.onReceiveValue(null);
                      }
                      mFilePathCallback = filePathCallback;

                      Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                      if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                          // create the file where the photo should go
                          File photoFile = null;
                          try {
                              photoFile = createImageFile();
                              takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                          } catch (IOException ex) {
                              // Error occurred while creating the File
                              Log.e(TAG, "Unable to create Image File", ex);
                          }

                          // continue only if the file was successfully created
                          if (photoFile != null) {
                              mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                              takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                      Uri.fromFile(photoFile));
                          } else {
                              takePictureIntent = null;
                          }
                      }

                      Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                      contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                      contentSelectionIntent.setType("image/*");

                      Intent[] intentArray;
                      if (takePictureIntent != null) {
                          intentArray = new Intent[]{takePictureIntent};
                      } else {
                          intentArray = new Intent[0];
                      }

                      Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                      chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                      chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.image_chooser));
                      chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                      startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                      return true;
                  }else{
                      return false;
                  }
                }

                // creating image files (Lollipop only)
                private File createImageFile() throws IOException {

                    File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DirectoryNameHere");

                    if (!imageStorageDir.exists()) {
                        imageStorageDir.mkdirs();
                    }

                    // create an image file name
                    imageStorageDir  = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                    return imageStorageDir;
                }

                // openFileChooser for Android 3.0+
                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                    mUploadMessage = uploadMsg;

                    try {
                        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DirectoryNameHere");

                        if (!imageStorageDir.exists()) {
                            imageStorageDir.mkdirs();
                        }

                        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");

                        mCapturedImageURI = Uri.fromFile(file); // save to the private variable

                        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                        captureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                        i.addCategory(Intent.CATEGORY_OPENABLE);
                        i.setType("image/*");

                        Intent chooserIntent = Intent.createChooser(i, getString(R.string.image_chooser));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Camera Exception:" + e, Toast.LENGTH_LONG).show();
                    }

                }

                // openFileChooser for Android < 3.0
                public void openFileChooser(ValueCallback<Uri> uploadMsg) {

                    openFileChooser(uploadMsg, "");
                }

                // openFileChooser for other Android versions
            /* may not work on KitKat due to lack of implementation of openFileChooser() or onShowFileChooser()
               https://code.google.com/p/android/issues/detail?id=62220
               however newer versions of KitKat fixed it on some devices */
                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

                    openFileChooser(uploadMsg, acceptType);
                }


                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);


                }

            });
            webshow.getSettings().setLoadsImagesAutomatically(true);
            webshow.getSettings().setAllowContentAccess(true);
            webshow.getSettings().setAllowFileAccess(true);
            webshow.getSettings().setJavaScriptEnabled(true);



            webshow.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webshow.loadUrl(url);


        }

        @Override
        public void onActivityResult ( int requestCode, final int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);

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

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

                if(requestCode==FILECHOOSER_RESULTCODE) {
                    if (null == this.mUploadMessage) {
                        return;
                    }

                    Uri result=null;

                    try{
                        if (resultCode != RESULT_OK) {
                            result = null;
                        } else {
                            // retrieve from the private variable if the intent is null
                            result = data == null ? mCapturedImageURI : data.getData();
                        }
                    }
                    catch(Exception e) {
                        Toast.makeText(getApplicationContext(), "activity :"+e, Toast.LENGTH_LONG).show();
                    }

                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }

            } // end of code for all versions except of Lollipop

            // start of code for Lollipop only
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }

                Uri[] results = null;

                // check that the response is a good one
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || data.getData() == null) {
                        // if there is not data, then we may have taken a photo
                        if (mCameraPhotoPath != null) {
                            results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                        }
                    } else {
                        String dataString = data.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;

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
    public void onSuccess (AppUpdateInfo result) {
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

private class myWebClient extends WebViewClient {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onLoadResource(WebView view, String url) {
        cookieManager.setAcceptThirdPartyCookies(webshow,true);
        cookieSyncManager.getInstance().sync();
        progressbar_layout.setVisibility(View.GONE);
        Log.d(TAG, "onPageFinished: "+cookies);
        super.onLoadResource(view, url);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                loadingUrl=url;
                progressbar_layout.setVisibility(View.VISIBLE);
                cookieManager.setAcceptCookie(true);
                cookieSyncManager=CookieSyncManager.createInstance(getBaseContext());
                cookies=cookieManager.getInstance().getCookie(url);
                cookieSyncManager.getInstance().startSync();
                super.onPageStarted(view, url, favicon);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageFinished(WebView view, String url) {


      /*          view.loadUrl("javascript:(function() { " +
                        "document.getElementsByTagName('footer')[0].style.display=\"none\"; " +
                        "})()");*/

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                super.onPageFinished(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                );

                if (url.startsWith("http://gharkidukaan.store/") || url.startsWith("http://m.gharkidukaan.store/") || url.startsWith("https://gharkidukaan.store/") || url.startsWith("https://m.gharkidukaan.store/") ||url.startsWith("https://www.tidio.com/talk/b1hd8rlhzioe2x0ayed1csb8asmua1vv") ) {
                    view.loadUrl(url);
                    webshow.setVisibility(View.VISIBLE);
                    Log.d("url", url);

                }
                else {
                    Log.d("url", url);
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(i);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                return true;
            }

}

}







