package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class PodcastProvider extends ContentProvider {

    private PodcastDBHelper db;

    public PodcastProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.

        // Consulta sendo realizada caso a uri seja válida, retornando a quantidade de linhas que
        // foram deletadas.
        if (uriEpisodio(uri)) {

            return db.getWritableDatabase().delete(PodcastDBHelper.DATABASE_TABLE, selection, selectionArgs);
        }else {

            return 0;
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.

        if (uriEpisodio(uri)) {

            long id = db.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE, null, values);
            return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(id));
        }else {

            return null;
        }
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.

        db = PodcastDBHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.

        Cursor cursor;

        if (uriEpisodio(uri)) {

           cursor = db.getReadableDatabase().query(PodcastDBHelper.DATABASE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }else {

           throw new UnsupportedOperationException("Not yet implemented");
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.

        // Retorna a quantidade de linhas que foram atualizadas.
        if (uriEpisodio(uri)) {

            return db.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE, values, selection, selectionArgs);
        }else {

            return 0;
        }
    }

    public boolean uriEpisodio(Uri uri) {

        return uri.getLastPathSegment().equals(PodcastProviderContract.EPISODE_TABLE);
    }

}
