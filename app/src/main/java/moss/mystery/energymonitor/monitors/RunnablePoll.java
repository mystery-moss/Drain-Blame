package moss.mystery.energymonitor.monitors;


import android.os.Handler;

import moss.mystery.energymonitor.processes.Process;
import moss.mystery.energymonitor.processes.ProcessHandler;

public class RunnablePoll implements Runnable{
    private final Handler handler;
    private final Long threshold;
    private final int interval;
    private final ProcessHandler processHandler;

    public RunnablePoll(Handler handler, Long threshold, int interval, ProcessHandler processHandler){
        this.handler = handler;
        this.threshold = threshold;
        this.interval = interval * 1000;
        this.processHandler = processHandler;
    }

    @Override
    public void run(){
        processHandler.parseProcs(threshold);
        handler.postDelayed(this, interval);
    }
}