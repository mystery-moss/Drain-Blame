package moss.mystery.energymonitor;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import moss.mystery.energymonitor.monitors.BatteryMonitor;
import moss.mystery.energymonitor.monitors.Interval;
import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.monitors.ScreenMonitor;
import moss.mystery.energymonitor.processes.Process;
import moss.mystery.energymonitor.processes.ProcessLibrary;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1;
    private BroadcastReceiver batteryMonitor;
    private BroadcastReceiver screenMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Register screen monitor - when declared in manifest, never runs
        screenMonitor = new ScreenMonitor();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        getApplicationContext().registerReceiver(screenMonitor, new IntentFilter(filter));

        //Register battery level change receiver
        //TODO: Which thread does this run in? Should I split it off to another one?
        //TODO: Should I explicitly start monitoring intervals here too?
        batteryMonitor = new BatteryMonitor();
        getApplicationContext().registerReceiver(batteryMonitor, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    public void listIntervals(View view){
        TextView box = (TextView) findViewById(R.id.tempText);

        StringBuilder procs = new StringBuilder();
        procs.append("CURRENT TIMESTAMP: ").append(System.currentTimeMillis()).append("\nINTERVALS:\n");

        ArrayList<Interval> intervals = MonitorLibrary.getIntervals();
        if(intervals == null){
            box.setText("No intervals recorded");
            return;
        }

        //TODO: For ArrayList only, hand-written counted loop is more efficient than iterator
        for(Interval interval : intervals){
            procs.append(interval.level).append(" - ").append(interval.level - 1);
            procs.append(": ").append(interval.length).append(" - Screen time = ").append(interval.screenOnTime).append('\n');
        }

        box.setText(procs);
    }

    public void reset(View view){
        MonitorLibrary.clearIntervals();
        MonitorLibrary.startup(this);
    }

    public void startInterval(View view){
        MonitorLibrary.newInterval(getApplicationContext(), 90, System.currentTimeMillis());
    }

    //private static int[] pids;
    //private static int[] ustart;
    //private static int[] sstart;

    public void listProcs(View view){
        TextView box = (TextView) findViewById(R.id.tempText);

        long start = System.currentTimeMillis();
        StringBuilder procs = new StringBuilder("PROCESSES:\n");

        ProcessLibrary.startup();
        ProcessLibrary.getPIDs();
        ProcessLibrary.parsePIDs();

        for(Process proc : ProcessLibrary.processes){
            procs.append(proc.name).append(" - ").append(proc.time).append('\n');
        }

//        //Get initial values for utime
//        if(pids == null){
//            pids = getPIDs();
//            extractTime(pids);
//            int length = pids.length;
//            ustart = new int[length];
//            sstart = new int[length];
//            System.arraycopy(ProcessLibrary.utime, 0, ustart, 0, length);
//            System.arraycopy(ProcessLibrary.stime, 0, sstart, 0, length);
//        }
//
//        //int[] pids = getPIDs();
//        String[] names = getNames(pids);
//
//        extractTime(pids);
//
//        int length = pids.length;
//        for(int i = 0; i < length; i++){
//            procs.append(names[i]).append("\n   ").append(ProcessLibrary.utime[i] - ustart[i]);
//            procs.append(", ").append(ProcessLibrary.stime[i] - sstart[i]).append('\n');
//        }

        procs.append("\nTIME TAKEN = ").append(String.valueOf(System.currentTimeMillis() - start));

        box.setText(procs);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            unregisterReceiver(batteryMonitor);
            unregisterReceiver(screenMonitor);
        }
        catch(Exception e){
            Log.d("MainActivity", "Receivers not registered:\n" + e);
        }
        Log.d("MainActivity", "Exited");
    }
}
