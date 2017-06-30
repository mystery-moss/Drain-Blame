package moss.mystery.energymonitor.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import moss.mystery.energymonitor.monitors.MonitorLibrary;

public class ScreenMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
            MonitorLibrary.screenOn();
        }
        else{
            MonitorLibrary.screenOff();
        }
    }
}