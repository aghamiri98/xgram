package sections.libs;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;


import net.hockeyapp.android.metrics.model.Base;

import org.checkerframework.checker.signedness.qual.Constant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import telegram.messenger.xtelex.util.Const;


public class DownloadManager extends AsyncTask<String, String, String> {

    public interface OnDownloadProgressListener {

        void percent(int percent);


        void downloadStart();

        void downloadedSucces();


        void downloadFaild();

        void downloadCancel();

    }


    private String title;
    private long total = 0;

    private OnDownloadProgressListener onDownloadProgressListener;


    public static final int DOWNLOAD_START = 0;
    public static final int DOWNLOAD_SUCCESS = 1;
    public static final int DOWNLOAD_FAILD = 2;
    public static final int DOWNLOAD_CANCEL = 3;


    public DownloadManager(String title, OnDownloadProgressListener onDownloadProgressListener) {
        this.title = title;
        this.onDownloadProgressListener = onDownloadProgressListener;

    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
        onDownloadProgressListener.downloadCancel();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onDownloadProgressListener.downloadStart();
    }


    @Override
    protected String doInBackground(String... file_urls) {
        int count;
        try {
            URL url = new URL(file_urls[0]);
            URLConnection conection = url.openConnection();
            conection.connect();
            final int lenghtOfFile = conection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            File file = new File(Const.DIR_DOWNLOAD_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            OutputStream output = new FileOutputStream(getPath(title));
            byte data[] = new byte[1024];
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                onDownloadProgressListener.percent((int) ((total * 100) / lenghtOfFile));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();


        } catch (Exception e) {
           new Handler().post(new Runnable() {
                @Override
                public void run() {
                    onDownloadProgressListener.downloadFaild();
                }
            });

            Log.e("Error: ", e.getMessage());
        }

        return null;
    }


    protected void onProgressUpdate(String... progress) {

    }


    @Override
    protected void onPostExecute(String file_url) {
        onDownloadProgressListener.downloadedSucces();

    }

    public static String getPath(String name) {
        return Const.DIR_DOWNLOAD_PATH + name + ".apk";
    }


}
