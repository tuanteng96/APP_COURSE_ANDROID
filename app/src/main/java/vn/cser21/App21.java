package vn.cser21;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.FileProvider;

import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import android.view.WindowManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;
import vn.cser21.QRCodeFragment;


public class App21 {
    public Context mContext;

    public App21(Context _mContext) {
        mContext = _mContext;

    }

    public void App21Result(Result result) {

        try {
           /* JSONObject json = new JSONObject();
            json.put("success", result.success);
            json.put("error", result.error);
            json.put("data", result.data);
            json.put("sub_cmd_id", result.sub_cmd_id);
            json.put("sub_cmd", result.sub_cmd);*/

            Gson gson = new Gson();

            String s = gson.toJson(result);
            s = "BASE64:" + DownloadFilesTask.strBase64(s); //Convert to 64 de trach ky tu dc biet
//            logLargeString(s);
            String script = "App21Result('" + s + "')";
            // wv.evaluateJavascript(script, null);

            MainActivity m = (MainActivity) mContext;
            m.evalJs(script);


        } catch (Throwable tx) {
            Log.wtf("e", "loi", tx);
        }
    }

    private void logLargeString(String content) {
        if (content.length() > 3000) {
            Log.d("mi", content.substring(0, 3000));
            logLargeString(content.substring(3000));
        } else {
            Log.d("mi", content);
        }
    }
    /**
     * @param json {sub_cmd, params, sub_cmd_id}
     */
    public void call(String json) {
        Result rs = new Result();
        rs.sub_cmd = "";
        rs.sub_cmd_id = 0;
        rs.params = "";
        try {
            JSONObject c = new JSONObject(json);
            rs.sub_cmd = c.getString("sub_cmd");
            rs.sub_cmd_id = c.getInt("sub_cmd_id");

            if (c.has("params"))
                rs.params = c.getString("params");


            Method method = App21.class.getDeclaredMethod(rs.sub_cmd, Result.class);


            if (method.equals(null)) throw new Throwable("NO_" + rs.sub_cmd);
            method.setAccessible(true);
            method.invoke(this, rs);

        } catch (Throwable tx) {
            //nothing to do

            rs.success = false;
            rs.error = tx.toString();
            rs.data = "";
            App21Result(rs);
        }
    }

    void _PERMISSION(final Result result, final String PermissionName, final Runnable granted) {
        final MainActivity m = (MainActivity) mContext;

        m.checkPermission(PermissionName, new Callback21() {
            @Override
            public void ok() {
                granted.run();
            }

            @Override
            public void no() {
                m.requirePermission(PermissionName, new Callback21() {
                    @Override
                    public void ok() {
                        granted.run();
                    }

                    @Override
                    public void no() {
                        Result rs = result.copy();
                        rs.success = false;
                        rs.error = "PERMISSION/" + PermissionName + "/DENIED";
                        App21Result(rs);
                    }
                });
            }
        });


    }

    ActivityResultIDManager activityResultIDManager = new ActivityResultIDManager();
    boolean IsMe = false;

    File save(Bitmap bmp, String filename) {
        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File mypath = new File(directory, filename);


        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            bmp.compress(filename.endsWith(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e("SAVE_IMAGE", e.getMessage(), e);
        }
        return mypath;
    }

    File newFile(String filename) {
        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File mypath = new File(directory, filename);


        return mypath;
    }

    String bitmapToBase64(Bitmap bitmap) {


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();


        return Base64.encodeToString(byteArray, Base64.URL_SAFE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    Map<String, String> mapParams(String params) {


        Map<String, String> map = new HashMap<String, String>();
        try {
            if (params == null || "".equals(params)) return map;
            for (String seg : params.split(",")) {
                String[] arr = seg.split(":");
                map.putIfAbsent(arr[0], arr.length > 1 ? arr[1] : null);
            }
        } catch (Exception e) {
            //
        }
        return map;
    }

    Loction21 loction21 = null;

    String now() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }


    void REBOOT(Result result) {

        int miliSecond = Integer.parseInt(result.params);

        Result rs = result.copy();
        rs.success = true;
        rs.data = "REBOOT AFTER " + miliSecond + "(MS)";

        App21Result(rs);


        Async21.run(miliSecond, new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
                System.exit(0);
            }
        });
    }

    void BACKGROUND(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        MainActivity m = (MainActivity) mContext;
        m.setBackground(rs.params);
    }

    void STATUS_BAR_COLOR(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        Log.e("Param", rs.params);

        MainActivity m = (MainActivity) mContext;
        m.changeStatusBarColor(rs.params);
    }

    void SHARE_SOCIAL(final Result result) {
        Result rs = result.copy();
        rs.success = false;
        Log.e("Param", rs.params);
        try {
            JSONObject jsonObject = new JSONObject(rs.params);
            JSONArray jsonArray =  jsonObject.getJSONArray("Images");
            List<String> images =new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String value = jsonArray.getString(i);
                images.add(value);
            }
            String text = jsonObject.getString("Content");
            Log.e("Images", images.toString());
            MainActivity m = (MainActivity) mContext;
            m.shareImages(images, text);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        App21Result(rs);
    }

    void DOWNLOAD_FILES(final Result result) {
        Result rs = result.copy();
        rs.success = false;
        Log.e("Param", rs.params);
        try {
            JSONArray jsonArray = new JSONArray(rs.params);
            List<String> images =new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String value = jsonArray.getString(i);
                images.add(value);
            }
            Log.e("Images", images.toString());
            MainActivity m = (MainActivity) mContext;
            m.saveImages(images, new MainActivity.DownloadImagesCallback(){
                @Override
                public void onSuccess() {
                    rs.success = true;
                    rs.params = "";
                    rs.data = "Save image successfully";
                    App21Result(rs);
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
       // App21Result(rs);
    }

    void NAVIGATION_COLOR(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        rs.data = "";
        Log.e("Param", rs.params);
        App21Result(rs);
    }

    void GET_FILES_LOCAL(final Result result) {
        Result rs = result.copy();

        try {
            JSONArray jsonArray = new JSONArray(rs.params);
            List<String> files =new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String value = jsonArray.getString(i);
                files.add(value);
            }
            final DataCacheManager dataCacheManager = new DataCacheManager(mContext);
           final List<String> urls = dataCacheManager.getFileFromUnZipedFolder(files);
           rs.success = true;
//            Gson gson = new Gson();
//            final String data = gson.toJson(urls);
            rs.data = urls;
           Log.d("dsd",urls.toString());
           App21Result(rs);
        } catch (JSONException e) {
            rs.success = false;
            rs.data = "Error";
            App21Result(rs);
            throw new RuntimeException(e);
        }
        Log.e("Param", rs.params);  // params sẽ là 1 mảng các tên file ở local nhé a

    }

    void GET_FILES_PATH(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        Log.e("Param", rs.params);
        App21Result(rs);
    }

    void DOWNLOAD_ZIP_SERVER(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        try {
            JSONArray jsonArray = new JSONArray(rs.params);
            List<String> files =new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String value = jsonArray.getString(i);
                files.add(value);
            }
            final DataCacheManager dataCacheManager = new DataCacheManager(mContext);
            dataCacheManager.downloadListFileAndUnZip(files, new DataCacheManager.DataCacheManagerCallback() {
                @Override
                public void onSuccess(String data) {

                }

                @Override
                public void onError(String data) {

                }

                @Override
                public void onCompleted() {
                    rs.success = true;
                    rs.data = "Success";
                    App21Result(rs);
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Log.e("Param", rs.params); // params sẽ là 1 mảng các file zip từ server cần download và giải nén
        App21Result(rs);
    }

    void GET_JSON_LOCAL(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        try {
            JSONArray jsonArray = new JSONArray(rs.params);
            List<String> files =new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String value = jsonArray.getString(i);
                files.add(value);
            }
            final DataCacheManager dataCacheManager = new DataCacheManager(mContext);
            final List<String> datas = dataCacheManager.readDataJSONFromUnZippedFolder(files);
           List<Map<String, Object>> jsonObjects = new ArrayList<>();
            for (String d: datas
                 ) {
                Gson gson = new Gson();
                Type type = new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType();
                jsonObjects.add( gson.fromJson(d, type));
            }

            rs.success = true;
            rs.data = jsonObjects;
            Log.d("dsd",datas.toString());
            App21Result(rs);
        } catch (JSONException e) {
            rs.success = false;
            rs.data = "Error";
            App21Result(rs);
            throw new RuntimeException(e);
        }
        Log.e("Param", rs.params); // params sẽ là 1 mảng các đường dẫn file json

    }


    void DELETE_ALL_LOCAL(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        final DataCacheManager dataCacheManager = new DataCacheManager(mContext);
        dataCacheManager.deleteAll();
        Log.e("Param", rs.params);
        App21Result(rs);
    }

    void SAVE_FILES_LOCAL(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        Log.e("Param", rs.params); // params sẽ là 1 mảng các đường dẫn file cần lưu
        App21Result(rs);
    }

    void INIT_UNZIP_LOCAL(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        final DataCacheManager dataCacheManager = new DataCacheManager(mContext);
        dataCacheManager.initialUnzip(rs.params, new DataCacheManager.DataCacheManagerCallback() {
            @Override
            public void onSuccess(String data) {
                rs.success = true;
                rs.data = "Success";
                App21Result(rs);
            }

            @Override
            public void onError(String data) {
                rs.success = false;
                rs.data = "Error";
                App21Result(rs);
            }

            @Override
            public void onCompleted() {

            }
        });
        Log.e("Param", rs.params); // params sẽ là đường dẫn file zip cần giải nén
    }

    void SET_BADGE(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        App21Result(rs);
        NotificationManager nMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    void GET_LOCATION(final Result result) {
        if (mContext instanceof MainActivity) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            if (EasyPermissions.hasPermissions(((MainActivity) mContext).getActivity(), perms)) {
                ((MainActivity) mContext).getCurrentLocation(result);
            } else {
                ((MainActivity) mContext).showRequestPermissionLocation(result, perms,MainActivity.LOCATION_PERMISSION_CODE);
            }
        }
    }

    void GET_NETWORK_TYPE(final Result result) {
        if (mContext instanceof MainActivity) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            if (EasyPermissions.hasPermissions(((MainActivity) mContext).getActivity(), perms)) {
                Map<String, Object> wifiInfo = WiFiManager.getWiFiInfo(mContext);
                Result rs = result.copy();
                rs.success = true;
                rs.data = wifiInfo;
                App21Result(rs);
            } else {
                ((MainActivity) mContext).showRequestPermissionLocation(result, perms, MainActivity.WIFI_INFO_CODE);
            }
        }

    }

    void OPEN_QRCODE(final Result result) {
        if (mContext instanceof MainActivity) {
            String[] perms = {Manifest.permission.CAMERA};
            if (EasyPermissions.hasPermissions(((MainActivity) mContext).getActivity(), perms)) {
                QRCodeFragment qrCodeFragment = QRCodeFragment.newInstance(new QRCodeFragment.QRCodeResult() {
                    @Override
                    public void onQRCode(String code) {
                        new Runnable() {
                            @Override
                            public void run() {
                                Result rs = result.copy();
                                rs.success = true;
                                rs.data = code;
                                App21Result(rs);
                            }
                        }.run();
                    }
                });
                FragmentTransaction ft = ((MainActivity) mContext).getSupportFragmentManager().beginTransaction();
                ft.add(R.id.layout, qrCodeFragment)
                        .addToBackStack("QRCodeFragment")
                        .commit();
            } else {
                ((MainActivity) mContext).showQrCodeScreen(result);
            }
        }
    }

    //TuanDev Finish App
    void FINISH_ACTIVITY(final Result result) {
        ((Activity) mContext).finish();
    }

    void BASE64(final Result result) {
        final App21 t = this;
        new Runnable() {
            @Override
            public void run() {
                Result rs = result.copy();
                rs.success = true;
                App21Result(rs);
                Base64Require rq = new Gson().fromJson(rs.params, Base64Require.class);
                MainActivity m = (MainActivity) mContext;
                DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
                downloadFilesTask.app21 = t;
                String base64 = downloadFilesTask.toBase64(rq.path);
                m.evalJs("" + rq.callback + "('" + base64 + "')");
            }
        }.run();
    }

    void _CAMERA(final Result result) {
        final String CAMERA = Manifest.permission.CAMERA;
        PackageManager packageManager = mContext.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            // this device has a camera
            _PERMISSION(result.copy(), CAMERA, new Runnable() {
                @Override
                public void run() {

                    Async21.run(0, new Runnable() {
                        @Override
                        public void run() {
                            final MainActivity m = (MainActivity) mContext;

                            Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            if (cInt.resolveActivity(m.getPackageManager()) == null) {
                                Result rs = result.copy();
                                rs.success = false;
                                rs.error = "resolveActivity()==null";
                                App21Result(rs);
                                return;
                            }


                            IsMe = true;
                            m.startActivityForResult(cInt, activityResultIDManager.put(new ActivityResultID() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void run() {
                                    if (this.resultCode == Activity.RESULT_OK) {
                                        Result rs = result.copy();
                                        try {
                                            //Data dataImage = this.intent.getExtras().get("data");

                                            Bitmap bp = (Bitmap) this.intent.getExtras().get("data");

                                            //imgCapture.setImageBitmap(bp);


                                            Map<String, String> map = mapParams(rs.params);
                                            String ext = map.containsKey("ext") ? map.get("ext") : "png";
                                            String pref = map.containsKey("pref") ? map.get("pref") : "IMG";
                                            rs.success = true;
                                            rs.data = "OK";

                                            File f = save(bp, pref + now() + "." + ext);
                                            rs.data = "file://" + f.getAbsolutePath();
                                            App21Result(rs);
                                        } catch (NullPointerException n) {
                                            rs.success = false;
                                            rs.error = n.getLocalizedMessage();
                                        }
                                    } else if (resultCode == Activity.RESULT_CANCELED) {
                                        Result rs = result.copy();
                                        rs.success = false;
                                        rs.error = "resultCode=" + resultCode;
                                        App21Result(rs);
                                    }
                                }
                            }), cInt.getExtras());
                        }
                    });
                }
            });


        } else {
            // no camera on this device
            Result rs = result.copy();

            rs.success = false;
            rs.error = "no camera on this device";
            App21Result(rs);
        }
    }

    private File createImageFile(String suffix) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                suffix,         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents

        return image;
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    Bitmap fromFile(File file) {

        String filePath = file.getPath();
        return BitmapFactory.decodeFile(filePath);
    }

    void CAMERA(final Result result) {
        final String CAMERA = Manifest.permission.CAMERA;
        PackageManager packageManager = mContext.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            // this device has a camera
            _PERMISSION(result.copy(), CAMERA, new Runnable() {
                @Override
                public void run() {

                    Async21.run(0, new Runnable() {
                        @Override
                        public void run() {
                            final MainActivity m = (MainActivity) mContext;

                            Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            if (cInt.resolveActivity(m.getPackageManager()) == null) {
                                Result rs = result.copy();
                                rs.success = false;
                                rs.error = "resolveActivity()==null";
                                App21Result(rs);
                                return;
                            }
                            final CameraInfo cameraInfo = new Gson().fromJson(result.params, CameraInfo.class);

                            //final File photoFile = newFile(pref + now() + "." + ext);

                            File _photoFile = null;
                            try {
                                _photoFile = createImageFile("." + cameraInfo.ext);
                            } catch (IOException ex) {
                                // Error occurred while creating the File

                            }
                            final File photoFile = _photoFile;
                            if (photoFile != null) {
                                try {

                                    Uri photoURI = FileProvider.getUriForFile(mContext,
                                            BuildConfig.APPLICATION_ID + ".provider",
                                            photoFile);
                                    //mPhotoFile = photoFile;
                                    cInt.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            IsMe = true;
                            m.startActivityForResult(cInt, activityResultIDManager.put(new ActivityResultID() {
                                @Override
                                public void run() {
                                    if (this.resultCode == Activity.RESULT_OK) {
                                        Result rs = result.copy();
                                        try {
                                            // Bitmap bp = (Bitmap) this.intent.getExtras().get("data");

                                            //imgCapture.setImageBitmap(bp);


                                            // File f = newFile(pref + now() + "." + ext);
                                            try {
                                                //copy(photoFile, f);

                                                int maxW = cameraInfo.maxwidth == 0 ? 1000 : cameraInfo.maxwidth;
                                                int maxh = cameraInfo.maxheight == 0 ? 1000 : cameraInfo.maxheight;

                                                //f.createNewFile();
                                                Bitmap source = fromFile(photoFile);
                                                Bitmap target = ImageUtil.scaleDown(source, maxW, maxh, false);
                                                File f = save(target, cameraInfo.pref + now() + "." + cameraInfo.ext);
                                                rs.success = true;
                                                rs.data = "file://" + f.getAbsolutePath();
                                                photoFile.deleteOnExit();

                                            } catch (Exception e) {
                                                rs.error = e.getLocalizedMessage();
                                                rs.success = false;
                                            }
                                            // File f = save(bp, pref + now() + "." + ext);

                                            App21Result(rs);
                                        } catch (NullPointerException n) {
                                            rs.success = false;
                                            rs.error = n.getLocalizedMessage();
                                        }
                                    } else if (resultCode == Activity.RESULT_CANCELED) {
                                        Result rs = result.copy();
                                        rs.success = false;
                                        rs.error = "resultCode=" + resultCode;
                                        App21Result(rs);
                                    }
                                }
                            }));
                        }
                    });
                }
            });


        } else {
            // no camera on this device
            Result rs = result.copy();

            rs.success = false;
            rs.error = "no camera on this device";
            App21Result(rs);
        }
    }

    void FILE(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        rs.data = "file OK";

        App21Result(rs);
    }

    void DELETE_FILE(final Result result) {
        Result rs = result.copy();

        try {
            String pre = "file://";
            String path = result.params;
            if (path != null && !"".equals(path)) {
                //path = path.toLowerCase();
                if (path.startsWith(pre)) path = path.replace(pre, "");
                File f = new File(path);
                if (!f.exists()) throw new Exception("FILE NOT EXIST");
                f.delete();
            }
            rs.success = true;
            rs.data = "deleted";
        } catch (Exception e) {
            rs.success = false;
            rs.error = e.getMessage();
        }
        App21Result(rs);
    }

    void REQUIRE_PERMISSIONS(final Result result) {
        final MainActivity m = (MainActivity) mContext;
        m.requirePermission(result.params, new Callback21() {
            @Override
            public void ok() {
                Result rs = result.copy();
                rs.success = true;
                App21Result(rs);
                ;
            }

            @Override
            public void no() {
                Result rs = result.copy();
                rs.success = false;
                App21Result(rs);
                ;
            }
        });
    }

    void LOCATION(final Result result) {
        final MainActivity m = (MainActivity) mContext;
        final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
        final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
        final App21 t = this;
        _PERMISSION(result, COARSE_LOCATION + "," + FINE_LOCATION, new Runnable() {
            @Override
            public void run() {

                if (loction21 == null) {
                    loction21 = new Loction21(mContext);
                    //loction21.mContext = mContext;
                }
                if (loction21.isLocationEnabled()) {
                    Result rs = result.copy();

                    loction21.app21 = t;
                    loction21.sourceResult = rs;
                    loction21.run(rs.params);

                } else {
                    Result rs = result.copy();
                    rs.success = false;
                    rs.error = "Turn off location";
                    App21Result(rs);
                    ;
                }
            }
        });
    }

    void DOWNLOAD(final Result result) {
        DownloadFilesTask downloadFilesTask = new DownloadFilesTask() {
            @Override
            protected void onPostExecute(String localPath) {
                Result rs = result.copy();
                rs.success = true;
                rs.data = localPath;
                app21.App21Result(rs);
            }
        };
        downloadFilesTask.app21 = this;
        downloadFilesTask.execute(result.params);
    }

    void GET_DOWNLOADED(final Result result) {
        DownloadFilesTask downloadFilesTask = new DownloadFilesTask() {
        };
        downloadFilesTask.app21 = this;
        Result rs = result.copy();
        rs.data = downloadFilesTask.getlist();
        rs.success = true;
        App21Result(rs);
        ;
    }

    void CLEAR_DOWNLOAD(final Result result) {
        DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
        downloadFilesTask.app21 = this;
        downloadFilesTask.clear(result.params, new Runnable() {
            @Override
            public void run() {
                Result rs = result.copy();
                rs.success = true;
                rs.data = "";
                App21Result(rs);
            }
        });
    }

    void POST_TO_SERVER(final Result result) {
        PostFileToServer postFileToServer = new PostFileToServer() {
            @Override
            protected void onPostExecute(Result result) {
                App21Result(result);
            }
        };
        postFileToServer.app21 = this;
        postFileToServer.execute(result);
    }

    void NOTI(final Result result) {

        try {
            Gson gson = new Gson();
            Noti21 noti21 = gson.fromJson(result.params, Noti21.class);
            // Noti21 noti21 = new Noti21();

            // noti21.notification = new Notification21();
            // noti21.notification.title = "test";


            final MainActivity m = (MainActivity) mContext;


            SERVER_NOTI.noti(noti21, mContext);

            result.success = true;
            App21Result(result);
        } catch (Exception ex) {
            result.success = false;
            result.error = ex.getMessage();
            App21Result(result);
        }
    }

    void NOTI_DATA(final Result result) {
        result.data = ((MainActivity) mContext).getBundle();
        result.success = true;
        App21Result(result);
    }

    void GET_PHONE(final Result result) {
        final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
        _PERMISSION(result, READ_PHONE_STATE, new Runnable() {
            @Override
            public void run() {
                TelephonyManager tMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                @SuppressLint("MissingPermission")
                String number = tMgr.getLine1Number();
                Result rs = result.copy();
                rs.success = true;
                rs.data = number;
                App21Result(rs);
                ;
            }
        });
    }

    void SEND_SMS(final Result result) {
        final String SEND_SMS = Manifest.permission.SEND_SMS;
        _PERMISSION(result, SEND_SMS, new Runnable() {
            @Override
            public void run() {

                try {

                    SMS sms = new Gson().fromJson(result.params, SMS.class);

                    SmsManager.getDefault().sendTextMessage(sms.number, null, sms.smsText, null, null);
                    Result rs = result.copy();
                    rs.success = true;

                    App21Result(rs);
                    ;
                } catch (Exception ex) {
                    Result rs = result.copy();
                    rs.success = false;
                    rs.error = ex.getMessage();
                    App21Result(rs);
                }

            }
        });
    }

    void ALARM_NOTI(final Result result) {

        final String WAKE_LOCK = Manifest.permission.WAKE_LOCK;

        _PERMISSION(result, WAKE_LOCK, new Runnable() {
            @Override
            public void run() {
                AlarmReceiver21.setConfig(result.params, mContext);
                new AlarmReceiver21().setAlarm(mContext);
                ;
                Result rs = result.copy();
                rs.success = true;
                App21Result(rs);

            }
        });
    }

    void GET_SERVER_NOTI(final Result result) {
        new SERVER_NOTI(mContext).run(result, new Callback21() {
            @Override
            public void ok() {
                result.success = true;
                App21Result(result);
            }

            @Override
            public void no() {
                result.success = false;
                if (this.lastExp != null) result.error = this.lastExp.getMessage();
                App21Result(result);

            }

        });
    }

    void IMAGE_ROTATE(final Result result) {

        ImageUtil imageUtil = new ImageUtil() {
            @Override
            protected void onPostExecute(Result rs) {
                App21Result(rs);
            }
        };
        imageUtil.downloadFilesTask = new DownloadFilesTask();
        imageUtil.downloadFilesTask.app21 = this;
        imageUtil.execute(result);

    }

    void VIBRATOR(final Result result) {

        VibratorInfo vibratorInfo = new Gson().fromJson(result.params, VibratorInfo.class);

        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(vibratorInfo.milliseconds, vibratorInfo.amplitude));
        } else {
            //deprecated in API 26
            v.vibrate(vibratorInfo.milliseconds);
        }
        result.success = true;
        App21Result(result);
    }

    void WV_VISIBLE(final Result result) {

        (new Runnable() {
            @Override
            public void run() {
                Result rs = result.copy();
                rs.success = true;
                MainActivity m = (MainActivity) mContext;
                m.wvVisibility(rs.params.equals("1"));
                App21Result(rs);
            }
        }).run();


    }

    void GET_TEXT(final Result result) {
        Result rs = result.copy();
        try {

            rs.success = true;
            DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
            downloadFilesTask.app21 = this;
            rs.data = downloadFilesTask.GET_TEXT(result.params);

        } catch (Exception ex) {
            rs.success = false;
            rs.error = ex.getLocalizedMessage();
        }
        App21Result(rs);

    }


    void RECORD_AUDIO(final Result result) {

        final App21 t = this;
        try {

            String _RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
            _PERMISSION(result, _RECORD_AUDIO, new Runnable() {
                @Override
                public void run() {
                    MainActivity m = (MainActivity) mContext;
                    final Result rs = result.copy();
                    m.record21.RecordAudio(rs, t);
                }
            });


        } catch (Exception ex) {
            result.success = false;
            result.error = ex.getLocalizedMessage();
            App21Result(result);
        }

    }

    void RECORD_VIDEO(final Result result) {

        final App21 t = this;
        try {

            String _RECORD_VIDEO = Manifest.permission.CAMERA;
            _PERMISSION(result, _RECORD_VIDEO, new Runnable() {
                @Override
                public void run() {
                    Async21.run(0, new Runnable() {
                        @Override
                        public void run() {
                            final MainActivity m = (MainActivity) mContext;


                            Intent cInt = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                            cInt.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                            cInt.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3000);


                            if (cInt.resolveActivity(m.getPackageManager()) == null) {
                                Result rs = result.copy();
                                rs.success = false;
                                rs.error = "resolveActivity()==null";
                                App21Result(rs);
                                return;
                            }


                            IsMe = true;
                            m.startActivityForResult(cInt, activityResultIDManager.put(new ActivityResultID() {
                                @Override
                                public void run() {
                                    if (this.resultCode == Activity.RESULT_OK) {
                                        Result rs = result.copy();
                                        try {

                                            DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
                                            downloadFilesTask.app21 = t;

                                            rs.success = true;
                                            rs.data = downloadFilesTask.saveVideoFileOnActivityResult(this.intent, "RECORD_VIDEO.mp4");
                                            App21Result(rs);
                                        } catch (NullPointerException n) {
                                            rs.success = false;
                                            rs.error = n.getLocalizedMessage();
                                        }
                                    } else if (resultCode == Activity.RESULT_CANCELED) {
                                        Result rs = result.copy();
                                        rs.success = false;
                                        rs.error = "resultCode=" + resultCode;
                                        App21Result(rs);
                                    }
                                }
                            }), cInt.getExtras());
                        }
                    });
                }
            });


        } catch (Exception ex) {
            result.success = false;
            result.error = ex.getLocalizedMessage();
            App21Result(result);
        }

    }

    void BROWSER(final Result result) {

        final App21 t = this;
        try {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.params));
            mContext.startActivity(browserIntent);

            result.success = true;

        } catch (Exception ex) {
            result.success = false;
            result.error = ex.getLocalizedMessage();

        }
        App21Result(result);
    }

    void GET_INFO(final Result result) {
        final App21 t = this;
        result.success = true;
        String info = "Android";
        info += ",PACKAGE_NAME:" + mContext.getPackageName();
        info += ",SDK_INT:" + Build.VERSION.SDK_INT;
        info += ",CODENAME:" + Build.VERSION.CODENAME;
        info += ",MANUFACTURER:" + Build.MANUFACTURER;
        info += ",PRODUCT:" + Build.PRODUCT;
        info += ",MODEL:" + Build.MODEL;
        result.data = info;
        App21Result(result);
    }

    void TEL(final Result result) {
        final App21 t = this;
        final String CALL_PHONE = Manifest.permission.CALL_PHONE;
        _PERMISSION(result, CALL_PHONE, new Runnable() {
            @Override
            public void run() {
                result.success = true;

                Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                phoneIntent.setData(Uri.parse("tel:" + result.params));


                try {
                    mContext.startActivity(phoneIntent);
                } catch (Exception e) {
                    result.success = false;
                    result.error = e.getLocalizedMessage();
                    // Instruct the user to install a PDF reader here, or something
                }

                App21Result(result);
            }
        });


    }

    void SHARE_OPEN(final Result result) {
        final App21 t = this;
        result.success = true;
        Intent target = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(result.params);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
        }
        App21Result(result);
    }

    //03/03/2022
    //hungnt
    void KEY(final Result result) {
        final App21 t = this;
        if (result.params != null && !result.params.isEmpty()) {
            //get
            String name = mContext.getPackageName();
            SharedPreferences sharedPref = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
            try {
                JSONObject jObject = new JSONObject(result.params);

                String key = jObject.has("key") ? jObject.getString("key") : null;
                String value = jObject.has("value") ? jObject.getString("value") : null;
                if (key != null && !key.isEmpty()) {
                    if (value != null) {

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(key, value);
                        editor.commit();
                        result.data = value;
                    } else {
                        result.data = sharedPref.getString(key, null);
                    }
                    result.success = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                result.error = e.getMessage();
            }


        }


        App21Result(result);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent intent, Activity activity) {
        // Activity act = activity.getCallingActivity().;


        if (IsMe) {
            activityResultIDManager.run(requestCode, resultCode, intent);
        }
        boolean t = IsMe;
        IsMe = false;
        return t; //true -> xuwr lys trong app21
    }
}

class Async21 {
    public final static void run(final int miliSeconds, final Runnable fn) {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(miliSeconds);
                    fn.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}

abstract class ActivityResultID implements Runnable {

    public int requestCode;
    public int resultCode;
    public Intent intent;

}

class ActivityResultIDManager {


    List<ActivityResultID> items = new ArrayList<>();
    int IncreId = 1;

    public int put(ActivityResultID activityResultID) {
        activityResultID.requestCode = IncreId++;
        items.add(activityResultID);
        return activityResultID.requestCode;
    }

    public int run(int requestCode, int resultCode, Intent intent) {
        ActivityResultID _r = null;
        for (ActivityResultID r : items) {
            if (r.requestCode == requestCode) {
                r.resultCode = resultCode;
                r.intent = intent;
                r.run();
                _r = r;
            }
        }
        if (_r != null) {
            items.remove(_r);
            return _r.requestCode;
        }
        return -1;
    }
}

class SMS {
    public String number;
    public String smsText;
}

class Base64Require {
    public String path;
    public String callback;
}

class VibratorInfo {
    public int amplitude;
    public int milliseconds;
}

class CameraInfo {
    public int maxwidth;
    public int maxheight;
    public String pref;
    public String ext;
}