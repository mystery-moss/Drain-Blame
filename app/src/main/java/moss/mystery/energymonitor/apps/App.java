package moss.mystery.energymonitor.apps;

//Information on process state within a battery interval

public class App {
    public String name;
    public long ticks;
    public long network;

    public App(String name){
        this.name = name;
        this.ticks = 0;
    }

    public App(String name, long ticks){
        this.name = name;
        this.ticks = ticks;
    }

    public void addTicks(long ticks){
        this.ticks += ticks;
    }
}