package com.moss.drainblame.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.moss.drainblame.ApplicationGlobals;
import com.moss.drainblame.FileParsing;
import com.moss.drainblame.MainService;
import com.moss.drainblame.R;
import com.moss.drainblame.classifier.ClassifiedApp;
import com.moss.drainblame.classifier.Classifier;
import com.moss.drainblame.processes.ProcessHandler;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG = "MainActivity";
    private static final int NUM_THRESHOLD = 4;
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

        //Start service, update status text
        if(globals.serviceEnabled) {
            startService(new Intent(this, MainService.class));
        }
        updateMonitorText();


        TextView info = (TextView) findViewById(R.id.infoText);
        info.setVisibility(View.GONE);
        info.setText(R.string.keep_running);
    }

    @Override
    protected void onStart() {
        super.onStart();
        populateAppList();
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
                updateMonitorText();
                populateAppList();
                return true;
            case R.id.action_togglemonitor:
                //Set 'toggle monitor' menu and status text
                if(globals.serviceEnabled){
                    item.setTitle(R.string.monitor_on);
                    globals.serviceEnabled = false;
                    stopService(new Intent(this, MainService.class));
                } else {
                    item.setTitle(R.string.monitor_off);
                    globals.serviceEnabled = true;
                    startService(new Intent(this, MainService.class));
                }
                updateMonitorText();
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
            case R.id.demo_populate:
                demoPopulate();
                return true;
            default:
                return true;
        }
    }

    //Call app classifier in a separate thread to avoid potential slowdowns
    private void populateAppList(){
        final Classifier classifier = new Classifier(globals.intervalHandler.getIntervals(), globals.intervalHandler.numIntervals());
        final Context context = this;
        final ProgressBar loading = (ProgressBar) findViewById(R.id.progressBar);

        final ListView listView = (ListView) findViewById(R.id.app_list);
        final TextView appText = (TextView) findViewById(R.id.appListText);
        final TextView info = (TextView) findViewById(R.id.infoText);
        final TextView heading = (TextView) findViewById(R.id.heading);

        listView.setVisibility(View.GONE);
        appText.setVisibility(View.GONE);
        info.setVisibility(View.GONE);
        heading.setVisibility(View.GONE);
        //Show loading icon
        loading.setVisibility(View.VISIBLE);

        new AsyncTask<Classifier, Void, Integer>() {
            @Override
            protected Integer doInBackground(Classifier... param){
                //Classify apps
                return param[0].classify();
            }

            @Override
            protected void onPostExecute(Integer numClassified){
                //Populate list view
                if(numClassified > 0){
                    //Populate view with classified apps
                    listView.setVisibility(View.VISIBLE);
                    heading.setVisibility(View.VISIBLE);

                    AppArrayAdapter adapter = new AppArrayAdapter(context, classifier.getClassifiedApps());
                    listView.setAdapter(adapter);

                    //If number of classified apps below threshold, show info text, else hide
                    if(numClassified <= NUM_THRESHOLD){
                        info.setVisibility(View.VISIBLE);
                    }
                } else {
                    //Display message about lack of intervals
                    appText.setVisibility(View.VISIBLE);
                }

                //Remove spinny thing
                loading.setVisibility(View.GONE);
            }
        }.execute(classifier);
    }

    private void updateMonitorText(){
        TextView enabled = (TextView) findViewById(R.id.monitorStatus);
        if(globals.serviceEnabled) {
            enabled.setText(R.string.monitor_running);
            enabled.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            enabled.setText(R.string.monitor_not_running);
            enabled.setTextColor(ContextCompat.getColor(this, R.color.red));
        }
    }

    //TODO: Temporary, for demo purposes
    private void demoPopulate(){
        final Context context = this;

        ListView listView = (ListView) findViewById(R.id.app_list);
        TextView appText = (TextView) findViewById(R.id.appListText);
        TextView heading = (TextView) findViewById(R.id.heading);
        TextView infoText = (TextView) findViewById(R.id.infoText);

        appText.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        heading.setVisibility(View.VISIBLE);
        infoText.setVisibility(View.VISIBLE);
        infoText.setText(R.string.demo_text);

        //Get x apps
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent .addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(intent , 0);
        Collections.shuffle(apps);

        int index = 0;
        ClassifiedApp[] classifiedApps = new ClassifiedApp[7];
        int[] classification =  {2, 2, 2, 1, 1, 0, 0};
        int[] confidence =      {2, 2, 1, 2, 1, 2, 0};

        //Turn them into classifiedApps
        for(ResolveInfo info : apps){
            if(index == 7){
                break;
            }
            classifiedApps[index] = new ClassifiedApp(info.activityInfo.applicationInfo.packageName, classification[index], confidence[index], false, index == 1 || index == 2);
            ++index;
        }

        if(index != 7){
            classifiedApps = Arrays.copyOf(classifiedApps, index);
        }

        AppArrayAdapter adapter = new AppArrayAdapter(context, classifiedApps);
        listView.setAdapter(adapter);
    }
}