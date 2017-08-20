package moss.mystery.energymonitor.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import moss.mystery.energymonitor.ApplicationGlobals;
import moss.mystery.energymonitor.MainService;
import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.apps.App;
import moss.mystery.energymonitor.classifier.ClassifierLibrary;
import moss.mystery.energymonitor.monitors.Interval;
import moss.mystery.energymonitor.processes.ProcessHandler;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG = "MainActivity";

    private ApplicationGlobals globals;

    //TODO: Hmmmm
    public static Context appContext = null;


    @Override
    //Perform checks and setup
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        globals = ApplicationGlobals.get(getApplicationContext());

        //Check permissions
        if(!ProcessHandler.noReadPermission()){
            //TODO: Open a popup telling the user that the app will not work
            TextView box = (TextView) findViewById(R.id.textBox);
            box.setText("ERROR: Cannot read process state [Root privileges required in Android 7+]");
            return;
        }

        //Start service
        if(globals.serviceEnabled) {
            startService(new Intent(this, MainService.class));
        }

        TextView box = (TextView) findViewById(R.id.textBox);
        box.setText("CPU Threshold value = ... I don't know, it's not static anymore");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        appContext = null;
    }

    //Launch 'options' activity
    public void options(View view){
        startActivity(new Intent(this, OptionsActivity.class));
    }

    //Launch 'options' activity
    public void listActivity(View view){
        startActivity(new Intent(this, AppListActivity.class));
    }

    //========================DEBUGGING=METHODS==========================
    public void listIntervals(View view){
        TextView box = (TextView) findViewById(R.id.textBox);

        StringBuilder info = new StringBuilder();
        info.append("CURRENT TIMESTAMP: ").append(System.currentTimeMillis()).append("\nINTERVALS:\n");

        ArrayList<Interval> intervals = globals.monitorLibrary.getIntervals();
        if(intervals == null){
            box.setText("No intervals recorded");
            return;
        }

        //TODO: For ArrayList only, hand-written counted loop is more efficient than iterator
        for(Interval interval : intervals){
            info.append("+++++++ ").append(interval.level).append(" - ").append(interval.level - 1);
            info.append(": ").append(interval.length / 1000).append(" - Screen ticks = ").append(interval.screenOnTime / 1000).append('\n');
            for(App proc : interval.activeProcs){
                info.append(proc.name).append(", ");
            }
            info.append('\n');
        }

        box.setText(info);
    }

    public void parseIntervals(View view){
        TextView box = (TextView) findViewById(R.id.textBox);


        box.setText("You should probably remove that button.");
    }

    public void categorise(View view){
        TextView box = (TextView) findViewById(R.id.textBox);

        ClassifierLibrary.classify(globals.monitorLibrary);

        box.setText("Observed processes categorised");
    }

//    public void listProcs(View view){
//        TextView box = (TextView) findViewById(R.id.tempText);
//
//        long start = System.currentTimeMillis();
//        StringBuilder procs = new StringBuilder("PROCESSES:\n");
//
//        ProcessHandler.parseProcs();
//
//        for(String key : ProcessHandler.processes.keySet()){
//            Process proc = ProcessHandler.processes.get(key);
//            procs.append(key).append(" - ").append(proc.prevTicks).append('\n');
//        }
//
//        procs.append("\nTIME TAKEN = ").append(String.valueOf(System.currentTimeMillis() - start));
//
//        box.setText(procs);
//    }
}