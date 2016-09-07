package greenwich.guidear;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class NewSettingsActivity extends Activity{
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen2);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.mesuramentSwitch);
        // m = yd/1.0936
        // yd = m * 1.0936
        final TextView distanceTxt = (TextView) findViewById(R.id.distanceText);
        //final double metricDistance = Double.parseDouble(distanceTxt.getText().toString());
        ListView listView = (ListView) findViewById(R.id.listView);
        Model[] modelItems = new Model[16];
        modelItems[0] = new Model("Animals", 1);
        modelItems[1] = new Model("Accomodation", 1);
        modelItems[2] = new Model("Food & Drink", 1);
        modelItems[3] = new Model("Education", 1);
        modelItems[4] = new Model("Entertainment", 1);
        modelItems[5] = new Model("Health", 1);
        modelItems[6] = new Model("Library", 1);
        modelItems[7] = new Model("Shops", 1);
        modelItems[8] = new Model("Money", 1);
        modelItems[9] = new Model("Petrol Station", 1);
        modelItems[10] = new Model("Police", 1);
        modelItems[11] = new Model("Post Office", 1);
        modelItems[12] = new Model("Sport", 1);
        modelItems[13] = new Model("Underground Station", 1);
        modelItems[14] = new Model("Tourist Attraction", 1);
        modelItems[15] = new Model("Transport", 1);
        CustomAdapter adapter = new CustomAdapter(this, modelItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);
                if(position %2 == 1)
                {
                    // Set a background color for ListView regular row/item
                    view.setBackgroundColor(Color.parseColor("#262626"));
                }
                else
                {
                    // Set the background color for alternate row/item
                    view.setBackgroundColor(Color.parseColor("#333333"));
                }
                return view;
            }
        };


        listView.setAdapter(adapter);


        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar2);
        final String[] distance = distanceTxt.getText().toString().split(" ");
        // set seek bar properties
        seekBar.incrementProgressBy(100); // step by 100 meters
        seekBar.setMax(5000);
        seekBar.setProgress(Integer.parseInt(distance[0])); // start value
        final DecimalFormat df = new DecimalFormat("#.##");

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                double metricDistance = Double.parseDouble(distance[0]);
                double yardDistance = metricDistance * 1.0936;
                if (isChecked){
                    distanceTxt.setText(""+df.format(yardDistance) + " yards");
                    distance[1] = " yards";

                }
                else if (!isChecked){
                    distanceTxt.setText("" + df.format(metricDistance/1.0936) + " meters");
                    distance[1] = " meters";
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceTxt.setText(String.valueOf(progress) + distance[1]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    @Override
    public void onBackPressed() {
        /*
        ListView myList = (ListView) findViewById(R.id.listView);
        int count = myList.getCount();
        for (int i = 0; i < count; i++)
        {
            ViewGroup row = (ViewGroup) myList.getChildAt(i);
            System.out.println(row.toString());
        }*/
        super.onBackPressed();
    }
}
