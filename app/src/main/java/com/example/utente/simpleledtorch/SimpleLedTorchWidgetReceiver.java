package com.example.utente.simpleledtorch;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Created by utente on 31/10/2015.
 *
 * TODO: da implementare. La logica è: al click sul widget accendo il led.
 * TODO: Verificare se il widget permette l'accension edel led anche da lockscreen
 */
public class SimpleLedTorchWidgetReceiver extends BroadcastReceiver {
    private static boolean isLightOn = false;
    private static Camera camera;

    @Override
    public void onReceive(Context context, Intent intent) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.simple_led_torch_layout);
        Log.d("BroadcastReceiver","onReceive");
        /*
        if(isLightOn) {
            views.setImageViewResource(R.id.button, R.drawable.off);
        } else {
            views.setImageViewResource(R.id.button, R.drawable.on);
        }
*/

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context,SimpleLedTorchWidget.class),
                views);

        if (isLightOn) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
                isLightOn = false;
            }

        } else {
            // Open the default i.e. the first rear facing camera.
            camera = Camera.open();

            if(camera == null) {
                Toast.makeText(context, "no_camera", Toast.LENGTH_SHORT).show();
            } else {
                // Set the torch flash mode
                Camera.Parameters param = camera.getParameters();
                param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                try {
                    camera.setParameters(param);
                    camera.startPreview();
                    isLightOn = true;
                } catch (Exception e) {
                    Toast.makeText(context, "no_flash", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
