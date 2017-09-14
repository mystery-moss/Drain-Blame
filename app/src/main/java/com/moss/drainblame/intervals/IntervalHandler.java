package com.moss.drainblame.intervals;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.util.Log;

import com.moss.drainblame.FileParsing;
import com.moss.drainblame.apps.AppHandler;
import com.moss.drainblame.processes.ProcessHandler;

public class IntervalHandler {
    private static final String DEBUG = "Monitor Library";
    private static final int MAX_INTERVALS = 1000;  //Max number of intervals to store before looping
    private static final int SAVE_INTERVAL = 8;     //Save data to file every X recorded intervals
    private final ProcessHandler processHandler;
    private final AppHandler appHandler;
    private final Context context;

    private int batteryLevel;
    private boolean charging;

    private boolean screenOn;
    private long screenOnStart;
    private long screenOnDuration;

    private long netRx;
    private long netTx;

    private boolean firstInterval;
    private long intervalStart;
    private final Interval[] intervals;
    private int intervalIndex;
    private boolean maxIntervals;
    private final Handler handler;
    private ProcessPoller processPoller;

    //Hardcoded minimum CPU tick threshold to consider a process as 'active'
    //Ideally, this should be dynamic and determined by the classifier
    private long threshold = 200;

    //Control=======================================================================================
    public IntervalHandler(ProcessHandler processHandler, AppHandler appHandler, Context context){
        this.processHandler = processHandler;
        this.appHandler = appHandler;
        this.context = context;
        handler = new Handler();
        firstInterval = true;
        screenOn = false;
        charging = false;
        batteryLevel = -1;
        intervals = new Interval[MAX_INTERVALS];
        intervalIndex = 0;
        maxIntervals = false;
    }

    public void shutdown(){
        Log.d(DEBUG, "Shutdown");
        stopPolling();
    }

    //TODO: Gracefully handle changing threshold partway through an interval
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

    //TODO: Refactor these two out into a separate, expandable resource tracking component
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
    //NOTE: Currently need to remember to call 'resetScreenCounter'
    //Ideally, call it automatically when 'getScreenOnDuration()' is called, if appropriate

    //Network tracking==============================================================================
    private long getNetworkBytes(){
        long rxnew = TrafficStats.getTotalRxBytes();
        long txnew = TrafficStats.getTotalTxBytes();

        if(rxnew - netRx < 0 || txnew - netTx < 0){
            return rxnew + txnew;
        }

        return (rxnew - netRx) + (txnew - netTx);
    }

    private void resetNetworkBytes(){
        netRx = TrafficStats.getTotalRxBytes();
        netTx = TrafficStats.getTotalTxBytes();
    }

    //Interval tracking=============================================================================
    public Interval[] getIntervals(){
        return intervals;
    }

    public int numIntervals(){
        if(maxIntervals){
            return MAX_INTERVALS;
        }
        return intervalIndex;
    }

    private void addInterval(Interval i){
        intervals[intervalIndex] = i;
        if(++intervalIndex >= MAX_INTERVALS){
            intervalIndex = 0;
            maxIntervals = true;
        }
        //Periodically save all data
        if(intervalIndex % SAVE_INTERVAL == 0){
            FileParsing.writeFile(context, this);
        }
    }

    //Add interval without autosaving
    public void populateInterval(Interval i){
        intervals[intervalIndex] = i;
        if(++intervalIndex >= MAX_INTERVALS){
            intervalIndex = 0;
            maxIntervals = true;
        }
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
            addInterval(new Interval(batteryLevel, timestamp - intervalStart, getScreenOnDuration(), getNetworkBytes(), appHandler.startNewSample(threshold)));
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
    }

    private void startPolling(int pollRate){
        if(pollRate < 1){
            pollRate = 1;
        }
        processPoller = new ProcessPoller(handler, pollRate, processHandler);
        handler.post(processPoller);
    }
}