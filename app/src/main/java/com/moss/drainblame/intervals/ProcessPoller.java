package com.moss.drainblame.intervals;

import android.os.Handler;

import com.moss.drainblame.processes.ProcessHandler;

/*
 *  Periodically calls parseProcs()
 */

public class ProcessPoller implements Runnable{
    private final Handler handler;
    private final int interval;
    private final ProcessHandler processHandler;

    public ProcessPoller(Handler handler, int interval, ProcessHandler processHandler){
        this.handler = handler;
        this.interval = interval * 1000;
        this.processHandler = processHandler;
    }

    @Override
    public void run(){
        processHandler.parseProcs();
        handler.postDelayed(this, interval);
    }
}