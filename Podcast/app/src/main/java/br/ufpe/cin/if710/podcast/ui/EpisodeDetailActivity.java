package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;

public class EpisodeDetailActivity extends Activity {

    public static final String ITEM_FEED = "itemFeed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        // Pegando o item que disparou a intent para esta tela.
        ItemFeed item = (ItemFeed) getIntent().getSerializableExtra(ITEM_FEED);

        // Atualizando valores com informações do item que chamou esta activity.
        TextView title = findViewById(R.id.title);
        title.setText(item.getTitle());

        TextView date = findViewById(R.id.date);
        date.setText(item.getPubDate());

        TextView description = findViewById(R.id.description);
        description.setText(item.getDescription());

    }
}
