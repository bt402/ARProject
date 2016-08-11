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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    Sensor accelerometer;
    Sensor magnetometer;
    private LocationManager mLocationManager;
    private Geocoder mGeoCoder;
    List<Address> locationAddress;
    ArrayList<String> foundPOIs;
    ArrayList<String> foundLoc;
    ArrayList<String> directionPOIs;

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

    String types = "cafe|restaurant|bar|school|train_station|shop|grocery_or_supermarket|bank";

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


        /*int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }
        catch (Exception gna){

        }*/

        ImageButton image = (ImageButton) findViewById(R.id.imageButton);
        image.setImageResource(R.drawable.settings);


        final Intent settingsScreen = new Intent(this, SettingsActivity.class);

        image.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                            settingsScreen.putExtra("defaultPOI", types);
                                            startActivityForResult(settingsScreen, 1);
                                     }
                                 }
        );

        Button updateBtn = (Button) findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foundPOIs = new ArrayList<String>();
                foundLoc = new ArrayList<String>();
                directionPOIs = new ArrayList<String>();
                EditText r = (EditText) findViewById(R.id.editText);
                new LoadPlaces().execute(r.getText().toString());
            }
        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == Activity.RESULT_OK) {
                    TextView r = (TextView) findViewById(R.id.editText);
                    r.setText(data.getStringExtra("radius").toString());
                    types = data.getStringExtra("POI").toString();
                    }
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
        directionPOIs = new ArrayList<String>();

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
        //mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
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
    float[] mGravity;
    float[] mGeomagnetic;
    @Override
    public void onSensorChanged(SensorEvent event){

        TextView t = (TextView) findViewById(R.id.textView2);
        String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                //System.out.println(Math.round(Math.toDegrees(orientation[0])));
                double degrees = Math.toDegrees(orientation[0]);
                if (Math.round(degrees) < 0){
                    degrees += 360;
                }
                String compassDirection = directions[ (int)Math.round(((degrees % 360) / 45)) % 8 ];
                t.setText(Float.toString(Math.round(Math.toDegrees(orientation[0]))) + " = " + compassDirection);
            }
    }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    public double poiAngle(double plat, double plon, double angle, double radius){
        radius = radius / 110572;
        double poilon = plon;
        double poilat = plat;
        double fpovlat = latitude + Math.cos(Math.toRadians(angle)) * radius;
        double fpovlon = longitude + Math.sin(Math.toRadians(angle)) * radius;

        double sidea = Math.sqrt(Math.pow((fpovlat - poilat),2) + Math.pow((fpovlon - poilon),2));
        double sideb = Math.sqrt(Math.pow((latitude - poilat),2) + Math.pow((longitude - poilon),2));
        double sidec = Math.sqrt(Math.pow((latitude - fpovlat),2) + Math.pow((longitude - fpovlon),2));

        double cosAngle = (Math.pow(sideb,2) + Math.pow(sidec, 2) - Math.pow(sidea,2)) / (2 * sideb * sidec);
        double radans = Math.acos(cosAngle);
        double ans = Math.toDegrees(radans);
        return ans;
    }

    public boolean inFOV(double lon, double lat, double angle, double radius){
        if (poiAngle(lon, lat, angle, radius) < 30){
            return true;
        }
        else {
            return false;
        }
    }

    public static double findDistance(double lon1, double lat1, double lon2, double lat2) {
        int planetRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = planetRadius * c;
        return Math.round(dist * 1000);
    }

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
                            double kilometer = Integer.parseInt(radiusValue.getText().toString()) / 1000;

                            double longitudeDir = kilometer / 111.2;
                            double latitudeDir = kilometer / 111.32;
                            TextView deg = (TextView) findViewById(R.id.textView2);
                            String[] degrees = deg.getText().toString().split(" ");
                            for (int i = 0; i < foundPOIs.size(); i++){
                                // dis[0] = lat, dis[1] = long
                                String[] dis = foundLoc.get(i).split(" ");
                                double poiLat = Double.parseDouble(dis[0]);
                                double poiLon = Double.parseDouble(dis[1]);
                                double angle = Double.parseDouble(degrees[0]);

                                String distance = ""+findDistance(longitude, latitude, poiLon, poiLat);
                                if (inFOV(poiLat, poiLon, angle, Integer.parseInt(radiusValue.getText().toString()))){
                                    directionPOIs.add(foundPOIs.get(i) + " " + distance + "m");
                                }
                            }

                            ListView lv = (ListView) findViewById(R.id.listView);
                            lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.simple_list_item_1, directionPOIs));

                        }
                    }
                    else if (status.equals("ZERO_RESULTS")){
                        directionPOIs.add("No places in radius");
                    }
                    else if (status.equals("OVER_QUERY_LIMIT")){
                        directionPOIs.add("OVER_QUERY_LIMIT");
                    }
                    else if (directionPOIs.isEmpty()){
                        directionPOIs.add("No places in radius");
                    }
                    ListView lv = (ListView) findViewById(R.id.listView);
                    lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.simple_list_item_1, directionPOIs));
                }
            });

        }

    }
}
