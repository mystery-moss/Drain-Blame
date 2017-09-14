package com.moss.drainblame.processes;

import com.moss.drainblame.apps.App;
import com.moss.drainblame.apps.AppHandler;

/*
 *  Information on a running process
 */

public class Process{
    private long prevTicks; //CPU ticks when last sampled
    private long startTime; //starttime when last observed
    private App app;        //Application associated with this process

    public Process(CPUTicks time, String name, AppHandler appHandler, boolean firstSample){
        prevTicks = time.ticks;
        startTime = time.start;
        app = appHandler.getApp(name);
        //If this is the very first interval but the process was already running, we can't attribute
        //the elapsed ticks of this process to the current interval.
        if(!firstSample) {
            app.addTicks(time.ticks);
        }
    }

    //Add new CPU ticks to associated app
    public void updateTicks(CPUTicks time){
        //If process has restarted since last observed, all of its ticks occurred in this interval
        if(startTime != time.start){
            startTime = time.start;
            prevTicks = time.ticks;
            app.addTicks(time.ticks);
        } else {
            //Otherwise attribute all ticks since last observation to this interval
            app.addTicks(time.ticks - prevTicks);
            prevTicks = time.ticks;
        }
    }
}