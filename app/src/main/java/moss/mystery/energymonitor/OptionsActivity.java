package moss.mystery.energymonitor;

import android.content.Intent;
import android.net.TrafficStats;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import moss.mystery.energymonitor.classifier.FileParsing;
import moss.mystery.energymonitor.monitors.Interval;
import moss.mystery.energymonitor.monitors.MonitorLibrary;
import moss.mystery.energymonitor.processes.ProcessInfo;
import moss.mystery.energymonitor.processes.ProcessLibrary;

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Button toggle = (Button) findViewById(R.id.toggleMonitoring);
        if(MainActivity.runService){
            toggle.setText("Stop Monitoring");
        } else {
            toggle.setText("Start Monitoring");
        }
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

    public void startInterval(View view){
        MonitorLibrary.setBatteryLevel(MonitorLibrary.getBatteryLevel() - 1);
    }

    public void toggleMonitoring(View view){
        Button toggle = (Button) findViewById(R.id.toggleMonitoring);

        if(MainActivity.runService){
            MainActivity.runService = false;
            stopService(new Intent(this, MainService.class));
            toggle.setText("Start Monitoring");
        } else {
            MainActivity.runService = true;
            startService(new Intent(this, MainService.class));
            toggle.setText("Stop Monitoring");
        }
    }

    static long rx = 0;
    static long tx = 0;
    public void network(View view){
        TextView text = (TextView) findViewById(R.id.statusText);
        long rxnew = TrafficStats.getTotalRxBytes();
        long txnew = TrafficStats.getTotalTxBytes();

        if(rxnew < rx || txnew < tx){
            text.setText("Total has decreased!");
        }
        else {
            rx = rxnew;
            tx = txnew;
            text.setText("Receive total: " + rx + "\nSend total: " + tx);
        }
    }
}