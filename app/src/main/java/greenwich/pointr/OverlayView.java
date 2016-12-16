package greenwich.pointr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;

// https://code.tutsplus.com/tutorials/android-sdk-augmented-reality-camera-sensor-setup--mobile-7873

public class OverlayView extends View implements SensorEventListener,
        LocationListener {

    public static final String DEBUG_TAG = "OverlayView Log";

    private final Context context;
    private Handler handler;

    // Houses of Parliment: 51.4998418, -0.1245903, 96m (Big Ben)
    private final static Location westminsterPalace = new Location("manual");
    static {
        westminsterPalace.setLatitude(51.4998418);
        westminsterPalace.setLongitude(-0.1245903d);
        westminsterPalace.setAltitude(96.0d);
    }

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
        // criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // while we want fine accuracy, it's unlikely to work indoors where we
        // do our testing. :)
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        Log.v(DEBUG_TAG, "Best provider: " + best);

        try {
            locationManager.requestLocationUpdates(best, 50, 0, this);
        }
        catch (SecurityException se){}
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        ArrayList<String> foundLoc = MainActivity.foundLoc; // lat, lon
        ArrayList<String> directionPOIs = MainActivity.directionPOIs; // name + distance  USE THIS

        if (directionPOIs != null && foundLoc != null){
            if (directionPOIs.size() > 0){

        for (int i = 0; i < directionPOIs.size(); i++){

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
                canvas.rotate((float)(0.0f- Math.toDegrees(orientation[2])));

                // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
                float dx = (float) ((canvas.getWidth()/ horizontalFOV) * (Math.toDegrees(orientation[0])-currentBearing));
                float dy = (float) ((canvas.getHeight()/ verticalFOV) * Math.toDegrees(orientation[1])) ;

                // wait to translate the dx so the horizon doesn't get pushed off
                canvas.translate(0.0f, 0.0f-dy);


                // make our line big enough to draw regardless of rotation and translation
                //canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight()/2, canvas.getWidth()+canvas.getHeight(), canvas.getHeight()/2, targetPaint);

                // now translate the dx
                canvas.translate(0.0f-dx, 0.0f);

                Bitmap bmpOrg = BitmapFactory.decodeResource(getResources(), R.drawable.frame);
                bmpOrg.setHasAlpha(true);
                int width = (int)(bmpOrg.getWidth() * 0.4f);
                int height = (int)(bmpOrg.getHeight() * 0.3f);

                // Scale the image to 30% of height and 40% of width
                Bitmap scaledbmp;
                scaledbmp = bmpOrg.createScaledBitmap(bmpOrg, width, height, true);

                Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                boxPaint.setAlpha(85);

                // >> 1 is equivelant to /2

                TextView textView = new TextView(context);
                textView.setText(directionPOIs.get(i));
                textView.setTextColor(Color.WHITE);
                textView.setDrawingCacheEnabled(true);
                textView.setTextSize(11);
                textView.setWidth(270);
                textView.setHeight(185);
                textView.setSingleLine(false);
                textView.layout(0, 0, 270, 185);
                // draw our point -- we've rotated and translated this to the right spot already
                canvas.drawBitmap(scaledbmp, canvas.getWidth()>> 1 , (canvas.getHeight()>> 1) , boxPaint);
                canvas.drawBitmap(textView.getDrawingCache(), (canvas.getWidth()>> 1) + 20, (canvas.getWidth()>> 1) + 330, null);
                //canvas.drawText(directionPOIs.get(i), (canvas.getWidth()>> 1) + 20, (canvas.getHeight()>> 1)  + 40, textPaint);
                //canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 18.0f, targetPaint);
                textView.setDrawingCacheEnabled(false);
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
