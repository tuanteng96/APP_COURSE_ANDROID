package vn.cser21;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;
import vn.cser21.incoming.CallNotEndEvent;
import vn.cser21.incoming.IncomingCallActivity;
import vn.cser21.incoming.IncomingEvent;


/*
Thay đổi cấu hình cho từng app
bao gồm:
- màu thương hiệu /res/color.xml
- Tên domain thương hiệu /res/string.xml
- Firebase notifiction /assets/google-service.json
*/
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    ANDROID ANDROID;
    WebView wv;
    App21 app21 = new App21(this);

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;
    private float dx;
    private float dy;

    //Upload Var
    private float m_downX;
    private static final int STORAGE_PERMISSION_CODE = 123;
     static final int LOCATION_PERMISSION_CODE = 231;
     static final int WIFI_INFO_CODE = 333;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri[]> mUploadMessage;

    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR = 1;
    private Result resultQrCode;
    private Result resultLocation;

    private FusedLocationProviderClient fusedLocationClient;
    


    private int resultKeyboardHeight;

    // End Upload Var

    public void showRequestPermissionLocation( Result result, String[] perms, int code) {
        resultLocation = result.copy();
        EasyPermissions.requestPermissions(this, "Vui lòng cấp quyền location ! ",
                code, perms);
    }

    public void showQrCodeScreen(Result result) {
        resultQrCode = result.copy();
        String[] perms = {Manifest.permission.CAMERA};
        EasyPermissions.requestPermissions(this, "Vui lòng cấp quyền camera ! ",
                201, perms);
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IncomingEvent event) {
        Intent incomingCallIntent = new Intent(this, IncomingCallActivity.class);
        incomingCallIntent.putExtra("URL_MP3", event.url);
        incomingCallIntent.putExtra("ID", event.id);
        incomingCallIntent.putExtra("IS_MAIN", true);
        startActivity(incomingCallIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventCallNotEnd(CallNotEndEvent event) {
        // call app
        Log.i("123321", event.duration + "---" + event.id);

        String script = "NotiMp3Push(" + event.id + "," + event.duration + ")";
        // wv.evaluateJavascript(script, null);

        //MainActivity m = (MainActivity) mContext;
        evalJs(script);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setBackground(String params) {

//
        //arr[0], arr.length>1 && arr[1].equals("1") ,arr.length > 2 && arr[2].equals("1")

        if (params == null) {
            params = getKey("bgColor", null);
        }
        if (params == null) return;


        String[] arr = params.split(";");
        String _v = arr[0];
        final boolean textStatusBarWhite = arr.length > 1 && arr[1].equals("1");
        boolean setKey = arr.length > 2 && arr[2].equals("1");

        WebView wv = findViewById(R.id.wv);

        if (setKey) {
            setKey("bgColor", params);
        }
        try {
            final int color = Color.parseColor(_v);
            if (_v != null) {

                wv.setBackgroundColor(color);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Stuff that updates the UI
                        Window w = getWindow();
                        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        w.setStatusBarColor(color);

                        View v = w.getDecorView();


                        if (textStatusBarWhite)
                            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        else
                            v.setSystemUiVisibility(0);
                    }
                });
            }
        } catch (Exception ex) {
            Log.i("setBackground", ex.getMessage());
        }
    }

    public void changeNavigationColor(String params) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                getWindow().setNavigationBarColor(Color.parseColor(params));
            }
        });
    }

    public void changeStatusBarColor(String params) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (params.contentEquals("light")) {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    );
                    getWindow().setStatusBarColor(Color.TRANSPARENT);
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    );
                    getWindow().setStatusBarColor(Color.GRAY);
                }
            }
        });
    }

    public void wvVisibility(final boolean VISIBLE) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wv.setVisibility(VISIBLE ? View.VISIBLE : View.INVISIBLE);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);


        //Kiểm tra sử lý bởi app21
        if (app21.onActivityResult(requestCode, resultCode, intent, this)) return;


        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return;
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }

    public String getKey(String keyName, String df) {
        String name = this.getPackageName();
        SharedPreferences sharedPref = getSharedPreferences("app", Context.MODE_PRIVATE);
        return sharedPref.getString(keyName, df);
    }

    public SharedPreferences getShared(String shareName) {
        return getSharedPreferences(shareName, Context.MODE_PRIVATE);
    }

    public void setKey(String keyName, String value) {

        SharedPreferences sharedPref = getSharedPreferences("app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyName, value);
        editor.commit();
    }


    void JavaScript(String fnName) {
        wv.loadUrl("javascript:" + fnName + "();");
    }

    void DoJS(String cmd, String _value) {
        String script = "app_response('" + cmd + "','" + _value + "')";
        wv.evaluateJavascript(script, null);
    }

    public void evalJs(final String script) {
        wv.post(new Runnable() {
            @Override
            public void run() {
                wv.evaluateJavascript(script, null);
            }
        });
    }

    public static int dpToPx(int dp) {
        return (int) (dp / Resources.getSystem().getDisplayMetrics().density);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = dpToPx(getResources().getDimensionPixelSize(resourceId));
        }
        return result;
    }

    public int getNavigationBarHeight() {
        Context context = this;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return dpToPx(resources.getDimensionPixelSize(resourceId));
        }
        return 0;
    }

    private Bitmap getBitmapFromAsset(String strName) throws IOException {
        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open(strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
    }

    @SuppressLint({"ClickableViewAccessibility", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //go
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (!isTaskRoot() && (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) || getIntent().hasCategory(Intent.CATEGORY_INFO))
                && Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            // Ẩn để gọi động
            //  ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }

        setContentView(R.layout.activity_main);

        // Get Token Key
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        String name = getPackageName();
                        SharedPreferences sharedPref = getSharedPreferences(name, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("FirebaseNotiToken", token);
                        editor.commit();
                    }
                });

        //load

        wv = (WebView) this.findViewById(R.id.wv);
        ANDROID = new ANDROID(this);
        wv.setBackgroundColor(Color.TRANSPARENT);
        //Luôn để mầu trắng
        //setBackground(null);

        wv.addJavascriptInterface(ANDROID, "ANDROID");

        WebSettings setting = wv.getSettings();
        //enable all

        setting.setAllowContentAccess(true);
        setting.setAllowFileAccess(true);

        setting.setAllowUniversalAccessFromFileURLs(true);
        //setting.setBlockNetworkImage(true);
        //setting.setBlockNetworkLoads(true);

        setting.setDatabaseEnabled(true);
        setting.setDisplayZoomControls(true);


        setting.setDomStorageEnabled(true);
        setting.setJavaScriptCanOpenWindowsAutomatically(true);
        setting.setJavaScriptEnabled(true);
        setting.setLightTouchEnabled(true);
        setting.setLoadWithOverviewMode(true);
        setting.setLoadsImagesAutomatically(true);
        setting.setMediaPlaybackRequiresUserGesture(true);
        //setting.setSafeBrowsingEnabled(true);
        setting.setSaveFormData(true);
        setting.setSavePassword(true);

        setting.setAllowFileAccessFromFileURLs(true);
        //setting.setAppCacheEnabled(true);
        //setting.setBlockNetworkLoads(true);
        //setting.setBlockNetworkLoads(true);
        // setting.setBlockNetworkImage(true);
        setting.setDisplayZoomControls(false);
        setting.setUseWideViewPort(true);
        setting.setBuiltInZoomControls(false);
        // setting.setBlockNetworkImage(true);

        setting.setCacheMode(WebSettings.LOAD_DEFAULT);
        setting.setSupportMultipleWindows(false);

        if (Build.VERSION.SDK_INT >= 21) {
            //WebSettings.setMixedContentMode(0);
            wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 19) {
            wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT < 19) {
            wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        //Mỗi một app có 1 domain riêng

        String domain = getString(R.string.app_domain);
        @SuppressLint("ResourceType")
        String color = getString(R.color.colorPrimary);
        String html = "";

        html = getAssetString("embed21.html");

        initWebView();

        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        Gson gson = new Gson();

        String jsonExtras = extras == null ? "{}" : gson.toJson(mapBundle(extras));
        html = html.replace("<body>", "<body><script> var ANDROID_EXTRAS =" + jsonExtras + "; document.documentElement.style.setProperty('--f7-safe-area-top', '" + getStatusBarHeight() + "px'); document.documentElement.style.setProperty('--f7-safe-area-bottom', '" + getNavigationBarHeight() + "px')</script>");

        //DEV Remove
        wv.loadDataWithBaseURL(domain, html + "", "text/html", "utf-8", "");
        //DEV Remove

        //DEV Open
        // Android phải chạy qua Ngrok, Không thể chạy qua Local

        //wv.loadUrl("https://0b75-42-113-60-107.ngrok-free.app/");
        //wv.setVisibility(View.VISIBLE);

        //DEV Open

        getNotificationPermission();
    }

    public void getNotificationPermission(){
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        202);
            }
        }catch (Exception e){
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras().get("NOTI_ID") != null) {
            Intent start = intent;
            startActivity(start);
            intent.replaceExtras(new Bundle());
            intent.setAction("");
            intent.setData(null);
            finish();
            return;
        }
        if (intent.getStringExtra("NOTI_ID") != null && intent.getStringExtra("click_action") != null) {
            if (!intent.getStringExtra("NOTI_ID").isEmpty() && !intent.getStringExtra("click_action").isEmpty()) {
                Intent start = intent;
                startActivity(start);
                intent.replaceExtras(new Bundle());
                intent.setAction("");
                intent.setData(null);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        evalJs("AppResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        evalJs("AppPause()");
    }

    Record21 record21 = new Record21();

    @Override
    protected void onStop() {
        super.onStop();
        record21.release();
        EventBus.getDefault().unregister(this);
    }

    String getAssetString(String name) {
        String str = "";
        try {
            InputStream input = getAssets().open(name);
            // myData.txt can't be more than 2 gigs.
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            str = new String(buffer);
        } catch (IOException e) {

        }
        return str;
    }

    Map<String, Object> mapBundle(Bundle bundle) {


        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (bundle == null) return map;
            for (String key : bundle.keySet()) {
                map.put(key, bundle.get(key));
            }
        } catch (Exception e) {
            //
        }
        return map;
    }

    public Map<String, Object> getBundle() {
        try {
            return mapBundle(getIntent().getExtras());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {


        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int w = wv.getWidth();
        int h = wv.getHeight();
        // do your stuff here... the below call will make sure the touch also goes to the webview.
        float x = event.getX();
        float y = event.getY();

        float pcw = x / w;
        float pch = (y - statusBarHeight) / h;

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                dx = x - mPreviousX;
                dy = y - mPreviousY;

        }

        //wv.loadUrl("javascript:ANDROID_TOUCH({dx:"+dx+",dy:"+dy+",x:"+x+",y:"+y+",mPreviousX:"+mPreviousX+",mPreviousY:"+mPreviousY+" ,action:"+action+" });");
        DoJS("TOUCH", "{\"dx\":" + dx + ",\"dy\":" + dy + ",\"x\":" + x + ",\"y\":" + y + ",\"mPreviousX\":" + mPreviousX + ",\"mPreviousY\":" + mPreviousY + ",\"action\":" + action + ",\"pcw\": " + pcw + ",\"pch\":" + pch + "}");
        // SharedPreferences sharedPref = getSharedPreferences("app", Context.MODE_PRIVATE);


        mPreviousX = x;
        mPreviousY = y;


        ANDROID.BackReset();
        return super.dispatchTouchEvent(event);
    }


    //Requesting permission upload
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openFileExplorer();
            return;
        }


        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        //for 21
        if (requestCode >= pcm.minId)
            pcm.RequestPermissionsResult(requestCode, permissions, grantResults);
        //code ccu
        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileExplorer();
                //Displaying a toast
//                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
//                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }

    }

    public class CurrentLocation {
        public double longitude;
        public double latitude;

        public CurrentLocation(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }


    @SuppressLint("MissingPermission")
    public void getCurrentLocation(Result result) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        result.success = true;
                        result.data = new CurrentLocation(location.getLongitude(), location.getLatitude());
                    } else {
                        result.data = "";
                        result.success = false;
                        result.error = "Vui lòng bật định vị";
                    }
                    app21.App21Result(result);
                });
    }

    public void getWiFiInfo(Result result) {
        Map<String, Object> wifiInfo = WiFiManager.getWiFiInfo(getApplicationContext());
        Result rs = result.copy();
        rs.success = true;
        rs.data = wifiInfo;
        app21.App21Result(rs);
    }

    public void openFileExplorer() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FILECHOOSER_RESULTCODE);
    }


    public void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void initWebView() {
        // Dev Hidden
        //wv.setWebViewClient(new Callback());
        // Dev Hidden

        // Dev Open
        wv.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                wv.evaluateJavascript("javascript:document.documentElement.style.setProperty('--f7-safe-area-top', '"+ getStatusBarHeight() +"px');", null);
                wv.evaluateJavascript("javascript:document.documentElement.style.setProperty('--f7-safe-area-bottom', '"+ getNavigationBarHeight() +"px');", null);
            }
        });

        // Dev Open

        wv.setWebChromeClient(new WebChromeClient() {
            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }

            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FCR);
            }

            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FCR);
            }

            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (mUMA != null) {
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCM);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);
                return true;
            }
        });

        //only on debug
        //https://developers.google.com/web/tools/chrome-devtools/remote-debugging/webviews
        // chrome://inspect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == 202) return;
        else if (requestCode == LOCATION_PERMISSION_CODE) {
            getCurrentLocation(resultLocation);
        } else if(requestCode == WIFI_INFO_CODE){
            getWiFiInfo(resultLocation);
        } else {
            QRCodeFragment qrCodeFragment = QRCodeFragment.newInstance(new QRCodeFragment.QRCodeResult() {
                @Override
                public void onQRCode(String code) {
                    new Runnable() {
                        @Override
                        public void run() {
                            resultQrCode.success = true;
                            resultQrCode.data = code;
                            app21.App21Result(resultQrCode);
                        }
                    }.run();
                }
            });

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.layout, qrCodeFragment)
                    .addToBackStack("QRCodeFragment")
                    .commit();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(requestCode == WIFI_INFO_CODE){
            Result rs = resultLocation.copy();
            rs.success = true;
            rs.data = "It looks like you've declined location permission. Please grant permission in App Settings to use this feature.";
            app21.App21Result(rs);
        }
    }

    public class Callback extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }
    }

    // Create an image file
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            switch (keyCode) {
//                case KeyEvent.KEYCODE_BACK:
//                    if (wv.canGoBack()) {
//                        wv.goBack();
//                    } else {
//                        finish();
//                    }
//                    return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    //End Requesting permission upload

    @Override
    public void onBackPressed() {
        String script = "ToBackBrowser()";
        evalJs(script);
    }

    void Subscribe(String topics) {
        if (topics == null || topics == "") return;
        setKey("subscribe", topics);
        String[] a1 = topics.split(",");
        for (int i = 0; i < a1.length; i++) {
            FirebaseMessaging.getInstance().subscribeToTopic(a1[i]);
        }
    }

    void UnSubscribe() {
        String topics = getKey("subscribe", "");
        if (topics == null || topics == "") return;
        String[] a1 = topics.split(",");
        for (int i = 0; i < a1.length; i++) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(a1[i]);
        }
    }

    public void checkPermission(String PermissionName, Callback21 callback21) {

        if (ContextCompat.checkSelfPermission(this, PermissionName)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            callback21.no();


        } else {
            callback21.ok();
        }
    }


    PermissionCallbackManager pcm = new PermissionCallbackManager();

    //https://developer.android.com/training/permissions/requesting
    public void requirePermission(String Permission, Callback21 callback21) {

        int requestCode = pcm.put(Permission, callback21);

        if (requestCode == pcm.exist) {
            //Đang chờ phản hồi
            return;
        }

        ActivityCompat.requestPermissions(this,
                Permission.split(","),//new String[]{Permission}
                requestCode);


    }


    class PermissionCallbackManager {
        List<PermissionCallback> lst;
        final int minId = 10000;
        final int exist = -999;
        private int _id = 0;

        public int put(String Permission, Callback21 callback21) {
            if (lst == null) lst = new ArrayList<PermissionCallback>();

            for (PermissionCallback p : lst) {
                if (p.Permission == Permission) {
                    p.callback21 = callback21;
                    return exist;
                }

            }

            PermissionCallback p = new PermissionCallback();
            p.Permission = Permission;
            p.callback21 = callback21;

            if (_id == 0) _id = minId;
            p.id = _id++;
            lst.add(p);
            return p.id;
        }

        public void RequestPermissionsResult(int requestCode,
                                             String[] permissions, int[] grantResults) {

            if (lst == null) lst = new ArrayList();
            PermissionCallback p = null;
            for (PermissionCallback _p : lst) {
                if (_p.id == requestCode) {
                    p = _p;
                    break;
                }
            }
            if (p == null) return;
            ;


            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                p.callback21.ok();
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                p.callback21.no();
            }
            lst.remove(p);
        }

        class PermissionCallback {
            public String Permission;
            /* Runnable grantted;
             Runnable denied;*/
            public Callback21 callback21;
            int id;
        }
    }

    public class ANDROID {
        Context mContext;


        boolean IsNoBack = false;
        boolean IsPrepend = false;

        /**
         * Instantiate the interface and set the context
         */
        ANDROID(Context c) {
            mContext = c;

        }


        @JavascriptInterface
        /*
        ở client cần 1 setInterval để luôn xác định giá trị IsNoBack
        * */
        public void NoBack(boolean _IsNoBack) {
            IsNoBack = _IsNoBack;
            if (_IsNoBack) IsPrepend = false;
        }

        public void BackReset() {
            IsPrepend = false;
            IsNoBack = false;
        }


        @JavascriptInterface
        public void OnNoBack() {

        }

        @JavascriptInterface
        public void Do(String cmd, String value) {

            String v = value;
            String[] segs = (v).split(":");

            String key = "";
            String va = "";
            if (segs.length > 0) key = segs[0];
            if (segs.length > 1) va = segs[1];
            switch (cmd) {
                case "setkey":
                    setKey(key, va);
                    break;
                case "getkey":
                    DoJS(cmd, getKey(key, ""));
                    break;
                case "subscribe":
                    Subscribe(value);
                    break;
                case "unsubscribe":
                    UnSubscribe();
                    break;
                case "call":
                    app21.call(value);
                    break;

            }
        }


        @JavascriptInterface
        public String toString() {
            return "This is ANDROID";
        }

    }

    public void shareImages(List<String> images, String text){
        new Thread(new DownloadImagesRunnable(images, text, DownloadType.share, new DownloadImagesCallback() {
            @Override
            public void onSuccess() {
                //
            }
        })).start();
    }

    public void saveImages(List<String> images,DownloadImagesCallback callback){
        new Thread(new DownloadImagesRunnable(images,"",DownloadType.save, callback )).start();
    }


    private Bitmap downloadImage(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.e("Error", "Failed to download image: " + e.getMessage());
            return null;
        }
    }

    private void shareImgs(List<Bitmap> images, String text, DownloadImagesCallback callback) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (Bitmap image : images) {
            // Lưu hình ảnh vào bộ nhớ tạm để chia sẻ
            File file = saveImageToExternalStorage(image);
            if (file != null) {
                uris.add(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() +".provider", file));
            }
        }
        Log.e("FILES: ", uris.toString());

        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("image/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        this.startActivity(Intent.createChooser(shareIntent, "Share"));
        callback.onSuccess();
    }

    private File saveImageToExternalStorage(Bitmap image) {
        File directory = new File(getCacheDir(), "images");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, "image_" + System.currentTimeMillis() + ".jpg");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return file;
        } catch (Exception e) {
            Log.e("Error", "Failed to save image: " + e.getMessage());
            return null;
        }
    }

    private void saveImagesToGallery(List<Bitmap> images, DownloadImagesCallback callback) {
        for (Bitmap image : images) {
            saveImageToGallery(image);
        }
        callback.onSuccess();

    }

    private void saveImageToGallery(Bitmap image) {
        // Lưu hình ảnh vào thư mục ảnh của thiết bị
        String savedImagePath = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                image,
                "Image_" + System.currentTimeMillis(),
                "Image downloaded from network"
        );
        Log.d("SUCCESS", "Image saved to: " + savedImagePath);

    }


    private void showDefaultDialog(Context context,String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    private class DownloadImagesRunnable implements Runnable {
        private List<String> imageUrls;
        private String text;
        private DownloadType type;
        private DownloadImagesCallback callback;

        public DownloadImagesRunnable(List<String> imageUrls, String text ,DownloadType type, DownloadImagesCallback callback) {
            this.imageUrls = imageUrls;
            this.type = type;
            this.text = text;
            this.callback = callback;
        }

        @Override
        public void run() {
             List<Bitmap> images = new ArrayList<>();
            for (String url : imageUrls) {
                try {
                    // Tải xuống hình ảnh từ URL
                    Bitmap image = downloadImage(url);
                    if (image != null) {
                        synchronized (images) {
                            images.add(image);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Error", "Failed to download image: " + e.getMessage());
                }
            }
            if(type == DownloadType.share){
                shareImgs(images,text,callback);
            } else if (type == DownloadType.save) {
                saveImagesToGallery(images,callback);
            }

        }

    }

    public interface DownloadImagesCallback {
        void onSuccess();
    }

}

enum DownloadType { share, save }

