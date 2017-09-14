package com.moss.drainblame.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.moss.drainblame.apps.App;
import com.moss.drainblame.intervals.Interval;

/*
 *  Classify apps. Currently instantiated once per classification, so could be converted to a
 *  single static method.
 */

public class Classifier {
    private static final String DEBUG = "Classifier";
    private static final int MIN_INTERVALS = 10;     //Minimum total intervals required to attempt classification
    private static final int MIN_APP_INTERVALS = 6;  //Minimum intervals to classify a single app
    private static final float HIGH_CONFIDENCE = 0.75f;
    private static final float MEDIUM_CONFIDENCE = 0.65f;

    //Classification/confidence levels
    public static final int HIGH = 2;
    public static final int MEDIUM = 1;
    public static final int LOW = 0;

    private ArrayList<Interval> intervals;
    private int size;

    private long cpuThreshold;
    //Network activity threshold to class interval as being 'high network'
    private long networkThreshold;

    //Cutoff points for different interval classes
    private long shortint;
    private long longint;

    //Final list
    private ArrayList<ClassifiedApp> classifiedApps;

    //Initial list
    private HashMap<String, UnclassifiedApp> unclassifiedApps;

    //Intermediate lists
    private HashSet<String> highDrain = new HashSet<>();
    private HashSet<String> mediumDrain = new HashSet<>();
    private HashSet<String> lowDrain = new HashSet<>();
    private HashSet<String> unclassified = new HashSet<>();
    private HashSet<String> insufficientInfo = new HashSet<>();

    public Classifier(Interval[] intervalArray, int size){
        classifiedApps = new ArrayList<>();
        unclassifiedApps = new HashMap<>();

        this.size = size;
        //Convert to list for ease of removing intervals, truncating to appropriate size as required
        this.intervals = new ArrayList<>(Arrays.asList(Arrays.copyOf(intervalArray, size)));
    }

    public ClassifiedApp[] getClassifiedApps(){
        ArrayList<ClassifiedApp> output = new ArrayList<>();

        //Sort output array in order high, medium drain and high, medium confidence.
        //Remove any low confidence and low drain apps
        for(int i = HIGH; i > LOW; --i){
            for(int j = HIGH; j > LOW; --j){
                for(ClassifiedApp app : classifiedApps){
                    if(app.classification == i && app.confidence == j){
                        output.add(app);
                    }
                }
            }
        }

        return output.toArray(new ClassifiedApp[0]);
    }

    public int classify(){
        if(size < MIN_INTERVALS){
            return 0;
        }

        setCPUThreshold();
        setThresholds();

        //SETUP====================================================================================
        //Remove any app in interval with ticks below CPU threshold
        //Not required if cpuThreshold == hardcoded value in intervalHandler
        //NB: Intervals are references to those stored in intervalHandler; deleting apps here
        //permanently removes them from the stored interval data! To avoid this, could perform this
        //check when recording app interval lengths instead
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
        //If more than x% of intervals containing an app are short, mark it as high drain, etc
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
        //Combine high drain and medium drain interval proportion to classify app as medium drain
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

        //Return number of classified apps
        return classifiedApps.size();
    }

    private void classifyApp(App app, ArrayList<ClassifiedApp> list, int classification, int confidence){
        classifyApp(app, list, classification, confidence, false);
    }

    private void classifyApp(App app, ArrayList<ClassifiedApp> list, int classification, int confidence, boolean network){
        list.add(new ClassifiedApp(app.name, classification, confidence, app.unknownPackage, network));
    }

    private void setCPUThreshold(){
        cpuThreshold = 200;
    }

    //Calculate thresholds for interval lengths and hardware resource activities
    private void setThresholds(){
        //Get average interval length + network usage
        long avgBatt = 0;
        long avgNet = 0;

        for(Interval interval : intervals){
            avgBatt += interval.length;
            avgNet += interval.networkBytes;
        }
        avgBatt /= size;
        avgNet /= size;

        //Short interval is <3/4 of average, long >5/4
        shortint = (avgBatt/4) * 3;
        longint = (avgBatt/4) * 5;

        //Low/high network is simply below/above average
        networkThreshold = avgNet;
    }
}