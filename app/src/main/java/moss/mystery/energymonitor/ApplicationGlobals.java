package moss.mystery.energymonitor;

import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.processes.Process;
import moss.mystery.energymonitor.processes.ProcessHandler;

public class ApplicationGlobals {
    private static ApplicationGlobals instance;
    public MonitorLibrary monitorLibrary;
    public ProcessHandler processHandler;

    private ApplicationGlobals(){
        processHandler = new ProcessHandler();
        monitorLibrary = new MonitorLibrary(processHandler);
    }

    public static ApplicationGlobals get(){
        if(instance == null){
            instance = new ApplicationGlobals();
        }
        return instance;
    }
}