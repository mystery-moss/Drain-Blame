package moss.mystery.energymonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import moss.mystery.energymonitor.classifier.FileParsing;

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
}
