package moss.mystery.energymonitor.processes;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

/**
 * Adapted from https://github.com/jaredrummler/AndroidProcesses
 */

public class ProcessLibrary {
    public static TreeSet<Process> processes;

    //TODO: This.
    public static boolean checkPermission(){
        return true;
    }

    public static boolean startup(){
        processes = new TreeSet<>();

        return checkPermission();
    }

    private static int[] pids;

    //TODO: Look into optimisations here, as this will run regularly
    public static void getPIDs(){
        File[] files = new File("/proc").listFiles();
        int[] pidsTemp = new int[files.length];
        int i = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                pidsTemp[i] = pid;
                i++;
            }
        }
        pids = new int[i];
        System.arraycopy(pidsTemp, 0, pids, 0, i);
    }

    public static void parsePIDs(){
        for(int pid: pids){
            addProc(pid);
        }
    }

    public static void addProc(int pid){
        BufferedReader reader = null;
        String name;
        try {
            reader = new BufferedReader(new FileReader(String.format("/proc/%d/cmdline", pid)));
            name = reader.readLine();   //TODO: Robustify! See getNames
        } catch (IOException e) {
            Log.e("ProcessLibrary", "Read error extracting name for process " + pid + ": " + e.toString());
            return;
        }
        long utime;
        long stime;
        try {
            reader = new BufferedReader(new FileReader(String.format("/proc/%d/stat", pid)));
            String line = reader.readLine(); //TODO: As above
            String[] fields = line.split("\\s+");
            utime = Integer.parseInt(fields[13]);
            stime = Integer.parseInt(fields[14]);
        } catch (IOException e) {
            Log.e("ProcessLibrary", "Read error extracting stat for process " + pid + ": " + e.toString());
            return;
        }
        processes.add(new Process(pid, stime + utime, name));
    }

//    public static void getNames(){
//        int length = pids.length;
//        String[] names = new String[length];
//        BufferedReader reader = null;
//        for(int i = 0; i < length; ++i) {
//            try {
//                reader = new BufferedReader(new FileReader(String.format("/proc/%d/cmdline", pids[i])));
//                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
//                    names[i] = line;
//                }
//            } catch (IOException e) {
//                Log.e("ProcessLibrary", "Read error extracting name for process " + pids[i] + ": " + e.toString());
//            }
//        }
//        if(reader != null) {
//            try {
//                reader.close();
//            } catch (IOException ignored) {
//            }
//        }
//    }
//
//    //TODO: This can certainly be optimised.
//    public static void extractTime(int[] pids){
//        int length = pids.length;
//        utime = new int[length];
//        stime = new int[length];
//        BufferedReader reader = null;
//        for(int i = 0; i < length; ++i) {
//            try {
//                reader = new BufferedReader(new FileReader(String.format("/proc/%d/stat", pids[i])));
//                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
//                    String[] fields = line.split("\\s+");
//                    utime[i] = Integer.parseInt(fields[13]);
//                    stime[i] = Integer.parseInt(fields[14]);
//                }
//            } catch (IOException e) {
//                Log.e("ProcessLibrary", "Read error extracting name for process " + pids[i] + ": " + e.toString());
//            }
//        }
//        if(reader != null) {
//            try {
//                reader.close();
//            } catch (IOException ignored) {
//            }
//        }
//    }
}
