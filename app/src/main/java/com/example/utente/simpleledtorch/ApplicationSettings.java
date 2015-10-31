package com.example.utente.simpleledtorch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ShareActionProvider;

/**
 * Created by utente on 04/10/2015.
 * Singleton
 */
enum ApplicationSettings {
    INSTANCE;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean isSendCrashReportPossibile;
    private boolean isShakeSelected;
    private boolean isLightOnAtStartup;
    private int shakeNumbers;
    private int shakesWindow;
    private int shakesDebounceWindow;

    ApplicationSettings(){
        shakeNumbers=shakesWindow=shakesDebounceWindow=0;
        isLightOnAtStartup=false;
        isShakeSelected=false;
    }

    public static ApplicationSettings getInstance(){
        return INSTANCE;
    }

    public void readPreferencesValues (Context context){
        sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* TODO: Costanti cablate. rendere parametri che le stringhe sia nei files xml che qui e riferirsi
                ai valori di default definiti nei files xml
         */
        isShakeSelected=sp.getBoolean("shake_preference", false);
        isSendCrashReportPossibile=sp.getBoolean("acra.disable",true);
        isLightOnAtStartup=sp.getBoolean("startup_preference", false);

        shakeNumbers=Integer.parseInt(sp.getString("pref_number_shakes", "4"));
        // String[] s=sp.getStringSet("pref_shake_window", "400");
        shakesWindow=Integer.parseInt(sp.getString("pref_shake_window", "400"));
        shakesDebounceWindow = Integer.parseInt(sp.getString("pref_shake_debounce", "800"));
    }

    public void setShakeSelected (Context context, boolean value){
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor=sp.edit();
        isShakeSelected=value;
        editor.putBoolean("shake_preference",isShakeSelected);
        editor.commit();
    }

    /**
     * Verifica se nelle preference lo shake è on
     * @return true se shake impostato
     */
    public boolean isShakeSelected(){return isShakeSelected;}

    /**
     * Verifica se posso inviare il report
     * @return True se posso inviare il report
     */
    public boolean isSendCrashReportPossibile(){return isSendCrashReportPossibile;}

    /**
     * Verifico se da preferenze devo accendere il led
     * @return true se devo accendere il led
     */
    public boolean isLightOnAtStartup(){return isLightOnAtStartup;}
    public int getShakeNumbers(){return shakeNumbers;}
    public int getShakesWindow(){return shakesWindow;}
    public int getShakesDebounceWindow(){return shakesDebounceWindow;}
}
