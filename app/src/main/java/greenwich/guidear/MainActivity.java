package greenwich.guidear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener, LocationListener, OnConnectionFailedListener, KeyEvent.Callback {

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private Geocoder mGeoCoder;
    List<Address> locationAddress;
    ArrayList<String> foundPOIs;
    ArrayList<String> foundLoc;

    // Google Places
    GooglePlaces googlePlaces;

    // Places List
    PlacesList nearPlaces;

    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();
    double longitude = 0.0;
    double latitude = 0.0;


    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*  GET PHONE NUMBER
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = mTelephonyMgr.getLine1Number();
        System.out.println(phoneNumber);
        */


        // API KEY FOR Google Places
        // AIzaSyDGeooGmYLYSSX9P9zl9dWEXnUC2Dkpj9U


        /*final Button buttonOne = (Button) findViewById(R.id.button);
        final ImageView image = (ImageView) findViewById(R.id.myImageView);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if (buttonOne.getText().toString().equals("Pika")){
                    image.setImageResource(R.drawable.pikachu);
                    image.setVisibility(View.VISIBLE);
                    buttonOne.setText("Chu");
                }
                else if (buttonOne.getText().toString().equals("Chu")){
                    image.setVisibility(View.INVISIBLE);
                    buttonOne.setText("Pika");
                }


            }
        });*/


        /*int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }
        catch (Exception gna){

        }*/
        // Create intent to access camera in Photo mode
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Start the camera
        //startActivityForResult(intent, 100);

        ImageButton image = (ImageButton) findViewById(R.id.imageButton);
        image.setImageResource(R.drawable.settings);


        final Intent settingsScreen = new Intent(this, SettingsActivity.class);

        image.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                            startActivity(settingsScreen);
                                     }
                                 }
        );

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onLocationChanged(Location location) {
        // Handle Location the longitude and latidue of the phone with Location Manager
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        TextView t = (TextView) findViewById(R.id.textView);

        // See if the phone has got permissions
        try{
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            t.setText("Long: " + longitude + "°, Lat: " + latitude + "°");
        }
        catch (SecurityException e){
            t.setText(""+e);
        }
        try{
            // Change the longitude and latidue into a address
            mGeoCoder = new Geocoder(this, Locale.getDefault());
            locationAddress = mGeoCoder.getFromLocation(latitude, longitude, 1);
            String address = locationAddress.get(0).getAddressLine(0);
            String city = locationAddress.get(0).getLocality();
            String state = locationAddress.get(0).getAdminArea();
            String country = locationAddress.get(0).getCountryName();
            String postCode = locationAddress.get(0).getPostalCode();
            String knownName = locationAddress.get(0).getFeatureName();

            t.append("\n" + address + ", " + state + ", " + city + ", " + country + ", " + postCode + ", " + knownName);
        }
        catch (IOException ioe){
            System.out.println(ioe);
        }
        // calling background Async task to load Google Places
        // After getting places from Google all the data is shown in listview
        foundPOIs = new ArrayList<String>();
        foundLoc = new ArrayList<String>();
        EditText r = (EditText) findViewById(R.id.editText);
        new LoadPlaces().execute(r.getText().toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }

    @Override
    protected void onResume(){
        super.onResume();
        // Register movement
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

        try{
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
        }
        catch (SecurityException e){

        }
    }

    @Override
    public void onPause(){
        super.onPause();
        // To save battery
        mSensorManager.unregisterListener(this);

        try{
            mLocationManager.removeUpdates(this);
        }
        catch (SecurityException e){}
    }

    // Handle sensor events
    @Override
    public void onSensorChanged(SensorEvent event){
        float degrees = Math.round(event.values[0]);

        TextView t = (TextView) findViewById(R.id.textView2);

        String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        String compassDirection = directions[ (int)Math.round((  ((double)degrees % 360) / 45)) % 8 ];

        t.setText(Float.toString(degrees) + " = " + compassDirection);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
/*
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        if (keyCode == 66){
            EditText r = (EditText) findViewById(R.id.editText);
            r.setText(r.getText());
        }
        return true;
    }*/

    class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();


            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            TextView t = (TextView) findViewById(R.id.textView);
            double longitude = 0.0;
            double latitude = 0.0;
            // See if the phone has got permissions
            Location location;
            try{
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
            catch (SecurityException e){
            }
            // See if the phone has got permissions
            try {
                // Separeate your place types by PIPE symbol "|"
                // If you want all types places make it as null
                // Check list of types supported by google
                //
                String types = "cafe|restaurant|bar|grocery_or_supermarket|gas_station|taxi_stand|bank|cemetery|park|school"; // Listing places only cafes, restaurants

                // Radius in meters - increase this value if you don't find any places
                // double radius = 1000; // 1000 meters
                double radius = Double.parseDouble(args[0]);
                // get nearest places
                nearPlaces = googlePlaces.search(latitude,
                        longitude, radius, types);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = nearPlaces.status;

                    // Check for all possible status
                    if(status.equals("OK")){
                        // Successfully got places details
                        if (nearPlaces.results != null) {
                            // loop through each place
                            for (Place p : nearPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                // Place reference won't display in listview - it will be hidden
                                // Place reference is used to get "place full details"
                                map.put(KEY_REFERENCE, p.reference);
                                // Place name
                                map.put(KEY_NAME, p.name);
                                /*
                                "geometry" : {
                                        "location" : {
                                            "lat" : -33.86755700000001,
                                            "lng" : 151.201527
                                        },
                                 */
                                String strLoc = p.geometry.location.lat + " " + p.geometry.location.lng;
                                foundLoc.add(strLoc);

                                // adding HashMap to ArrayList
                                placesListItems.add(map);

                                for (String s: map.keySet()){
                                        String value = map.get(s).toString();
                                        String[] arr = value.split(" ");
                                        if (s == "name") {
                                            //System.out.println(value);
                                            foundPOIs.add(value);
                                        }
                                }
                            }

                            TextView directionView = (TextView) findViewById(R.id.textView2);
                            String[] arr = directionView.getText().toString().split(" ");
                            System.out.println(arr[2]);
                            EditText radiusValue = (EditText) findViewById(R.id.editText);
                            double kilometer = Integer.parseInt(radiusValue.getText()+"") / 1000;

                            double longitudeDir = kilometer / 111.2;
                            double latitudeDir = kilometer / 111.32;


                            ListView lv = (ListView) findViewById(R.id.listView);
                            lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.simple_list_item_1, foundPOIs));

                            /*
                            TextView t = (TextView) findViewById(R.id.textView3);
                            String allPoi = "";
                            for (int i = 0; i < foundPOIs.size(); i++){
                                allPoi += (i+1) + "." + foundPOIs.get(i) + ", ";
                            }
                            t.setText("Near you:"+allPoi);
                            */
                        }
                    }
                    else if (status.equals("ZERO_RESULTS")){
                        foundPOIs.add("No places in radius");
                    }
                    ListView lv = (ListView) findViewById(R.id.listView);
                    lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.simple_list_item_1, foundPOIs));
                }
            });

        }

    }
}
