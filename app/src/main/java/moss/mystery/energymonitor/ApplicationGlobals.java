package moss.mystery.energymonitor;

import android.content.Context;

import moss.mystery.energymonitor.apps.AppHandler;
import moss.mystery.energymonitor.intervals.IntervalHandler;
import moss.mystery.energymonitor.processes.ProcessHandler;

/*
 * Access to components which should be linked to application lifetime.
 */

public class ApplicationGlobals {
    private static ApplicationGlobals instance;
    public IntervalHandler intervalHandler;
    public ProcessHandler processHandler;
    public AppHandler appHandler;
    public Context appContext;
    public boolean serviceEnabled;

    private ApplicationGlobals(Context appContext){
        this.appContext = appContext;
        this.appHandler = new AppHandler(appContext);
        this.processHandler = new ProcessHandler(appHandler);
        this.intervalHandler = new IntervalHandler(processHandler, appHandler);
        //TODO: Potentially record user preference for this, read it here
        this.serviceEnabled = true;
    }

    public static ApplicationGlobals get(Context context){
        if(instance == null){
            instance = new ApplicationGlobals(context);
        }
        return instance;
    }
}