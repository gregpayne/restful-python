package com.labvolution.restfulpython;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Blinkt {
    private static final String TAG = "BLINKT";

    final Context context;
    final String baseURL;
    final RequestQueue requestQueue;

    /**
     * Callback Functional Interface
     */
    @FunctionalInterface
    interface UpdateUiEventListener {
        void onEvent(ArrayList<Boolean> arrayList);
    }

    // Constructor
    public Blinkt(Context context, String baseURL) {
        this.context = context;
        this.baseURL = baseURL + "blinkt";
        Log.d(TAG, "Base URL: " + this.baseURL);

        requestQueue = Volley.newRequestQueue(context); // This setups up a new request queue which we will need to make HTTP requests.
    }

    /**
     * Update method which sends REST Request to server to control Blinkt LEDs
     * @param eventListener Callback to update switch positions
     * @param url Specific URL to Request. Starts with '/' and does not include the base URL
     *            which for the Blinkt class is set to http://192.168.0.160:5000/blinkt
     * @param color String representation of the color. eg. '#FFFFFF'
     * @param state String representation of state in lower case. eg. 'true' or 'false'
     */
    public void updateLEDs(UpdateUiEventListener eventListener, String url, String color, String state) {
        Log.d(TAG, "URL: " + url);
        ArrayList<Boolean> switchStates = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, this.baseURL + url,
                (response) -> {
//                        Log.d("Volley", response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                // Read the state from the response message to set the toggle switches
                                switchStates.add(jsonArray.getJSONObject(i).getBoolean("state"));
                            }
                            eventListener.onEvent(switchStates);

                        } catch (JSONException e) {
                            Log.e("Volley", "Invalid JSON Object.");
                        }
                    },
                (error) -> Log.d("Volley", error.toString()))
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("brightness", "0.2");
                    params.put("color", color);
                    params.put("state", state);
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
