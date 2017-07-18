package moss.mystery.energymonitor.processes;

public class Process{
    public long prevTicks;
    public long currTicks;
    private int pid;

    public boolean active;


    public Process(long tick, int _pid){
        prevTicks = tick;
        currTicks = tick;
        pid = _pid;
        active = false;
    }

    public long updateTicks(long ticks, int _pid){
        //Account for process restarts
        if(pid != _pid){
            pid = _pid;
            prevTicks = ticks;
            currTicks = ticks;
            return 0;
        } else {
            //Also account for possibility of process restarting with same PID - negative tick delta
            long diff = ticks - currTicks;
            if (diff < 0) {
                prevTicks = ticks;
                currTicks = ticks;
                return 0;
            } else {
                prevTicks = currTicks;
                currTicks = ticks;
                return diff;
            }
        }
    }
}