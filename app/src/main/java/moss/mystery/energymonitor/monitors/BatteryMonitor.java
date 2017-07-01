package moss.mystery.energymonitor.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.BatteryManager;
import android.util.Log;
import android.view.Display;

public class BatteryMonitor extends BroadcastReceiver {
    private boolean chargeState = false; //TODO: Would this be better static? Maybe move to Library
    private boolean startup = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int previousLevel = MonitorLibrary.getCurrentBatteryLevel();

        //Has the service just been started?
        if(startup){
            MonitorLibrary.startup(context);
            MonitorLibrary.setBatteryLevel(level);
            chargeState = isCharging;
            startup = false;
            Log.d("Battery Monitor", "Service started, level = " + level);
            return;
        }

        //If chargeState state has not changed and level has not dropped, ignore this broadcast
        if(chargeState == isCharging && (level >= previousLevel) || level == -1){
            return;
        }

        //Charger has been connected
        if(isCharging){
            chargeState = true;
            MonitorLibrary.chargerConnected();
            Log.d("Battery Monitor", "Charger connected");
        }
        //Either charger has just been disconnected, or battery level has dropped
        //TODO: Go over logic here, double check - pretty sure I'm missing possibilities
        //At the very least I doubt it's robust unless all phones are consistent in this
        else{
            Log.d("Battery Monitor", "Entering else! Old = " + previousLevel + ", New = " + level);
            if(MonitorLibrary.isCharging()){
                MonitorLibrary.chargerDisconnected();
                Log.d("Battery Monitor", "Charger disconnected");
                //If charger has just been disconnected and battery is not at 100%, ignore first
                //interval (because it may be shorter than normal)
                if(MonitorLibrary.getCurrentBatteryLevel() != 100){
                    MonitorLibrary.setBatteryLevel(level);
                    Log.d("Battery Monitor", "Ignoring this interval - level = " + level);
                    return;
                }
            }
            //Begin a new battery interval
            MonitorLibrary.newInterval(level, System.currentTimeMillis());
            Log.d("Battery Monitor", "Beginning new interval - level = " + level);
        }
    }
}