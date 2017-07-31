package moss.mystery.energymonitor.monitors;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;

import java.util.ArrayList;

import moss.mystery.energymonitor.processes.ProcessLibrary;

public class MonitorLibrary {
    private static final String DEBUG = "Monitor Library";
    //Status
    public static boolean running = false;

    //Battery
    private static int currentBatteryLevel = -1;
    private static boolean charging = false;

    //Screen
    private static boolean screenOn = false;
    private static long screenStart;
    private static long screenOnTime;

    //Interval
    private static boolean firstInterval = true;
    private static long intervalStart;
    private static ArrayList<Interval> intervals = new ArrayList<Interval>();
    private static AlarmManager alarm;
    private static Handler handler = new Handler();
    private static boolean scheduled = false;

    //Control=======================================================================================
    public static void startup(Context context){
        Log.d("MonitorLibrary", "STARTING UP");
        firstInterval = true;
        running = true;

        //Get screen state
        //Handles multiple displays, though in practice this situation is undefined
        screenOn = false;
        resetScreenCounter();

        if(Build.VERSION.SDK_INT >= 20) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn();
                    Log.d("MonitorLibrary", "Startup - screen is on");
                    //TODO: Confirm this breaks from the for loop... I forget
                    break;
                }
            }
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if(powerManager.isScreenOn()){
                screenOn();
                Log.d("MonitorLibrary", "Startup - screen is on");
            }
        }
    }
    //Battery tracking==============================================================================
    public static int getCurrentBatteryLevel(){
        return currentBatteryLevel;
    }
    public static void setBatteryLevel(int level) {
        currentBatteryLevel = level;
    }
    public static void chargerConnected(){
        charging = true;
        stopRecording();
    }
    public static void chargerDisconnected(){
        charging = false;
    }
    public static boolean isCharging(){
        return charging;
    }

    //Screen tracking===============================================================================
    public static void screenOn(){
        screenOn = true;
        screenStart = System.currentTimeMillis();
    }
    public static void screenOff(){
        if(screenOn) {
            screenOn = false;
            screenOnTime += System.currentTimeMillis() - screenStart;
        }
    }
    public static void resetScreenCounter(){
        screenOnTime = 0;
        if(screenOn){
            screenStart = System.currentTimeMillis();
        }
    }
    public static long getScreenOnTime(){
        if(screenOn){
            screenOnTime += System.currentTimeMillis() - screenStart;
        }
        return screenOnTime;
    }

    //Interval tracking=============================================================================
    public static void stopRecording(){
        firstInterval = true;
        cancelAlarm();
    }
    public static void clearIntervals(){
        intervals.clear();
    }
    public static ArrayList<Interval> getIntervals(){
        return intervals;
    }
    public static void populateInterval(Interval i){
        intervals.add(i);
    }
    public static void newInterval(Context context, int level, long timestamp){
        //Don't record details of previous interval for the first interval
        if(!firstInterval){
            intervals.add(new Interval(currentBatteryLevel, timestamp - intervalStart, getScreenOnTime(), ProcessLibrary.getActiveProcs()));
        }
        else{
            firstInterval = false;
            ProcessLibrary.resetActive();
        }

        //Periodically poll for running processes
        //TODO: Factor this out, account for varying poll times
        Log.d("MonitorLibrary", "Setting up the alarm");

        //TODO: Make this check nicer!
        if(scheduled){
            handler.removeCallbacks(poll_prelim);
        }
        handler.post(poll_prelim);
        scheduled = true;


//        //Increase context capabilities?
//        context = context.getApplicationContext();
//
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        long firstMillis = System.currentTimeMillis();
//
//        //Halt any existing alarms
//        if (alarm != null){
//            alarm.cancel (pIntent);
//        }
//
//        alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        //Fire alarm immediately, then every thirty seconds
//        //TODO: Why does it only trigger every minute, not every 30 seconds?
//        //And even that is pretty inconsistent...
//        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 30 * 1000, pIntent);

        resetScreenCounter();
        intervalStart = timestamp;
        currentBatteryLevel = level;
    }

    //TODO: Make this nicer - its an object, not a method
    private static Runnable poll_prelim = new Runnable(){
        @Override
        public void run(){
            //TODO: Maybe pass this to something else via intent? Unclear, research effects of this
            Log.d(DEBUG, "Polling processes");
            ProcessLibrary.parseProcs(0);

            handler.postDelayed(poll_prelim, 30000);
        }
    };

    //TODO: Tidy this up, work out how context actually works
    public static void cancelAlarm(){
        if(scheduled){
            handler.removeCallbacks(poll_prelim);
        }
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        long firstMillis = System.currentTimeMillis();
//
//        if (alarm != null){
//            alarm.cancel (pIntent);
//        }
    }
}
