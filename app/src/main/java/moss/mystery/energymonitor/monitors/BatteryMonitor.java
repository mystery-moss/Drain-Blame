package moss.mystery.energymonitor.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryMonitor extends BroadcastReceiver {
    private boolean isCharging = false; //TODO: Would this be better static? Maybe move to Library
    private boolean startup = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isChargingNew = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int previousLevel = MonitorLibrary.getCurrentBatteryLevel();

        //Has the service just been started?
        if(startup){
            MonitorLibrary.startup(context);
            MonitorLibrary.setBatteryLevel(level);
            isCharging = isChargingNew;
            startup = false;
            Log.d("Battery Monitor", "Service started, level = " + level);
            return;
        }



        //If isCharging state has not changed and level has not dropped, ignore this broadcast
        if(isCharging == isChargingNew && (level >= previousLevel) || level == -1){
            return;
        }

        Log.d("Battery Monitor", "CHARGE STATE: " + isCharging + " CHARGING = " + isChargingNew);

        //Charger has been connected
        if(isChargingNew){
            isCharging = true;
            MonitorLibrary.chargerConnected();
            Log.d("Battery Monitor", "Charger connected");
        }
        //Either charger has just been disconnected, or battery level has dropped
        //TODO: Go over logic here, double check - pretty sure I'm missing possibilities
        //At the very least I doubt it's robust unless all phones are consistent in this
        else{
            Log.d("Battery Monitor", "Entering else! Old = " + previousLevel + ", New = " + level);
            //If was previously charging, this update must be disconnected charger
            //TODO: Handle case where battery level changes and charger disconnected at same time
            if(MonitorLibrary.isCharging()){
                MonitorLibrary.chargerDisconnected();
                isCharging = false;
                Log.d("Battery Monitor", "Charger disconnected");
                //If charger has just been disconnected and battery is not at 100%, ignore first
                //interval (because it may be shorter than normal)
                if(MonitorLibrary.getCurrentBatteryLevel() != 100){
                    MonitorLibrary.setBatteryLevel(level);
                    Log.d("Battery Monitor", "Ignoring first interval - level = " + level);
                    return;
                }
            }
            //Begin a new battery interval
            //TODO: If this is long running, maybe split it out into a separate thread???
            //As long as timestamps are accurate - could maybe get screen on time in this thread
            MonitorLibrary.newInterval(context, level, System.currentTimeMillis());
            Log.d("Battery Monitor", "Beginning new interval - level = " + level);
        }
    }
}