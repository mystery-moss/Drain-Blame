package com.moss.drainblame.classifier;

import com.moss.drainblame.apps.App;

public class UnclassifiedApp {
    public final App appRef;
    //Counts of active intervals
    public int shortactive = 0;
    public int longactive = 0;
    public int totalactive = 0;

    public UnclassifiedApp(App app){
        this.appRef = app;
    }

    //Proportion short intervals
    public float shortPercent(){
        if(totalactive == 0){
            return 0;
        }
        return (float) shortactive / totalactive;
    }

    //Proportion long
    public float longPercent(){
        if(totalactive == 0){
            return 0;
        }
        return (float) longactive / totalactive;
    }
}
