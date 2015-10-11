package com.example.utente.simpleledtorch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Gabriele on 03/10/2015.
 * Thanx to Francesco Azzola - Action logic rebuilt from ground by Gabriele Galluzzo
 *  Surviving with Android (http://www.survivingwithandroid.com)
 */

public class ShakeEventManager implements SensorEventListener {
    private SensorManager sManager;
    private Sensor sensor;
    private boolean isSensorRegistered;

    private static  int MOV_COUNTS = 4;
    private static  final int MOV_THRESHOLD = 4;
    private static  final float ALPHA = 0.8F;
    private static  int SHAKE_WINDOW_TIME_INTERVAL = 400; // milliseconds
    private static  int SHAKE_WINDOW_TIME_INTERVAL_GRACE = 500; // milliseconds

    private boolean isActionPossible=true;

    // Gravity force on x,y,z axis
    private float gravity[] = new float[3];

    private int counter;
    private long firstMovTime;
    private long lastActionTime;
    private ShakeListener listener;

    ApplicationSettings applicationSettings=ApplicationSettings.getInstance();
    Context context;

    public ShakeEventManager() {
        isSensorRegistered=false;
    }

    public void setListener(ShakeListener listener) {
        this.listener = listener;
    }

    public void initSensor(Context ctx) {
        this.context=ctx;
        sManager = (SensorManager)  ctx.getSystemService(Context.SENSOR_SERVICE);
        sensor = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // registerSensor();
        updateValuesFromSettings();
    }

    private void updateValuesFromSettings(){
        applicationSettings.readPreferencesValues(context);
        MOV_COUNTS = applicationSettings.getShakeNumbers();
        SHAKE_WINDOW_TIME_INTERVAL = applicationSettings.getShakesWindow() ; // milliseconds
        SHAKE_WINDOW_TIME_INTERVAL_GRACE = applicationSettings.getShakesDebounceWindow(); // milliseconds
    }

    public void registerSensor() {
        if (!isSensorRegistered) {
            sManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            isSensorRegistered=true;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Resto insensibile per il grace time
        if ((System.currentTimeMillis() - lastActionTime)<SHAKE_WINDOW_TIME_INTERVAL_GRACE) {
            Log.d("ShakeEVMan", "Sono nella finestra di debounce");
            Log.d("ShakeEVMan", String.format("   lastActionTime: %d", lastActionTime));
            Log.d("ShakeEVMan", String.format("   now: %d", System.currentTimeMillis()));
            return;
        }

        applicationSettings.readPreferencesValues(context);

        float maxAcc = calcMaxAcceleration(sensorEvent);
        if (maxAcc>=MOV_THRESHOLD){
            // Primo shake
            if (counter==0){
                firstMovTime=System.currentTimeMillis();
            }

            // Controllo se il tempo passato nello shake è nella finestra di ammissibilità
            if ((System.currentTimeMillis()-firstMovTime)<SHAKE_WINDOW_TIME_INTERVAL){
                counter++;  // Aggiorno gli shake
            } else {
                counter=0;
            }

            if (counter >= MOV_COUNTS){
                if (listener!=null)
                    listener.onShake();
                lastActionTime=System.currentTimeMillis();
                counter=0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void deregisterSensor()  {
        if (isSensorRegistered) {
            sManager.unregisterListener(this);
            isSensorRegistered=false;
        }
    }


    private float calcMaxAcceleration(SensorEvent event) {
        gravity[0] = calcGravityForce(event.values[0], 0);
        gravity[1] = calcGravityForce(event.values[1], 1);
        gravity[2] = calcGravityForce(event.values[2], 2);

        float accX = event.values[0] - gravity[0];
        float accY = event.values[1] - gravity[1];
        float accZ = event.values[2] - gravity[2];

        float max1 = Math.max(accX, accY);
        return Math.max(max1, accZ);
    }

    // Low pass filter
    private float calcGravityForce(float currentVal, int index) {
        return  ALPHA * gravity[index] + (1 - ALPHA) * currentVal;
    }


    private void resetAllData() {
        Log.d("SwA", "Reset all data");
        counter = 0;
        firstMovTime = System.currentTimeMillis();
        //  isActionPossible=false;
    }

    public static interface ShakeListener {
        public void onShake();
        // Callback per aggiornare l'activity
    }
}
