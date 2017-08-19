package moss.mystery.energymonitor.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import moss.mystery.energymonitor.monitors.MonitorLibrary;

//NB: Receiver should not be registered until after MonitorLibrary is initialised!

public class BatteryMonitor extends BroadcastReceiver {
    private boolean startup = true;
    private boolean charging = false;
    private int previousLevel = -1;
    private boolean populated = false;
    private MonitorLibrary monitorLibrary;

    public void restart(){
        startup = true;
    }

    public BatteryMonitor(MonitorLibrary monitorLibrary){
        this.monitorLibrary = monitorLibrary;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Extract intent information
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean newCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        //If app has just started, pass all battery state info to MonitorLibrary
        if(startup){
            charging = newCharging;
            previousLevel = level;
            if(charging){
                monitorLibrary.chargerConnected();
            } else {
                monitorLibrary.chargerDisconnected();
            }
            monitorLibrary.setBatteryLevel(level);
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
            monitorLibrary.chargerConnected();
            return;
        }
        //Charger has been disconnected (was previously charging, now is not)
        if(charging){
            charging = false;
            monitorLibrary.chargerDisconnected();
            //Cause next statement to fire, updating battery level here and in MonitorLibrary
            previousLevel = level + 1;
        }
        //Battery level has dropped
        if(level < previousLevel){
            previousLevel = level;
            monitorLibrary.setBatteryLevel(level);
        }
    }
}