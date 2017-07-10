package moss.mystery.energymonitor.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 85624;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Received!");
        context = context.getApplicationContext();
        Intent i = new Intent(context, ProcessMonitor.class);
        context.startService(i);
    }
}