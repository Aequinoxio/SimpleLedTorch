package com.example.utente.simpleledtorch;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by utente on 11/10/2015.
 *
 * Application's and activity lifecycle logging facility interface
 */
public interface ActivityLifecycleCallbacks{
    public void onActivityStopped(Activity activity);
    public void onActivityStarted(Activity activity);
    public void onActivitySaveInstanceState(Activity activity, Bundle outState);
    public void onActivityResumed(Activity activity);
    public void onActivityPaused(Activity activity);
    public void onActivityDestroyed(Activity activity);
    public void onActivityCreated(Activity activity, Bundle savedInstanceState);
}
