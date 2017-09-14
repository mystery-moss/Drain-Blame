package com.moss.drainblame;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import com.moss.drainblame.apps.App;
import com.moss.drainblame.intervals.Interval;
import com.moss.drainblame.intervals.IntervalHandler;

/*
 *  Handle intervalData file
 */

public class FileParsing {
    private static final String DEBUG = "File Parsing";
    private static final String FILENAME = "intervalData";
    private static final int VERSION = 6;

    private static final int DATA_FIELDS = 4; //Number of data fields at start of each interval line
    private static final int APP_FIELDS = 3;  //Number of data fields for each recorded app

    //Check whether external storage can be written
    private static boolean checkStorage(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    //Check whether external storage can be read
    private static boolean checkStorageReadOnly(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    //Write data file to external storage
    public static boolean writeFile(Context context, IntervalHandler intervalHandler){
        Log.d(DEBUG, "Writing intervals to file");
        if(!checkStorage()){
            return false;
        }

        Interval[] intervals = intervalHandler.getIntervals();
        int size = intervalHandler.numIntervals();

        //Don't write if no intervals recorded
        if(size == 0) {
            Log.d(DEBUG, "No intervals recorded, not writing to file");
            return true;
        }

        try {
            File path = context.getExternalFilesDir(null);
            File file = new File(path, FILENAME);

            String data = parseToString(intervals, size);

            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data.getBytes());
            stream.close();
        }
        catch(Exception e){
            Log.e(DEBUG, "Unable to write data to file - " + e);
            return false;
        }
        return true;
    }

    //Read from external storage, populate list of intervals in IntervalHandler
    public static boolean readFile(Context context, IntervalHandler monitorLibrary){
        Log.d(DEBUG, "Reading intervals from file");
        if(!checkStorageReadOnly()){
            return false;
        }

        //for each line in file, get string, pass to parse function
        try {
            File path = context.getExternalFilesDir(null);
            File file = new File(path, FILENAME);

            BufferedReader read = new BufferedReader(new FileReader(file));
            //Check file version
            String line = read.readLine();
            int version = Integer.parseInt(line);
            if(version != VERSION){
                Log.e(DEBUG, "Cannot read file - incompatible version - required " + VERSION + ", found " + version);
                return false;
            }
            while((line = read.readLine()) != null){
                parseFromString(line, monitorLibrary);
            }
            read.close();
        }
        catch(Exception e){
            Log.e(DEBUG, "Unable to read data file - " + e);
            return false;
        }
        return true;
    }

    //Convert 'Interval' store to string
    private static String parseToString(Interval[] intervals, int size) {
        StringBuilder data = new StringBuilder();
        data.append(VERSION).append('\n');

        for (int i = 0; i < size; i++) {
            Interval x = intervals[i];

            data.append(x.level).append(' ').append(x.length).append(' ').append(x.screenOnTime).append(' ').append(x.networkBytes);

            for (App p : x.activeApps) {
                data.append(' ').append(p.name).append(' ').append(p.ticks).append(' ').append(p.unknownPackage);
            }
            data.append('\n');
        }

        return data.toString();
    }

    //Read from string to 'Interval' store
    //Note that this adds to existing intervals, so don't repeatedly re-read same file
    private static void parseFromString(String string, IntervalHandler monitorLibrary){
        String[] data = string.split(" ");

        //Check overall formatting correct
        if(data.length < DATA_FIELDS){
            Log.e(DEBUG, "Error parsing line in data file");
            return;
        }

        int level = Integer.parseInt(data[0]);
        long length = Long.parseLong(data[1]);
        long screenOnTime = Long.parseLong(data[2]);
        long networkBytes = Long.parseLong(data[3]);

        int numProcs = (data.length - DATA_FIELDS) / APP_FIELDS;
        App[] activeProcs = new App[numProcs];

        if(numProcs > 0){
            for(int i = 0; i < numProcs; i++){
                activeProcs[i] = new App(
                        data[(APP_FIELDS * i) + DATA_FIELDS],
                        Long.parseLong(data[(APP_FIELDS * i) + DATA_FIELDS + 1]),
                        Boolean.valueOf(data[(APP_FIELDS * i) + DATA_FIELDS + 2]));
            }
        }

        monitorLibrary.populateInterval(new Interval(level, length, screenOnTime, networkBytes, activeProcs));
    }
}
