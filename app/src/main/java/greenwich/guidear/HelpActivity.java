package greenwich.guidear;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpActivity extends Activity{

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_screen);

        WebView gif = (WebView) findViewById(R.id.myGif);
        gif.loadUrl("https://lh6.ggpht.com/GpGN0C9tz-jRGgPuOSMYopCnvirP-T79W1gg1eW8pbObWBM8GGVjbSxIY3uWTVCCdSA=w300");

        TextView helpText = (TextView) findViewById(R.id.helpTxt);

        helpText.setText("If you're compass is not updating please try the following: \n" +
                         "\u2022 shake the device around\n" +
                         "\u2022 move the device like an eight\n" +
                         "\u2022 tap on the devices back\n" +
                         "Alternatively:\n" +
                         "1. Tilt your phone forward and back\n" +
                         "2. Move it side to side\n" +
                         "3. And then tilt left and right");

    }
}
