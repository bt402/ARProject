package greenwich.pointr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import static greenwich.pointr.MainActivity.KEY_REFERENCE;
import static greenwich.pointr.MainActivity.directionPOIs;

// https://code.tutsplus.com/tutorials/android-sdk-augmented-reality-camera-sensor-setup--mobile-7873

public class OverlayView extends View implements SensorEventListener,
        LocationListener {

    public static final String DEBUG_TAG = "OverlayView Log";

    private final Context context;
    private Handler handler;
    
    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";
    String gyroData = "Gyro Data";

    private LocationManager locationManager = null;
    private SensorManager sensors = null;

    private Location lastLocation;
    private float[] lastAccelerometer;
    private float[] lastCompass;

    private float verticalFOV;
    private float horizontalFOV;

    private boolean isAccelAvailable;
    private boolean isCompassAvailable;
    private boolean isGyroAvailable;

    private Sensor accelSensor;
    private Sensor compassSensor;
    private Sensor gyroSensor;

    ArrayList<Double> positionsX;
    ArrayList<Double> positionY;

    ArrayList<ImageView> imageList = new ArrayList<>();
    ArrayList<TextView> textList = new ArrayList<>();

    public OverlayView(Context context) {
        super(context);
        this.context = context;
        this.handler = new Handler();
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        sensors = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        startSensors();
        startGPS();

        // get some camera parameters
        Camera camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        verticalFOV = params.getVerticalViewAngle();
        horizontalFOV = params.getHorizontalViewAngle();
        camera.release();

    }

    private void startSensors() {
        isAccelAvailable = sensors.registerListener(this, accelSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        isCompassAvailable = sensors.registerListener(this, compassSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        isGyroAvailable = sensors.registerListener(this, gyroSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void startGPS() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        Log.v(DEBUG_TAG, "Best provider: " + best);

        try {
            locationManager.requestLocationUpdates(best, 50, 0, this);
        }
        catch (SecurityException se){}
    }

    Object[] referenceArray = new Object[20];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        ArrayList<String> foundLoc = MainActivity.foundLoc; // lat, lon
        ArrayList<String> directionPOIs = MainActivity.directionPOIs; // name + distance  USE THIS
        final LinkedHashSet<String> referenceList = MainActivity.referenceNum;

        if (directionPOIs != null && foundLoc != null) {
            if (directionPOIs.size() > 0) {

                ArrayList<Double> elevationListArray = new ArrayList<>(GoogleElevation.elevationList);
                referenceList.toArray(referenceArray);
                positionsX = new ArrayList<>();
                positionY = new ArrayList<>();

                imageList = MainActivity.imageList;
                textList = MainActivity.textList;

                referenceArray = referenceList.toArray();
                if (elevationListArray.size() > 0) {

                    int size = 0;

                    if (directionPOIs.size() > elevationListArray.size()){
                        size = elevationListArray.size();
                    }
                    else if (elevationListArray.size() > directionPOIs.size()){
                        size = directionPOIs.size();
                    }

                    if (size > imageList.size()){
                        size = imageList.size() - 1;
                    }

                    for (int i = 0; i < size; i++) {

                        // Draw something fixed (for now) over the camera view
                        float currentBearing = 0.0f;

                        Location location = new Location("manual");

                        String[] dis = foundLoc.get(i).split(" ");
                        double poiLat = Double.parseDouble(dis[0]);
                        double poiLon = Double.parseDouble(dis[1]);

                        location.setLatitude(poiLat);
                        location.setLongitude(poiLon);

                        if (lastLocation != null) {
                            currentBearing = lastLocation.bearingTo(location);
                        }

                        // compute rotation matrix
                        float rotation[] = new float[9];
                        float identity[] = new float[9];
                        if (lastAccelerometer != null && lastCompass != null) {
                            boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                                    identity, lastAccelerometer, lastCompass);
                            if (gotRotation) {
                                float cameraRotation[] = new float[9];
                                // remap such that the camera is pointing straight down the Y
                                // axis
                                SensorManager.remapCoordinateSystem(rotation,
                                        SensorManager.AXIS_X, SensorManager.AXIS_Z,
                                        cameraRotation);

                                // orientation vector
                                float orientation[] = new float[3];
                                SensorManager.getOrientation(cameraRotation, orientation);

                                // draw horizon line (a nice sanity check piece) and the target (if it's on the screen)
                                canvas.save();
                                // use roll for screen rotation
                                canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));

                                // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
                                float dx = (float) ((canvas.getWidth() / horizontalFOV) * (Math.toDegrees(orientation[0]) - currentBearing));
                                float dy = (float) ((canvas.getHeight() / verticalFOV) * Math.toDegrees(orientation[1]));

                                // wait to translate the dx so the horizon doesn't get pushed off
                                canvas.translate(0.0f, 0.0f - dy);

                                // now translate the dx
                                canvas.translate(0.0f - dx, 0.0f);

                                /*ImageView img = new ImageView(context);
                                img.setBackgroundResource(R.drawable.frame);
                                img.setDrawingCacheEnabled(true);
                                img.setScaleX(0.4f);
                                img.setScaleY(0.3f);
                                img.setAlpha(0.5f);
                                img.layout(0, 0, 270, 185);*/

                                imageList.get(i).setBackgroundResource(R.drawable.frame);
                                imageList.get(i).setDrawingCacheEnabled(true);
                                imageList.get(i).setScaleX(0.4f);
                                imageList.get(i).setScaleY(0.3f);
                                imageList.get(i).setAlpha(0.5f);
                                imageList.get(i).layout(0, 0, 270, 185);

                                imageList.get(i).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Starting new intent
                                        Intent in = new Intent(getContext(),
                                                PlaceInfo.class);
                                        // Sending place refrence id to single place activity
                                        // place refrence id used to get "Place full details"
                                        in.putExtra(KEY_REFERENCE, "");
                                        getContext().startActivity(in);
                                    }
                                });

                                Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                boxPaint.setAlpha(85);

                                // >> 1 is equivelant to /2

                                /*TextView textView = new TextView(context);
                                textView.setText(directionPOIs.get(i));
                                textView.setTextColor(Color.WHITE);
                                textView.setDrawingCacheEnabled(true);
                                textView.setTextSize(11);
                                textView.setWidth(270);
                                textView.setHeight(185);
                                textView.setSingleLine(false);
                                textView.layout(0, 0, 270, 185);*/

                                //textList.get(i).setTextColor(Color.WHITE);
                                textList.get(i).setDrawingCacheEnabled(true);
                                textList.get(i).setTextSize(11);
                                textList.get(i).setWidth(270);
                                textList.get(i).setHeight(185);
                                textList.get(i).setSingleLine(false);
                                textList.get(i).layout(0, 0, 270, 185);

                                System.out.println("Added: " + textList.get(i).getText());

                                int height = this.getHeight();


                                double elev = elevationListArray.get(i) - MainActivity.getMyElevation();
                                double elevPercentage = elev / MainActivity.getMyElevation();


                                int yPos = (int) Math.round(height * elevPercentage);

                                int middleY = (int) height >> 1;

                                if (yPos < 0) {
                                    yPos = middleY - Math.abs(yPos);
                                } else {
                                    yPos = middleY + yPos;
                                }

                                // draw our point -- we've rotated and translated this to the right spot already

                                //System.out.println(directionPOIs.get(i) + " --> Position X: " + Math.abs(dx) + " Postion Y: " + Math.abs(dy));

                                double testXVal = imageList.get(i).getX() + 270;
                                double textYVal = imageList.get(i).getY() + 185;
                                positionsX.add((double)testXVal);
                                positionY.add((double)textYVal);

                                canvas.drawBitmap(imageList.get(i).getDrawingCache(), canvas.getWidth() >> 1, yPos, boxPaint);
                                canvas.drawBitmap(textList.get(i).getDrawingCache(), (canvas.getWidth() >> 1) + 20, yPos, null);
                                imageList.get(i).setDrawingCacheEnabled(false);
                                textList.get(i).setDrawingCacheEnabled(false);
                                canvas.restore();

                            }
                        }

                        canvas.save();
                        canvas.translate(15.0f, 15.0f);
                        canvas.restore();

                    }
                }
            }
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 540, 1090

        //if (event.getX() >= 540 && event.getX() <= 640 && event.getY() >= 1090 && event.getY() <= 1190){
        if (positionsX != null && positionY != null){
        if (positionsX.size() == positionY.size()){
            for (int i = 0; i < positionsX.size(); i++){
                if (event.getX() >= Math.abs(positionsX.get(i)) && event.getX() <= Math.abs(positionsX.get(i)) + 320
                        && event.getY() >= Math.abs(positionY.get(i)) && event.getY() <= Math.abs(positionY.get(i)) + 235){
                    Toast toast = Toast.makeText(context, "message", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setText(directionPOIs.get(i));
                    toast.show();
                    // Starting new intent
                    Intent in = new Intent(getContext(),
                            PlaceInfo.class);
                    // Sending place refrence id to single place activity
                    // place refrence id used to get "Place full details"
                    in.putExtra(KEY_REFERENCE, (String)referenceArray[i]);
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(in);
                }
            }
        }
        }
        else {
            String radius;
            SharedPreferences sharedPreferences = null;
            sharedPreferences = getContext().getSharedPreferences("SettingsSave", 1);
            String radiusState = sharedPreferences.getString("r", "1000");
            String[] radiusStateString = radiusState.split(" ");
            radius = radiusStateString[0];
            MainActivity.LoadPlaces loadPlaces = new MainActivity().new LoadPlaces();
            loadPlaces.execute(radius);
        }
        //}
        return false;
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.d(DEBUG_TAG, "onAccuracyChanged");

    }

    public void onSensorChanged(SensorEvent event) {
        // Log.d(DEBUG_TAG, "onSensorChanged");

        StringBuilder msg = new StringBuilder(event.sensor.getName())
                .append(" ");
        for (float value : event.values) {
            msg.append("[").append(String.format("%.3f", value)).append("]");
        }

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lastAccelerometer = event.values.clone();
                accelData = msg.toString();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroData = msg.toString();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastCompass = event.values.clone();
                compassData = msg.toString();
                break;
        }

        this.invalidate();
    }

    public void onLocationChanged(Location location) {
        // store it off for use when we need it
        lastLocation = location;
    }

    public void onProviderDisabled(String provider) {
        // ...
    }

    public void onProviderEnabled(String provider) {
        // ...
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ...
    }

    // this is not an override
    public void onPause() {
        try {
            locationManager.removeUpdates(this);
        }
        catch (SecurityException se){}
        sensors.unregisterListener(this);
    }

    // this is not an override
    public void onResume() {
        startSensors();
        startGPS();
    }

}
