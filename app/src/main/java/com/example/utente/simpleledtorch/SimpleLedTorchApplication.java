package com.example.utente.simpleledtorch;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by utente on 04/10/2015.
 */

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "http://pippokennedy.iriscouch.com/acra-simpleledtorch/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "pippo",
        formUriBasicAuthPassword = "kennedy",

        customReportContent = {
                // Campi obblicatori per acralyzer
                ReportField.REPORT_ID, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION,ReportField.PACKAGE_NAME,ReportField.BUILD, ReportField.STACK_TRACE,

                // Campo per sapere il cellulare su cui è installato
                ReportField.INSTALLATION_ID,

                // Campi utili per avere altre info
                ReportField.PHONE_MODEL,ReportField.CUSTOM_DATA, ReportField.LOGCAT, ReportField.SETTINGS_GLOBAL, ReportField.DEVICE_FEATURES,
                ReportField.SETTINGS_SECURE, ReportField.SETTINGS_SYSTEM, ReportField.SHARED_PREFERENCES, ReportField.THREAD_DETAILS
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_report
)

public class SimpleLedTorchApplication  extends Application implements ActivityLifecycleCallbacks {
    // File che memorizza una UUID e verificare se l'applicazione è già stata eseguita
    private static final String INSTALLATION = "INSTALLATO";

    /**
     * Verifica se l'applicazione è già stata lanciata e comunica il first run tramite ACRA
     */
    private void checkFirstRunAndSendData(){
        Context context = getApplicationContext();

        // Test per vedere il first run
        File installation = new File(context.getFilesDir(), INSTALLATION);
        if (!installation.exists()) {
            // Provo a comunicare i dati al server. Se non ci riesco resta in first run
            ACRA.getErrorReporter().handleSilentException(new Throwable("Primo avvio applicazione"));

            try {
                FileOutputStream out = new FileOutputStream(installation);

                String id = UUID.randomUUID().toString()+"\n";
                out.write(id.getBytes());
                out.close();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
            // Se arrivo qui allora ho comunicato i dati e scrivo il file
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);

        // Verifico se è la prima esecuzione e provo a mandare i dati
        checkFirstRunAndSendData();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.i("Track Stopped", activity.getLocalClassName());

    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.i("Track Started", activity.getLocalClassName());

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.i("Track SaveInstanceState", activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.i("Track Resumed", activity.getLocalClassName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.i("Track Paused", activity.getLocalClassName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.i("Track Destroyed", activity.getLocalClassName());
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.i("Track Created", activity.getLocalClassName());
    }
}
