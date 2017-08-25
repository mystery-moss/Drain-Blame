package moss.mystery.energymonitor.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import moss.mystery.energymonitor.ApplicationGlobals;
import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.classifier.ClassifiedApp;
import moss.mystery.energymonitor.classifier.Classifier;

public class AppArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final ClassifiedApp[] apps;
    private final PackageManager pm;

    public AppArrayAdapter(Context context, ClassifiedApp[] apps){
        super(context, -1, apps);
        this.context = context;
        this.apps = apps;
        this.pm = context.getPackageManager();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolderItem viewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.app_list_view, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.bigText = convertView.findViewById(R.id.bigText);
            viewHolder.smallText = convertView.findViewById(R.id.smallText);
            viewHolder.image = convertView.findViewById(R.id.icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        ClassifiedApp app = apps[position];

        //Get appInfo
        ApplicationInfo ai = null;
        if(!app.unknownPackage) {
            try {
                Log.d("TEMP", "Getting ai for " + app.name);
                ai = pm.getApplicationInfo(app.name, 0);
            } catch (PackageManager.NameNotFoundException ignored) {
                Log.d("TEMP", "Nope");
            }
        }

        if(ai != null) {
            viewHolder.bigText.setText(pm.getApplicationLabel(ai));
            viewHolder.image.setImageDrawable(pm.getApplicationIcon(ai));
        } else {
            viewHolder.bigText.setText(app.name);
        }

        //Set classification text
        StringBuilder text = new StringBuilder();
        switch(app.classification){
            case(Classifier.HIGH):
                text.append(context.getString(R.string.high_drain));
                break;
            case(Classifier.MEDIUM):
                text.append(context.getString(R.string.medium_drain));
                break;
            case(Classifier.LOW):
                text.append(context.getString(R.string.low_drain));
        }
        if(app.network){
            text.append(context.getString(R.string.when_network));
        }

        text.append(" - ");
        switch(app.confidence){
            case(Classifier.HIGH):
                text.append(context.getString(R.string.high_confidence));
                break;
            case(Classifier.MEDIUM):
                text.append(context.getString(R.string.medium_confidence));
                break;
            case(Classifier.LOW):
                text.append(context.getString(R.string.low_confidence));
        }

        viewHolder.smallText.setText(text);

        return convertView;
    }

    private static class ViewHolderItem {
        TextView bigText;
        TextView smallText;
        ImageView image;
    }
}
