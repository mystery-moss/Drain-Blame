package moss.mystery.energymonitor.classifier;

import android.support.v4.util.ArraySet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import moss.mystery.energymonitor.apps.App;
import moss.mystery.energymonitor.intervals.Interval;
import moss.mystery.energymonitor.intervals.IntervalHandler;

//TODO: Make sure I'm not in a position where 'intervals' can be updated while I'm using it!
    //Ok, currently using a 'clone' which should be ok
public class Classifier {
    private static final String DEBUG = "Classifier";
    private static final int MIN_INTERVALS = 10;     //Minimum total intervals required to attempt classification
    private static final int MIN_APP_INTERVALS = 4;  //Minimum intervals for a single app required
    private static final float MAX_APP_PERCENTAGE = 0.9f;   //Apps active in too many intervals cannot be reliably classified
    private static final float HIGH_CONFIDENCE = 0.75f;
    private static final float MEDIUM_CONFIDENCE = 0.65f;

    private static final int HIGH = 2;
    private static final int MEDIUM = 1;
    private static final int LOW = 0;

    private long cpuThreshold;
    private long shortint;
    private long longint;
    private long networkThreshold;

    private ArrayList<ClassifiedApp> classifiedApps;
    private HashMap<String, UnclassifiedApp> unclassifiedApps;
    private HashSet<String> highDrain;
    private HashSet<String> mediumDrain;
    private HashSet<String> lowDrain;
    private HashSet<String> unclassified;
    private HashSet<String> insufficientInfo;

    public Classifier(){
        classifiedApps = new ArrayList<>();
        unclassifiedApps = new HashMap<>();
        highDrain = new HashSet<>();
        mediumDrain = new HashSet<>();
        lowDrain = new HashSet<>();
        unclassified = new HashSet<>();
        insufficientInfo = new HashSet<>();
    }

    public ClassifiedApp[] getClassifiedApps(){
        return classifiedApps.toArray(new ClassifiedApp[0]);
    }

    public boolean classify(Interval[] intervalArray, int size){
        if(size < MIN_INTERVALS){
            return false;
        }

        //Convert to list for ease of removing intervals, truncating to appropriate size as required
        ArrayList<Interval> intervals = new ArrayList<>(Arrays.asList(Arrays.copyOf(intervalArray, size)));

        setCPUThreshold();
        setThresholds(intervals);

        //SETUP====================================================================================
        //Remove any process in interval with ticks below CPU threshold
        for(Interval interval : intervals){
            ArrayList<App> pastThreshold = new ArrayList<>();
            for(App app : interval.activeApps){
                if(app.ticks >= cpuThreshold){
                    pastThreshold.add(app);
                }
            }
            interval.activeApps = pastThreshold.toArray(new App[0]);
        }

        //Remove any intervals with no active apps
        ArrayList<Interval> toRemove = new ArrayList<>();
        for(Interval interval : intervals){
            if(interval.activeApps.length == 0){
                toRemove.add(interval);
            }
        }
        intervals.removeAll(toRemove);
        toRemove.clear();

        //Record interval lengths of each app, mark as unclassified
        for(Interval interval : intervals){
            for(App app : interval.activeApps){
                unclassified.add(app.name);
                UnclassifiedApp uApp = unclassifiedApps.get(app.name);
                if(uApp == null){
                    uApp = new UnclassifiedApp(app);
                    unclassifiedApps.put(app.name, uApp);
                }
                ++uApp.totalactive;
                if(interval.length < shortint){
                    ++uApp.shortactive;
                } else if(interval.length > longint){
                    ++uApp.longactive;
                }
            }
        }

        //If any app is observed in too few intervals to make a meaningful classification, ignore it
        for(String name : unclassified){
            UnclassifiedApp app = unclassifiedApps.get(name);
            if(app.totalactive < MIN_APP_INTERVALS){
                unclassifiedApps.remove(name);
                insufficientInfo.add(name);
            }
        }
        unclassified.removeAll(insufficientInfo);

        //PASS 1===================================================================================
        //If more than 80% of intervals containing an app are short, mark it as high drain.
        //If more than 80% are long, mark as low drain.
        //If more than 80% are medium, medium drain (unlikely, but hey, it could happen)
        for(String name : unclassified){
            UnclassifiedApp app = unclassifiedApps.get(name);
            if(app.shortPercent() >= MEDIUM_CONFIDENCE){
                classifyApp(app.appRef, classifiedApps, HIGH, app.shortPercent() >= HIGH_CONFIDENCE ? HIGH : MEDIUM);
                unclassifiedApps.remove(name);
                highDrain.add(name);
            } else if(app.longPercent() >= MEDIUM_CONFIDENCE){
                classifyApp(app.appRef, classifiedApps, LOW, app.longPercent() >= HIGH_CONFIDENCE ? HIGH : MEDIUM);
                unclassifiedApps.remove(name);
                lowDrain.add(name);
            } else if(app.shortPercent() + app.longPercent() <= 1- MEDIUM_CONFIDENCE){
                classifyApp(app.appRef, classifiedApps, MEDIUM, (app.longPercent() + app.shortPercent() <= 1 - HIGH_CONFIDENCE) ? HIGH : MEDIUM);
                unclassifiedApps.remove(name);
                mediumDrain.add(name);
            }
        }
        unclassified.removeAll(highDrain);
        unclassified.removeAll(mediumDrain);
        unclassified.removeAll(lowDrain);


        //PASS 2===================================================================================
        //Modality - Network:
        //For all unclassified apps, check whether they are high drain when running in intervals
        //with above-average network usage

        for(String name : unclassified) {
            UnclassifiedApp app = unclassifiedApps.get(name);
            UnclassifiedApp appNet = new UnclassifiedApp(null);
            for (Interval interval : intervals) {
                for (App activeApp : interval.activeApps) {
                    if (activeApp.name.equals(name)) {
                        if (interval.networkBytes >= networkThreshold) {
                            ++appNet.totalactive;
                            if (interval.length < shortint) {
                                ++appNet.shortactive;
                            } else if (interval.length > longint) {
                                ++appNet.longactive;
                            }
                        }
                        break;
                    }
                }
            }

            //Ignore if insufficient observed intervals
            if (appNet.totalactive < MIN_APP_INTERVALS) {
                continue;
            }

            //Attempt classification
            if (appNet.shortPercent() >= MEDIUM_CONFIDENCE) {
                classifyApp(app.appRef, classifiedApps, HIGH, appNet.shortPercent() >= HIGH_CONFIDENCE ? HIGH : MEDIUM, true);
                unclassifiedApps.remove(name);
                highDrain.add(name);
            }
        }
        unclassified.removeAll(highDrain);

        //PASS 3===================================================================================
        //Have now classified all high and medium confidence apps. Attempt low confidence classification:
        //Combine high drain and medium drain interval proportion to attribute app as medium drain
        for(String name : unclassified){
            UnclassifiedApp app = unclassifiedApps.get(name);
            //If short + medium >= MEDIUM_CONFIDENCE
            if(app.longPercent() <= 1 - MEDIUM_CONFIDENCE){
                classifyApp(app.appRef, classifiedApps, MEDIUM, app.longPercent() <= 1 - HIGH_CONFIDENCE ? MEDIUM : LOW);
                unclassifiedApps.remove(name);
                mediumDrain.add(name);
                //If medium + long >= HIGH_CONFIDENCE
            } else if(app.shortPercent() <= 1 - HIGH_CONFIDENCE){
                classifyApp(app.appRef, classifiedApps, LOW, LOW);
                unclassifiedApps.remove(name);
                lowDrain.add(name);
            }
        }
        unclassified.removeAll(mediumDrain);
        unclassified.removeAll(lowDrain);

        return true;
    }

    private void classifyApp(App app, ArrayList<ClassifiedApp> list, int classification, int confidence){
        classifyApp(app, list, classification, confidence, false);
    }

    private void classifyApp(App app, ArrayList<ClassifiedApp> list, int classification, int confidence, boolean network){
        list.add(new ClassifiedApp(app.name, classification, confidence, app.unknownApp, network));
    }

    private void removeHighDrain(String target, ArrayList<Interval> intervals, ArrayList<Interval> toRemove){
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
        intervals.removeAll(toRemove);
        toRemove.clear();
    }


    private void setCPUThreshold(){
        cpuThreshold = 200;
    }

    private void setThresholds(ArrayList<Interval> intervals){
        //Extract all interval lengths
        int size = intervals.size();

        //Get average interval length + network usage
        long avgBatt = 0;
        long avgNet = 0;

        for(Interval interval : intervals){
            avgBatt += interval.length;
            avgNet += interval.networkBytes;
        }
        avgBatt /= size;
        avgNet /= size;

        //Short is <3/4 of average, long >5/4
        shortint = (avgBatt/4) * 3;
        longint = (avgBatt/4) * 5;

        //Low/high network is simply below/above average
        networkThreshold = avgNet;
    }
}