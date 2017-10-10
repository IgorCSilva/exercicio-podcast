package br.ufpe.cin.if710.podcast.download;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.notifications.NotificationUtils;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

import static br.ufpe.cin.if710.podcast.notifications.NotificationUtils.criarNotificacaoSimples;

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
            // Pegando item pela intent que o disparou.
            ItemFeed item = (ItemFeed) i.getSerializableExtra("item");

            //checar se tem permissao... Android 6.0+
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            root.mkdirs();

            Log.d("IGOR ","" + root); // diretório
            Log.d("IGOR ","" + Uri.parse(item.getDownloadLink()).getLastPathSegment()); // Nome do arquivo.

            File output = new File(root, Uri.parse(item.getDownloadLink()).getLastPathSegment());

            // Se o arquivo existe ele é destruído.
            if (output.exists()) {
                output.delete();
            }

            // Passando o link de download para o formato correto a ser utilizado.
            URL url = new URL(Uri.parse(item.getDownloadLink()).toString());

            // Estabelecendo conexão com o link dado.
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            FileOutputStream fileOutputStream = new FileOutputStream(output.getPath());

            // Caminhos completos do arquivo baixado.
            Log.d("IGOR", output.getPath().toString());
            Log.d("IGOR", output.getAbsolutePath().toString());

            BufferedOutputStream out = new BufferedOutputStream(fileOutputStream);

            try {
                InputStream in = httpURLConnection.getInputStream();
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            finally {
                fileOutputStream.getFD().sync();
                out.close();
                httpURLConnection.disconnect();
            }

            // Criando ação para ser executado ao término do download.
            Intent intent = new Intent(DOWNLOAD_COMPLETE);

            // Passando o item para pegar suas informações para a criação da notificação.
            intent.putExtra("item", item);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } catch (IOException e2) {
            Log.e(getClass().getName(), "Exception durante download", e2);
        }
    }

    // Este método é chamando quando se executa a chamada stopService().
    // Quando a activity é morta pelo usuário o onDestroy() não é chamado.
    // Não sei sobre outras possibilidades.
    @Override
    public void onDestroy() {
        super.onDestroy();

            Toast.makeText(getApplicationContext(),"onDestroy() - Service", Toast.LENGTH_SHORT).show();
    }
}
