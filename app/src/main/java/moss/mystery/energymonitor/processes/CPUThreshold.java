package moss.mystery.energymonitor.processes;

//Determine number of CPU ticks for an 'active' process.
//Preliminary implementation!

import java.util.HashMap;

import moss.mystery.energymonitor.BuildConfig;

public class CPUThreshold {
    private static final int scaling = 4;

    public static long getThreshold(){
        //For now, just run parseProcs 6 times, record CPU ticks used to do this.

        ProcessLibrary.parseProcs(0);
        HashMap<String, Process> procs = ProcessLibrary.processList();
        Process app = procs.get(BuildConfig.APPLICATION_ID);

        app.intTicks = 0;

        for(int i = 0; i < 6; i++){
            ProcessLibrary.parseProcs(0);
        }

        long ticks = app.intTicks;

        ProcessLibrary.reset();

        return ticks * scaling;
    }
}
