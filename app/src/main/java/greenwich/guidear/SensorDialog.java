package greenwich.guidear;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class SensorDialog extends DialogFragment{

    // Retrieve information about device
    PackageManager manager;

    boolean accelerometer;
    boolean compass;

    public void accelerometerExists(Context context){
        manager = context.getPackageManager();
        accelerometer = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        compass = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);

        if (!accelerometer || !compass){
            new AlertDialog.Builder(context)
                    .setTitle("Critical Alert")
                    .setMessage("This device does not have necessary hardware. Compass and/or Accelerometer not detected!")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();
        }

    }
}
