package moss.mystery.energymonitor.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.classifier.ClassifiedApp;

public class AppArrayAdapter extends ArrayAdapter {
    private final LayoutInflater inflater;
    private final ClassifiedApp[] apps;

    public AppArrayAdapter(Context context, ClassifiedApp[] apps){
        super(context, -1, apps);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.apps = apps;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View rowView = inflater.inflate(R.layout.app_list_view, parent, false);

        TextView bigText = rowView.findViewById(R.id.bigText);
        TextView smallText = rowView.findViewById(R.id.smallText);
//        ImageView image = rowView.findViewById(R.id.icon);

        ClassifiedApp app = apps[position];

        bigText.setText(app.name);
        smallText.setText(app.classification + " - " + app.confidence + " confidence");
//        if(ai != null) {
//            image.setImageDrawable(pm.getApplicationIcon(ai));
//        }

        return rowView;
    }
}
