package moss.mystery.energymonitor.processes;

public class Process{
    public long intTicks;   //CPU ticks in the current sample interval
    private long prevTicks; //CPU ticks when last sampled
    private long startTime; //starttime when last observed
    public boolean active;  //Was process active in last sample interval

    public Process(ProcessTime time, boolean _active){
        prevTicks = time.ticks;
        intTicks = time.ticks;
        startTime = time.start;
        active = _active;
    }

    //If this is the very first interval but the process was already running, we can't attribute the
    //elapsed ticks of this process to the current interval - so set to 0 instead.
    //Also cannot determine whether process was active in this interval.
    public Process(ProcessTime time){
        prevTicks = time.ticks;
        intTicks = 0;
        startTime = time.start;
        active = false;
    }

    public void updateTicks(ProcessTime time, long threshold){
        //If process has restarted since last observed, all of its ticks occurred in this interval
        if(startTime != time.start){
            startTime = time.start;
            prevTicks = time.ticks;
            intTicks += time.ticks;
        } else {
            //Otherwise attribute all ticks since last observation to this interval
            long newTicks = time.ticks - prevTicks;
            intTicks += newTicks;
            prevTicks = time.ticks;
        }
        //Mark as active if ticks are past threshold
        if(intTicks >= threshold){
            active = true;
        }
    }
}