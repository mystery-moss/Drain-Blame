package moss.mystery.energymonitor.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import moss.mystery.energymonitor.monitors.MonitorLibrary;

public class ScreenMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
            MonitorLibrary.screenOn();
            Log.d("Screen Monitor", "Screen on");
        }
        else{
            MonitorLibrary.screenOff();
            Log.d("Screen Monitor", "Screen off");
        }
    }
}