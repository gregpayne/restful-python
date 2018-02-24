package com.labvolution.restfulpython;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;

import java.util.ArrayList;

// https://www.londonappdeveloper.com/consuming-a-json-rest-api-in-android/
// https://blog.miguelgrinberg.com/post/designing-a-restful-api-with-python-and-flask

// TODO: 06/01/2018 See tasks in comment below
/*
Have a local table/JSONObject in the python app that holds status for all connected devices
For each request, update the status and return the table to the Android app

 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    ColorPickerView colorPicker;
    Switch[] toggleSwitch = new Switch[8];
    Switch toggleAll;
    SeekBar blinktBrightnessSlider;

    String baseURL = "http://192.168.0.160:5000/";

    Blinkt blinkt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        blinkt = new Blinkt(this, this.baseURL);

        blinktBrightnessSlider = (SeekBar) findViewById(R.id.blinktBrighnessSlider);
        blinktBrightnessSlider.setProgress(50);

        toggleSwitch[0] = (Switch) findViewById(R.id.switch0);
        toggleSwitch[1] = (Switch) findViewById(R.id.switch1);
        toggleSwitch[2] = (Switch) findViewById(R.id.switch2);
        toggleSwitch[3] = (Switch) findViewById(R.id.switch3);
        toggleSwitch[4] = (Switch) findViewById(R.id.switch4);
        toggleSwitch[5] = (Switch) findViewById(R.id.switch5);
        toggleSwitch[6] = (Switch) findViewById(R.id.switch6);
        toggleSwitch[7] = (Switch) findViewById(R.id.switch7);
        toggleAll = (Switch) findViewById(R.id.setAll);

        colorPicker = (ColorPickerView) findViewById(R.id.colorPicker);
        colorPicker.setAlpha(1.0f);
        colorPicker.addOnColorSelectedListener((color) -> {
            Log.d("Color Picker", "Color: " + Integer.toHexString(color));
            blinkt.updateLEDs(this::updateBlinktSwitches,
                    "/color", getCurrentColor(), "null", "null"); // state is not used by the REST API
        });

        blinktBrightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                String brightness = calculateBrightness(seekBar.getProgress());
                Log.d(TAG, "Seekbar positionL " + seekBar.getProgress() + " Brightness" + brightness);

                blinkt.updateLEDs((arrayList) -> updateBlinktSwitches(arrayList), // Using a Lambda method reference
                        "/brightness" + "", "null", "", brightness);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only want to the response to update the UI depending on the Switch position
        blinkt.updateLEDs((arrayList -> {
            updateBlinktSwitches(arrayList);
            Toast.makeText(this, "Connected to Raspberry Pi", Toast.LENGTH_SHORT).show();
        }), "/init", "null", "null", "null"); // color and state can be blank as they are not queried
    }

    /**
     * Switch OnClick method for individual Switch/LED
     * @param view Switch clicked
     */
    public void singleBlinktSwitchOnClick(View view) {
        // define callback method as part of the passed in parameters
        blinkt.updateLEDs(this::updateBlinktSwitches, // Using a Lambda method reference
                "/" + getSwitchNumber(view), getCurrentColor(), getSwitchState(view), getBrightness(blinktBrightnessSlider));
    }

    /**
     * Switch OnClick method for controlling all LEDs
     * @param view Switch clicked
     */
    public void allBlinktSwitchOnClick(View view) {
        // define callback method as part of passed in parameters
        blinkt.updateLEDs((arrayList) -> updateBlinktSwitches(arrayList), // For Method Reference, see above method
                "/all", getCurrentColor(), getSwitchState(view), getBrightness(blinktBrightnessSlider));
    }

    /**
     * Update Blinkt Switch's with REST response
     * @param arrayList Response from Blinkt REST API with state of switches/LEDs
     */
    private void updateBlinktSwitches(ArrayList<Boolean> arrayList) {
        boolean allOn = true; // Use to enable 'Set All' switch if all switches are on
        boolean singleSwitch;
        for (int i = 0; i < arrayList.size(); i++) {
            singleSwitch = arrayList.get(i);
            toggleSwitch[i].setChecked(singleSwitch);
            allOn = allOn & singleSwitch;
        }
        toggleAll.setChecked(allOn);
    }

    private String calculateBrightness(int seekbar) {
        return Float.toString(Math.min(Math.max(seekbar, 1), 99) / 100.0f);
    }

    private String getCurrentColor() {
        return "#" + Integer.toHexString(colorPicker.getSelectedColor()).substring(2);
    }

    private String getSwitchState(View view) {
        return Boolean.toString(((Switch) view).isChecked()).toLowerCase();
    }

    private Integer getSwitchNumber(View view) {
        return Integer.parseInt(((Switch) view).getText().toString());
    }

    private String getBrightness(View view) {
        return Float.toString(((SeekBar) view).getProgress() / 100.0f);
    }
}
