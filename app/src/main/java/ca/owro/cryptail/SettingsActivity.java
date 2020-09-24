package ca.owro.cryptail;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.TooltipCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import ca.owro.cryptail.R;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends BaseActivity {

    TextView valueTextView, refreshTextView, notificationTextView, pipTextView;
    SwitchCompat notificationSwitch, pipSwitch;
    Spinner currencySpinner, refreshSpinner;
    Button apiTestButton, requestFeatureButton;
    //RequestQueue requestQueue;

    static final String SHARED_PREFS = BuildConfig.APPLICATION_ID;
    static final String NOTIFICATIONS = "notifications";
    static final String PIP = "pip";
    static final String CURRENCY = "currency";
    static final String AUTOREFRESH = "autorefresh";

    String valueCurrencyState = "";
    String autoRefreshState = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Settings");

        valueTextView = findViewById(R.id.currencyNameTextView);
        refreshTextView = findViewById(R.id.refreshTimeTextView);
        notificationTextView = findViewById(R.id.enableNotificationTextView);
        pipTextView = findViewById(R.id.enablePIPTextView);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        pipSwitch = findViewById(R.id.pipSwitch);
        apiTestButton = findViewById(R.id.apiTestButton);
        requestFeatureButton = findViewById(R.id.requestFeatureButton);
        currencySpinner = findViewById(R.id.currencySpinner);
        refreshSpinner = findViewById(R.id.refreshSpinner);

        //not implemented
        pipSwitch.setVisibility(View.INVISIBLE);
        pipTextView.setVisibility(View.INVISIBLE);

        valueCurrencyState = currencySpinner.getSelectedItem().toString();
        autoRefreshState = refreshSpinner.getSelectedItem().toString();

        //requestQueue = Volley.newRequestQueue(this);

        // tooltip fix for pre-API 26 (v8)
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            Log.i("Pre API 26 Device!","Version: " + android.os.Build.VERSION.SDK_INT + ", so fixing tooltips...");
            TooltipCompat.setTooltipText(valueTextView,getString(R.string.valCurToolTip));
            TooltipCompat.setTooltipText(refreshTextView,getString(R.string.refreshToolTip));
            TooltipCompat.setTooltipText(notificationTextView,getString(R.string.notificationToolTip));
            TooltipCompat.setTooltipText(pipTextView,getString(R.string.pipToolTip));
        }

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveStringDataToSharedPrefs(CURRENCY, currencySpinner.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        refreshSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveStringDataToSharedPrefs(AUTOREFRESH, refreshSpinner.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        pipSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                    // PIP not possible pre API 26
                    Utilities.generateToast(SettingsActivity.this,"PIP is not available in Android 7.1 or lower",Toast.LENGTH_LONG);
                    pipSwitch.setChecked(false);
                }
                saveBooleanDataToSharedPrefs(PIP,isChecked);
            }
        });

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveBooleanDataToSharedPrefs(NOTIFICATIONS,isChecked);
                if (!isChecked) {
                    String ns = Context.NOTIFICATION_SERVICE;
                    NotificationManager nMgr = (NotificationManager) getSystemService(ns);
                    nMgr.cancelAll();
                }
            }
        });

        apiTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testApiConnection();
            }

        });

        requestFeatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    openRequestFeatureDialog();
                } catch (Exception ue) {
                    Utilities.generateToast(SettingsActivity.this,"Error sending feature request. Ensure you have a WiFi/network connection",Toast.LENGTH_SHORT);
                }
            }
        });

        loadDataFromSharedPreferences();

    }

    void openRequestFeatureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        View layoutView = inflater.inflate(R.layout.request_feature_dialog, null);
        final EditText etEmail = layoutView.findViewById(R.id.requestFeatureEmail);
        final EditText etMessage = layoutView.findViewById(R.id.requestFeatureMessage);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(layoutView)
                // Add action buttons
                .setNeutralButton("Send", (dialog, id) -> {

                    if (!etEmail.getText().toString().equals("") && !etMessage.getText().toString().equals("")) {
                        postFeatureRequest(etEmail.getText().toString(),etMessage.getText().toString());
                        Utilities.generateToast(SettingsActivity.this,"Feature request sent!",Toast.LENGTH_SHORT);
                    } else {
                        Utilities.generateToast(SettingsActivity.this,"Enter an email & message to send a feature request",Toast.LENGTH_SHORT);
                    }
                });
        builder.create();
        builder.show();
    }

    void postFeatureRequest(String email, String message) {

        HashMap params = new HashMap();
        params.put("from", "Cryptail User <" + email.trim() + ">");
        params.put("to", Constants.MAIL_TO_EMAIL);
        params.put("subject", "Cryptail Feature Request");
        params.put("text", message);

        Map<String, String> headers = new HashMap<>();
        String credentials = "api:"+ Constants.MAIL_API_KEY;
        String auth = "Basic "
                + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headers.put("Authorization", auth);

        String url = "https://api.mailgun.net/v3/" + Constants.MAIL_DOMAIN_NAME + "/messages";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // response
                    Log.d("Response", response);
                },
                error -> {
                    // error
                    Log.d("Error.Response", error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(postRequest.setShouldCache(false));
    }

    private void testApiConnection() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,Constants.CRYPTO_API_TEST_URL,null,
                response -> {
                    if (response != null) {
                        Utilities.generateToast(SettingsActivity.this,"Connection Succeeded",Toast.LENGTH_SHORT);
                    } else {
                        Utilities.generateToast(SettingsActivity.this,"Error - no data received from endpoint",Toast.LENGTH_SHORT);
                    }

                }, error -> {
                    Log.e("Volley", error.toString());
                    Utilities.generateToast(SettingsActivity.this,"Error - no connection. Ensure you have a WiFi/network connection",Toast.LENGTH_SHORT);
                });
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        //requestQueue.add(jsonArrayRequest);
    }

    void saveBooleanDataToSharedPrefs(String key, boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,isChecked);
        editor.apply();
    }

    void saveStringDataToSharedPrefs(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    void loadDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);

        /*if (sharedPreferences.contains(NOTIFICATIONS) && sharedPreferences.contains(PIP)) {
            // shared prefs exist
            Log.i("Shared Prefs Status","Switches exist!");
            notificationSwitch.setChecked(sharedPreferences.getBoolean(NOTIFICATIONS,false));
            pipSwitch.setChecked(sharedPreferences.getBoolean(PIP,false));
        } else {
            //shared prefs do not exist, do not try to read
            Log.i("Shared Prefs Status","selections do not exist!");
        }*/

        if (sharedPreferences.contains(NOTIFICATIONS)) {
            notificationSwitch.setChecked(sharedPreferences.getBoolean(NOTIFICATIONS,true));
        } else {
            notificationSwitch.setChecked(true);
        }

        if (sharedPreferences.contains(PIP)) {
            pipSwitch.setChecked(sharedPreferences.getBoolean(PIP,false));
        } else {
            pipSwitch.setChecked(false);
        }

        if (sharedPreferences.contains(CURRENCY)) {
            currencySpinner.setSelection(getIndex(currencySpinner,sharedPreferences.getString(CURRENCY,"")));
        } else {
            currencySpinner.setSelection(0);
        }

        if (sharedPreferences.contains(AUTOREFRESH)) {
            refreshSpinner.setSelection(getIndex(refreshSpinner,sharedPreferences.getString(AUTOREFRESH,"")));
        } else {
            refreshSpinner.setSelection(2);
        }
    }

    // get the index from a spinner using the value
    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return 0;
    }


    /*
    BaseActivity abstract overrides
     */


    @Override
    int getLayoutId() {
        return R.layout.settings_main;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.action_settings;
    }
}
