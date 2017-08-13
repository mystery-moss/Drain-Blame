package moss.mystery.energymonitor.processes;

import android.net.TrafficStats;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Adapted from https://github.com/jaredrummler/AndroidProcesses
 */

public class ProcessLibrary {
    private static final String PROC = "/proc";
    private static final String CMDLINE = "/proc/%d/cmdline";
    private static final String STAT = "/proc/%d/stat";
    private static final String DEBUG = "ProcessLibrary";

    private static boolean firstSample = true;          //Flag for handling special case
    private static HashMap<String, Process> processes;

    //TODO: This.
    public static boolean checkPermission(){
        return true;
    }

    //Tidy up process list
    public static void reset(){
        processes = new HashMap<String, Process>();
        firstSample = true;
    }

    //TODO: Look into optimisations here, as this will run regularly and takes the longest
    //Store a list of all files in directory, flag saying whether or not they are valid
    //If a new file is not in the list, check to see if valid
    //If a file in the list is no longer present in directory, remove from list
    //Edge case: Unreadable dir gets replaced by readable one with same name within one sample
    //Workaround: Refresh entire list every set number of samples, hope it wasn't important
    //Would need benchmarks to justify making this change
    public static void parseProcs(long threshold){
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
                ProcessTime time = getTicks(pid);
                if(time == null){
                    continue;
                }

                Process proc = processes.get(name);
                //If process not already recorded, add it to store
                if(proc == null){
                    if(firstSample){
                        //Handle special case (cannot know when ticks occurred)
                        processes.put(name, new Process(time));
                    } else {
                        //All ticks occurred in this interval, mark as active if past threshold
                        processes.put(name, new Process(time, time.ticks >= threshold));
                    }
                //Else update elapsed ticks
                } else {
                    proc.updateTicks(time, threshold);
                }
            }
        }
        firstSample = false;
    }

    //Get list of procs active in this interval, reset interval ticks and active flags for all procs
    public static ProcessInfo[] startNewSample(){
        List<ProcessInfo> procList = new ArrayList<ProcessInfo>();

        for(String key : processes.keySet()){
            Process proc = processes.get(key);
            if(proc.active){
                procList.add(new ProcessInfo(key, proc.intTicks));
                proc.active = false;
            }
            proc.intTicks = 0;
        }

        return procList.toArray(new ProcessInfo[0]);
    }

    public static HashMap<String, Process> processList(){
        return processes;
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

    private static ProcessTime getTicks(int pid){
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

        return new ProcessTime(utime + stime, start);
    }
}