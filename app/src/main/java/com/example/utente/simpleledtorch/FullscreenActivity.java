package com.example.utente.simpleledtorch;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.logging.Logger;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements ShakeEventManager.ShakeListener {
    private SimpleLedTorchApplication simpleLedTorchApplication;
    private final static String restartFromRotationKey = "restartFromRotation";

    private Camera cam;
    private boolean toggleLedIsOff;
    private ShakeEventManager shakeEventManager;
    private boolean isFlashAvailable=false;
    private boolean isCameraAvailable=false;

    private boolean pauseFromMenuSettings =false;
    private boolean pauseLedStatus = false;
    private boolean changeOrientation;

    // Variabili per gestire il menu. Per ora non ho trovato di meglio. Il meglio sarebbe poter recuperare il menu
    // a partire dall'id e quindi l'itemMenu
    private Menu menuShake;
    private MenuItem itemShake;

    ApplicationSettings applicationSettings=ApplicationSettings.getInstance();

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

//    private View mContentView;
//    private View mControlsView;
//    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        simpleLedTorchApplication= (SimpleLedTorchApplication) getApplication();
        simpleLedTorchApplication.onActivityCreated(this, savedInstanceState);

        Log.d("***FullScreenActivity", "onCreate");

        // Imposto i default values se non è mai stato chiamato
        PreferenceManager.setDefaultValues(this, R.xml.application_preference, false);

        applicationSettings.readPreferencesValues(this);

        // Avvio tutto ma non registro il sensore
        shakeEventManager = new ShakeEventManager();
        shakeEventManager.setListener(this);
        shakeEventManager.initSensor(this);

        // Inizializzo tutte le variabili dell'HW prima di fare qualsiasi altra operazione
        isCameraAvailable = isCameraSupported(getApplicationContext().getPackageManager());
        isFlashAvailable = isFlashSupported(getApplicationContext().getPackageManager());

//        // Se devo avviare il led all'inizio dell'app lo faccio subito
//        if (applicationSettings.isLightOnAtStartup()){
//            startStopLed(true);
//        }

        //setToggleLedIsOff();

        // Imposto l'UI
        ActionBar actionBar = getSupportActionBar();
        try {
            actionBar.show();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        initUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        Log.d("***FullScreenActivity", "onSaveInstanceState");

        // Se ruoto il cellulare salvo lo stato del led - altrimenti ricomincio
        if (changeOrientation) {
            outState.putBoolean(restartFromRotationKey, toggleLedIsOff);
        }
        else {
            outState.putBoolean(restartFromRotationKey, applicationSettings.isLightOnAtStartup());
        }

        // Chiamo alla fine in accordo all'esempio presente in
        // http://developer.android.com/training/basics/activity-lifecycle/recreating.html
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d("***FullScreenActivity", "onStart");

    }

    @Override
    protected void onRestoreInstanceState(Bundle outState){
        super.onRestoreInstanceState(outState);
        if (changeOrientation) {
            toggleLedIsOff = outState.getBoolean(restartFromRotationKey, false);
            startStopLed(toggleLedIsOff);
        }
        Log.d("***FullScreenActivity", "onRestoreInstanceState");
    }

    @Override
    public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
        Log.d("***FullScreenActivity", "onConfigurationChanged");

        switch (configuration.orientation){
            case Configuration.ORIENTATION_PORTRAIT :

            case Configuration.ORIENTATION_LANDSCAPE:
                changeOrientation=true;
                TextView textView = (TextView) findViewById(R.id.fullscreen_content);
                // Prelevo lo stato del touch button prima di impostare il nuovo layout
                boolean isButtonDisabled=textView.isClickable();
                setContentView(R.layout.activity_fullscreen);
                // Recupero il bottone e reimposto lo stato
                textView = (TextView) findViewById(R.id.fullscreen_content);
                textView.setClickable(isButtonDisabled);
                updateUI();
                break ;
            default:
                changeOrientation=false;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("***FullScreenActivity", "onDestroy");
        shakeEventManager.deregisterSensor();
        releaseCameraInstance();

//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageFromServiceReceiver);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d("***FullScreenActivity", "onPause");
//        if (!pauseFromMenuSettings) {
//            shakeEventManager.deregisterSensor();
//            releaseCameraInstance();
//        }

//        shakeEventManager.deregisterSensor();
//        releaseCameraInstance();
//        toggleLedIsOff=true;    // Consistenza dell'UI
//
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageFromServiceReceiver);
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d("***FullScreenActivity", "onStop");

        shakeEventManager.deregisterSensor();
        releaseCameraInstance();
        toggleLedIsOff=true;    // Consistenza dell'UI

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageFromServiceReceiver);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        // Mi salvo il menu per futuri riferimenti
        menuShake=menu;

        menuShake.findItem(R.id.action_shake_menu).setIcon(applicationSettings.isShakeSelected()?
                        R.drawable.ic_radio_button_on_white_24dp:
                        R.drawable.ic_radio_button_off_white_24dp
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("***FullScreenActivity", "onOptionsItemSelected");


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            pauseFromMenuSettings = true;
            pauseLedStatus=!toggleLedIsOff; // LA logica è invertita in quanto passerò questo valore per accendere il led -> se toggleLedIsOff è true allora devo passare false
            Intent intent = new Intent(this, MySettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_about) {
            String s = getString(R.string.app_name) +" - V " + BuildConfig.VERSION_NAME ;
            s+="\nby "+ getString(R.string.Autore);
            new AlertDialog.Builder(FullscreenActivity.this)
                    .setTitle(R.string.action_about)
                    .setMessage(s)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
            return true;
        }

        if (id == R.id.action_shake_menu){
            if (applicationSettings.isShakeSelected()){
                // Se lo shake era attivo lo spengo
//                item.setIcon(R.drawable.ic_radio_button_off_white_24dp);
                shakeEventManager.deregisterSensor();
            } else{
                // altrimenti lo attivo
//                item.setIcon(R.drawable.ic_radio_button_on_white_24dp);
                shakeEventManager.registerSensor();
            }

            applicationSettings.setShakeSelected(getApplicationContext(),!applicationSettings.isShakeSelected());
            updateUI();
        }

        if (id == R.id.action_touch_menu){
            TextView textView= (TextView) findViewById(R.id.fullscreen_content);
            if (textView.isClickable()){
                textView.setClickable(false);
            } else {
                textView.setClickable(true);
            }
            updateUI();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d("***FullScreenActivity", "onPostCreate");

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

//    /**
//     * Touch listener to use for in-layout UI controls to delay hiding the
//     * system UI. This is to prevent the jarring behavior of controls going away
//     * while interacting with activity UI.
//     */
//    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
//            return false;
//        }
//    };

//    private void toggle() {
//        if (mVisible) {
//            hide();
//        } else {
//            show();
//        }
//    }

//    private void hide() {
//        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
//        mControlsView.setVisibility(View.GONE);
//        mVisible = false;
//
//        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable);
//        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
//    }

//    private final Runnable mHidePart2Runnable = new Runnable() {
//        @SuppressLint("InlinedApi")
//        @Override
//        public void run() {
//            // Delayed removal of status and navigation bar
//
//            // Note that some of these constants are new as of API 16 (Jelly Bean)
//            // and API 19 (KitKat). It is safe to use them, as they are inlined
//            // at compile-time and do nothing on earlier devices.
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }
//    };

//    @SuppressLint("InlinedApi")
//    private void show() {
//        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
//    }
//
//    private final Runnable mShowPart2Runnable = new Runnable() {
//        @Override
//        public void run() {
//            // Delayed display of UI elements
//            ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.show();
//            }
//            mControlsView.setVisibility(View.VISIBLE);
//        }
//    };
//
//    private final Handler mHideHandler = new Handler();
//    private final Runnable mHideRunnable = new Runnable() {
//        @Override
//        public void run() {
//            hide();
//        }
//    };

//    /**
//     * Schedules a call to hide() in [delay] milliseconds, canceling any
//     * previously scheduled calls.
//     */
//    private void delayedHide(int delayMillis) {
//        mHideHandler.removeCallbacks(mHideRunnable);
//        mHideHandler.postDelayed(mHideRunnable, delayMillis);
//    }


    ///////////////
    public void releaseCameraInstance(){
        if (cam!=null) {
            cam.release();
            cam=null;
        }
    }

    private void setToggleLedIsOff(){
        // Se non ho il flash o la camera allora non ho nulla da fare qui
        if (!isCameraAvailable || !isFlashAvailable){
            toggleLedIsOff=true;
            return;
        }

        try {
            cam= Camera.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        Camera.Parameters p = cam.getParameters();
        toggleLedIsOff= p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF);
    }


    public void toggleLed(View v){
        startStopLed(toggleLedIsOff);
    }

    /**
     * switch on/off the led whenever is called
     * @param turnOnLed
     */
    private void startStopLed(boolean turnOnLed){

        // Se non ho il flash o la camera allora non ho nulla da fare qui
        if (!isCameraAvailable || !isFlashAvailable)    return;

        try {
            cam= Camera.open();
        }catch (Exception e){
            e.printStackTrace();
        }

        Camera.Parameters p = cam.getParameters();
        if (turnOnLed){
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            cam.startPreview();
        }else{
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(p);
            cam.stopPreview();
            releaseCameraInstance();
        }

        toggleLedIsOff= p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF);

        updateUI();
    }

    // handler for received Intents for the "AggiornaInterfaccia" event
    private BroadcastReceiver mMessageFromServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Aggiorno l'interfaccia
            updateUI();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.d("***FullScreenActivity", "onResume");

        applicationSettings.readPreferencesValues(getApplicationContext());

        // Se devo avviare il led all'inizio dell'app lo faccio subito
        toggleLedIsOff=true;

        if (pauseFromMenuSettings){
            toggleLedIsOff=pauseLedStatus;
            startStopLed(pauseLedStatus);
        } else if (applicationSettings.isLightOnAtStartup()){
            startStopLed(true);
            toggleLedIsOff=false;
        }
        // Ripristino il default
        pauseFromMenuSettings=false;

        // Se non ritorno dalla settings activity allora reimposto tutto
        if (applicationSettings.isShakeSelected()) {
            shakeEventManager.registerSensor();
        } else {
            shakeEventManager.deregisterSensor();
        }

        //if (!toggleLedIsOff)

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageFromServiceReceiver,
                new IntentFilter("AggiornaInterfaccia"));

        updateUI();

        //    Test per le preferenze di invio
//        ACRA.getErrorReporter().handleSilentException(new Throwable("Test send Crash report"));
//        if (BuildConfig.DEBUG){
//            TextView t = (TextView)findViewById(R.id.textView);
//            t.setText(String.format("Num Shakes:%d", applicationSettings.getShakeNumbers()));
//            t = (TextView)findViewById(R.id.textView2);
//            t.setText(String.format("Shake window:%d", applicationSettings.getShakesWindow()));
//            t = (TextView)findViewById(R.id.textView3);
//            t.setText(String.format("Shake debounce:%d", applicationSettings.getShakesDebounceWindow()));
//            t.setText(applicationSettings.isSendCrashReportPossibile()?"SI":"NO");
//        }
    }


    private void updateUI(){
        TextView textView;
        if (menuShake != null){
            itemShake=menuShake.findItem(R.id.action_shake_menu);
            itemShake.setIcon(applicationSettings.isShakeSelected()?
                    R.drawable.ic_radio_button_on_white_24dp:
                    R.drawable.ic_radio_button_off_white_24dp
            );

            itemShake=menuShake.findItem(R.id.action_touch_menu);
            textView = (TextView) findViewById(R.id.fullscreen_content);
            itemShake.setIcon(textView.isClickable() ?
                            R.drawable.ic_lock_open_white_24dp:
                            R.drawable.ic_lock_white_24dp
            );
        }

        textView=(TextView) findViewById(R.id.textViewShakeOnOff);

        String textString="";
        if (applicationSettings.isShakeSelected()){
            textView.setText(R.string.ShakeOn);
            textView=(TextView) findViewById(R.id.fullscreen_content);
            textString= textView.isClickable()? getString(R.string.TextInfoContentShake):getString(R.string.TextInfoContentNoTouch);
        } else {
            textView.setText(R.string.ShakeOff);
            textView=(TextView) findViewById(R.id.fullscreen_content);
            textString= textView.isClickable()? getString(R.string.TextInfoContentNoShake):getString(R.string.TextInfoContentNoTouch);
        }
        textView.setText(textString);

        textView =(TextView) findViewById(R.id.fullscreen_content);
        FrameLayout frameLayout= (FrameLayout) findViewById(R.id.frameLayoutStatusLed);

        if (toggleLedIsOff) {
            textView.setBackgroundResource(R.drawable.button_colorblue);
            frameLayout.setBackgroundColor(Color.argb(0, 0, 0x99, 0xcc));
        } else {
            textView.setBackgroundResource(R.drawable.button_colored);
            frameLayout.setBackgroundColor(Color.RED);
        }

        if (!textView.isClickable()) {
            textView.setBackground(null);
        }
    }

    private void initUI(){
        TextView textView;
        if (!isFlashAvailable || !isCameraAvailable) {
            textView=(TextView) findViewById(R.id.textViewLedNotSupported);
            textView.setVisibility(View.VISIBLE);
            textView=(TextView) findViewById(R.id.fullscreen_content);
            textView.setVisibility(View.INVISIBLE);
        } else {
            //tb.setEnabled(true);
            textView=(TextView) findViewById(R.id.textViewLedNotSupported);
            textView.setVisibility(View.INVISIBLE);
            textView=(TextView) findViewById(R.id.fullscreen_content);
            textView.setVisibility(View.VISIBLE);
        }

        updateUI();
    }

    /**
     * @param packageManager
     * @return true <b>if the device support camera flash</b><br/>
     * false <b>if the device doesn't support camera flash</b>
     */
    private boolean isFlashSupported(PackageManager packageManager){
        // if device support camera flash?
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return true;
        }
        return false;
    }

    /**
     * @param packageManager
     * @return true <b>if the device support camera</b><br/>
     * false <b>if the device doesn't support camera</b>
     */
    private boolean isCameraSupported(PackageManager packageManager){
        // if device support camera?
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }


    public void onShake(){
        startStopLed(toggleLedIsOff);
    }
}
