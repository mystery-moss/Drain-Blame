package moss.mystery.energymonitor.processes;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Adapted from https://github.com/jaredrummler/AndroidProcesses
 */

public class ProcessLibrary {
    public static String[] utime;
    public static String[] stime;

    //TODO: This.
    public static boolean checkPermission(){
        return true;
    }

    //TODO: Look into optimisations here, as this will run regularly
    public static int[] getPIDs(){
        File[] files = new File("/proc").listFiles();
        int[] processes = new int[files.length];
        int i = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                processes[i] = pid;
                i++;
            }
        }

        //Shrink array
        int[] array = new int[i];
        System.arraycopy(processes, 0, array, 0, i);

        return array;
    }

    public static String[] getNames(int[] pids){
        int length = pids.length;
        String[] names = new String[length];
        BufferedReader reader = null;
        for(int i = 0; i < length; ++i) {
            try {
                reader = new BufferedReader(new FileReader(String.format("/proc/%d/cmdline", pids[i])));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    names[i] = line;
                }
            } catch (IOException e) {
                Log.e("ProcessLibrary", "Read error extracting name for process " + pids[i] + ": " + e.toString());
            }
        }
        if(reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }

        return names;
    }

    //TODO: This can certainly be optimised.
    public static void extractTime(int[] pids){
        int length = pids.length;
        utime = new String[length];
        stime = new String[length];
        BufferedReader reader = null;
        for(int i = 0; i < length; ++i) {
            try {
                reader = new BufferedReader(new FileReader(String.format("/proc/%d/stat", pids[i])));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String[] fields = line.split("\\s+");
                    utime[i] = fields[13];
                    stime[i] = fields[14];
                }
            } catch (IOException e) {
                Log.e("ProcessLibrary", "Read error extracting name for process " + pids[i] + ": " + e.toString());
            }
        }
        if(reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }
}
