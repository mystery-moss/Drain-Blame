package moss.mystery.energymonitor.monitors;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;

import java.util.ArrayList;

public class MonitorLibrary {
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

    //Control=======================================================================================
    public static void startup(Context context){
        Log.d("MonitorLibrary", "STARTING UP");
        firstInterval = true;

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
    }
    public static void clearIntervals(){
        intervals.clear();
    }
    public static ArrayList<Interval> getIntervals(){
        return intervals;
    }
    public static void newInterval(int level, long timestamp){
        //Don't record details of previous interval for the first interval
        if(!firstInterval){
            intervals.add(new Interval(currentBatteryLevel, timestamp - intervalStart, getScreenOnTime()));
        }
        else{
            firstInterval = false;
        }

        resetScreenCounter();
        intervalStart = timestamp;
        currentBatteryLevel = level;
    }
}
