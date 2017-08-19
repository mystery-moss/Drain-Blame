package moss.mystery.energymonitor.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;

public class ScreenMonitor extends BroadcastReceiver {
    private final MonitorLibrary monitorLibrary;

    public ScreenMonitor(MonitorLibrary monitorLibrary){
        this.monitorLibrary = monitorLibrary;
    }

    //Record current screen state in MonitorLibrary
    public void startTracking(Context context){
        monitorLibrary.setScreenOff();
        monitorLibrary.resetScreenCounter();

        if(Build.VERSION.SDK_INT >= 20) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    monitorLibrary.setScreenOn();
                    break;
                }
            }
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if(powerManager.isScreenOn()){
                monitorLibrary.setScreenOn();
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
            monitorLibrary.setScreenOn();
            Log.d("Screen Monitor", "Screen on");
        }
        else{
            monitorLibrary.setScreenOff();
            Log.d("Screen Monitor", "Screen off");
        }
    }
}