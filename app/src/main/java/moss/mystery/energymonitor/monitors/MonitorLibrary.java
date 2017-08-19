package moss.mystery.energymonitor.monitors;

import android.app.AlarmManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import moss.mystery.energymonitor.ui.MainActivity;
import moss.mystery.energymonitor.processes.ProcessHandler;

public class MonitorLibrary {
    private static final String DEBUG = "Monitor Library";
    //TODO: TEMP
    public static int debug = 0;

    private ProcessHandler processHandler;

    //Battery
    private int batteryLevel;
    private boolean charging;

    //Screen
    private boolean screenOn;
    private long screenStart;
    private long screenOnTime;

    //Network
    private long rx;
    private long tx;

    //Interval
    private boolean firstInterval;
    private long intervalStart;
    private ArrayList<Interval> intervals;
    private AlarmManager alarm;
    private Handler handler;
    private RunnablePoll runnablePoll;
    public long threshold = 50;           //CPU tick threshold to consider a process as 'active'

    //Control=======================================================================================
    public MonitorLibrary(ProcessHandler processHandler){
        Log.d(DEBUG, "Startup");
        this.processHandler = processHandler;
        handler = new Handler();
        firstInterval = true;
        screenOn = false;
        charging = false;
        batteryLevel = -1;
        intervals = new ArrayList<Interval>();
    }

    public void shutdown(){
        Log.d(DEBUG, "Shutdown");
        stopPolling();
    }

    //TODO: Ideally gracefully handle changing threshold partway through an interval
    public void setThreshold(long t){
        threshold = t;
    }
    //Battery tracking==============================================================================
    public int getBatteryLevel(){
        return batteryLevel;
    }
    public void setBatteryLevel(int level) {
        if(level < batteryLevel){
            newInterval(System.currentTimeMillis(), false);
        }
        batteryLevel = level;
    }
    public void chargerConnected(){
        charging = true;
        stopPolling();
        //TODO: Double check that this is the best place for this ***********************************************
        processHandler.reset();
        firstInterval = true;
        Log.d(DEBUG, "Charger connected");
    }
    public void chargerDisconnected(){
        charging = false;
        //TODO: As above! ****************************************
        newInterval(System.currentTimeMillis(), true);
        Log.d(DEBUG, "Charger disconnected");
    }
    public boolean isCharging(){
        return charging;
    }

    //TODO: Refactor these two out into a separate, exapdable resource tracking thing
    //Screen tracking===============================================================================
    public void setScreenOn(){
        if(!screenOn) {
            screenOn = true;
            screenStart = System.currentTimeMillis();
        }
    }
    public void setScreenOff(){
        if(screenOn) {
            screenOn = false;
            screenOnTime += System.currentTimeMillis() - screenStart;
        }
    }
    public void resetScreenCounter(){
        screenOnTime = 0;
        if(screenOn){
            screenStart = System.currentTimeMillis();
        }
    }
    public long getScreenOnTime() {
        if(screenOn){
            screenOnTime += System.currentTimeMillis() - screenStart;
        }
        return screenOnTime;
    }
    //TODO: Currently need to remember to call 'resetScreenCounter' - better to call it when 'getScreenOnTime()' is called, if this is appropriate behaviour
        //As below for network!

    //Network tracking==============================================================================
    private long getNetworkBytes(){
        long rxnew = TrafficStats.getTotalRxBytes();
        long txnew = TrafficStats.getTotalTxBytes();

        if(rxnew - rx < 0 || txnew - tx < 0){
            ++debug;
            return 0;
        }

        return (rxnew - rx) + (txnew - tx);
    }

    private void resetNetworkBytes(){
        rx = TrafficStats.getTotalRxBytes();
        tx = TrafficStats.getTotalTxBytes();
    }

    //Interval tracking=============================================================================
    public void clearIntervals(){
        intervals.clear();
    }
    public ArrayList<Interval> getIntervals(){
        return intervals;
    }
    public void populateInterval(Interval i){
        intervals.add(i);
    }

    //TODO: SPECIAL CASE AS ABOVE DOUBEL CHECK PLEASE ************************************************
    public void newInterval(long timestamp, boolean specialCase){
        //Record previous interval (unless this is the first interval)
        if(!firstInterval){
            intervals.add(new Interval(batteryLevel, timestamp - intervalStart, getScreenOnTime(), getNetworkBytes(), processHandler.startNewSample()));
            if(MainActivity.appContext != null) {
                Toast toast = Toast.makeText(MainActivity.appContext, "BATTERY LEVEL DROPPED - " + (timestamp - intervalStart)/1000, Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else{
            if(!specialCase) {
                firstInterval = false;
            }
            processHandler.startNewSample();
        }

        resetScreenCounter();
        resetNetworkBytes();
        intervalStart = timestamp;

        //Periodically poll for running processes
        stopPolling();
        startPolling(30);
    }

    private void stopPolling(){
        try{
            handler.removeCallbacks(runnablePoll);
        } catch(Exception ignored){
            Log.d(DEBUG, "Error removing 'runnablePoll' callbacks: " + ignored);
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

    private void startPolling(int interval){
        if(interval < 1){
            interval = 1;
        }

        //If poll rate is below 60 seconds, use a 'Runnable' task
        if(interval < 60){
            runnablePoll = new RunnablePoll(handler, threshold, interval, processHandler);
            handler.post(runnablePoll);
        }
        //Else use an alarm
        else {
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
        }
    }
}
