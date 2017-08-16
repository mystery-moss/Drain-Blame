package moss.mystery.energymonitor.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.processes.ProcessInfo;

public class AppArrayAdapter extends ArrayAdapter {
    private final LayoutInflater inflater;
    private final ProcessInfo[] apps;
    private final PackageManager pm;

    public AppArrayAdapter(Context context, ProcessInfo[] apps){
        super(context, -1, apps);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.apps = apps;
        this.pm = context.getPackageManager();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View rowView = inflater.inflate(R.layout.app_list_view, parent, false);

        TextView bigText = (TextView) rowView.findViewById(R.id.bigText);
        TextView smallText = (TextView) rowView.findViewById(R.id.smallText);
        ImageView image = (ImageView) rowView.findViewById(R.id.icon);

        ProcessInfo proc = apps[position];
        ApplicationInfo ai;
        ai = AppListActivity.getAppInfo(proc.name, pm);

        bigText.setText(ai != null ? (String) pm.getApplicationLabel(ai) : "[Unknown]");
        smallText.setText(proc.name);
        if(ai != null) {
            image.setImageDrawable(pm.getApplicationIcon(ai));
        }

        return rowView;
    }
}
