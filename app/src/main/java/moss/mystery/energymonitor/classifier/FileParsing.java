package moss.mystery.energymonitor.classifier;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

import moss.mystery.energymonitor.monitors.Interval;
import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.processes.ActiveApp;

public class FileParsing {
    //TODO: Look into making error catching here more robust? E.g. have closing files in 'finally' block rather than within the try?
    private static final String DEBUG = "File Parsing";

    //Check whether external storage can be written
    public static boolean checkStorage(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    //Check whether external storage can be read
    public static boolean checkStorageReadOnly(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    //Write data file to external storage
    public static boolean writeFile(Context context, MonitorLibrary monitorLibrary){
        Log.d(DEBUG, "Writing intervals to file");
        if(!checkStorage()){
            return false;
        }

        ArrayList<Interval> intervals = monitorLibrary.getIntervals();
        //Don't write if no intervals recorded
        if(intervals == null || intervals.size() == 0) {
            Log.d(DEBUG, "No intervals recorded, not writing to file");
            return true;
        }

        try {
            File path = context.getExternalFilesDir(null);
            Log.d(DEBUG, "Path = " + path.toString());
            File file = new File(path, "intervalData");

            String data = parseToString(intervals);

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

    public static boolean readFile(Context context, MonitorLibrary monitorLibrary){
        Log.d(DEBUG, "Reading intervals from file");
        if(!checkStorageReadOnly()){
            return false;
        }

        //for each line in file, get string pass to parse function
        try {
            File path = context.getExternalFilesDir(null);
            File file = new File(path, "intervalData");

            BufferedReader read = new BufferedReader(new FileReader(file));
            String line;
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
    private static String parseToString(ArrayList<Interval> intervals) {
        int size = intervals.size();
        StringBuilder data = new StringBuilder(size + '\n');

        for (int i = 0; i < size; i++) {
            Interval x = intervals.get(i);

            data.append(x.level).append(' ').append(x.length).append(' ').append(x.screenOnTime).append(' ').append(x.networkBytes);

            for (ActiveApp p : x.activeProcs) {
                data.append(' ').append(p.name).append(' ').append(p.ticks);
            }
            data.append('\n');
        }

        return data.toString();
    }

    //Read from string to 'Interval' store
    //Note that this adds to existing intervals, so don't repeatedly re-read same file
    private static void parseFromString(String string, MonitorLibrary monitorLibrary){
        String[] data = string.split(" ");

        //Check overall formatting correct
        if(data.length < 3){
            Log.e(DEBUG, "Error parsing line in data file");
            return;
        }

        //TODO: Error checking here!!!
        int level = Integer.parseInt(data[0]);
        long length = Long.parseLong(data[1]);
        long screenOnTime = Long.parseLong(data[2]);
        long networkBytes = Long.parseLong(data[3]);

        int numProcs = (data.length - 4) / 2;
        ActiveApp[] activeProcs = new ActiveApp[numProcs];


        if(numProcs > 0){
            for(int i = 0; i < numProcs; i++){
                activeProcs[i] = new ActiveApp(data[(2 * i) + 4], Long.parseLong(data[(2 * i) + 5]));
            }
        }

        monitorLibrary.populateInterval(new Interval(level, length, screenOnTime, networkBytes, activeProcs));
    }
}
