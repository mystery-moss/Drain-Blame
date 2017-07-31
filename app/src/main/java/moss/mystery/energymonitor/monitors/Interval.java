package moss.mystery.energymonitor.monitors;

import moss.mystery.energymonitor.processes.ProcessInfo;

public class Interval {
    //'level' is battery level at the START of the interval
    public int level;
    public long length;
    public long screenOnTime;
    public ProcessInfo[] activeProcs;

    public Interval(int level, long length, long screenOnTime, ProcessInfo[] activeProcs){
        this.level = level;
        this.length = length;
        this.screenOnTime = screenOnTime;
        this.activeProcs = activeProcs;
    }
}