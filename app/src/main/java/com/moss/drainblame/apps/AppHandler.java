package com.moss.drainblame.apps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Keeps track of which applications have been active and when
 */

public class AppHandler {
    private HashMap<String, App> apps;
    private Context appContext;

    public AppHandler(Context appContext){
        this.apps = new HashMap<>();
        this.appContext = appContext;
    }

    //Return app reference based on /proc/[pid]/cmdline, creating if none exists
    public App getApp(String processName){
        //Find ApplicationInfo associated with this name
        PackageManager pm = appContext.getPackageManager();
        ApplicationInfo ai = getApplicationInfo(processName, pm);
        String name;

        //If ai cannot be found for this process, use processName as its ID
        if(ai == null) {
            name = processName;
        } else {
            name = ai.packageName;
        }

        App app = apps.get(name);
        if(app == null){
            app = new App(name, ai == null);
            apps.put(name, app);
        }
        return app;
    }

    //Return array of apps that were active in the current sample, and reset all activity ticks to 0
    public App[] startNewSample(long threshold){
        ArrayList<App> activeApps = new ArrayList<>();

        for(String key : apps.keySet()){
            App app = apps.get(key);
            if(app.ticks > threshold){
                activeApps.add(new App(app.name, app.ticks, app.unknownPackage));
            }
            app.ticks = 0;
        }

        return activeApps.toArray(new App[0]);
    }

    //Reset all app ticks to 0
    public void resetTicks(){
        for(String key : apps.keySet()){
            App app = apps.get(key);
            app.ticks = 0;
        }
    }

    //Query PackageManager to get the label associated with a given /proc/[pid]/cmdline
    private ApplicationInfo getApplicationInfo(String name, PackageManager pm) {
        ApplicationInfo ai;
        ArrayList<String> testStrings = new ArrayList<>();

        //If name extracted from /proc/[pid]/cmdline contains and '/'s, split on them
        if (name.contains("/")) {
            String[] slashSplit = name.split("[/]");
            //Assuming name is of the form 'x.y.[...z]', so look for '.'s
            for (String str : slashSplit) {
                if (str.contains(".")) {
                    testStrings.add(str);
                }
            }
            //If no substrings containing '.'s are found, just try the last substring
            if (testStrings.size() == 0) {
                testStrings.add(slashSplit[slashSplit.length - 1]);
            }
        } else {
            testStrings.add(name);
        }

        //Now have one (or potentially multiple) target strings to test. May be of forms:
        //"x", "x.y...", or "x.y:z", where ':' could be any special char
        //Want to try trimming anything following a special char from the end of the string, request
        //appInfo from result. Repeat until either an app is found or we run out of things to trim
        for (String str : testStrings) {
            //Try this string as the package name
            try {
                ai = pm.getApplicationInfo(str, 0);
                return ai;
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            int i = str.length() - 1;
            char c;
            while (i > 0) {
                //Find position of last non-char symbol in the string
                c = str.charAt(i);
                if (!Character.isLetter(c)) {
                    //Remove everything from this char onwards, use result as package name
                    try {
                        ai = pm.getApplicationInfo(str.substring(0, i), 0);
                        return ai;
                    } catch (PackageManager.NameNotFoundException ignored) {
                    }
                }
                --i;
            }
        }
        return null;
    }
}
