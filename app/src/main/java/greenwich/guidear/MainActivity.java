package greenwich.guidear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements SensorEventListener, LocationListener, OnConnectionFailedListener, KeyEvent.Callback {

    private static double myElevation;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    private LocationManager mLocationManager;
    ArrayList<String> foundPOIs;
    ArrayList<String> foundLoc;
    ArrayList<String> directionPOIs;
    ArrayList<String> referenceNum;

    // Information boxes components
    // Use to remove the redundant box of screen
    ArrayList<ImageView> imageList = new ArrayList<ImageView>();
    ArrayList<TextView> textList = new ArrayList<TextView>();

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

    // String types = "cafe|restaurant|bar|school|train_station|shop|grocery_or_supermarket|bank";
    // Defaults if no settings found
    String types = "null";
    String radius = "1000";

    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // API KEY FOR Google Places
        // AIzaSyDGeooGmYLYSSX9P9zl9dWEXnUC2Dkpj9U

        ImageButton settingsBtn = (ImageButton) findViewById(R.id.imageButton);
        ImageButton helpBtn = (ImageButton) findViewById(R.id.imageButton2);

        settingsBtn.setImageResource(R.drawable.settings);
        helpBtn.setImageResource(R.drawable.help);

        Display d = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        int width = size.x;
        int height = size.y;

        helpBtn.setX(width - 192);

        final Intent settingsScreen = new Intent(this, SettingsActivity.class);
        final Intent helpScreen = new Intent(this, HelpActivity.class);

        SharedPreferences sharedPreferences = null;
        String radiusState = "1000";
        String typesState = "null";
        try {
            // Access settings
            sharedPreferences = getSharedPreferences("SettingsSave", 1);
            radiusState = sharedPreferences.getString("r", "1000");
            typesState = sharedPreferences.getString("types", "null");
        }
        catch (NullPointerException npe){
            System.out.println(npe);
        }


        radius = radiusState;
        types = typesState;

        settingsBtn.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                            settingsScreen.putExtra("defaultPOI", types);
                                            startActivityForResult(settingsScreen, 1);
                                     }
                                 }
        );

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startActivityForResult(helpScreen, 2);
            }
        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.myRelativeLayout);
        // Make the entire screen clicable -- Force update / Cheating
        relativeLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeRedundantBoxes();
                        new LoadPlaces().execute(radius);
                    }
                }
        );


        // Handle uncaught exceptions and email me the log
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                Intent intent = new Intent (Intent.ACTION_SEND);
                intent.setType ("plain/text");
                intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"marcinbrett@gmail.com"});
                intent.putExtra (Intent.EXTRA_SUBJECT, "GuideAR log file (Courtesy of Josh T)");
                //intent.putExtra (Intent.EXTRA_STREAM, Uri.parse ("file://" + fullName));
                intent.putExtra (Intent.EXTRA_TEXT, e.getMessage() + " \n \n " + errors); // do this so some email clients don't complain about empty body.
                startActivity (intent);
                System.out.println(errors);
                System.exit(1); // kill off the crashed app
            }
        });


    }

    public void addImg(double angle, String text, double elevation, final String reference){
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.myRelativeLayout);
        ImageView img = new ImageView(MainActivity.this);
        TextView txt = new TextView(MainActivity.this);

        Display d = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        int width = size.x;
        int height = size.y;

        angle = angle/30;

        double elev = elevation - myElevation;
        double elevPercentage = elev / myElevation;


        int xPos = (int)Math.round(width*angle);
        int yPos = (int)Math.round(height*elevPercentage);

        int middleY = (int) height / 2;
        int middleX = (int) width / 2;

        if (yPos < 0){
            yPos = middleY - Math.abs(yPos);
        }
        else {
            yPos = middleY + yPos;
        }


        img.setBackgroundResource(R.drawable.frame);
        img.setScaleX(0.4f);
        img.setScaleY(0.3f);
        img.setX((-270) + xPos);
        img.setY((-185) + yPos);
        img.setAlpha(0.5f);

        txt.setText(text);
        txt.setTextSize(13);
        txt.setWidth(270);
        txt.setHeight(185);
        txt.setSingleLine(false);
        txt.setTextColor(Color.parseColor("#ffffff"));
        txt.setX(xPos);
        txt.setY(yPos + 2);

        imageList.add(img);
        textList.add(txt);

        relativeLayout.addView(img);
        relativeLayout.addView(txt);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        PlaceInfo.class);
                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra(KEY_REFERENCE, reference);
                startActivity(in);
            }
        });


    }

    public void removeRedundantBoxes(){
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.myRelativeLayout);
        for (int i = 0; i < imageList.size(); i++){
            relativeLayout.removeView(imageList.get(i));
        }
        for (int i = 0; i < textList.size(); i++){
            relativeLayout.removeView(textList.get(i));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == Activity.RESULT_OK) {
                    radius = data.getStringExtra("radius").toString();
                    types = data.getStringExtra("POI").toString();
                    }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onLocationChanged(Location location) {
        // Handle Location the longitude and latidue of the phone with Location Manager
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        // See if the phone has got permissions
        try{
            spinner.setVisibility(View.VISIBLE);
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        catch (SecurityException e){

        }
        // calling background Async task to load Google Places
        // After getting places from Google all the data is shown in listview
        foundPOIs = new ArrayList<String>();
        foundLoc = new ArrayList<String>();
        directionPOIs = new ArrayList<String>();
        referenceNum = new ArrayList<>();
        GooglePlaces.foundLoc = new ArrayList<>();
        removeRedundantBoxes();
        new LoadMyElevation().execute();
        spinner.setVisibility(View.GONE);
        new LoadPlaces().execute(radius);
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
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI); // Compass Listener
        // Acceleromteter and Mangetometer Listener
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
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
        // x axis = event.values[0]
        // y axis = event.values[1]
        // z axis = event.values[2]
        float orientation[] = new float[3];

        // Check if phone has got accelerometer and magnetometer
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    //System.out.println(Math.round(Math.toDegrees(orientation[0])));
                    double degrees = Math.toDegrees((orientation[0]));
                    if (Math.round(degrees) < 0){
                        degrees += 360;
                    }
                    String compassDirection = directions[ (int)Math.round(((degrees % 360) / 45)) % 8 ];
                    t.setText(Float.toString(Math.round(Math.toDegrees(orientation[0]))) + " = " + compassDirection);
                }
                updateView(orientation[0]);
            }
        }
        // Check if phone has got Compass
         else if (mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){
            float compassDegrees = Math.round(event.values[0]);
             float degrees = Math.round(event.values[0]);
             if (degrees < 0){
                 degrees += 360;
             }
            String compassDirection = directions[ (int)Math.round((  ((double)degrees % 360) / 45)) % 8 ];
            t.setText(Float.toString(compassDegrees) + " = " + compassDirection);
        }


    }

    public void updateView(double ang){
        final double angle = ang;
        new Thread(new Runnable() {
            @Override
            public void run() {
                removeRedundantBoxes();
                if (longitude != 0 && latitude != 0){
                    for (int i = 0; i < foundPOIs.size(); i++){
                        // dis[0] = lat, dis[1] = long
                        String[] dis = foundLoc.get(i).split(" ");
                        double poiLat = Double.parseDouble(dis[0]);
                        double poiLon = Double.parseDouble(dis[1]);

                        String distance = ""+findDistance(longitude, latitude, poiLon, poiLat);

                        double ang = poiAngle(poiLat, poiLon, angle, Integer.parseInt(radius));

                        if (inFOV(poiLat, poiLon, angle, Integer.parseInt(radius))){
                            addImg(ang, foundPOIs.get(i) + " " + distance + "m", GoogleElevation.elevationList.get(i), referenceNum.get(i));
                        }
                    }

                }
            }
        });
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
            final TextView t = (TextView) findViewById(R.id.textView);
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
                            t.setText("");
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
                                referenceNum.add(p.reference);

                                // adding HashMap to ArrayList
                                placesListItems.add(map);

                                for (String s: map.keySet()){
                                        String value = map.get(s).toString();
                                        String[] arr = value.split(" ");
                                        if (s == "name") {
                                            foundPOIs.add(value);
                                        }
                                }
                            }

                        }
                        TextView directionView = (TextView) findViewById(R.id.textView2);
                        String[] arr = directionView.getText().toString().split(" ");
                        double kilometer = Integer.parseInt(radius) / 1000;
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

                            double ang = poiAngle(poiLat, poiLon, angle, Integer.parseInt(radius));

                            if (inFOV(poiLat, poiLon, angle, Integer.parseInt(radius))){
                                directionPOIs.add(foundPOIs.get(i) + " " + distance + "m");
                                addImg(ang, foundPOIs.get(i) + " " + distance + "m", GoogleElevation.elevationList.get(i), referenceNum.get(i));
                            }
                        }

                        //ListView lv = (ListView) findViewById(R.id.listView);
                        //lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.simple_list_item_1, directionPOIs));
                    }
                    else if (status.equals("ZERO_RESULTS")){
                        t.setText("No places in radius");
                    }
                    else if (status.equals("OVER_QUERY_LIMIT")){
                        t.setText("OVER_QUERY_LIMIT");
                    }
                    else if (directionPOIs.isEmpty()){
                        t.setText("No places in radius");
                    }
                    //ListView lv = (ListView) findViewById(R.id.listView);
                    //lv.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.simple_list_item_1, directionPOIs));
                }
            });

        }

    }

    class LoadMyElevation extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            myElevation = GoogleElevation.getElevation(latitude, longitude);
            return null;
        }
    }
}
