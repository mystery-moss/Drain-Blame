package moss.mystery.energymonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import moss.mystery.energymonitor.classifier.ClassifierLibrary;
import moss.mystery.energymonitor.monitors.Interval;
import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.processes.Process;
import moss.mystery.energymonitor.processes.ProcessInfo;
import moss.mystery.energymonitor.processes.ProcessLibrary;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG = "MainActivity";
    public static boolean runService = true;

    @Override
    //Perform checks and setup
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check permissions
        if(!ProcessLibrary.checkPermission()){
            //TODO: Open a popup telling the user that the app will not work
            TextView box = (TextView) findViewById(R.id.textBox);
            box.setText("ERROR: Cannot read process state [Root privileges required in Android 7+]");
            return;
        }

        //Start service
        if(runService) {
            startService(new Intent(this, MainService.class));
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    //Launch 'options' activity
    public void options(View view){
        startActivity(new Intent(this, OptionsActivity.class));
    }

    //========================DEBUGGING=METHODS==========================
    public void listIntervals(View view){
        TextView box = (TextView) findViewById(R.id.textBox);

        StringBuilder info = new StringBuilder();
        info.append("CURRENT TIMESTAMP: ").append(System.currentTimeMillis()).append("\nINTERVALS:\n");

        ArrayList<Interval> intervals = MonitorLibrary.getIntervals();
        if(intervals == null){
            box.setText("No intervals recorded");
            return;
        }

        //TODO: For ArrayList only, hand-written counted loop is more efficient than iterator
        for(Interval interval : intervals){
            info.append("+++++++ ").append(interval.level).append(" - ").append(interval.level - 1);
            info.append(": ").append(interval.length / 1000).append(" - Screen ticks = ").append(interval.screenOnTime / 1000).append('\n');
            for(ProcessInfo proc : interval.activeProcs){
                info.append(proc.name).append(", ");
            }
            info.append('\n');
        }

        box.setText(info);
    }

    //TODO: Fix!!
    public void reset(View view){
        MonitorLibrary.clearIntervals();
        MonitorLibrary.startup();
    }



    public void parseIntervals(View view){
        TextView box = (TextView) findViewById(R.id.textBox);

        ArrayList<Interval> intervals = MonitorLibrary.getIntervals();
        if(intervals == null){
            box.setText("No intervals");
            return;
        }

        //First pass: Extract list of processes and add lengths of intervals they are active in
        HashMap<String, ArrayList<Integer>> processes = new HashMap<String, ArrayList<Integer>>();

        for(Interval interval : intervals){
            for(ProcessInfo proc : interval.activeProcs){
                ArrayList<Integer> procIntervals = processes.get(proc.name);
                if(procIntervals == null){
                    processes.put(proc.name, new ArrayList<Integer>());
                    procIntervals = processes.get(proc.name);
                }
                procIntervals.add(ClassifierLibrary.classifyInterval(interval.length));
            }
        }

        StringBuilder info = new StringBuilder("PROCESSES:\n");

        //Assign basic levels to processes
        for(String key : processes.keySet()){
            info.append(key).append(": ");
            ArrayList<Integer> lengths = processes.get(key);
            for(int length : lengths){
                info.append(length).append(", ");
            }
            info.append('\n');
        }

        box.setText(info);
    }

//    public void listProcs(View view){
//        TextView box = (TextView) findViewById(R.id.tempText);
//
//        long start = System.currentTimeMillis();
//        StringBuilder procs = new StringBuilder("PROCESSES:\n");
//
//        ProcessLibrary.parseProcs();
//
//        for(String key : ProcessLibrary.processes.keySet()){
//            Process proc = ProcessLibrary.processes.get(key);
//            procs.append(key).append(" - ").append(proc.prevTicks).append('\n');
//        }
//
//        procs.append("\nTIME TAKEN = ").append(String.valueOf(System.currentTimeMillis() - start));
//
//        box.setText(procs);
//    }
}