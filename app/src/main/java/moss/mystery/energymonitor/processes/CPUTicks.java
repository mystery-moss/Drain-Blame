package moss.mystery.energymonitor.processes;

//Holds ticks information from /proc/[pid]/stat

public class CPUTicks {
    public final long ticks;    //(14) utime + (15) stime
    public final long start;    //(22) starttime

    public CPUTicks(long ticks, long start){
        this.ticks = ticks;
        this.start = start;
    }
}