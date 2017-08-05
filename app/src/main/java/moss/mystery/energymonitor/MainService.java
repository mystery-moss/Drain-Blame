package moss.mystery.energymonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import moss.mystery.energymonitor.monitors.BatteryMonitor;
import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.monitors.ScreenMonitor;
import moss.mystery.energymonitor.processes.ProcessLibrary;

public class MainService extends Service {
    private static final String DEBUG = "MainService";
    private static final int NOTIFICATION_ID = 956231;
    private boolean running = false;
    private BatteryMonitor batteryMonitor;
    private ScreenMonitor screenMonitor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DEBUG, "Received start command");
        //If already running, ignore
        if(running){
            return START_STICKY;
        }

        //Create taskbar notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_temp)
                .setContentTitle("Battery Monitor")
                .setContentText("Recording app power usage.");
        Intent notificationIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        //Used to cancel notification
        startForeground(NOTIFICATION_ID, mBuilder.build());

        //Start monitor components
        ProcessLibrary.reset();
        MonitorLibrary.startup();

        Context context = getApplicationContext();

        //Register screen monitor
        screenMonitor = new ScreenMonitor();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(screenMonitor, new IntentFilter(filter));
        screenMonitor.startTracking(context);

        //Register battery level change receiver
        batteryMonitor = new BatteryMonitor();
        context.registerReceiver(batteryMonitor, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        Log.d(DEBUG, "Service started");
        running = true;
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        Log.d(DEBUG, "Service starting up");
    }

    @Override
    public void onDestroy(){
        MonitorLibrary.shutdown();

        try {
            unregisterReceiver(batteryMonitor);
            unregisterReceiver(screenMonitor);
        }
        catch(Exception e){
            Log.d("MainActivity", "Receivers not registered: " + e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG, "Binding not supported");
        return null;
    }
}
