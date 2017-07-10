package moss.mystery.energymonitor.processes;

public class Process{
    public long startTime;
    public long elapsedTime;
    private int pid;

    public Process(long _startTime, int _pid){
        startTime = _startTime;
        elapsedTime = 0;
        pid = _pid;
    }

    //Account for process restarts
    public void updateTime(long time, int _pid){
        if(pid != _pid){
            pid = _pid;
            startTime = time;
        } else {
            long diff = time - startTime;
            //Also account for possibility of process restarting with same PID - negative time delta
            if (diff > 0) {
                elapsedTime += diff;
            }
            startTime = time;
        }
    }
}