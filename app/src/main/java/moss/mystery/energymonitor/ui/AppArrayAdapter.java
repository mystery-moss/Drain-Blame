package moss.mystery.energymonitor.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.classifier.ClassifiedApp;

public class AppArrayAdapter extends ArrayAdapter {
    private final LayoutInflater inflater;
    private final ClassifiedApp[] apps;
    private final boolean showText;

    public AppArrayAdapter(Context context, ClassifiedApp[] apps, boolean showText){
        super(context, -1, apps);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.apps = apps;
        this.showText = showText;
        Log.d("ArrayAdapter", "Created - length = " + apps.length);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Log.d("ArrayAdapter", "Running");
        View rowView;
        //Special case if there are no apps to display
        if(showText){
            rowView = inflater.inflate(R.layout.list_view_text, parent, false);

            if(position == 0) {
                TextView text = rowView.findViewById(R.id.listInfoText);
                text.setText(R.string.too_few_intervals);
            }
            return rowView;
        }

        rowView = inflater.inflate(R.layout.app_list_view, parent, false);

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
