package com.moss.drainblame;

import android.content.Context;

import com.moss.drainblame.apps.AppHandler;
import com.moss.drainblame.intervals.IntervalHandler;
import com.moss.drainblame.processes.ProcessHandler;

/*
 * Access to components which should be linked to application lifetime.
 */

public class ApplicationGlobals {
    private static ApplicationGlobals instance;
    public IntervalHandler intervalHandler;
    public ProcessHandler processHandler;
    public AppHandler appHandler;
    public boolean serviceEnabled;

    private ApplicationGlobals(Context appContext){
        this.appHandler = new AppHandler(appContext);
        this.processHandler = new ProcessHandler(appHandler);
        this.intervalHandler = new IntervalHandler(processHandler, appHandler, appContext);
        //Load data from file, if one is present
        FileParsing.readFile(appContext, intervalHandler);

        this.serviceEnabled = true;
    }

    public static ApplicationGlobals get(Context context){
        if(instance == null){
            instance = new ApplicationGlobals(context);
        }
        return instance;
    }
}