package moss.mystery.energymonitor.processes;

//Information on process state within a battery interval

public class ProcessInfo {
    public String name;
    public long ticks;

    public ProcessInfo(String n, long t){
        name = n;
        ticks = t;
    }
}