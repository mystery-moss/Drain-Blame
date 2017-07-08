package moss.mystery.energymonitor.processes;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/**
 * Adapted from https://github.com/jaredrummler/AndroidProcesses
 */

public class ProcessLibrary {
    private static final String PROC = "/proc";
    private static final String CMDLINE = "/proc/%d/cmdline";
    private static final String STAT = "/proc/%d/stat";
    private static final String DEBUG = "ProcessLibrary";
    public static HashMap<String, Process> processes;

    //TODO: This.
    public static boolean checkPermission(){
        return true;
    }

    //TODO: This resets all process info, so don't forget to remove it later!
    public static boolean startup(){
        processes = new HashMap<String, Process>();

        return checkPermission();
    }

    //TODO: Look into optimisations here, as this will run regularly and takes the longest
    //Main optimisation would be to store list of forbidden files, avoid trying to access them each call
    public static void parseProcs(){
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
                //Get process time values
                long time = getTime(pid);
                if(time == -1){
                    continue;
                }

                //If process not already recorded, add it to store, else update elapsed time
                Process proc = processes.get(name);
                if(proc == null){
                    processes.put(name, new Process(time));
                } else {
                    proc.updateTime(time);
                }
            }
        }
    }

    //TODO: Robustify - check best practices for handling BufferedReaders
    private static String getName(int pid){
        BufferedReader reader = null;
        String name;
        try {
            reader = new BufferedReader(new FileReader(String.format(Locale.US, CMDLINE, pid)));
            name = reader.readLine();   //TODO: Robustify! See getNames
        } catch (IOException e) {
            Log.e(DEBUG, "Read error extracting name for process " + pid + ": " + e.toString());
            return null;
        }
        try {
            reader.close();
        } catch (IOException ignored) {
        }
        return name;
    }

    private static long getTime(int pid){
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(String.format(Locale.US, STAT, pid)));
            line = reader.readLine(); //TODO: As above
        } catch (IOException e) {
            Log.e(DEBUG, "Read error extracting stat for process " + pid + ": " + e.toString());
            return -1;
        }
        try {
            reader.close();
        } catch (IOException ignored) {
        }

        String[] fields = line.split(" "); //TODO: Test that this works consistently, vs regex for multiple spaces
        int utime = Integer.parseInt(fields[13]);
        int stime = Integer.parseInt(fields[14]);

        return utime + stime;
    }
}
