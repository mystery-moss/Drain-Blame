package moss.mystery.energymonitor.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import moss.mystery.energymonitor.processes.ProcessHandler;

//Call parseProcs() upon receipt of alarm

public class AlarmReceiver extends BroadcastReceiver {
    private static final String DEBUG = "AlarmReceiver";
    public static final int REQUEST_CODE = 85624;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DEBUG, "Received");
        //parseProcs()
//        context = context.getApplicationContext();
//        Intent i = new Intent(context, procMon.class);
//        context.startService(i);
    }
}