package moss.mystery.energymonitor.classifier;

import android.support.v4.util.ArraySet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import moss.mystery.energymonitor.apps.App;
import moss.mystery.energymonitor.intervals.Interval;
import moss.mystery.energymonitor.intervals.IntervalHandler;

//TODO: Make sure I'm not in a position where 'intervals' can be updated while I'm using it!
    //Ok, currently using a 'clone' which should be ok
public class ClassifierLibrary {
    private static final String DEBUG = "ClassifierLibrary";
    private static final double thresholdShort = 0.6;
    private static final double thresholdLong = 0.6;

    private static long cpuThreshold;
    private static long shortint;
    private static long longint;

    public static ArrayList<ClassifiedApp> classifiedApps = new ArrayList<ClassifiedApp>();

    //TODO: Gracefully handle too few intervals to do any useful work (including 0)
    public static void classify(IntervalHandler monitorLibrary){
        ArrayList<Interval> intervals = (ArrayList<Interval>) monitorLibrary.getIntervals().clone();

        setCpuThreshold();
        setBatteryThresholds(intervals);

        //PASS 0:
        //Remove any process in interval with ticks below CPU threshold
        for(Interval interval : intervals){
            ArrayList<App> pastThreshold = new ArrayList<App>();
            for(App info : interval.activeApps){
                if(info.ticks >= cpuThreshold){
                    pastThreshold.add(info);
                }
            }
            interval.activeApps = pastThreshold.toArray(new App[0]);
        }

        //PASS 0:
        //Remove any intervals with no active apps
        ArrayList<Interval> toRemove = new ArrayList<>();
        for(Interval interval : intervals){
            if(interval.activeApps.length == 0){
                toRemove.add(interval);
            }
        }
        for(Interval interval : toRemove){
            intervals.remove(interval);
        }

        //TODO: The 'import' for ArraySet - check it! There are two versions
        HashMap<String, ClassifiedApp> highDrain = new HashMap<String, ClassifiedApp>();

        //PASS 1:
        //Remove any processes active in a short interval with no other active processes
        //These have high confidence!
        //TODO: More efficient to just reset it?
        toRemove = new ArrayList<>();
        for(Interval interval : intervals){
            if(interval.length < shortint && interval.activeApps.length == 1){
                //If this app has not already been recorded, add to 'highDrain' list
                String name = interval.activeApps[0].name;
                ClassifiedApp app = highDrain.get(name);
                if(app == null) {
                    highDrain.put(name, new ClassifiedApp(interval.activeApps[0].name, "High", "High drain"));
                }

                toRemove.add(interval);
            }
        }
        for(Interval interval : toRemove){
            intervals.remove(interval);
        }

        //PASS 2:
        //If a high-drain app is active with other apps in a short interval, attribute the high-drain
        //to that app - remove this interval from consideration
        for(String target : highDrain.keySet()){
            removeHighDrain(target, intervals);
        }
        //TODO: What about if they are active in longer intervals?

        //TODO: Temporary kludge: just attempt to classify all here in one pass.
        //If app is active in >threshold% short intervals, mark it as short and attribute any high-drain intervals containing it to it

        //Get list of unclassified apps [[[which are active in short intervals]]]:
        ArraySet<String> unclassified = new ArraySet<String>();
        for(Interval interval : intervals){
            for(App proc : interval.activeApps){
                if(highDrain.get(proc.name) == null) {
                    unclassified.add(proc.name);
                }
            }
        }
        //For each such app, calculate percentage of intervals which are short and long
        for(String app : unclassified){
            int shortactive = 0;
            int longactive = 0;
            int totalactive = 0;
            for(Interval interval : intervals){
                for(App proc : interval.activeApps){
                    if(proc.name.equals(app)){
                        ++totalactive;
                        if(interval.length < shortint){
                            ++shortactive;
                        } else if(interval.length > longint){
                            ++longactive;
                        }
                        break;
                    }
                }
            }
            double shortpercent = (double) shortactive / totalactive;
            double longpercent = (double) longactive / totalactive;

            //Get percentages
            if(shortpercent > thresholdShort){
                classifiedApps.add(new ClassifiedApp(app, "Medium", "High drain"));
            } else if(longpercent > thresholdLong){
                classifiedApps.add(new ClassifiedApp(app, "Medium", "Low drain"));
            }
        }



        //PASS ??:
        //If a given process is active in >threshold% long intervals, mark it as long
        //TODO: More work here!
        //Need to account for possibility that it just isn't active very often!
        //So confidence will be related to total number of observed active intervals for this proc


        //Collate lists into final classifiedApp list
        for(String key : highDrain.keySet()){
            classifiedApps.add(highDrain.get(key));
        }
    }

    private static void removeHighDrain(String target, ArrayList<Interval> intervals){
        ArrayList<Interval> toRemove = new ArrayList<>();

        for(Interval interval : intervals){
            if(interval.length < shortint){
                for(App proc : interval.activeApps){
                    if(proc.name.equals(target)){
                        toRemove.add(interval);
                        break;
                    }
                }
            }
        }

        for(Interval interval : toRemove){
            intervals.remove(interval);
        }
    }


    private static void setCpuThreshold(){
        cpuThreshold = 200;
    }

    private static void setBatteryThresholds(ArrayList<Interval> intervals){
        //Extract all interval lengths
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
        shortint = avg / 5;
        longint = shortint * 2;
        Log.d(DEBUG, "Short = " + shortint);
        Log.d(DEBUG, "Long = " + longint);
    }
}
