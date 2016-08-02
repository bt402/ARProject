package greenwich.guidear;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends Activity implements SensorEventListener, LocationListener {

    float[] Gravity;
    float[] Geomag;
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    Sensor accelerometer;
    Sensor magnetometer;

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




        final Button buttonOne = (Button) findViewById(R.id.button);
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
        });

        // Create intent to access camera in Photo mode
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Start the camera
        //startActivityForResult(intent, 100);
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
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            t.setText("Long: " + longitude + "°, Lat: " + latitude + "°");
        }
        catch (SecurityException e){
            t.setText(""+e);
        }
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
    // test
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
}
