package moss.mystery.energymonitor.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryMonitor extends BroadcastReceiver {
    private boolean charging = false; //TODO: Would this be better static? Maybe move to Library
    private boolean running = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int previous = MonitorLibrary.getCurrentBatteryLevel();

        //If neither charging state nor level has changed, ignore this broadcast
        //TODO: Need to handle situation where service has just been started - does it immediately get a broadcast?
        if(charging == isCharging && (level == -1 || level >= previous)){
            if(running){
                return;
            }
            //Else service has just started, and we are not connected to a charger
            running = true;
        }

        //Charger has been connected
        if(isCharging){
            charging = true;
            MonitorLibrary.chargerConnected();
        }
        //Either charger has just been disconnected, or battery level has dropped
        else{
            //Begin a new battery interval
            MonitorLibrary.newInterval(level, System.currentTimeMillis());
        }
    }
}