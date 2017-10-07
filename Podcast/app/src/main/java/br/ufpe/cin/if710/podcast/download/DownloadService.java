package br.ufpe.cin.if710.podcast.download;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrador on 29/09/2017.
 */

public class DownloadService extends IntentService {

    public static final String DOWNLOAD_COMPLETE = "igor.android.services.action.DOWNLOAD_COMPLETE";


    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onHandleIntent(Intent i) {
        try {
            //checar se tem permissao... Android 6.0+
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            root.mkdirs();
            Log.d("IGOR ","" + root);
            Log.d("IGOR ","" + i.getData().getLastPathSegment());
            File output = new File(root, i.getData().getLastPathSegment());
            if (output.exists()) {
                output.delete();
            }
            URL url = new URL(i.getData().toString());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            FileOutputStream fos = new FileOutputStream(output.getPath());
            BufferedOutputStream out = new BufferedOutputStream(fos);
            try {
                InputStream in = c.getInputStream();
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            finally {
                fos.getFD().sync();
                out.close();
                c.disconnect();
            }

            /*
            NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder;
            NotificationC.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setTicker(tickerText)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(mContentIntent)
                    .setSound(soundURI)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(mVibratePattern);

            // Passa a notificação para o notification manager.
            mNotifyManager.notify(MY_NOTIFICATION_ID, mBuilder.build());
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_COMPLETE));

            /**/
            Toast.makeText(getApplicationContext(), "Download finalizado...", Toast.LENGTH_SHORT).show();

            sendBroadcast(new Intent("igor.broadcasts.exemplo"));

        } catch (IOException e2) {
            Log.e(getClass().getName(), "Exception durante download", e2);
        }
    }


}
