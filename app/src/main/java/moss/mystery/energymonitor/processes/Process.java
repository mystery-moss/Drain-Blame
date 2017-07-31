package moss.mystery.energymonitor.processes;

public class Process{
    public long prevTicks;  //CPU ticks when previously sampled
    public long currTicks;  //CPU ticks when last sampled
    public boolean active;  //Was process active in last sample interval
    public long intTicks;   //CPU ticks in current measurement interval
    private int pid;

    public Process(long tick, int _pid){
        prevTicks = tick;
        currTicks = tick;
        pid = _pid;
        active = false;
        intTicks = tick;
    }

    public long updateTicks(long ticks, int _pid){
        //Account for process restarts
        if(pid != _pid){
            pid = _pid;
            prevTicks = ticks;
            currTicks = ticks;
            intTicks = ticks;
            return 0;
        } else {
            //Also account for possibility of process restarting with same PID - negative tick delta
            long diff = ticks - currTicks;
            if (diff < 0) {
                prevTicks = ticks;
                currTicks = ticks;
                intTicks = ticks;
                return 0;
            } else {
                prevTicks = currTicks;
                currTicks = ticks;
                intTicks += diff;
                return diff;
            }
        }
    }
}