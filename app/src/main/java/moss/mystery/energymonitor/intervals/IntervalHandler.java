package moss.mystery.energymonitor.intervals;

import android.net.TrafficStats;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import moss.mystery.energymonitor.apps.AppHandler;
import moss.mystery.energymonitor.ui.MainActivity;
import moss.mystery.energymonitor.processes.ProcessHandler;

public class IntervalHandler {
    private static final String DEBUG = "Monitor Library";
    private final ProcessHandler processHandler;
    private final AppHandler appHandler;

    private int batteryLevel;
    private boolean charging;

    private boolean screenOn;
    private long screenOnStart;
    private long screenOnDuration;

    private long netRx;
    private long netTx;

    private boolean firstInterval;
    private long intervalStart;
    private final ArrayList<Interval> intervals;
    private final Handler handler;
    private ProcessPoller processPoller;
    private long threshold = 50;           //CPU tick threshold to consider a process as 'active'

    //Control=======================================================================================
    public IntervalHandler(ProcessHandler processHandler, AppHandler appHandler){
        this.processHandler = processHandler;
        this.appHandler = appHandler;
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
    public void setBatteryLevel(int newLevel) {
        if(newLevel < batteryLevel){
            newInterval(System.currentTimeMillis(), false);
        }
        batteryLevel = newLevel;
    }

    public void chargerConnected(){
        charging = true;
        stopPolling();
        //Clear processes and app ticks
        processHandler.reset();
        appHandler.resetTicks();
        firstInterval = true;
        Log.d(DEBUG, "Charger connected");
    }

    public void chargerDisconnected(){
        charging = false;
        //Start new interval with special case flag - record processes but don't record details of
        //interval, because first interval after charger disconnect may be of abnormal length
        newInterval(System.currentTimeMillis(), true);
        Log.d(DEBUG, "Charger disconnected");
    }

    //TODO: Refactor these two out into a separate, exapdable resource tracking thing
    //Screen tracking===============================================================================
    public void setScreenOn(){
        if(!screenOn) {
            screenOn = true;
            screenOnStart = System.currentTimeMillis();
        }
    }

    public void setScreenOff(){
        if(screenOn) {
            screenOn = false;
            screenOnDuration += System.currentTimeMillis() - screenOnStart;
        }
    }

    public void resetScreenCounter(){
        screenOnDuration = 0;
        if(screenOn){
            screenOnStart = System.currentTimeMillis();
        }
    }

    public long getScreenOnDuration() {
        if(screenOn){
            screenOnDuration += System.currentTimeMillis() - screenOnStart;
        }
        return screenOnDuration;
    }
    //TODO: Currently need to remember to call 'resetScreenCounter' - better to call it when 'getScreenOnDuration()' is called, if this is appropriate behaviour
        //As below for network!

    //Network tracking==============================================================================
    private long getNetworkBytes(){
        long rxnew = TrafficStats.getTotalRxBytes();
        long txnew = TrafficStats.getTotalTxBytes();

        if(rxnew - netRx < 0 || txnew - netTx < 0){
            return 0;
        }

        return (rxnew - netRx) + (txnew - netTx);
    }

    private void resetNetworkBytes(){
        netRx = TrafficStats.getTotalRxBytes();
        netTx = TrafficStats.getTotalTxBytes();
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

    private void newInterval(long timestamp, boolean specialCase){
        //Don't record previous interval if there isn't one
        if(firstInterval){
            //If special case flag is set, the next interval will be the first 'official' one
            if(!specialCase) {
                firstInterval = false;
            }
            appHandler.resetTicks();
        } else {
            //Record previous interval
            intervals.add(new Interval(batteryLevel, timestamp - intervalStart, getScreenOnDuration(), getNetworkBytes(), appHandler.startNewSample(threshold)));
        }

        resetScreenCounter();
        resetNetworkBytes();
        intervalStart = timestamp;

        //Periodically poll for running processes
        stopPolling();
        startPolling(30);
    }

    private void stopPolling(){
        if(processPoller != null) {
            try {
                handler.removeCallbacks(processPoller);
            } catch (Exception ignored) {
            }
            finally{
               processPoller = null;
            }
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

    private void startPolling(int pollRate){
        if(pollRate < 1){
            pollRate = 1;
        }

        //If poll rate is below 60 seconds, use a 'Runnable' task
        if(pollRate < 60){
            processPoller = new ProcessPoller(handler, pollRate, processHandler);
            handler.post(processPoller);
        }
        //Else use an alarm
        else {
            //TODO: Needs to receive a referenced to ProcessHandler so it can call parseProcs
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
