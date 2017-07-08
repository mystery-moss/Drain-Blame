package moss.mystery.energymonitor.processes;

import android.support.annotation.NonNull;

public class Process{
    public long startTime;
    public long elapsedTime;

    public Process(long startTime){
        this.startTime = startTime;
        this.elapsedTime = 0;
    }

    //Account for potential resets of utime and stime
    public void updateTime(long time){
        long diff = time - startTime;
        if(diff > 0){
            elapsedTime += diff;
        }
        startTime = time;
    }
}
