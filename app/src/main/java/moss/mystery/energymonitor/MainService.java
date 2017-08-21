package moss.mystery.energymonitor;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import moss.mystery.energymonitor.receivers.BatteryReceiver;
import moss.mystery.energymonitor.receivers.ScreenStateReceiver;
import moss.mystery.energymonitor.ui.MainActivity;

public class MainService extends Service {
    private static final String DEBUG = "MainService";
    private static final int NOTIFICATION_ID = 956231;
    private boolean running = false;

    private ApplicationGlobals globals;
    private BatteryReceiver batteryReceiver;
    private ScreenStateReceiver screenStateReceiver;

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
                .setContentText("Recording app power usage.")
                //.setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true);
        Intent notificationIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, mBuilder.build());

        Context context = getApplicationContext();

        //Start monitor components
        globals = ApplicationGlobals.get(context);

        //Determine CPU threshold
        //TODO: Split this out into another thread?

        //Register screen state receiver
        screenStateReceiver = new ScreenStateReceiver(globals.intervalHandler);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(screenStateReceiver, new IntentFilter(filter));
        screenStateReceiver.startTracking(context);

        //Register battery level change receiver
        batteryReceiver = new BatteryReceiver(globals.intervalHandler);
        context.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

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
        Context context = getApplicationContext();
        try {
            context.unregisterReceiver(batteryReceiver);
            context.unregisterReceiver(screenStateReceiver);
        }
        catch(Exception e){
            Log.d(DEBUG, "Receivers not registered");
        }
        
        globals.intervalHandler.shutdown();

        Log.d(DEBUG, "Service shutting down");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG, "Binding not supported");
        return null;
    }
}
