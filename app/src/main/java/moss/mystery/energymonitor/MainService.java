package moss.mystery.energymonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import moss.mystery.energymonitor.monitors.BatteryMonitor;
import moss.mystery.energymonitor.monitors.ScreenMonitor;

public class MainService extends Service {
    private static final String DEBUG = "MainService";
    private BroadcastReceiver batteryMonitor;
    private BroadcastReceiver screenMonitor;

    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_temp)
                .setContentTitle("Battery Monitor")
                .setContentText("Battery % goes here");
        //TODO: Maybe change activity called when notification is clicked
        Intent notificationIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        //TODO: Fix this int (notification identifier)
        //Used to cancel notification
        startForeground(85623, mBuilder.build());

        //Register screen monitor - when declared in manifest, never runs
        screenMonitor = new ScreenMonitor();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        getApplicationContext().registerReceiver(screenMonitor, new IntentFilter(filter));

        //Register battery level change receiver
        //TODO: Should I split it off to another thread?
        //TODO: Should I explicitly start monitoring intervals here too?
        batteryMonitor = new BatteryMonitor();
        getApplicationContext().registerReceiver(batteryMonitor, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        Log.d(DEBUG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        Log.d(DEBUG, "Service starting up");
    }

    @Override
    public void onDestroy(){
        try {
            unregisterReceiver(batteryMonitor);
            unregisterReceiver(screenMonitor);
        }
        catch(Exception e){
            Log.d("MainActivity", "Receivers not registered:\n" + e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG, "Binding not currently supported");
        return null;
    }
}
