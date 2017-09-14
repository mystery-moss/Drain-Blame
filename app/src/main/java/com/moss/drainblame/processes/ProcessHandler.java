package com.moss.drainblame.processes;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import com.moss.drainblame.apps.AppHandler;

/*
 * Adapted from https://github.com/jaredrummler/AndroidProcesses
 * Queries for currently running processes, keeps track of those previously observed
 */

public class ProcessHandler {
    private static final String PROC = "/proc";
    private static final String CMDLINE = "/proc/%d/cmdline";
    private static final String STAT = "/proc/%d/stat";
    private static final String DEBUG = "ProcessHandler";

    private final HashMap<String, Process> processes;
    private final AppHandler appHandler;
    private boolean firstSample; //Flag for handling special case

    public ProcessHandler(AppHandler appHandler){
        this.processes = new HashMap<>();
        this.firstSample = true;
        this.appHandler = appHandler;
    }

    public void reset(){
        processes.clear();
        firstSample = true;
    }

    public void parseProcs(){
        Log.d(DEBUG, "Parsing processes");
        //Parse /proc directory
        File[] files = new File(PROC).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
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
                if(proc == null) {
                    processes.put(name, new Process(time, name, appHandler, firstSample));
                //Else update elapsed ticks
                } else {
                    proc.updateTicks(time);
                }
            }
        }
        firstSample = false;
    }

    //Extract name from /proc/[pid]/cmdline
    private static String getName(int pid){
        BufferedReader reader = null;
        String name = null;
        try {
            reader = new BufferedReader(new FileReader(String.format(Locale.US, CMDLINE, pid)));
            name = reader.readLine();
        } catch (IOException e) {
            Log.e(DEBUG, "Read error extracting name for process " + pid + ": " + e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }

        return name == null ? null : name.trim();
    }

    //Extract information from /proc/[pid]/stat
    private static CPUTicks getTicks(int pid){
        BufferedReader reader = null;
        String line = null;
        try {
            reader = new BufferedReader(new FileReader(String.format(Locale.US, STAT, pid)));
            line = reader.readLine();
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

        String[] fields = line.split("\\s+");
        long utime = Long.parseLong(fields[13]);
        long stime = Long.parseLong(fields[14]);
        long start = Long.parseLong(fields[21]);

        return new CPUTicks(utime + stime, start);
    }

    //Check whether we are allowed to read information in /proc
    //Lifted straight from https://github.com/jaredrummler/AndroidProcesses - see for more info
    public static boolean noReadPermission() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/mounts"));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] columns = line.split("\\s+");
                if (columns.length == 6 && columns[1].equals("/proc")) {
                    return columns[3].contains("hidepid=1") || columns[3].contains("hidepid=2");
                }
            }
        } catch (IOException e) {
            Log.d(DEBUG, "Error reading /proc/mounts. Checking if UID 'readproc' exists.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return android.os.Process.getUidForName("readproc") == 3009;
    }
}