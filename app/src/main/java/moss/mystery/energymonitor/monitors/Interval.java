package moss.mystery.energymonitor.monitors;

public class Interval {
    //'level' is battery level at the START of the interval
    public int level;
    public long length;
    public long screenOnTime;
    public String[] activeProcs;

    public Interval(int level, long length, long screenOnTime, String[] activeProcs){
        this.level = level;
        this.length = length;
        this.screenOnTime = screenOnTime;
        this.activeProcs = activeProcs;
    }
}