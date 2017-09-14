package com.moss.drainblame.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.moss.drainblame.intervals.IntervalHandler;

/*
 *  Listen for changes in battery state.
 */

public class BatteryReceiver extends BroadcastReceiver {
    private boolean startup = true;
    private boolean charging = false;
    private int previousLevel = -1;
    private IntervalHandler intervalHandler;

    public void restart(){
        startup = true;
    }

    public BatteryReceiver(IntervalHandler intervalHandler){
        this.intervalHandler = intervalHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Extract intent information
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean newCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        //If app has just started, pass all battery state info to IntervalHandler
        if(startup){
            charging = newCharging;
            previousLevel = level;
            if(charging){
                intervalHandler.chargerConnected();
            } else {
                intervalHandler.chargerDisconnected();
            }
            intervalHandler.setBatteryLevel(level);
            startup = false;
            return;
        }


        //If charging state has not changed and level has not dropped, ignore this broadcast
        if(charging == newCharging && ((level >= previousLevel) || level == -1)){
            return;
        }
        //Charger has been connected
        if(newCharging){
            charging = true;
            intervalHandler.chargerConnected();
            return;
        }
        //Charger has been disconnected (was previously charging, now is not)
        if(charging){
            charging = false;
            intervalHandler.chargerDisconnected();
            //Cause next statement to fire, updating battery level here and in IntervalHandler
            previousLevel = level + 1;
        }
        //Battery level has dropped
        if(level < previousLevel){
            previousLevel = level;
            intervalHandler.setBatteryLevel(level);
        }
    }
}