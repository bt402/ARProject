package greenwich.pointr;

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
import android.os.Environment;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener, LocationListener, OnConnectionFailedListener, KeyEvent.Callback {

    private static double myElevation;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    private LocationManager mLocationManager;
    private static Context applicationContext;
    public static TextView errorMessageTextView;
    public static TextView directionView;
    public static TextView deg;
    public static LinkedHashSet<String> foundPOIs;
    public static ArrayList<String> foundLoc;
    public static ArrayList<String> directionPOIs;
    public static LinkedHashSet<String> referenceNum;

    // Information boxes components
    // Use to remove the redundant box of screen
    static ArrayList<ImageView> imageList = new ArrayList<>();
    static ArrayList<TextView> textList = new ArrayList<>();

    // Default text color is white
    String hexColor = "#ffffff";

    // Google Places
    GooglePlaces googlePlaces;

    // Places List
    PlacesList nearPlaces;

    // Use to cancel AsyncTask
    LoadPlaces loadPlaces;

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

    static int indexIterator = 0;
    ImageView shuffleImg;

    private OverlayView arContent;
    static MainActivity instance;

    public static double getMyElevation() {
        return myElevation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // API KEY FOR Google Places
        // AIzaSyDGeooGmYLYSSX9P9zl9dWEXnUC2Dkpj9U
        instance = this;
        applicationContext = getApplicationContext();
        errorMessageTextView  = (TextView) findViewById(R.id.textView);
        directionView = (TextView) findViewById(R.id.textView2);
        deg = (TextView) findViewById(R.id.textView2);
        // Check if Compass and/or Accelerometer are avaliable
        SensorDialog sensorDialog = new SensorDialog();
        sensorDialog.accelerometerExists(this);

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
            sharedPreferences = getSharedPreferences("SettingsSave", 0);
            radiusState = sharedPreferences.getString("r", "1000");
            typesState = sharedPreferences.getString("types", "null");
        }
        catch (NullPointerException npe){
            System.out.println(npe);
        }

        String[] radiusStateString = radiusState.split(" ");

        radius = radiusStateString[0];
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
                        loadPlaces = new LoadPlaces();
                        loadPlaces.execute(radius);
                        shuffleBoxes();
                    }
                }
        );


        shuffleImg = new ImageView(MainActivity.this);
        shuffleImg.setImageResource(R.drawable.shuffle);
        shuffleImg.setX((width / 2)-64);
        shuffleImg.setScaleX(0.9f);
        shuffleImg.setScaleY(0.9f);
        relativeLayout.addView(shuffleImg);

        // Handle uncaught exceptions and email me the log
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("plain/text");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"marcinbrett@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "GuideAR log file");
                    //intent.putExtra (Intent.EXTRA_STREAM, Uri.parse ("file://" + fullName));
                    intent.putExtra(Intent.EXTRA_TEXT, e.getMessage() + " \n \n " + errors); // do this so some email clients don't complain about empty body.
                    startActivity(intent);
                    System.out.println(errors);
                    System.exit(1); // kill off the crashed app
            }
        });

        FrameLayout arViewPane = (FrameLayout) findViewById(R.id.mySurfaceView);

        arContent = new OverlayView(getApplicationContext());
        arViewPane.addView(arContent);


        //GSM.setColorFilter(Color.parseColor("#FF3F51B5"));

        final ImageButton GSM = (ImageButton) findViewById(R.id.gsmBtn);
        GSM.setImageResource(R.drawable.gsm);
        GSM.setScaleX(0.3f);
        GSM.setScaleY(0.3f);
        GSM.setX(GSM.getX() + 40);
        GSM.setY((height-256) - 250);

        final ImageButton WIFI = (ImageButton) findViewById(R.id.wifiBtn);
        WIFI.setImageResource(R.drawable.wifi);
        WIFI.setScaleY(0.3f);
        WIFI.setScaleX(0.3f);
        WIFI.setX(GSM.getX() + 150);
        WIFI.setY((height-256) - 250);

        final ImageButton GPS = (ImageButton) findViewById(R.id.gpsBtn);
        GPS.setImageResource(R.drawable.gps);
        GPS.setScaleX(0.2f);
        GPS.setScaleY(0.2f);
        GPS.setX((WIFI.getX()) + 150);
        GPS.setY((height - 256) - 250);


        GSM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GSM.setColorFilter(Color.parseColor("#FF3F51B5"));
                WIFI.setColorFilter(Color.parseColor("#FFFFFF"));
                GPS.setColorFilter(Color.parseColor("#FFFFFF"));
                setLocation("network");

                loadPlaces = new LoadPlaces();
                loadPlaces.execute(radius);

                Toast toast = Toast.makeText(getApplicationContext(), "message", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setText("GSM location: "+bestLocation);
                toast.show();

                try {
                    recordData("GSMData");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        WIFI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WIFI.setColorFilter(Color.parseColor("#FF3F51B5"));
                GSM.setColorFilter(Color.parseColor("#FFFFFF"));
                GPS.setColorFilter(Color.parseColor("#FFFFFF"));
                setLocation("passive");

                loadPlaces = new LoadPlaces();
                loadPlaces.execute(radius);

                Toast toast = Toast.makeText(getApplicationContext(), "message", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setText("WiFi location: "+bestLocation);
                toast.show();

                try {
                    recordData("WiFiData");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        GPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPS.setColorFilter(Color.parseColor("#FF3F51B5"));
                WIFI.setColorFilter(Color.parseColor("#FFFFFF"));
                GSM.setColorFilter(Color.parseColor("#FFFFFF"));
                setLocation("gps");

                loadPlaces = new LoadPlaces();
                loadPlaces.execute(radius);

                Toast toast = Toast.makeText(getApplicationContext(), "message", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setText("GPS location: "+bestLocation);
                toast.show();

                try {
                    recordData("GPSData");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        SmartSuggestions smartSuggestions = new SmartSuggestions();
        try {
            smartSuggestions.checkInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        txt.setTextColor(Color.parseColor(hexColor));
        txt.setX(xPos);
        txt.setY(yPos + 2);

        if (!textList.contains(text)){
            imageList.add(img);
            textList.add(txt);
        }

        int[] outLocation = new int[2];
        img.getLocationOnScreen(outLocation);

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

    LinkedHashSet<ImageView> overlapList = new LinkedHashSet<>();
    LinkedHashSet<TextView> overlapText = new LinkedHashSet<>();

    public void shuffleBoxes(){
        //final ArrayList<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < textList.size(); i++){
            for (int j = (textList.size())/2; j < textList.size(); j ++ ) {
                if (textList.get(j).getText().toString().contains(textList.get(i).getText().toString())) {
                        textList.remove(j);
                        imageList.remove(j);
                }
            }
        }

        for (int i = 0; i < imageList.size(); i++) {
            for (int j = 0; j < imageList.size(); j++) {
                // left, top, right, bottom
                //  txt.setWidth(270);
                //  txt.setHeight(185);
                // http://stackoverflow.com/questions/23302698/java-check-if-two-rectangles-overlap-at-any-point
                double x1 = imageList.get(i).getX();
                double y1 = imageList.get(i).getY();
                double x2 = x1 + 270;
                double y2 = y1 + 185;


                double x3 = imageList.get(j).getX();
                double y3 = imageList.get(j).getY();
                double x4 = x3 + 270;
                double y4 = y3 + 185;

                if (x2 < x3 || x4 < x1 || y2 < y3 || y4 < y1) {
                        // DOES NOT OVERLAP
                } else { // OVERLAP
                    //if (!overlapText.contains(textList.get(i)) || !overlapText.contains(textList.get(j))) {
                        System.out.println(textList.get(i).getText().toString() + " overlaps with " + textList.get(j).getText().toString());
                        overlapList.add(imageList.get(i));
                        overlapList.add(imageList.get(j));
                        overlapText.add(textList.get(i));
                        overlapText.add(textList.get(j));
                        shuffleImg.setVisibility(View.VISIBLE);
                    //}
                }
            }
        }

                shuffleImg.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view)
                  {
                      if (indexIterator == 0 ){
                          indexIterator = imageList.size()/2;
                      }

                      if (indexIterator >= imageList.size()) {
                          System.out.println("Index iterator = " + indexIterator + " imageList size = " + imageList.size() + " textList size = " + textList.size());
                          imageList.get(imageList.size()-1).setAlpha(0.5f);
                          textList.get(textList.size()-1).setAlpha(0.5f);
                          textList.get(textList.size()-1).setTextColor(Color.rgb(255,255,255));
                          indexIterator = textList.size()/2;
                      }
                      if (indexIterator >= 1){
                          imageList.get(indexIterator-1).setAlpha(0.5f);
                          textList.get(indexIterator-1).setAlpha(0.5f);
                          textList.get(indexIterator-1).setTextColor(Color.rgb(255,255,255));
                      }
                      System.out.println(indexIterator);
                      System.out.println("Size of the list is: " + textList.size());
                      for(int counter = 0; counter < textList.size(); counter++) {
                          //if(overlapText.contains(textList.get(counter))) {
                          imageList.get(indexIterator).bringToFront();
                          imageList.get(indexIterator).setAlpha(1f);
                          textList.get(indexIterator).bringToFront();
                          textList.get(indexIterator).setAlpha(1f);
                          textList.get(indexIterator).setTextColor(Color.rgb(71,74,209));
                         // }
                      }
                      indexIterator++;
                  }
                  }
               );



    }

    public void showSnackbar(String text, final String type){
        Snackbar.make(findViewById(R.id.myCoordinatorLayout), text, Snackbar.LENGTH_LONG)
                .setAction("YES", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        types = type;
                        hexColor = "#a21c1c";
                        loadPlaces = new LoadPlaces();
                        loadPlaces.execute(radius);

                    }
                }).setDuration(6000).show();
    }


    public void recordData(String fileName) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileName + ".csv");
        file.createNewFile();
        if(file.exists())
        {

            if (directionPOIs.size() > 0) {

                OutputStream fo = new FileOutputStream(file);

                Object[] foundPOIArray = foundPOIs.toArray();

                fo.write(("My location (lng, lat):, " + latitude + "," + longitude + "\r\n").getBytes());

                for (int i = 0; i < foundPOIs.size(); i++)
                {
                    // dis[0] = lat, dis[1] = long
                    String[] dis = foundLoc.get(i).split(" ");
                    double poiLat = Double.parseDouble(dis[0]);
                    double poiLon = Double.parseDouble(dis[1]);
                    String distance = ""+findDistance(longitude, latitude, poiLon, poiLat);
                    fo.write((foundPOIArray[i].toString() + ", " + distance + "," + poiLat + "," + poiLon + "\r\n").getBytes());
                }
                fo.close();
            }
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
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        // See if the phone has got permissions

        try{
            spinner.setVisibility(View.VISIBLE);
            if (bestLocation != null){
                location = bestLocation;
            }
            else {
                location = getLocation();
            }
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        catch (SecurityException e){
            longitude = 0.0;
            latitude = 0.0;
        }
        // calling background Async task to load Google Places
        // After getting places from Google all the data is shown in listview
        foundPOIs = new LinkedHashSet<String>();
        foundLoc = new ArrayList<String>();
        directionPOIs = new ArrayList<String>();
        referenceNum = new LinkedHashSet<>();
        GooglePlaces.foundLoc = new ArrayList<>();
        new LoadMyElevation().execute();
        spinner.setVisibility(View.GONE);
        loadPlaces = new LoadPlaces();
        loadPlaces.execute(radius);
        shuffleBoxes();
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

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 1, this);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 2000, 1, this);
        }
        catch (SecurityException se){}
    }

    @Override
    public void onPause(){
        // To save battery
        super.onPause();
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
    public void onSensorChanged(final SensorEvent event){

        TextView t = (TextView) findViewById(R.id.textView2);
        String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        float orientation[];

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
        final int PLANET_RADIUS = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = PLANET_RADIUS * c;
        //return Math.round(dist * 1000);
        return Double.parseDouble(String.format("%.2f", dist * 1000));
    }
    Location bestLocation = null;
    private void setLocation(String location){
        try {
            bestLocation = mLocationManager.getLastKnownLocation(location);
        }
        catch (SecurityException se){}
    }

    private Location getLocation(){
        // http://stackoverflow.com/questions/20438627/getlastknownlocation-returns-null
        mLocationManager = (LocationManager)applicationContext.getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
       if (bestLocation == null) {
           Location l = null;
           for (String provider : providers) {
               try {
                   l = mLocationManager.getLastKnownLocation(provider);
               } catch (SecurityException se) {
               }
               if (l == null) {
                   continue;
               }
               if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                   // Found best last known location: %s", l);
                   bestLocation = l;
               }
           }
       }
        return bestLocation;
    }

    class LoadPlaces extends AsyncTask<String, String, String> {
        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            // creating Places class object
            googlePlaces = new GooglePlaces();

            double longitude = 0.0;
            double latitude = 0.0;
            Location location;

            try{
                location = getLocation();
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
            catch (SecurityException e){
                longitude = 0.0;
                latitude = 0.0;
            }
            // See if the phone has got permissions
            try {
                // Radius in meters - increase this value if you don't find any places
                // double radius = 1000; // 1000 meters/yards
                // double radius = Double.parseDouble(args[0]);
                String[] radiusString = args[0].split(" ");
                double radius = Double.parseDouble(radiusString[0]);

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
                            errorMessageTextView.setText("");
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

                        String[] arr = directionView.getText().toString().split(" ");
                        double kilometer = (int)Double.parseDouble(radius) / 1000;
                        double longitudeDir = kilometer / 111.2;
                        double latitudeDir = kilometer / 111.32;

                        String[] degrees = deg.getText().toString().split(" ");
                        Object[] foundPOIArray = foundPOIs.toArray();
                        ArrayList<Double> elevationListArray = new ArrayList<>(GoogleElevation.elevationList);
                        ArrayList<String> referenceNumArray = new ArrayList<>(referenceNum);
                        for (int i = 0; i < foundPOIArray.length-1; i++){
                            // dis[0] = lat, dis[1] = long
                            String[] dis = foundLoc.get(i).split(" ");
                            double poiLat = Double.parseDouble(dis[0]);
                            double poiLon = Double.parseDouble(dis[1]);
                            double angle = Double.parseDouble(degrees[0]);

                            String distance = ""+findDistance(longitude, latitude, poiLon, poiLat);

                            double ang = poiAngle(poiLat, poiLon, angle, (int)Double.parseDouble(radius));

                            if (inFOV(poiLat, poiLon, angle, (int)Double.parseDouble(radius))){
                                if (!directionPOIs.contains(foundPOIArray[i].toString()) && elevationListArray.size() > 0){
                                    directionPOIs.add(foundPOIArray[i].toString() + " " + distance + "m");
                                    addImg(ang, foundPOIArray[i].toString() + " " + distance + "m", elevationListArray.get(i), referenceNumArray.get(i));
                                }

                            }
                        }
                    }
                    else if (status.equals("ZERO_RESULTS")){
                        errorMessageTextView.setText("No places in radius");
                        shuffleImg.setVisibility(View.INVISIBLE);
                    }
                    else if (status.equals("OVER_QUERY_LIMIT")){
                        errorMessageTextView.setText("OVER_QUERY_LIMIT");
                    }
                    else if (directionPOIs.isEmpty()){
                        errorMessageTextView.setText("No places in radius");
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
