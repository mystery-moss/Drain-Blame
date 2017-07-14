package moss.mystery.energymonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class MainService extends Service {
    private static final String DEBUG = "MainService";

    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_temp)
                .setContentTitle("BACKGROUND")
                .setContentText("Placeholder text")
                .setPriority(NotificationCompat.PRIORITY_MIN);
        //TODO: Maybe change activity called when notification is clicked
        Intent notificationIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        //TODO: Fix this int (notification identifier)
        //Used to cancel notification
        startForeground(85623, mBuilder.build());

        Log.d(DEBUG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        Log.d(DEBUG, "Service starting up");
    }

    @Override
    public void onDestroy(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(DEBUG, "Binding not currently supported");
        return null;
    }
}
