package moss.mystery.energymonitor.ui;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import moss.mystery.energymonitor.ApplicationGlobals;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO: Split out into separate thread, show loading spinner!
        Classifier classifier = new Classifier();

        AppArrayAdapter adapter;

        if(classifier.classify(globals.intervalHandler.getIntervals())){
            //Populate view with classified apps
            adapter = new AppArrayAdapter(this, globals.classifier.getClassifiedApps(), false);
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

    public void options(View view){
        startActivity(new Intent(this, OptionsActivity.class));
    }
}