package moss.mystery.energymonitor.monitors;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ProcessMonitor extends IntentService {
    public ProcessMonitor(){
        super("ProcessMonitor");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        // Do task
        Log.d("ProcessMonitor", "Monitoring");
    }
}
