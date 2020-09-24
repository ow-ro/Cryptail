package ca.owro.cryptail;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import ca.owro.cryptail.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class FeedActivity extends BaseActivity {

    static final String SHARED_PREFS = BuildConfig.APPLICATION_ID;
    static final String SELECTIONS = "selections";
    static final String CURRENCY = "currency";
    static final String AUTOREFRESH = "autorefresh";
    private static final String CHANNEL_ID = "4";
    private static final int FEED_NOTIFICATION_ID = 2;
    final String[] cryptoNamesArray = {"Bitcoin","Ethereum","Ripple XRP","Bitcoin Cash","Bitcoin SV","Tether","Litecoin","EOS","Binance Coin","Monero"};

    int testNum = 0;

    TextView lastUpdatedTextView, genericTitle;
    SwipeRefreshLayout swipeRefreshLayout;
    HashMap<String, Integer> selectedCryptos = new HashMap<>();
    ListView feedListView;

    JSONObject liveJSONData;
    ProgressBar progressBar;

    ArrayList<Cryptocurrency> cryptos;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Feed");
        cryptos = new ArrayList<>();
        mContext = getApplicationContext();

        startService(new Intent(this, KillNotificationService.class));

        feedListView = findViewById(R.id.feedListView);
        lastUpdatedTextView = findViewById(R.id.lastUpdatedTextView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.indeterminateBar);

        updateTimestamp();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                getJSONData(new ServerCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        Log.i("JSON","Result");
                        refreshFeedListView();
                        updateTimestamp();
                    }
                }, false);
            }
        });

        getJSONData(new ServerCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.i("JSON","Result");
                refreshFeedListView();
                progressBar.setVisibility(View.GONE);
                updateTimestamp();
                if (Utilities.userAllowsNotifications(mContext)) {
                    buildAndShowFeedNotification();
                }
            }
        }, true);


        // AUTOREFRESH
        final Handler handler = new Handler();
        Runnable refresh = new Runnable() {
            @Override
            public void run() {
                getJSONData(new ServerCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        refreshFeedListView();
                        updateTimestamp();
                        if (Utilities.userAllowsNotifications(mContext)) {
                            buildAndShowFeedNotification();
                        }
                        Utilities.generateToast(FeedActivity.this, "Autorefresh Complete", Toast.LENGTH_SHORT);
                    }
                }, false);
                handler.postDelayed(this, 60 * getAutoRefreshMilliseconds());
                //Utilities.generateToast(FeedActivity.this, "Autorefresh Complete", Toast.LENGTH_SHORT);
            }
        };
       handler.postDelayed(refresh, 60 * getAutoRefreshMilliseconds());

    }

    int getAutoRefreshMilliseconds() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        if (sharedPreferences.contains(AUTOREFRESH)) {
            return Utilities.getAutoRefreshMilliseconds(sharedPreferences.getString(AUTOREFRESH,""));
        } else {
            return 5000;
        }
    }

    void getJSONData(final ServerCallback callback, boolean showProgressBar) {

        if (showProgressBar) {
            progressBar.setVisibility(View.VISIBLE);
        }

        //build the url
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        String cryptos = "";
        String currency = "";
        String apiUrl = "";

        if (sharedPreferences.contains(SELECTIONS)) {
            String savedString = sharedPreferences.getString(SELECTIONS, "");
            StringTokenizer st = new StringTokenizer(savedString, ",");
            for (int i = 0; i < Constants.SELECTED_CRYPTOS.length; i++) {
                if (Integer.parseInt(st.nextToken()) == 1) {
                    cryptos += Constants.CRYPTO_TICKERS[i] + ",";
                }
            }
            if (cryptos.endsWith(",")) {
                cryptos.substring(0, cryptos.length() - 1);
            }
            currency = Utilities.parseSelectedCurrency(sharedPreferences.getString(CURRENCY,"$ USD"), 1);

            apiUrl = Utilities.buildApiURL(cryptos, currency);
        }


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                apiUrl,null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.i("JSON 1, in onResponse", response.toString());
                liveJSONData = response;
                callback.onSuccess(liveJSONData);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d("Volley error json object ", "Error: " + error.getMessage());

            }
        }){
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }

    private void refreshFeedListView() {
        ArrayList feedList = getAllUserSelectedCryptos();
        feedListView.setAdapter(new FeedListAdapter(this, feedList));
    }

    private ArrayList getAllUserSelectedCryptos() {

        //ArrayList<Cryptocurrency> cryptos = new ArrayList<>();
        cryptos.clear();


        Log.i("JSON 2", "ddd");

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        if (sharedPreferences.contains(SELECTIONS)) {
            String savedString = sharedPreferences.getString(SELECTIONS, "");
            StringTokenizer st = new StringTokenizer(savedString, ",");
            for (int i = 0; i < Constants.SELECTED_CRYPTOS.length; i++) {
                if (Integer.parseInt(st.nextToken()) == 1) {
                    // these are the users selected cryptos, so create object for it
                    Cryptocurrency cur = new Cryptocurrency();
                    // lets get the user's currency setting
                    String currency = sharedPreferences.getString(CURRENCY,"$ USD");
                    // lets extract the relevant JSON data

                    //Log.i("JSON 3", liveJSONData.toString());

                    // LIVE PRICE:
                    String livePrice = "";
                    try {
                        livePrice = liveJSONData.getJSONObject("DISPLAY").getJSONObject(Constants.CRYPTO_TICKERS[i]).getJSONObject(Utilities.parseSelectedCurrency(currency,1)).getString("PRICE");
                    } catch (JSONException e) { e.printStackTrace(); }

                    // DAY CHANGE:
                    String dayPercentChange = "";
                    String dayValueChange = "";
                    try {
                        dayPercentChange = liveJSONData.getJSONObject("DISPLAY").getJSONObject(Constants.CRYPTO_TICKERS[i]).getJSONObject(Utilities.parseSelectedCurrency(currency,1)).getString("CHANGEPCT24HOUR");
                        dayValueChange = liveJSONData.getJSONObject("DISPLAY").getJSONObject(Constants.CRYPTO_TICKERS[i]).getJSONObject(Utilities.parseSelectedCurrency(currency,1)).getString("CHANGE24HOUR");
                    } catch (JSONException e) { e.printStackTrace(); }

                    // HR CHANGE:
                    String hrPercentChange = "";
                    String hrValueChange = "";
                    try {
                        hrPercentChange = liveJSONData.getJSONObject("DISPLAY").getJSONObject(Constants.CRYPTO_TICKERS[i]).getJSONObject(Utilities.parseSelectedCurrency(currency,1)).getString("CHANGEPCTHOUR");
                        hrValueChange = liveJSONData.getJSONObject("DISPLAY").getJSONObject(Constants.CRYPTO_TICKERS[i]).getJSONObject(Utilities.parseSelectedCurrency(currency,1)).getString("CHANGEHOUR");
                    } catch (JSONException e) { e.printStackTrace(); }

                    // LIVE PRICE:
                    String volume = "";
                    try {
                        volume = liveJSONData.getJSONObject("DISPLAY").getJSONObject(Constants.CRYPTO_TICKERS[i]).getJSONObject(Utilities.parseSelectedCurrency(currency,1)).getString("VOLUME24HOUR");
                    } catch (JSONException e) { e.printStackTrace(); }

                    cur.setName(Constants.CRYPTO_NAMES[i]);
                    cur.setLogoId(Constants.CRYPTO_LOGOS[i]);
                    cur.setSymbol(Constants.CRYPTO_SYMBOLS[i]);
                    cur.setConversion("1 x " + Constants.CRYPTO_SYMBOLS[i] + " = " + livePrice);
                    cur.setHrPercent(hrPercentChange);
                    cur.setHrValue(hrValueChange);
                    cur.setDayPercent(dayPercentChange);
                    cur.setDayValue(dayValueChange);
                    cur.setDayVolume(volume);
                    cur.setLivePrice(livePrice);
                    cryptos.add(cur);
                }
            }

        } else {
            // handle
        }

        return cryptos;

    }

    void updateTimestamp() {
        Date currentTime = Calendar.getInstance().getTime();
        lastUpdatedTextView.setText("Last Updated: " + currentTime.toString());
    }

    void buildAndShowFeedNotification() {

        // CONTENT INTENT
        Intent notifyIntent = new Intent(this, FeedActivity.class);
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        // ACTION INTENT
        Intent intentAction = new Intent(this,ActionReceiver.class);
        intentAction.putExtra("action","refresh");
        PendingIntent pIntentLogin = PendingIntent.getBroadcast(this,1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            /* Create or update. */
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
        }


        // generate notification text from current selected cryptos
        String notificationText = Utilities.getNotificationCryptoText(cryptos);

        // create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_blockchain_1)
                .setContentTitle("Crypto Feed")
                //.setContentText("info...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationText + "\n" + lastUpdatedTextView.getText().toString()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setSound(Uri.EMPTY)
                .setContentIntent(notifyPendingIntent)
                .addAction(R.drawable.ic_refresh_24px, "Refresh Data", pIntentLogin);



        // show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(FEED_NOTIFICATION_ID, builder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //region Top Menu Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feed_header, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.feed_refresh) {
            getJSONData(new ServerCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    Log.i("JSON","Result");
                    refreshFeedListView();
                    progressBar.setVisibility(View.GONE);
                    updateTimestamp();
                }
            }, true);
        }
        return true;
    }
    //endregion

    //region BaseActivity abstract methods
    @Override
    int getLayoutId() {
        return R.layout.activity_feed;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.action_feed;
    }

    //endregion


}


