package greenwich.guidear;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Marcin on 08/08/2016.
 */
public class SettingsActivity extends Activity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);
        LoadPreferences();
        // find the seek bar and change value of the textbox
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final EditText radiusText = (EditText) findViewById(R.id.editText2);
        // set seek bar properties
        seekBar.incrementProgressBy(100); // step by 100 meters
        seekBar.setMax(5000);
        seekBar.setProgress(Integer.parseInt(radiusText.getText().toString())); // start value


        Bundle state = getIntent().getExtras();
        if (state != null){
            // defaultPOI
            String temp = state.get("defaultPOI").toString();
            String[] arr = temp.split("\\|");

            for (int i = 0; i < arr.length; i++){
                System.out.println(arr[i]);
                String switchName = arr[i] + "Switch";
                int resID = getResources().getIdentifier(switchName, "id", getPackageName());
                Switch tempSwitch = (Switch) findViewById(resID);
                //tempSwitch.setChecked(true);
            }
        }

        Button saveBtn = (Button) findViewById(R.id.saveSettingsBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           SavePreferences();
                                           Intent resultIntent = new Intent();
                                           resultIntent.putExtra("radius", radiusText.getText().toString());
                                           final String POIString = PointOfInterestBuilder();
                                           resultIntent.putExtra("POI", POIString);
                                           setResult(Activity.RESULT_OK, resultIntent);
                                           finish();
                                       }
                                   }
        );

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private void SavePreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsSave", 1);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Radius of the search
        EditText radiusText = (EditText) findViewById(R.id.editText2);
        editor.putString("r", radiusText.getText().toString());

        // All 18 switches urgh!
        Switch airportSwitch = (Switch) findViewById(R.id.airportSwitch);
        editor.putBoolean("airportSwitch", airportSwitch.isChecked());

        Switch atmSwitch = (Switch) findViewById(R.id.atmSwitch);
        editor.putBoolean("atmSwitch", atmSwitch.isChecked());

        Switch bankSwitch = (Switch) findViewById(R.id.bankSwitch);
        editor.putBoolean("bankSwitch", bankSwitch.isChecked());

        Switch barSwitch = (Switch) findViewById(R.id.barSwitch);
        editor.putBoolean("barSwitch", barSwitch.isChecked());

        Switch bus_stationSwitch = (Switch) findViewById(R.id.bus_stationSwitch);
        editor.putBoolean("bus_stationSwitch", bus_stationSwitch.isChecked());

        Switch cafeSwitch = (Switch) findViewById(R.id.cafeSwitch);
        editor.putBoolean("cafeSwitch", cafeSwitch.isChecked());

        Switch city_hallSwitch = (Switch) findViewById(R.id.city_hallSwitch);
        editor.putBoolean("city_hallSwitch", city_hallSwitch.isChecked());

        Switch doctorSwitch = (Switch) findViewById(R.id.doctorSwitch);
        editor.putBoolean("doctorSwitch", doctorSwitch.isChecked());

        Switch gas_stationSwitch = (Switch) findViewById(R.id.gas_stationSwitch);
        editor.putBoolean("gas_stationSwitch", gas_stationSwitch.isChecked());

        Switch hospitalSwitch = (Switch) findViewById(R.id.hospitalSwitch);
        editor.putBoolean("hospitalSwitch", hospitalSwitch.isChecked());

        Switch librarySwitch = (Switch) findViewById(R.id.librarySwitch);
        editor.putBoolean("librarySwitch", librarySwitch.isChecked());

        Switch liquor_storeSwitch = (Switch) findViewById(R.id.liquor_storeSwitch);
        editor.putBoolean("liquor_storeSwitch", liquor_storeSwitch.isChecked());

        Switch parkSwitch = (Switch) findViewById(R.id.parkSwitch);
        editor.putBoolean("parkSwitch", parkSwitch.isChecked());

        Switch schoolSwitch = (Switch) findViewById(R.id.schoolSwitch);
        editor.putBoolean("schoolSwitch", schoolSwitch.isChecked());

        Switch restaurantSwitch = (Switch) findViewById(R.id.restaurantSwitch);
        editor.putBoolean("restaurantSwitch", restaurantSwitch.isChecked());

        Switch train_stationSwitch = (Switch) findViewById(R.id.train_stationSwitch);
        editor.putBoolean("train_stationSwitch", train_stationSwitch.isChecked());

        Switch post_officeSwitch = (Switch) findViewById(R.id.post_officeSwitch);
        editor.putBoolean("post_officeSwitch", post_officeSwitch.isChecked());

        Switch universitySwitch = (Switch) findViewById(R.id.universitySwitch);
        editor.putBoolean("universitySwitch", universitySwitch.isChecked());

        Switch grocery_or_supermarketSwitch = (Switch) findViewById(R.id.grocery_or_supermarketSwitch);
        editor.putBoolean("grocery_or_supermarketSwitch", grocery_or_supermarketSwitch.isChecked());

        Switch lodgingSwitch = (Switch) findViewById(R.id.lodgingSwitch);
        editor.putBoolean("lodgingSwitch", lodgingSwitch.isChecked());

        Switch everythingSwitch = (Switch) findViewById(R.id.nullSwitch);
        editor.putBoolean("nullSwitch", everythingSwitch.isChecked());

        Switch electronics_storeStwich = (Switch) findViewById(R.id.electronics_storeSwitch);
        editor.putBoolean("electronics_storeSwitch", electronics_storeStwich.isChecked());

        final String POIString = PointOfInterestBuilder();
        editor.putString("types", POIString);

        editor.commit();
    }

    private void LoadPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsSave", 1);
        String radiusState = sharedPreferences.getString("r", "1000");
        EditText radiusText = (EditText) findViewById(R.id.editText2);
        radiusText.setText(radiusState);

        // All 18 switches urgh!
        Switch airportSwitch = (Switch) findViewById(R.id.airportSwitch);
        Boolean airportSwitchState = sharedPreferences.getBoolean("airportSwitch", false);
        airportSwitch.setChecked(airportSwitchState);

        Switch atmSwitch = (Switch) findViewById(R.id.atmSwitch);
        Boolean atmSwitchState = sharedPreferences.getBoolean("atmSwitch", false);
        atmSwitch.setChecked(atmSwitchState);

        Switch bankSwitch = (Switch) findViewById(R.id.bankSwitch);
        Boolean bankSwitchState = sharedPreferences.getBoolean("bankSwitch", false);
        bankSwitch.setChecked(bankSwitchState);

        Switch barSwitch = (Switch) findViewById(R.id.barSwitch);
        Boolean barSwitchState = sharedPreferences.getBoolean("barSwitch", false);
        barSwitch.setChecked(barSwitchState);

        Switch bus_stationSwitch = (Switch) findViewById(R.id.bus_stationSwitch);
        Boolean bus_stationSwitchState = sharedPreferences.getBoolean("bus_stationSwitch", false);
        bus_stationSwitch.setChecked(bus_stationSwitchState);

        Switch cafeSwitch = (Switch) findViewById(R.id.cafeSwitch);
        Boolean cafeSwitchState = sharedPreferences.getBoolean("cafeSwitch", false);
        cafeSwitch.setChecked(cafeSwitchState);

        Switch city_hallSwitch = (Switch) findViewById(R.id.city_hallSwitch);
        Boolean city_hallSwitchState = sharedPreferences.getBoolean("city_hallSwitch", false);
        city_hallSwitch.setChecked(city_hallSwitchState);

        Switch doctorSwitch = (Switch) findViewById(R.id.doctorSwitch);
        Boolean doctorSwitchState = sharedPreferences.getBoolean("doctorSwitch", false);
        doctorSwitch.setChecked(doctorSwitchState);

        Switch gas_stationSwitch = (Switch) findViewById(R.id.gas_stationSwitch);
        Boolean gas_stationSwitchState = sharedPreferences.getBoolean("gas_stationSwitch", false);
        gas_stationSwitch.setChecked(gas_stationSwitchState);

        Switch hospitalSwitch = (Switch) findViewById(R.id.hospitalSwitch);
        Boolean hospitalSwitchState = sharedPreferences.getBoolean("hospitalSwitch", false);
        hospitalSwitch.setChecked(hospitalSwitchState);

        Switch librarySwitch = (Switch) findViewById(R.id.librarySwitch);
        Boolean librarySwitchState = sharedPreferences.getBoolean("librarySwitch", false);
        librarySwitch.setChecked(librarySwitchState);

        Switch liquor_storeSwitch = (Switch) findViewById(R.id.liquor_storeSwitch);
        Boolean liquor_storeSwitchState = sharedPreferences.getBoolean("liquor_storeSwitch", false);
        liquor_storeSwitch.setChecked(liquor_storeSwitchState);

        Switch parkSwitch = (Switch) findViewById(R.id.parkSwitch);
        Boolean parkSwitchState = sharedPreferences.getBoolean("parkSwitch", false);
        parkSwitch.setChecked(parkSwitchState);

        Switch schoolSwitch = (Switch) findViewById(R.id.schoolSwitch);
        Boolean schoolSwitchState = sharedPreferences.getBoolean("schoolSwitch", false);
        schoolSwitch.setChecked(schoolSwitchState);

        Switch restaurantSwitch = (Switch) findViewById(R.id.restaurantSwitch);
        Boolean restaurantSwitchState = sharedPreferences.getBoolean("restaurantSwitch", false);
        restaurantSwitch.setChecked(restaurantSwitchState);

        Switch train_stationSwitch = (Switch) findViewById(R.id.train_stationSwitch);
        Boolean train_stationSwitchState = sharedPreferences.getBoolean("train_stationSwitch", false);
        train_stationSwitch.setChecked(train_stationSwitchState);

        Switch post_officeSwitch = (Switch) findViewById(R.id.post_officeSwitch);
        Boolean post_officeSwitchState = sharedPreferences.getBoolean("post_officeSwitch", false);
        post_officeSwitch.setChecked(post_officeSwitchState);

        Switch universitySwitch = (Switch) findViewById(R.id.universitySwitch);
        Boolean universitySwitchState = sharedPreferences.getBoolean("universitySwitch", false);
        universitySwitch.setChecked(universitySwitchState);

        Switch grocery_or_supermarketSwitch = (Switch) findViewById(R.id.grocery_or_supermarketSwitch);
        Boolean grocery_or_supermarketSwitchState = sharedPreferences.getBoolean("grocery_or_supermarketSwitch", false);
        grocery_or_supermarketSwitch.setChecked(grocery_or_supermarketSwitchState);

        Switch lodgingSwitch = (Switch) findViewById(R.id.lodgingSwitch);
        Boolean lodgingSwitchState = sharedPreferences.getBoolean("lodgingSwitch", false);
        lodgingSwitch.setChecked(lodgingSwitchState);

        Switch nullSwitch = (Switch) findViewById(R.id.nullSwitch);
        Boolean nullSwitchState = sharedPreferences.getBoolean("nullSwitch", false);
        nullSwitch.setChecked(nullSwitchState);

        Switch electronics_storeSwitch = (Switch) findViewById(R.id.electronics_storeSwitch);
        Boolean electronics_storeState = sharedPreferences.getBoolean("electronics_storeSwitch", false);
        electronics_storeSwitch.setChecked(electronics_storeState);

    }

    public String PointOfInterestBuilder(){
        String poiBuilder = "";
        ArrayList<String> intrestList = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsSave", 1);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // All 18 switches urgh!
        Switch airportSwitch = (Switch) findViewById(R.id.airportSwitch);
        Switch atmSwitch = (Switch) findViewById(R.id.atmSwitch);
        Switch bankSwitch = (Switch) findViewById(R.id.bankSwitch);
        Switch barSwitch = (Switch) findViewById(R.id.barSwitch);
        Switch bus_stationSwitch = (Switch) findViewById(R.id.bus_stationSwitch);
        Switch cafeSwitch = (Switch) findViewById(R.id.cafeSwitch);
        Switch city_hallSwitch = (Switch) findViewById(R.id.city_hallSwitch);
        Switch doctorSwitch = (Switch) findViewById(R.id.doctorSwitch);
        Switch gas_stationSwitch = (Switch) findViewById(R.id.gas_stationSwitch);
        Switch hospitalSwitch = (Switch) findViewById(R.id.hospitalSwitch);
        Switch librarySwitch = (Switch) findViewById(R.id.librarySwitch);
        Switch liquor_storeSwitch = (Switch) findViewById(R.id.liquor_storeSwitch);
        Switch parkSwitch = (Switch) findViewById(R.id.parkSwitch);
        Switch schoolSwitch = (Switch) findViewById(R.id.schoolSwitch);
        Switch restaurantSwitch = (Switch) findViewById(R.id.restaurantSwitch);
        Switch train_stationSwitch = (Switch) findViewById(R.id.train_stationSwitch);
        Switch post_officeSwitch = (Switch) findViewById(R.id.post_officeSwitch);
        Switch universitySwitch = (Switch) findViewById(R.id.universitySwitch);
        Switch grocery_or_supermarketSwitch = (Switch) findViewById(R.id.grocery_or_supermarketSwitch);
        Switch lodgingSwitch = (Switch) findViewById(R.id.lodgingSwitch);
        Switch nullSwitch = (Switch) findViewById(R.id.nullSwitch);
        Switch electronics_storeSwitch = (Switch) findViewById(R.id.electronics_storeSwitch);

        if (airportSwitch.isChecked()){
            intrestList.add("airport");
        }
        if (atmSwitch.isChecked()){
            intrestList.add("atm");
        }
        if (bankSwitch.isChecked()){
            intrestList.add("bank");
        }
        if (barSwitch.isChecked()){
            intrestList.add("bar");
        }
        if(bus_stationSwitch.isChecked()){
            intrestList.add("bus_station");
        }
        if (cafeSwitch.isChecked()){
            intrestList.add("cafe");
        }
        if(city_hallSwitch.isChecked()){
            intrestList.add("city_hall");
        }
        if (doctorSwitch.isChecked()){
            intrestList.add("doctor");
        }
        if (gas_stationSwitch.isChecked()){
            intrestList.add("gas_station");
        }
        if (hospitalSwitch.isChecked()){
            intrestList.add("hospital");
        }
        if (librarySwitch.isChecked()){
            intrestList.add("library");
        }
        if (liquor_storeSwitch.isChecked()){
            intrestList.add("liquor_store");
        }
        if (parkSwitch.isChecked()){
            intrestList.add("park");
        }
        if(schoolSwitch.isChecked()){
            intrestList.add("school");
        }
        if (restaurantSwitch.isChecked()){
            intrestList.add("restaurant");
        }
        if (train_stationSwitch.isChecked()){
            intrestList.add("train_station");
        }
        if(post_officeSwitch.isChecked()){
            intrestList.add("post_office");
        }
        if (universitySwitch.isChecked()){
            intrestList.add("university");
        }
        if (grocery_or_supermarketSwitch.isChecked()){
            intrestList.add("grocery_or_supermarket");
        }
        if (lodgingSwitch.isChecked()){
            intrestList.add("lodging");
        }
        if (nullSwitch.isChecked()){
            intrestList = new ArrayList<>();
            intrestList.add("null");
        }
        if (electronics_storeSwitch.isChecked()){
            intrestList.add("electronics_store");
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
