package moss.mystery.energymonitor.processes;

//Holds ticks information from /proc/[pid]/stat

public class CPUTicks {
    public final long ticks;    //(14) utime + (15) stime
    public final long start;    //(22) starttime

    public CPUTicks(long _t, long _s){
        ticks = _t;
        start = _s;
    }
}