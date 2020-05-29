package sections.libs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.telegram.messenger.MessagesController;

import java.io.File;

import sections.datamodel.Update_Model;
import telegram.messenger.xtelex.ApplicationLoader;
import telegram.messenger.xtelex.BuildConfig;
import telegram.messenger.xtelex.R;
import telegram.messenger.xtelex.util.Const;
import telegram.messenger.xtelex.util.Utils;

public class DirectInstallAppHelper {

    public static void checkUpdate(Update_Model update_model) {

        Utils.log("apk file url:"+update_model.getFile_url(),false);
        Utils.log("apk update version:"+update_model.getUpdate_version(),false);



        int current_version = BuildConfig.VERSION_CODE;
        int new_version = Integer.parseInt(update_model.getUpdate_version());
        if (new_version > current_version) {
            ////Log.i(Const.TAG, "new version great than current version");
            long interval = 2160000;//6hour
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            long last_update_dl_time = preferences.getLong("update_dl_time", 0);
            String last_update_version = preferences.getString("update_version", "");
            if (last_update_dl_time + interval < System.currentTimeMillis()) {
                if (!update_model.getUpdate_version().equals(last_update_version)) {
                    ////Log.i(Const.TAG, "version is not equal");
                    downloadApp(update_model);

                } else {
                    //Log.i(Const.TAG, "version is equal");
                    SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
                    edit.putLong("update_dl_time", System.currentTimeMillis());
                    edit.apply();
                    if (existApp()) {
                        //Log.i(Const.TAG, "app is exist");
                        installApp(ApplicationLoader.applicationContext);
                    }else {
                        //Log.i(Const.TAG, "app not exist");
                        downloadApp(update_model);
                    }
                }

            }

        }else {
            //Log.i(Const.TAG, "new version lower than current version");
        }

    }

    private static void downloadApp(Update_Model update_model) {
        DownloadManager downloadManager = new DownloadManager(ApplicationLoader.applicationContext.getResources().getString(R.string.AppName), new DownloadManager.OnDownloadProgressListener() {
            @Override
            public void percent(int percent) {

            }

            @Override
            public void downloadStart() {
                //Log.i(Const.TAG, "start download app");
            }

            @Override
            public void downloadedSucces() {
                //Log.i(Const.TAG, "downloadedSucces");
                DirectInstallAppHelper.installApp(ApplicationLoader.applicationContext);
                saveDownloadLog(update_model);
            }

            @Override
            public void downloadFaild() {

            }

            @Override
            public void downloadCancel() {

            }
        });
        downloadManager.execute(update_model.getFile_url());

    }


    private static void installApp(Context context) {
        //Log.i(Const.TAG, "install app");
        //File apkFile = new File(DownloadManager.getPath("sample.apk"));
        String appName = context.getResources().getString(R.string.AppName);
        File apkFile = new File(Environment.getExternalStorageDirectory() + "/Telegram/Apks/" + appName + ".apk");
        Intent intent;
        PackageManager pm = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            pm = context.getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent);
            }
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(DownloadManager.getPath(appName + ".apk")), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }


    }

    private static void saveDownloadLog(Update_Model update_model) {
        //Log.i(Const.TAG, "saveDownloadLog");
        SharedPreferences.Editor edit = MessagesController.getGlobalMainSettings().edit();
        edit.putString("update_version", update_model.getUpdate_version());
        edit.putLong("update_dl_time", System.currentTimeMillis());
        edit.apply();
    }


    private static boolean existApp() {
        boolean isExist = false;
        String appName = ApplicationLoader.applicationContext.getResources().getString(R.string.AppName);
        File apkFile = new File(Environment.getExternalStorageDirectory() + "/Telegram/Apks/" + appName + ".apk");
        if (apkFile.exists()) {
            isExist = true;
        }
        return isExist;
    }


}
