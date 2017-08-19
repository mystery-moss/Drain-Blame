package moss.mystery.energymonitor;

import android.content.Context;

import moss.mystery.energymonitor.apps.App;
import moss.mystery.energymonitor.apps.AppHandler;
import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.processes.Process;
import moss.mystery.energymonitor.processes.ProcessHandler;

/*
 * Access to components which should be linked to application lifetime.
 */

public class ApplicationGlobals {
    private static ApplicationGlobals instance;
    public MonitorLibrary monitorLibrary;
    public ProcessHandler processHandler;
    public AppHandler appHandler;
    public Context appContext;
    public boolean serviceEnabled;

    private ApplicationGlobals(Context _appContext){
        appContext = _appContext;
        appHandler = new AppHandler(appContext);
        processHandler = new ProcessHandler(appHandler);
        monitorLibrary = new MonitorLibrary(processHandler, appHandler);
        //TODO: Potentially record user preference for this, read it here
        serviceEnabled = true;
    }

    public static ApplicationGlobals get(Context context){
        if(instance == null){
            instance = new ApplicationGlobals(context);
        }
        return instance;
    }
}