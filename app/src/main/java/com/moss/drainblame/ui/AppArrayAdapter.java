package com.moss.drainblame.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moss.drainblame.R;
import com.moss.drainblame.classifier.ClassifiedApp;
import com.moss.drainblame.classifier.Classifier;

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
            viewHolder.appName = convertView.findViewById(R.id.appName);
            viewHolder.classification = convertView.findViewById(R.id.classification);
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
                ai = pm.getApplicationInfo(app.name, 0);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        //Flags for setting colours
        boolean red = false;

        //Get app name
        if(ai != null) {
            viewHolder.appName.setText(pm.getApplicationLabel(ai));
            viewHolder.image.setImageDrawable(pm.getApplicationIcon(ai));
        } else {
            viewHolder.appName.setText(app.name);
            viewHolder.appName.setTextColor(ContextCompat.getColor(context, R.color.grey));
        }

        //Set classification text
        StringBuilder text = new StringBuilder();
        switch(app.classification){
            case Classifier.HIGH:
                text.append(context.getString(R.string.high_drain));
                red = true;
                break;
            case Classifier.MEDIUM:
                text.append(context.getString(R.string.medium_drain));
                break;
            case Classifier.LOW:
                text.append(context.getString(R.string.low_drain));
        }
        if(app.network){
            text.append(context.getString(R.string.when_network));
        }
        viewHolder.classification.setText(text);

        //Set colours
        if(red) {
            viewHolder.classification.setTextColor(ContextCompat.getColor(context, R.color.red));
        }

        return convertView;
    }

    private static class ViewHolderItem {
        TextView appName;
        TextView classification;
        TextView confidence;
        ImageView image;
    }
}
