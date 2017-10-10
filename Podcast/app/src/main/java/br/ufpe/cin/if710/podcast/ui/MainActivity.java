package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.download.DownloadService;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

import static br.ufpe.cin.if710.podcast.notifications.NotificationUtils.criarNotificacaoSimples;

public class MainActivity extends Activity {



    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast

    private ListView items; // Variável para a manipulação do ListView.
    InternoReceiver mReceiver; // Receiver interno do app.

    // Padrão de vibração ao receber a notificação de download completo da música.
    private long[]  mVibratePattern = { 0, 200, 200, 300, 400, 400, 500 };

    // Variável para indicar se está em primeiro.
    boolean primeiroPlano = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referenciando o listView.
        items = (ListView) findViewById(R.id.items);

        // Criando o receiver interno ao app.
        mReceiver = new InternoReceiver();
        // Registrando o receiver.
        IntentFilter filterLocal = new IntentFilter(DownloadService.DOWNLOAD_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterLocal);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Verifica se o dispositivo está conectado ou conectando.
        if(verConnection(getApplicationContext())){

            // Se sim, o fluxo segue para o download dos dados da internet.
            new DownloadXmlTask().execute(RSS_FEED);
        }else {

            // Se não, o fluxo vai diretamente acessar o banco de dados para obter as informações devidas.
            new GetInfoFromDB().execute();
        }
    }

    public boolean verConnection(Context context) {

        boolean result;
        // Criando um gerenciador de conectividade.
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(context.CONNECTIVITY_SERVICE);

        // Criando variável para verificar informações da conectividade.
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // Se a variável acima foi criada com sucesso e o aparelho está conectado ou conectando
        // result será true.
        result = (networkInfo != null) && networkInfo.isConnectedOrConnecting();

        // Retornando o status da conectividade do dispositivo.
        return result;
    }


    @Override
    protected void onResume(){
        super.onResume();

        // Indicando que o aparelho está em primeiro plano.
        primeiroPlano = true;
    }

    @Override
    protected void onPause(){
        super.onPause();

        // Indicando que o aparelho saiu de primeiro plano.
        primeiroPlano = false;
    }

    // Classe para criação do BroadcastReceiver interno que é chamado quando o download de um áudi
    // é finalizado.
    class InternoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){

            // Criando intent para finalizar a execução do service de download.
            Intent downloadService = new Intent( getApplicationContext(),DownloadService.class);

            // Executando o método onDestroy() do service de download.
            stopService(downloadService);

            // Se o app estiver em primeiro plano será exibido apenas um toast.
            if(primeiroPlano){

                // Exibindo toast.
                Toast.makeText(getApplicationContext(), "Download finalizado!" , Toast.LENGTH_LONG).show();
            }else {
                // Se o app não estiver em primeiro plano uma notificação é lançada.

                // Pegando o item que disparou o download enviado pelo service de download.
                ItemFeed item = (ItemFeed) intent.getSerializableExtra("item");

                // Criando uma notificação com o título do episódio e id para que uma notificação
                // não sobrescreva a outra no caso de dois downloads acontecendo ao mesmo tempo.
                criarNotificacaoSimples(getApplicationContext(), item, item.getId());
            }

            // Fazendo o dispositivo vibrar.
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(mVibratePattern, -1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Eliminando o adapter.
        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        adapter.clear();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Desresgritando o broadcast receiver apenas quando a activity for destruída,
        // para que mesmo em segundo plano funcione.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }


    private class DownloadXmlTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

            // Exibindo toast ao iniciar a execução do asyncTask.
            Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        // Método que será executado em segundo plano.
        @Override
        protected Void doInBackground(String... params) {

            // Lista de Itens.
            List<ItemFeed> itemList;

            try {
                // Lista recebendo informações do parser.
                itemList = XmlFeedParser.parse(getRssFeed(params[0]));

                // Inserindo item por item no banco de dados.
                for (ItemFeed item : itemList) {

                   ContentValues contentValues = new ContentValues();

                    contentValues.put(PodcastDBHelper.EPISODE_TITLE, item.getTitle());
                    contentValues.put(PodcastDBHelper.EPISODE_DATE, item.getPubDate());
                    contentValues.put(PodcastDBHelper.EPISODE_DESC, item.getDescription());
                    contentValues.put(PodcastDBHelper.EPISODE_LINK, item.getLink());
                    contentValues.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
                    contentValues.put(PodcastDBHelper.EPISODE_FILE_URI, "");

                    getContentResolver().insert(PodcastProviderContract.EPISODE_LIST_URI, contentValues);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            // Sinalizando que terminou o acesso aos dados da internet.
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();

            // Iniciando acesso ao banco de dados.
            new GetInfoFromDB().execute();
        }
    }

    // Classe para acessar o banco de dados.
    private class GetInfoFromDB extends AsyncTask<String, Void, List<ItemFeed>> {

        @Override
        protected void onPreExecute() {
            // Indicando que se está iniciando o acesso ao banco de dados.
            Toast.makeText(getApplicationContext(), "Iniciando acesso ao banco de dados...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();

            // Realizando consulta ao banco de dados.
            Cursor queryCursor = getContentResolver().query(
                    PodcastProviderContract.EPISODE_LIST_URI,
                    null, "", null, null
            );

            // Variável que armazena o id do item da lista para ser utilizado na notificação.
            int item_id = 0;

            // Enquanto houver um próximo elemento...
            while (queryCursor.moveToNext()){

                // Pegando os dados salvos no banco de dados e atualizando a lista.
                String item_title = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.TITLE));
                String item_date = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DATE));
                String item_description = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DESCRIPTION));
                String item_link = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.EPISODE_LINK));
                String item_download_link = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.DOWNLOAD_LINK));
                String item_uri = queryCursor.getString(queryCursor.getColumnIndex(PodcastProviderContract.EPISODE_URI));

                itemList.add(new ItemFeed(item_id, item_title, item_link, item_uri, item_description, item_download_link, item_uri));

                // Incrementando o id para o próximo elemento.
                item_id++;
            }

            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {

            Toast.makeText(getApplicationContext(), "Acesso ao banco finalizado!", Toast.LENGTH_SHORT).show();

            //Adapter Personalizado
            XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);

            // Atualizar o list view
            items.setAdapter(adapter);
            items.setTextFilterEnabled(true);
        }
    }



    //TODO Opcional - pesquise outros meios de obter arquivos da internet
    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }
}
