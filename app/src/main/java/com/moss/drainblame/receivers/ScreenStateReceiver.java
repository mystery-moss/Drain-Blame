package com.moss.drainblame.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;

import com.moss.drainblame.intervals.IntervalHandler;

/*
 *  Listen for changes in screen state
 */

public class ScreenStateReceiver extends BroadcastReceiver {
    private static final String DEBUG = "ScreenStateReceiver";
    private final IntervalHandler intervalHandler;

    public ScreenStateReceiver(IntervalHandler intervalHandler){
        this.intervalHandler = intervalHandler;
    }

    //Record current screen state in IntervalHandler
    public void startTracking(Context context){
        intervalHandler.setScreenOff();
        intervalHandler.resetScreenCounter();

        if(Build.VERSION.SDK_INT >= 20) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    intervalHandler.setScreenOn();
                    break;
                }
            }
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if(powerManager.isScreenOn()){
                intervalHandler.setScreenOn();
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
            intervalHandler.setScreenOn();
            Log.d(DEBUG, "Screen on");
        }
        else{
            intervalHandler.setScreenOff();
            Log.d(DEBUG, "Screen off");
        }
    }
}