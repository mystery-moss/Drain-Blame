package moss.mystery.energymonitor.ui;

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
import moss.mystery.energymonitor.intervals.Interval;
import moss.mystery.energymonitor.processes.ProcessHandler;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG = "MainActivity";
    private ApplicationGlobals globals;

    @Override
    //Perform checks and setup
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        globals = ApplicationGlobals.get(getApplicationContext());

        //Check permissions
        if(ProcessHandler.noReadPermission()){
            //TODO: Open a popup telling the user that the app will not work
            TextView box = (TextView) findViewById(R.id.textBox);
            box.setText("ERROR: Cannot read process state [Root privileges required in Android 7+]");
            return;
        }

        //Start service
        if(globals.serviceEnabled) {
            startService(new Intent(this, MainService.class));
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    public void options(View view){
        startActivity(new Intent(this, OptionsActivity.class));
    }

    public void listActivity(View view){
        startActivity(new Intent(this, AppListActivity.class));
    }

    //========================DEBUGGING=METHODS==========================
    public void listIntervals(View view){
        TextView box = (TextView) findViewById(R.id.textBox);

        StringBuilder info = new StringBuilder();
        info.append("CURRENT TIMESTAMP: ").append(System.currentTimeMillis()).append("\nINTERVALS:\n");

        ArrayList<Interval> intervals = globals.intervalHandler.getIntervals();
        if(intervals == null){
            box.setText("No intervals recorded");
            return;
        }

        for(Interval interval : intervals){
            info.append("+++++++ ").append(interval.level).append(" - ").append(interval.level - 1);
            info.append(": ").append(interval.length / 1000).append(" - Screen ticks = ").append(interval.screenOnTime / 1000).append('\n');
            for(App proc : interval.activeApps){
                info.append(proc.name).append(": ").append(proc.ticks).append(", ");
            }
            info.append('\n');
        }

        box.setText(info);
    }
}