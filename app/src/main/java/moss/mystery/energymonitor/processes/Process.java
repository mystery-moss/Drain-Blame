package moss.mystery.energymonitor.processes;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;

import moss.mystery.energymonitor.apps.App;
import moss.mystery.energymonitor.apps.AppHandler;

public class Process{
    private long prevTicks; //CPU ticks when last sampled
    private long startTime; //starttime when last observed
    private App app;        //Application associated with this process

    public Process(CPUTicks time, String name, AppHandler appHandler){
        prevTicks = time.ticks;
        startTime = time.start;
        app = appHandler.getApp(name);
        app.addTicks(time.ticks);
    }

    //If this is the very first interval but the process was already running, we can't attribute the
    //elapsed ticks of this process to the current interval - so set to 0 instead.
    public Process(CPUTicks time, String name, AppHandler appHandler, int flag){
        prevTicks = time.ticks;
        startTime = time.start;
        app = appHandler.getApp(name);
    }

    public void updateTicks(CPUTicks time){
        //If process has restarted since last observed, all of its ticks occurred in this interval
        if(startTime != time.start){
            startTime = time.start;
            prevTicks = time.ticks;
            app.addTicks(time.ticks);
        } else {
            //Otherwise attribute all ticks since last observation to this interval
            long newTicks = time.ticks - prevTicks;
            app.addTicks(newTicks);
            prevTicks = time.ticks;
        }
    }
}