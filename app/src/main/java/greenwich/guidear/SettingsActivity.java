package greenwich.guidear;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.SeekBar;

/**
 * Created by Marcin on 08/08/2016.
 */
public class SettingsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);

        // find the seek bar and change value of the textbox
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final EditText radiusText = (EditText) findViewById(R.id.editText2);
        // set seek bar properties
        seekBar.setProgress(0); // start value
        seekBar.incrementProgressBy(100); // step by 100 meters
        seekBar.setMax(5000);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setRadius(String radius){
        final EditText radiusText = (EditText) findViewById(R.id.editText2);
        radiusText.setText(radius);
    }


}
