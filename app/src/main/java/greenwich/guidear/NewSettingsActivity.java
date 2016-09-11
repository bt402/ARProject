package greenwich.guidear;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;


import java.text.DecimalFormat;
import java.util.ArrayList;


public class NewSettingsActivity extends Activity{

    Model[] modelItems = new Model[16];
    boolean checked = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen2);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.mesuramentSwitch);
        // m = yd/1.0936
        // yd = m * 1.0936
        final TextView distanceTxt = (TextView) findViewById(R.id.distanceText);
        //final double metricDistance = Double.parseDouble(distanceTxt.getText().toString());
        final ListView listView = (ListView) findViewById(R.id.listView);
        final ToggleButton everythingToggle = (ToggleButton) findViewById(R.id.EverythingToggle);

        modelItems[0] = new Model("Animals", 0);
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


        final CustomAdapter adapter = new CustomAdapter(this, modelItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);
                if(position %2 == 1)
                {
                    view.setBackgroundColor(Color.parseColor("#262626"));
                }
                else
                {
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


        everythingToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    checked = true;
                    for (int i = 0; i < adapter.toggleButtonArrayList.size(); i++){
                        System.out.println(adapter.toggleButtonArrayList.size());
                        adapter.toggleButtonArrayList.get(i).setChecked(false);
                        adapter.toggleButtonArrayList.get(i).setEnabled(false);
                    }
                }
                else {
                    checked = false;
                    for (int i = 0; i < adapter.toggleButtonArrayList.size(); i++){
                        adapter.toggleButtonArrayList.get(i).setEnabled(true);
                    }
                }


            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(checked){
                    for (int k = 0; k < adapter.toggleButtonArrayList.size(); k++){
                        adapter.toggleButtonArrayList.get(k).setChecked(false);
                        adapter.toggleButtonArrayList.get(k).setEnabled(false);
                    }
                }
                else {
                    for (int k = 0; k < adapter.toggleButtonArrayList.size(); k++){
                        adapter.toggleButtonArrayList.get(k).setEnabled(true);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                System.out.println(checked);
                if(checked){
                    for (int k = 0; k < adapter.toggleButtonArrayList.size(); k++){
                        adapter.toggleButtonArrayList.get(k).setChecked(false);
                        adapter.toggleButtonArrayList.get(k).setEnabled(false);
                    }
                }
                else {
                    for (int k = 0; k < adapter.toggleButtonArrayList.size(); k++){
                        adapter.toggleButtonArrayList.get(k).setEnabled(true);
                    }
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
        System.out.println(PointOfInterestBuilder());
        super.onBackPressed();
    }

    public String PointOfInterestBuilder(){
        String poiBuilder = "";
        ArrayList<String> intrestList = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsSave", 1);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        for (int i =0; i < modelItems.length; i++){
            if (modelItems[i].getValue() == 1){
                if (modelItems[i].getName() == "Animals"){
                    intrestList.add("veterinary_care|zoo");
                }
                else if (modelItems[i].getName() == "Accomodation"){
                    intrestList.add("lodging");
                }
                else if (modelItems[i].getName() == "Food & Drink"){
                    intrestList.add("bakery|bar|café|department_store|furniture_store|grocery_or_supermarket|meal_delivery|meal_takeaway|restaurant");
                }
                else if (modelItems[i].getName() == "Education"){
                    intrestList.add("school|university");
                }
                else if (modelItems[i].getName() == "Entertainment"){
                    intrestList.add("amusement_park|aquarium|bowling_alley|casino|movie_rental|movie_theater|moving_company|night_club");
                }
                else if (modelItems[i].getName() == "Health"){
                    intrestList.add("dentist|doctor|hospital|pharmacy|physiotherapist|spa");
                }
                else if (modelItems[i].getName() == "Library"){
                    intrestList.add("library");
                }
                else if (modelItems[i].getName() == "Shops"){
                    intrestList.add("clothing_store|convenience_store|electronics_store|florist|hardware_store|home_goods_store|jewelry_store|liquor_store|pet_store|shoe_store|shopping_mall|store|grocery_or_supermarket");
                }
                else if (modelItems[i].getName() == "Money"){
                    intrestList.add("accounting|atm|bank");
                }
                else if (modelItems[i].getName() == "Petrol Station"){
                    intrestList.add("gas_station");
                }
                else if (modelItems[i].getName() == "Police"){
                    intrestList.add("police");
                }
                else if (modelItems[i].getName() == "Post Office"){
                    intrestList.add("post_office");
                }
                else if (modelItems[i].getName() == "Sport"){
                    intrestList.add("gym|stadium");
                }
                else if (modelItems[i].getName() == "Underground Station"){
                    intrestList.add("subway_station");
                }
                else if (modelItems[i].getName() == "Tourist Attraction"){
                    intrestList.add("art_gallery|campground|museum|park|rv_park");
                }
                else if (modelItems[i].getName() == "Transport"){
                    intrestList.add("airport|bus_station|parkingtaxi_stand|train_station|transit_station");
                }
            }
        }


        for (int i = 0; i < intrestList.size(); i++){
            poiBuilder += intrestList.get(i) + "|";
        }
        // remove last pipe
        if (poiBuilder != null && poiBuilder.length() > 0 && poiBuilder.charAt(poiBuilder.length()-1)=='|') {
            poiBuilder = poiBuilder.substring(0, poiBuilder.length()-1);
        }

        return poiBuilder;
    }

}
