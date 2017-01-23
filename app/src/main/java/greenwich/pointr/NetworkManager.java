package greenwich.pointr;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.Toast;

/*
 * To prevent application from crashing, use this class to check for internet connection
 * If no internet connection is present, the application would not work and crash on MainActivity
 * when trying to execute network related (Google Places) code.
 * Checking for network connection on splash screen will allow to either continue with launch or
 * it will stop the application and appropriate message will be displayed in the toast
 */

public class NetworkManager {

    public void checkConnection(Context context){
        if (!isNetworkAvailable(context)) {
            context = context.getApplicationContext();
            CharSequence text = "No internet connection!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            toast.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 20);
        }
        else {
            context = context.getApplicationContext();
            CharSequence text = "Internet Connection Established!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            toast.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 20);

        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
