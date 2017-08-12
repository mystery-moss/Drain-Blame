package moss.mystery.energymonitor.classifier;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import moss.mystery.energymonitor.monitors.Interval;
import moss.mystery.energymonitor.monitors.MonitorLibrary;

public class ClassifierLibrary {
    private static final String DEBUG = "ClassifierLibrary";
    private static long shortint;
    private static long longint;

    //Determine thresholds for intervals
    public static void getThresholds(){
        ArrayList<Interval> intervals = MonitorLibrary.getIntervals();

        int size = intervals.size();

        //TODO: Fix this. Uses hardcoded values if insufficient data to give a useful result
        if(size < 5){
            shortint = 120000;
            longint = 1500000;
            return;
        }

        //Get average length of the longest 5 intervals
        long[] lengths = new long[size];

        int i = 0;
        for(Interval interval : intervals){
            lengths[i] = interval.length;
            ++i;
        }

        Arrays.sort(lengths);
        long avg = 0;
        for(i = size - 6; i < size; ++i){
            Log.d(DEBUG, "adding " + lengths[i]);
            if(lengths[i] < 1){
                continue;
            }
            avg += lengths[i];
        }
        avg /= 5;

        //A 'short' interval is < 1/3 of this value, long is > 2/3
        shortint = avg / 3;
        longint = shortint * 2;
        Log.d(DEBUG, "Short = " + shortint);
        Log.d(DEBUG, "Long = " + longint);
    }

    //TODO: Finalise this
    //Return an integer representing interval length - negative short, 0 medium, positive long
    public static int classifyInterval(long length){
        if(length < shortint){
            return -1;
        }
        if(length > longint){
            return 1;
        }
        return 0;
    }
}
