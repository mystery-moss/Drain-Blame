package moss.mystery.energymonitor.processes;

import android.support.annotation.NonNull;

public class Process implements Comparable<Process>{
    public int pid;
    public long time;
    public String name;

    public Process(int pid, long time, String name){
        this.pid = pid;
        this.time = time;
        this.name = name;
    }

    @Override
    public int compareTo(@NonNull Process i){
        return this.pid - i.pid;
    }

}
