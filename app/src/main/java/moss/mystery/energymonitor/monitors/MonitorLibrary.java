package moss.mystery.energymonitor.monitors;

import java.util.ArrayList;

public class MonitorLibrary {
    private static int currentBatteryLevel = -1;
    private static boolean screenOn = false;
    private static boolean charging = false;

    //Interval tracking
    private static boolean firstInterval = true;
    private static long intervalStart;
    private static ArrayList<Interval> intervals = new ArrayList<Interval>();

    public static void screenOn(){
        screenOn = true;
    }
    public static void screenOff(){
        screenOn = false;
    }
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
        //TODO: Special handling for first interval
        //If battery is not at 100%, first interval may be shorter than normal, so ignore it
        if(firstInterval){
            firstInterval = false;
            intervalStart = timestamp;
            currentBatteryLevel = level;
            return;
        }

        //Record details of previous interval
        intervals.add(new Interval(currentBatteryLevel, timestamp - intervalStart));

        intervalStart = timestamp;
        currentBatteryLevel = level;
    }
}
