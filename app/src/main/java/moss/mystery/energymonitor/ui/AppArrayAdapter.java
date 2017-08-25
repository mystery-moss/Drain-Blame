package moss.mystery.energymonitor.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import moss.mystery.energymonitor.ApplicationGlobals;
import moss.mystery.energymonitor.R;
import moss.mystery.energymonitor.classifier.ClassifiedApp;

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

        viewHolder.bigText.setText(app.name);
        viewHolder.smallText.setText(app.classification + " - " + app.confidence + " confidence");

        //Get icon
        try {
            Log.d("TEMP", "Getting ai for " + apps[position].name);
            ApplicationInfo ai = pm.getApplicationInfo(apps[position].name, 0);
            viewHolder.image.setImageDrawable(pm.getApplicationIcon(ai));
        } catch (PackageManager.NameNotFoundException ignored) {
            Log.d("TEMP", "Nope");
        }

        return convertView;
    }

    static class ViewHolderItem {
        TextView bigText;
        TextView smallText;
        ImageView image;
    }
}
