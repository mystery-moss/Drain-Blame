package moss.mystery.energymonitor.monitors;

import moss.mystery.energymonitor.apps.App;

public class Interval {
    public int level;       //Battery level at start of interval - e.g. 80 = interval from 80 to 79%
    public long length;
    public long screenOnTime;
    public long networkBytes;
    public App[] activeProcs;

    public Interval(int level, long length, long screenOnTime, long networkBytes, App[] activeProcs){
        this.level = level;
        this.length = length;
        this.screenOnTime = screenOnTime;
        this.networkBytes = networkBytes;
        this.activeProcs = activeProcs;
    }
}