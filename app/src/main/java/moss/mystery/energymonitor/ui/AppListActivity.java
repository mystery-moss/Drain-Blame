package moss.mystery.energymonitor.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import moss.mystery.energymonitor.ApplicationGlobals;
import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.classifier.Classifier;

public class AppListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        ApplicationGlobals globals = ApplicationGlobals.get(getApplicationContext());

        //Args are context, layout, the array to take data from
        AppArrayAdapter adapter = new AppArrayAdapter(this, globals.classifier.getClassifiedApps());

        ListView listView = (ListView) findViewById(R.id.app_list);
        listView.setAdapter(adapter);
    }
}
