package moss.mystery.energymonitor.monitors;


import android.os.Handler;

import moss.mystery.energymonitor.processes.ProcessLibrary;

public class RunnablePoll implements Runnable{
    private final Handler handler;
    private final Long threshold;
    private final int interval;

    public RunnablePoll(Handler h, Long t, int i){
        handler = h;
        threshold = t;
        interval = i * 1000;
    }

    @Override
    public void run(){
        ProcessLibrary.parseProcs(threshold);
        handler.postDelayed(this, interval);
    }
}