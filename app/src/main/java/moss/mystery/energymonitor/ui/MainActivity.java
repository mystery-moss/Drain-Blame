package moss.mystery.energymonitor.ui;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import moss.mystery.energymonitor.ApplicationGlobals;
import moss.mystery.energymonitor.FileParsing;
import moss.mystery.energymonitor.MainService;
import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.apps.App;
import moss.mystery.energymonitor.classifier.ClassifiedApp;
import moss.mystery.energymonitor.classifier.Classifier;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        globals = ApplicationGlobals.get(getApplicationContext());

        //Check permissions
        if(ProcessHandler.noReadPermission()){
            //TODO: Make this work properly in final version
            DialogFragment dialog = new ErrorDialog();
            dialog.show(getSupportFragmentManager(), "permission_error");
        }



        TextView enabled = (TextView) findViewById(R.id.monitorStatus);
        //Start service
        if(globals.serviceEnabled) {
            startService(new Intent(this, MainService.class));
            enabled.setText(R.string.monitor_running);
        } else {
            enabled.setText(R.string.monitor_not_running);
            //TODO: Add a button to start monitor?
        }

        TextView info = (TextView) findViewById(R.id.infoText);
        info.setText(String.valueOf(globals.intervalHandler.numIntervals()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO: Split out into separate thread, show loading spinner!
        Classifier classifier = new Classifier();

        AppArrayAdapter adapter;
        Log.d(DEBUG, "Called");

        if(classifier.classify(globals.intervalHandler.getIntervals(), globals.intervalHandler.numIntervals())){
            //Populate view with classified apps
            Log.d(DEBUG, "Populating list");
            adapter = new AppArrayAdapter(this, classifier.getClassifiedApps(), false);
        } else {
            //Populate view with message about lack of intervals
            adapter = new AppArrayAdapter(this, new ClassifiedApp[1], true);
        }

        ListView listView = (ListView) findViewById(R.id.app_list);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);

        //Set 'toggle monitor' text
        MenuItem toggle = menu.findItem(R.id.action_togglemonitor);
        if(globals.serviceEnabled) {
            toggle.setTitle(R.string.monitor_off);
        } else {
            toggle.setTitle(R.string.monitor_on);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                //Update the monitor status text
                //Call classifier again
                //Maybe do something to the page so it's clear it has actually refreshed - e.g. pop up a spinner for a second
                    //Wait, that will come automatically from the classifier update, so all good
                return true;
            //Set 'toggle monitor' text
            case R.id.action_togglemonitor:
                TextView enabled = (TextView) findViewById(R.id.monitorStatus);
                if(globals.serviceEnabled){
                    item.setTitle(R.string.monitor_on);
                    globals.serviceEnabled = false;
                    stopService(new Intent(this, MainService.class));
                    enabled.setText(R.string.monitor_not_running);
                } else {
                    item.setTitle(R.string.monitor_off);
                    globals.serviceEnabled = true;
                    startService(new Intent(this, MainService.class));
                    enabled.setText(R.string.monitor_running);
                }
                return true;
            case R.id.action_readfile:
                if(FileParsing.readFile(getApplicationContext(), globals.intervalHandler)){
                    Toast.makeText(getApplicationContext(), "File read", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error reading file", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_writefile:
                if(FileParsing.writeFile(getApplicationContext(), globals.intervalHandler)){
                    Toast.makeText(getApplicationContext(), "File written", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error writing file", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return true;
        }
    }

//    public void options(View view){
//        startActivity(new Intent(this, OptionsActivity.class));
//    }
}