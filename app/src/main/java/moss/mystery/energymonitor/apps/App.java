package moss.mystery.energymonitor.apps;

//Information on process state within a battery interval

public class App {
    public final String name;           //Name from PackageManager, if known, else null
    public final boolean unknownApp;    //True if no app could be associated with this process
    public long ticks;

    public App(String name, boolean unknownApp){
        this(name, 0, unknownApp);
    }

    public App(String name, long ticks, boolean unknownApp){
        this.name = name;
        this.ticks = ticks;
        this.unknownApp = unknownApp;
    }

    public void addTicks(long ticks){
        this.ticks += ticks;
    }
}