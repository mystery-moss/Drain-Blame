package moss.mystery.energymonitor.ui;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.classifier.ClassifierLibrary;

public class AppListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

//        //TEMPORARY for testing
//        ProcessHandler.parseProcs(0);
//        App[] apps = ProcessHandler.startNewSample();

        //Args are context, layout, the array to take data from
        AppArrayAdapter adapter = new AppArrayAdapter(this, ClassifierLibrary.classifiedApps);

        ListView listView = (ListView) findViewById(R.id.app_list);
        listView.setAdapter(adapter);
    }

    //TODO: Make sure this is in a try block to catch anything I've missed
    public static ApplicationInfo getAppInfo(String proc, PackageManager pm){
        ApplicationInfo ai;
        //Request app name from package name string
        //TODO: This can be removed - if no "/", whole name is added to testStrings.
        try {
            ai = pm.getApplicationInfo(proc, 0);
            return ai;
        } catch (final PackageManager.NameNotFoundException ignored) {}

        //Not found - package name likely obscured by extra elements. Try removing them.
        ArrayList<String> testStrings = new ArrayList<String>();

        //First remove any '/'s
        if(proc.contains("/")) {
            String[] slashSplit = proc.split("[/]");
            //Assuming name is of the form 'x.y.[...z]', so look for '.'s
            for(String str : slashSplit){
                if(str.contains(".")){
                    testStrings.add(str);
                }
            }
            //If no substrings containing '.'s are found, just try the last substring
            if(testStrings.size() == 0){
                testStrings.add(slashSplit[slashSplit.length - 1]);
            }
        }
        else {
            testStrings.add(proc);
        }

        //Now have one (or potentially multiple) target strings to test. May be of forms:
        //"x", "x.y...", or "x.y:z", where ':' could be any special char
        //Want to try trimming anything following a special char from the end of the string, request
        //appInfo from result. Repeat until either an app is found or we run out of things to trim
        for(String str : testStrings){
            //Try this string as the package name
            try {
                ai = pm.getApplicationInfo(str, 0);
                return ai;
            } catch (final PackageManager.NameNotFoundException ignored) {}

            int i = str.length() - 1;
            char c;
            while(i > 0){
                //Find position of last non-char symbol in the string
                c = str.charAt(i);
                if(!Character.isLetter(c)){
                    //Remove everything from this char onwards, use result as package name
                    try {
                        ai = pm.getApplicationInfo(str.substring(0, i), 0);
                        return ai;
                    } catch (final PackageManager.NameNotFoundException ignored) {}
                }
                --i;
            }
        }

        return null;
    }
}
