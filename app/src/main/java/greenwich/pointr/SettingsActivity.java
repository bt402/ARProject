package greenwich.pointr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;


public class SettingsActivity extends Activity {

    Model[] modelItems = new Model[16];
    boolean checked = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);

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


        final CustomAdapter adapter = new CustomAdapter(this, modelItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the current item from ListView
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 1) {
                    view.setBackgroundColor(Color.parseColor("#262626"));
                } else {
                    view.setBackgroundColor(Color.parseColor("#333333"));
                }
                return view;
            }
        };


        listView.setAdapter(adapter);


        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar2);
        // set seek bar properties
        seekBar.incrementProgressBy(100); // step by 100 meters/yards
        seekBar.setMax(5000);
        final DecimalFormat df = new DecimalFormat("#.##");
        LoadPreferences();

        final String[] distance = distanceTxt.getText().toString().split(" ");
        seekBar.setProgress((int) Double.parseDouble(distance[0])); // start value

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                double metricDistance = Double.parseDouble(distance[0]);
                double yardDistance = metricDistance * 1.0936;
                if (isChecked) {
                    distanceTxt.setText("" + df.format(yardDistance) + " yards");
                    distance[1] = " yards";

                } else if (!isChecked) {
                    distanceTxt.setText("" + df.format(metricDistance / 1.0936) + " meters");
                    distance[1] = " meters";
                }
            }
        });


        everythingToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checked = true;
                    for (int i = 0; i < adapter.toggleButtonArrayList.size(); i++) {
                        System.out.println(adapter.toggleButtonArrayList.size());
                        adapter.toggleButtonArrayList.get(i).setChecked(false);
                        adapter.toggleButtonArrayList.get(i).setEnabled(false);
                    }
                } else {
                    checked = false;
                    for (int i = 0; i < adapter.toggleButtonArrayList.size(); i++) {
                        adapter.toggleButtonArrayList.get(i).setEnabled(true);
                    }
                }


            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (checked) {
                    for (int k = 0; k < adapter.toggleButtonArrayList.size(); k++) {
                        adapter.toggleButtonArrayList.get(k).setChecked(false);
                        adapter.toggleButtonArrayList.get(k).setEnabled(false);
                    }
                } else {
                    for (int k = 0; k < adapter.toggleButtonArrayList.size(); k++) {
                        adapter.toggleButtonArrayList.get(k).setEnabled(true);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                System.out.println(checked);
                if (checked) {
                    for (int k = 0; k < adapter.toggleButtonArrayList.size(); k++) {
                        adapter.toggleButtonArrayList.get(k).setChecked(false);
                        adapter.toggleButtonArrayList.get(k).setEnabled(false);
                    }
                } else {
                    for (int k = 0; k < adapter.toggleButtonArrayList.size(); k++) {
                        adapter.toggleButtonArrayList.get(k).setEnabled(true);
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceTxt.setText(String.valueOf(progress) + " " + distance[1]);
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
        TextView distanceTxt = (TextView) findViewById(R.id.distanceText);
        String[] distance = distanceTxt.getText().toString().split(" ");
        String radiusText = distance[0];

        if (distance[1].equals("yards")) {
            radiusText = "" + (int) (Double.parseDouble(distance[0]) / 1.0936);
        } else if (distance[1].equals("meters")) {
            radiusText = "" + (int) Double.parseDouble(distance[0]);
        }

        SavePreferences();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("radius", radiusText);
        String POIString = PointOfInterestBuilder();

        SharedPreferences sharedPreferences = getSharedPreferences("RefineSettings", 2);


        if (POIString == ""){
            SettingsDialogError settingsDialogError = new SettingsDialogError();
            settingsDialogError.emptyList(this);
        }
        else {
            Map<String,?> keys = sharedPreferences.getAll();

            String poiBuilder = "";
            for(Map.Entry<String,?> entry : keys.entrySet()){
                //Log.d("map values", entry.getValue().toString());
                if (!entry.getKey().toString().contains("Checked")){
                    poiBuilder += entry.getValue().toString()+"|";
                }
            }
            // remove last pipe
            if (poiBuilder != null && poiBuilder.length() > 0 && poiBuilder.charAt(poiBuilder.length() - 1) == '|') {
                poiBuilder = poiBuilder.substring(0, poiBuilder.length() - 1);
            }

            POIString = poiBuilder;
            resultIntent.putExtra("POI", POIString);
            setResult(Activity.RESULT_OK, resultIntent);

            super.onBackPressed();
        }
    }

    public String PointOfInterestBuilder() {
        String poiBuilder = "";
        ArrayList<String> intrestList = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsSave", 1);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        for (int i = 0; i < modelItems.length; i++) {
            if (modelItems[i].getValue() == 1) {
                if (modelItems[i].getName() == "Animals") {
                    intrestList.add("veterinary_care|zoo");
                } else if (modelItems[i].getName() == "Accomodation") {
                    intrestList.add("lodging");
                } else if (modelItems[i].getName() == "Food & Drink") {
                    intrestList.add("bakery|bar|café|department_store|furniture_store|grocery_or_supermarket|meal_delivery|meal_takeaway|restaurant");
                } else if (modelItems[i].getName() == "Education") {
                    intrestList.add("school|university");
                } else if (modelItems[i].getName() == "Entertainment") {
                    intrestList.add("amusement_park|aquarium|bowling_alley|casino|movie_rental|movie_theater|moving_company|night_club");
                } else if (modelItems[i].getName() == "Health") {
                    intrestList.add("dentist|doctor|hospital|pharmacy|physiotherapist|spa");
                } else if (modelItems[i].getName() == "Library") {
                    intrestList.add("library");
                } else if (modelItems[i].getName() == "Shops") {
                    intrestList.add("clothing_store|convenience_store|electronics_store|florist|hardware_store|home_goods_store|jewelry_store|liquor_store|pet_store|shoe_store|shopping_mall|store|grocery_or_supermarket");
                } else if (modelItems[i].getName() == "Money") {
                    intrestList.add("accounting|atm|bank");
                } else if (modelItems[i].getName() == "Petrol Station") {
                    intrestList.add("gas_station");
                } else if (modelItems[i].getName() == "Police") {
                    intrestList.add("police");
                } else if (modelItems[i].getName() == "Post Office") {
                    intrestList.add("post_office");
                } else if (modelItems[i].getName() == "Sport") {
                    intrestList.add("gym|stadium");
                } else if (modelItems[i].getName() == "Underground Station") {
                    intrestList.add("subway_station");
                } else if (modelItems[i].getName() == "Tourist Attraction") {
                    intrestList.add("art_gallery|campground|museum|park|rv_park");
                } else if (modelItems[i].getName() == "Transport") {
                    intrestList.add("airport|bus_station|parking|taxi_stand|train_station|transit_station");
                }
            }
        }

        if (checked){
            poiBuilder = "null";
        }


        for (int i = 0; i < intrestList.size(); i++) {
            poiBuilder += intrestList.get(i) + "|";
        }
        // remove last pipe
        if (poiBuilder != null && poiBuilder.length() > 0 && poiBuilder.charAt(poiBuilder.length() - 1) == '|') {
            poiBuilder = poiBuilder.substring(0, poiBuilder.length() - 1);
        }

        return poiBuilder;
    }

    private void SavePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsSave", 1);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Radius of the search
        TextView distanceTxt = (TextView) findViewById(R.id.distanceText);
        editor.putString("r", distanceTxt.getText().toString());

        ToggleButton everythingToggle = (ToggleButton) findViewById(R.id.EverythingToggle);
        editor.putBoolean("everythingTog", everythingToggle.isChecked());

        ToggleButton measureToggle = (ToggleButton) findViewById(R.id.mesuramentSwitch);
        editor.putBoolean("measurementTog", measureToggle.isChecked());

        for (int i = 0; i < modelItems.length; i++) {
            if (modelItems[i].getName() == "Animals") {
                editor.putInt("AnimalsSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Accomodation") {
                editor.putInt("AccomodationSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Food & Drink") {
                editor.putInt("FoodNDrinkSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Education") {
                editor.putInt("EducationSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Entertainment") {
                editor.putInt("EntertainmentSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Health") {
                editor.putInt("HealthSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Library") {
                editor.putInt("LibrarySwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Shops") {
                editor.putInt("ShopsSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Money") {
                editor.putInt("MoneySwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Petrol Station") {
                editor.putInt("PetrolSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Police") {
                editor.putInt("PoliceSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Post Office") {
                editor.putInt("PostOfficeSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Sport") {
                editor.putInt("SportSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Underground Station") {
                editor.putInt("UndergroundSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Tourist Attraction") {
                editor.putInt("TouristSwitch", modelItems[i].getValue());
            } else if (modelItems[i].getName() == "Transport") {
                editor.putInt("TransportSwitch", modelItems[i].getValue());
            }

        }


        final String POIString = PointOfInterestBuilder();
        editor.putString("types", POIString);

        editor.commit();
    }

    private void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsSave", 1);
        String radiusState = sharedPreferences.getString("r", "1000 meters");
        TextView distanceTxt = (TextView) findViewById(R.id.distanceText);
        distanceTxt.setText(radiusState);
        ToggleButton everythingToggle = (ToggleButton) findViewById(R.id.EverythingToggle);
        everythingToggle.setChecked(sharedPreferences.getBoolean("everythingTog", true));
        checked = sharedPreferences.getBoolean("everythingTog", true);
        ToggleButton measurementToggle = (ToggleButton) findViewById(R.id.mesuramentSwitch);
        measurementToggle.setChecked(sharedPreferences.getBoolean("measurementTog", true));

        if (measurementToggle.isChecked()) {
            String[] distance = radiusState.split(" ");
            distanceTxt.setText(distance[0] + " yards");
        } else {
            String[] distance = radiusState.split(" ");
            distanceTxt.setText(distance[0] + " meters");
        }

        modelItems[0] = new Model("Animals", sharedPreferences.getInt("AnimalsSwitch", 0));
        modelItems[1] = new Model("Accomodation", sharedPreferences.getInt("AccomodationSwitch", 0));
        modelItems[2] = new Model("Food & Drink", sharedPreferences.getInt("FoodNDrinkSwitch", 0));
        modelItems[3] = new Model("Education", sharedPreferences.getInt("EducationSwitch", 0));
        modelItems[4] = new Model("Entertainment", sharedPreferences.getInt("EntertainmentSwitch", 0));
        modelItems[5] = new Model("Health", sharedPreferences.getInt("HealthSwitch", 0));
        modelItems[6] = new Model("Library", sharedPreferences.getInt("LibrarySwitch", 0));
        modelItems[7] = new Model("Shops", sharedPreferences.getInt("ShopsSwitch", 0));
        modelItems[8] = new Model("Money", sharedPreferences.getInt("MoneySwitch", 0));
        modelItems[9] = new Model("Petrol Station", sharedPreferences.getInt("PetrolSwitch", 0));
        modelItems[10] = new Model("Police", sharedPreferences.getInt("PoliceSwitch", 0));
        modelItems[11] = new Model("Post Office", sharedPreferences.getInt("PostOfficeSwitch", 0));
        modelItems[12] = new Model("Sport", sharedPreferences.getInt("SportSwitch", 0));
        modelItems[13] = new Model("Underground Station", sharedPreferences.getInt("UndergroundSwitch", 0));
        modelItems[14] = new Model("Tourist Attraction", sharedPreferences.getInt("TouristSwitch", 0));
        modelItems[15] = new Model("Transport", sharedPreferences.getInt("TransportSwitch", 0));

    }

}
