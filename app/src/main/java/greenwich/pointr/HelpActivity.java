package greenwich.pointr;

import android.app.Activity;
import android.media.Image;
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

        ImageView helpImg = (ImageView) findViewById(R.id.questionMark);
        helpImg.setImageResource(android.R.drawable.ic_menu_help);

        ImageView poweredByGoogle = (ImageView) findViewById(R.id.poweredByGoogle);
        poweredByGoogle.setImageResource(R.drawable.powered_by_google_on_non_white);
        poweredByGoogle.setScaleX(0.5f);
        poweredByGoogle.setScaleY(0.5f);

        Uri gifUrl = Uri.parse("file:///android_res/drawable/compass.gif");
        WebView gif = (WebView) findViewById(R.id.myGif);
        gif.loadUrl(gifUrl.toString());

        TextView helpText = (TextView) findViewById(R.id.helpTxt);
/*
        helpText.setText("\tIf your compass is not updating please try \n" +
                         "\tthe following: \n" +
                         "\t \t \u2022 shake the device around\n" +
                         "\t \t \u2022 move the device like an eight\n" +
                         "\t \t \u2022 tap on the devices back\n" +
                         "\t Alternatively:\n" +
                         "\t \t \u2022 Tilt your phone forward and back\n" +
                         "\t \t \u2022 Move it side to side\n" +
                         "\t \t \u2022 And then tilt left and right");
*/
        helpText.setText("\n\tIf your compass is not updating please try \n" +
                         "\tthe following: \n" +
                         "\t \t \u2022 Tilt your device forwards and backwards\n" +
                         "\t \t \u2022 Move your device from side to side\n" +
                         "\t \t \u2022 Tilt your device left to right \n \n");
    }
}
