package greenwich.pointr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashsScreen extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashsScreen.this, MainActivity.class);
                SplashsScreen.this.startActivity(mainIntent);
                SplashsScreen.this.finish();
            }
        }, 1500);
    }
}
