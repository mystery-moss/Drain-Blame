package moss.mystery.energymonitor.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import moss.mystery.energymonitor.processes.ProcessLibrary;

public class BatteryMonitor extends BroadcastReceiver {
    private static boolean isCharging = false; //TODO: Maybe move to Library?
    private static boolean running = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isChargingNew = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int previousLevel = MonitorLibrary.getCurrentBatteryLevel();

        //Has the service just been started?
        if(!running){
            MonitorLibrary.startup(context);
            //TODO: Currently this responds with whether processes can be checked - fix to be useful!
            ProcessLibrary.reset();
            MonitorLibrary.setBatteryLevel(level);
            isCharging = isChargingNew;
            running = true;
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
        //Ah, on reset can reach situation where isCharging = true, isChargingNew = false,
        //but MonitorLibrary.isCharging() = false
        else{
            Log.d("Battery Monitor", "Entering else! Old = " + previousLevel + ", New = " + level);
            //If was previously charging, this update must be disconnected charger
            //TODO: Handle case where battery level changes and charger disconnected at same ticks
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
            //TODO: Sanity check this bit, or maybe remove local 'isCharging'?
            if(MonitorLibrary.isCharging() != isCharging){
                isCharging = MonitorLibrary.isCharging();
            }

            //Begin a new battery interval
            //TODO: If this is long running, maybe split it out into a separate thread???
            //As long as timestamps are accurate - could maybe get screen on ticks in this thread
            MonitorLibrary.newInterval(context, level, System.currentTimeMillis());
            Log.d("Battery Monitor", "Beginning new interval - level = " + level);
        }
    }
}