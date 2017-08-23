package moss.mystery.energymonitor.classifier;

import moss.mystery.energymonitor.apps.App;

public class UnclassifiedApp {
    public final App appRef;
    public int shortactive = 0;
    public int longactive = 0;
    public int totalactive = 0;

    public UnclassifiedApp(App app){
        this.appRef = app;
    }

    public float shortPercent(){
        if(totalactive == 0){
            return 0;
        }
        return (float) shortactive / totalactive;
    }

    public float longPercent(){
        if(totalactive == 0){
            return 0;
        }
        return (float) longactive / totalactive;
    }
}
