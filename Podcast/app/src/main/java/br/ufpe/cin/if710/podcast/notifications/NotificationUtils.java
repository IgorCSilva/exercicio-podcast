package br.ufpe.cin.if710.podcast.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;
import br.ufpe.cin.if710.podcast.ui.MainActivity;

/**
 * Created by Administrador on 09/10/2017.
 */

public class NotificationUtils {


    private static PendingIntent criarPendingIntent(Context ctx, ItemFeed item, int id){

        // Estabelecendo para que ao clicar na notificação o usuário deve ser direcionado
        // à tela de detalhes da música baixada.
        Intent resultIntent = new Intent(ctx, EpisodeDetailActivity.class);
        resultIntent.putExtra(EpisodeDetailActivity.ITEM_FEED, item);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void criarNotificacaoSimples(Context ctx, ItemFeed item, int id){

        PendingIntent resultPendingIntent = criarPendingIntent(ctx, item, id);

        // Criando notificação.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Fronteiras da Ciência - " + id)
                .setContentText(item.getTitle())
                .setContentIntent(resultPendingIntent);

        // Passando a notificação para o gerenciador de notificações.
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);

        // Lançando a notificação.
        notificationManagerCompat.notify(id, mBuilder.build());
    }

    /*
    public static void criarNotificacaoCompleta(Context ctx, String texto, int id){

        Uri uriSom = Uri.parse("android.resource://" + ctx.getPackageName() + "/raw/som_notificacao");
        PendingIntent pitAcao = PendingIntent.getBroadcast(ctx, 0, new Intent(ACAO_NOTIFICACAO), 0);
        PendingIntent pitDelete = PendingIntent.getBroadcast(ctx, 0, new Intent(ACAO_DELETE), 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_porquin);
        PendingIntent pitNotificacao = criarPendingIntent(ctx, texto, id);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_notificacao)
                        .setContentTitle("Completa")
                        .setContentText(texto)
                        .setTicker("Chegou uma notificação!")
                        .setWhen(System.currentTimeMillis())
                        .setLargeIcon(largeIcon)
                        .setAutoCancel(true)
                        .setContentIntent(pitNotificacao)
                        .setDeleteIntent(pitDelete)
                        .setLights(Color.BLUE, 1000, 5000)
                        .setSound(uriSom)
                        .setVibrate(new long[]{100, 500, 200, 800})
                        .addAction(R.drawable.ic_acao_notificacao, "Ação Customizada", pitAcao)
                        .setNumber(id)
                        .setSubText("Subtexto");

        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        nm.notify(id, mBuilder.build());
    }

    public static void criarNotificationBig(Context ctx, int idNotificacao){

        int numero = 5;
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Mensagens não lidas:");

        for(int i = 1; i <= numero; i++){
            inboxStyle.addLine("Mensatem " + i);
        }

        inboxStyle.setSummaryText("Clique para exibir");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setContentTitle("Notificação")
                .setContentText("Vários itens pendentes")
                .setContentIntent(criarPendingIntent(ctx, "Mensagens não lidas", -1))
                .setNumber(numero)
                .setStyle(inboxStyle);

        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        nm.notify(idNotificacao, mBuilder.build());
    }

    private void criarNotificacaoCustom(Context ctx, String texto, int id){

        PendingIntent resultPendingIntent = criarPendingIntent(ctx, texto, id);

        RemoteViews views = new RemoteViews("android.dominando.ex26_notification_2", R.layout.layout_notificacao);

        Notification n = new NotificationCompat.Builder("android.dominando.ex26_notification_2.MainActivity.class")
                .setSmallIcon(R.drawable.ic_acao_notificacao)
                .setContent(views)
                .setOngoing(true)
                .build();
    }

    /**/
}
