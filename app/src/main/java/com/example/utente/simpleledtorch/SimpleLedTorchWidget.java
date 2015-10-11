package com.example.utente.simpleledtorch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by utente on 03/10/2015.
 */

// TODO: al click accendere/spegnere il led ed aggiornare l'immagine sul widget
public class SimpleLedTorchWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout);
        Intent launchActivity = new Intent(context, FullscreenActivity.class);

        // Imposto i flag per evitare di aprire nuovamente l'attività: mi serve di riaprirne solo una
        launchActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Imposto l'intent per lanciare la main activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context,R.integer.intentMainActivity, launchActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.simple_led_torch_widget, pendingIntent);

        // Tell the AppWidgetManager to perform an update on the current app
        // widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive (Context context, Intent intent){
        super.onReceive(context, intent);

        // Aggiorno il widget
        final ComponentName appWidgets=new ComponentName(context.getPackageName(),getClass().getName());
        final AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
        final int ids[]=appWidgetManager.getAppWidgetIds(appWidgets);
        if (ids.length > 0) {
            onUpdate(context,appWidgetManager,ids);
        }
    }

}
