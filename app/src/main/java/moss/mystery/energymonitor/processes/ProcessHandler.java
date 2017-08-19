package moss.mystery.energymonitor.processes;

import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import moss.mystery.energymonitor.apps.App;
import moss.mystery.energymonitor.apps.AppHandler;

/**
 * Adapted from https://github.com/jaredrummler/AndroidProcesses
 * Queries for currently running processes, keeps track of those previously observed
 */

public class ProcessHandler {
    private static final String PROC = "/proc";
    private static final String CMDLINE = "/proc/%d/cmdline";
    private static final String STAT = "/proc/%d/stat";
    private static final String DEBUG = "ProcessHandler";

    private boolean firstSample = true;          //Flag for handling special case
    private HashMap<String, Process> processes;
    private AppHandler appHandler;

    public ProcessHandler(AppHandler _appHandler){
        processes = new HashMap<>();
        firstSample = true;
        appHandler = _appHandler;
    }

    //Tidy up process list
    public void reset(){
        //TODO: Is it more efficient to empty the existing hashmap?
        processes = new HashMap<>();
        firstSample = true;
    }

    //TODO: Look into optimisations here, as this will run regularly and takes the longest
    //Store a list of all files in directory, flag saying whether or not they are valid
    //If a new file is not in the list, check to see if valid
    //If a file in the list is no longer present in directory, remove from list
    //Edge case: Unreadable dir gets replaced by readable one with same name within one sample
    //Workaround: Refresh entire list every set number of samples, hope it wasn't important
    //Would need benchmarks to justify making this change
    public void parseProcs(long threshold){
        Log.d(DEBUG, "Parsing processes");
        //Parse /proc directory
        File[] files = new File(PROC).listFiles();
        for (File file : files) {
            if (file.isDirectory()) { //TODO: TEST - If file no longer exists, this just returns false and all is well
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                //Get process name
                String name = getName(pid);
                if(name == null){
                    continue;
                }
                //Get process CPU tick values
                CPUTicks time = getTicks(pid);
                if(time == null){
                    continue;
                }

                Process proc = processes.get(name);
                //If process not already recorded, add it to store
                if(proc == null){
                    if(firstSample){
                        //Handle special case (cannot know when ticks occurred)
                        processes.put(name, new Process(time, name, appHandler, 0));
                    } else {
                        //All ticks occurred in this interval
                        processes.put(name, new Process(time, name, appHandler));
                    }
                //Else update elapsed ticks
                } else {
                    proc.updateTicks(time);
                }
            }
        }
        firstSample = false;
    }

    //TODO: Robustify - check best practices for handling BufferedReaders
    private static String getName(int pid){
        BufferedReader reader = null;
        String name = null;
        try {
            reader = new BufferedReader(new FileReader(String.format(Locale.US, CMDLINE, pid)));
            name = reader.readLine();   //TODO: Robustify! See getNames
        } catch (IOException e) {
            Log.e(DEBUG, "Read error extracting name for process " + pid + ": " + e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }

        if(name == null){
            return null;
        }
        return name.trim();
    }

    private static CPUTicks getTicks(int pid){
        BufferedReader reader = null;
        String line = null;
        try {
            reader = new BufferedReader(new FileReader(String.format(Locale.US, STAT, pid)));
            line = reader.readLine(); //TODO: As above
        } catch (IOException e) {
            Log.e(DEBUG, "Read error extracting stat for process " + pid + ": " + e);
            return null;
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException ignored){}
            }
        }

        if(line == null){
            return null;
        }

        String[] fields = line.split(" "); //TODO: Test that this works consistently, vs regex for multiple spaces
        long utime = Long.parseLong(fields[13]);
        long stime = Long.parseLong(fields[14]);
        long start = Long.parseLong(fields[21]);

        return new CPUTicks(utime + stime, start);
    }

    //TODO: This.
    //Check whether we are allowed to read information in /proc
    public static boolean checkPermission(){
        return true;
    }
}