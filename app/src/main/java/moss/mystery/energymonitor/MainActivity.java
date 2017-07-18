package moss.mystery.energymonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import moss.mystery.energymonitor.monitors.Interval;
import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.processes.Process;
import moss.mystery.energymonitor.processes.ProcessLibrary;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Begin monitoring
        //TODO: This is not the best place for this check
        if(!MonitorLibrary.running){
            //Ensure that we have the ability to read process info
            Log.d("MAIN", "STARTING PROCESS-LIBRARY!");
            if(!ProcessLibrary.startup()){
                //TODO: Update when UI is finalised
                TextView box = (TextView) findViewById(R.id.tempText);
                box.setText("ERROR: Cannot read process state [Root privileges required in Android 7+]");
                return;
            }

            //Start service
            Intent intent = new Intent(this, MainService.class);
            startService(intent);
        }
    }

    public void listIntervals(View view){
        TextView box = (TextView) findViewById(R.id.tempText);

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
            info.append(": ").append(interval.length).append(" - Screen time = ").append(interval.screenOnTime).append('\n');
            for(String proc : interval.activeProcs){
                info.append(proc).append(", ");
            }
            info.append('\n');
        }

        box.setText(info);
    }

    public void reset(View view){
        MonitorLibrary.clearIntervals();
        MonitorLibrary.startup(this);
    }

    public void startInterval(View view){
        MonitorLibrary.newInterval(getApplicationContext(), 71, System.currentTimeMillis());
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
//            procs.append(key).append(" - ").append(proc.currTicks).append('\n');
//        }
//
//        procs.append("\nTIME TAKEN = ").append(String.valueOf(System.currentTimeMillis() - start));
//
//        box.setText(procs);
//    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("MainActivity", "Exited");
    }
}