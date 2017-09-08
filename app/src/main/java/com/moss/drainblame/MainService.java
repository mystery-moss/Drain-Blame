package com.moss.drainblame;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.moss.drainblame.receivers.BatteryReceiver;
import com.moss.drainblame.receivers.ScreenStateReceiver;
import com.moss.drainblame.ui.MainActivity;

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

        Context context = getApplicationContext();

        //Create taskbar notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_content))
                .setOngoing(true);
        Intent notificationIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, mBuilder.build());

        //Start monitor components
        globals = ApplicationGlobals.get(context);

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

        //Save interval data to file
        FileParsing.writeFile(context, globals.intervalHandler);
        
        globals.intervalHandler.shutdown();

        Log.d(DEBUG, "Service shutting down");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG, "Binding not supported");
        return null;
    }
}
