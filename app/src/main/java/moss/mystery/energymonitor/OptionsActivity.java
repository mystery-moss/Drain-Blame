package moss.mystery.energymonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import moss.mystery.energymonitor.classifier.FileParsing;
import moss.mystery.energymonitor.monitors.Interval;
import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.processes.ProcessInfo;

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
    }

    public void writeFile(View view){
        TextView text = (TextView) findViewById(R.id.statusText);

        if(FileParsing.writeFile(this)){
            text.setText("File written");
        }
        else{
            text.setText("Error: Unable to write file");
        }

    }

    public void readFile(View view){
        TextView text = (TextView) findViewById(R.id.statusText);

        if(FileParsing.readFile(this)){
            text.setText("File read");
        }
        else{
            text.setText("Error: Unable to read file");
        }
    }

    public void cpuTicks(View view){
        TextView text = (TextView) findViewById(R.id.statusText);

        StringBuilder data = new StringBuilder();

        ArrayList<Interval> intervals = MonitorLibrary.getIntervals();
        if(intervals == null){
            text.setText("No intervals recorded");
            return;
        }

        for(Interval interval : intervals){
            data.append("+++++++ ").append(interval.level).append(" - ").append(interval.level - 1);
            data.append(": ").append(interval.length / 1000).append(" - Screen ticks = ").append(interval.screenOnTime / 1000).append('\n');
            for(ProcessInfo proc : interval.activeProcs){
                data.append(proc.name).append(": ").append(proc.ticks).append(", ");
            }
            data.append('\n');
        }

        text.setText(data);
    }
}