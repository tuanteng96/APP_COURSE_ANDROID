package vn.cser21;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class DataCacheManager {

    private Context context;

    DataCacheManager(Context applicationContext){
        context = applicationContext;
    }
    private File getDocumentDirectory() {
        File documentsDirectory = new File(getDataDirectory(context));
        Log.d("HEHE", documentsDirectory.getAbsolutePath());
        File cacheDirectory = new File(documentsDirectory, "cache");
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        return cacheDirectory;
    }

    public void initialUnzip( String fileName, DataCacheManagerCallback cacheManagerCallback ){
       final  String name = fileName.replace("\"","");
        final String path = getDocumentDirectory().getPath();
       final String[] separatedArray = name.split("\\.");
       if (checkExist(separatedArray[0])){
           cacheManagerCallback.onSuccess(name);
           return;
       }
        try {
            unzipFromAssets(name,cacheManagerCallback);
        } catch (IOException e) {
            cacheManagerCallback.onError(name);
            throw new RuntimeException(e);
        }

    }

    private boolean checkExist(String fileName) {
        File documentsDirectory = getDocumentDirectory();
        File destinationFile = new File(documentsDirectory, fileName);
        return destinationFile.exists();
    }

    private  void unzipFromAssets( String zipFileName, DataCacheManagerCallback dataCacheManagerCallback) throws IOException {
        AssetManager assetManager = context.getAssets();
        Log.d("NAME", zipFileName);
        // Mở tệp zip từ thư mục assets
        InputStream   inputStream = assetManager.open(zipFileName);
        unZip(inputStream,dataCacheManagerCallback);
    }


    private void unZip(InputStream inputStream, DataCacheManagerCallback cacheManagerCallback){
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            File directory = getDocumentDirectory();
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                File file = new File(directory, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // Tạo các thư mục cha nếu chúng chưa tồn tại
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    // Ghi dữ liệu từ entry vào tệp trong thư mục đầu ra
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.close();
                }
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
            cacheManagerCallback.onSuccess("");
        } catch (IOException e) {
            cacheManagerCallback.onError("");
        }
    }

    public void downloadListFileAndUnZip(List<String> files, DataCacheManagerCallback dataCacheManagerCallback) {
        final int[] index = {0};
        for (String f : files){
            try {
             URL url = new URL(f);
             InputStream inputStream = url.openStream();
             unZip(inputStream, new DataCacheManagerCallback() {
                 @Override
                 public void onSuccess(String data) {
                     index[0]++;
                     if(index[0] == files.size()){
                         dataCacheManagerCallback.onCompleted();
                     }
                 }

                 @Override
                 public void onError(String data) {
                     index[0]++;
                     if(index[0] == files.size()){
                         dataCacheManagerCallback.onCompleted();
                     }
                 }

                 @Override
                 public void onCompleted() {

                 }
             });

            } catch (MalformedURLException e) {
                dataCacheManagerCallback.onError("");
                throw new RuntimeException(e);
            } catch (IOException e) {
                dataCacheManagerCallback.onError("");
                throw new RuntimeException(e);
            }

        }


    }
    public List<String> readDataJSONFromUnZippedFolder(List<String> paths) {
        List<String> dataList = new ArrayList<>();
        File documentsDirectory = getDocumentDirectory();
        for (String path : paths) {
            if (checkExist(path)) {
                String data = dataFromFile(new File(documentsDirectory, path));
                dataList.add(data);
            } else {
                dataList.add(null);
            }
        }
        return dataList;
    }

    private String dataFromFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<String> getFileFromUnZipedFolder(List<String> images) {
        List<String> fileList = new ArrayList<>();
        File documentsDirectory = getDocumentDirectory();
        for (String image : images) {
            if (checkExist(image)) {
                fileList.add("file://"+(new File(documentsDirectory, image).getAbsolutePath()));
            } else {
                fileList.add(null);
            }
        }
        return fileList;
    }

    public  void  deleteAll(){
        File file = getDocumentDirectory();
        deleteCache(file);
    }
    private void deleteCache(File path) {
        if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                deleteCache(file);
            }
        }
        path.delete();
    }

    private  String getDataDirectory(@NonNull Context applicationContext) {
        final String name = "scer";
        File flutterDir = applicationContext.getDir(name, Context.MODE_PRIVATE);
        if (flutterDir == null) {
            flutterDir = new File(getDataDirPath(applicationContext), "app_" + name);
        }
        return flutterDir.getPath();
    }

    private  String getDataDirPath(Context applicationContext) {
        if (Build.VERSION.SDK_INT >= 24) {
            return applicationContext.getDataDir().getPath();
        } else {
            return applicationContext.getApplicationInfo().dataDir;
        }
    }



    public interface DataCacheManagerCallback {
        void onSuccess(String data);
        void onError(String data);

        void onCompleted();
    }

}


