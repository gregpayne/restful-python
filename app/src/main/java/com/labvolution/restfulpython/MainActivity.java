package com.labvolution.restfulpython;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.colorpicker.shishank.colorpicker.ColorPicker;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// https://www.londonappdeveloper.com/consuming-a-json-rest-api-in-android/
// https://blog.miguelgrinberg.com/post/designing-a-restful-api-with-python-and-flask

// TODO: 06/01/2018 See tasks in comment below
/*
Have a local table/JSONObject in the python app that holds status for all connected devices
For each request, update the status and return the table to the Android app

 */
public class MainActivity extends AppCompatActivity {

    RequestQueue requestQueue;

    ColorPickerView colorPicker;
    Switch[] toggleSwitch = new Switch[8];
    Switch toggleAll;

    String baseURL = "http://192.168.0.160:5000/";
    String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this); // This setups up a new request queue which we will need to make HTTP requests.

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
        colorPicker.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int i) {
                Log.d("Color Picker","Color: " + Integer.toHexString(i));
                updateLEDs();
            }
        });

        setLedState("");
    }

//    private void getRepoList(String username) {
//        // First we insert the username into the repo url
//        // The repo url is defined in GitHubs API docs (https://developer.github.com/v3/repos/)
//        this.url = this.baseURL + username + "/repos";
//
//        // Next we create a new JsonArrayRequest. This will use Volley to make an HTTP request
//        // that expexts a JSON Array Response
//        // To fully understand this read: https://developer.android.com/training/volley/index.html
//        final JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        // Check the length of out response to see if the user has any repos
//                        if (response.length() > 0) {
//                            // The user does have repos, so lets loop through them all
//                            for (int i = 0; i < response.length(); i++) {
//                                try {
//                                    // For each repo, add a new line to our repo list
//                                    JSONObject jsonObject = response.getJSONObject(i);
//                                    String repoName = jsonObject.get("name").toString();
//                                    String lastUpdated = jsonObject.get("updated_at").toString();
//                                    addToRepoList(repoName, lastUpdated);
//                                } catch (JSONException e) {
//                                    Log.e("Volley", "Invalid JSON Object.");
//                                }
//
//                            }
//                        }
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // If there is an HTTP error, add a note to our repo list
//                        setRepoListText("Error while calling REST API");
//                        Log.e("Volley", error.toString());
//                    }
//                }
//        );
//        // Add the request we just defined to out request queue
//        // The request queue will automatically handle the request as soon as it can
//        requestQueue.add(arrayRequest);
//    }

    private void setLedState(String url) {
        this.url = this.baseURL + "blinkt" + url;
        Log.d("Volley", this.url);

        // Next we create a new JsonArrayRequest. This will use Volley to make an HTTP request
        // that expexts a JSON Array Response
        // To fully understand this read: https://developer.android.com/training/volley/index.html
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, this.url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Check the length of out response to see if the user has any repos
                        if (response.length() > 0) {
                            // The user does have repos, so lets loop through them all
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    // For each repo, add a new line to our repo list
                                    JSONObject jsonObject = response.getJSONObject(i);
                                } catch (JSONException e) {
                                    Log.e("Volley", "Invalid JSON Object.");
                                }
                            }
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If there is an HTTP error, add a note to our repo list
                        Log.e("Volley", error.toString());
                    }
                }
        );
        // Add the request we just defined to out request queue
        // The request queue will automatically handle the request as soon as it can
        Log.d("Volley", jsonArrayRequest.toString());
        requestQueue.add(jsonArrayRequest);
    }

    public void ledSwitch(View view) {
        Switch s = (Switch) view;
        int led = Integer.parseInt(s.getText().toString());
        boolean state = s.isChecked();
        int ledState = 0;
        if (state) { ledState = 1; } // Could rather use Function interface
        setLedState("/" + led + "/" + ledState);
    }

    public void setAll(View view) {
        updateLEDs();
    }

    private void updateLEDs() {
        String url = "http://192.168.0.160:5000/blinkt/all";
        final boolean state = toggleAll.isChecked();

        final String color = "#" + Integer.toHexString(colorPicker.getSelectedColor()).substring(2);
        Log.d("Color Picker", "Color:" + color);

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Volley", response);

                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                // Read the state from the response message to set the toggle switches
                                toggleSwitch[i].setChecked(jsonArray.getJSONObject(i).getBoolean("state"));
                            }

                        } catch (JSONException e) {
                            Log.e("Volley", "Invalid JSON Object.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("brightness", "0.2");
                params.put("color", color);
                params.put("state", Boolean.toString(state));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        Log.d("Volley", stringRequest.getBodyContentType());
        requestQueue.add(stringRequest);
    }
}
