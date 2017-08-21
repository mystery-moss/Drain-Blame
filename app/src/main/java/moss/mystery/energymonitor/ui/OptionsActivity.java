package moss.mystery.energymonitor.ui;

import android.content.Intent;
import android.net.TrafficStats;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import moss.mystery.energymonitor.ApplicationGlobals;
import moss.mystery.energymonitor.MainService;
import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.FileParsing;
import moss.mystery.energymonitor.intervals.Interval;
import moss.mystery.energymonitor.apps.App;

public class OptionsActivity extends AppCompatActivity {
    private ApplicationGlobals globals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        globals = ApplicationGlobals.get(getApplicationContext());

        Button toggle = (Button) findViewById(R.id.toggleMonitoring);
        if(globals.serviceEnabled){
            toggle.setText("Monitor Off");
        } else {
            toggle.setText("Monitor On");
        }
    }

    public void writeFile(View view){
        TextView text = (TextView) findViewById(R.id.statusText);

        if(FileParsing.writeFile(this, globals.intervalHandler)){
            text.setText("File written");
        }
        else{
            text.setText("Error: Unable to write file");
        }

    }

    public void readFile(View view){
        TextView text = (TextView) findViewById(R.id.statusText);

        if(FileParsing.readFile(this, globals.intervalHandler)){
            text.setText("File read");
        }
        else{
            text.setText("Error: Unable to read file");
        }
    }

    public void toggleMonitoring(View view){
        Button toggle = (Button) findViewById(R.id.toggleMonitoring);

        if(globals.serviceEnabled){
            globals.serviceEnabled = false;
            stopService(new Intent(this, MainService.class));
            toggle.setText("Monitor On");
        } else {
            globals.serviceEnabled = true;
            startService(new Intent(this, MainService.class));
            toggle.setText("Monitor Off");
        }
    }
}