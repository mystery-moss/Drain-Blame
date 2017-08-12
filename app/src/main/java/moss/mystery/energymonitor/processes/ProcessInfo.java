package moss.mystery.energymonitor.processes;

//Information on process state within a battery interval

public class ProcessInfo {
    public String name;
    public long ticks;
    public long network;

    public ProcessInfo(String name, long tick){
        name = name;
        ticks = tick;
    }
}