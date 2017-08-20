package moss.mystery.energymonitor.apps;

//Information on process state within a battery interval

public class App {
    public String appName;      //Name from PackageManager, if known, else null
    public String processName;  //Name of process, if app name is not known
    public long ticks;

    public App(String appName, String processName){
        this.appName = appName;
        this.processName = processName;
        this.ticks = 0;
    }

    public void addTicks(long ticks){
        this.ticks += ticks;
    }
}