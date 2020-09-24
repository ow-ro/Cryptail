package ca.owro.cryptail;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import ca.owro.cryptail.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;


public class ActionReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "4";
    private static final int FEED_NOTIFICATION_ID = 2;
    static final String SHARED_PREFS = BuildConfig.APPLICATION_ID;
    static final String SELECTIONS = "selections";
    static final String CURRENCY = "currency";
    Context mContext;
    JSONObject liveJSONData;
    ArrayList<Cryptocurrency> cryptos;

        @Override
        public void onReceive(Context context, Intent intent) {

            cryptos = new ArrayList<>();
            mContext = context;
            // allows notification to be killed when app is swiped from quick access
            mContext.startService(new Intent(mContext, KillNotificationService.class));


            String action = intent.getStringExtra("action");
            if(action.equals("refresh")){
                refreshNotificationData();
            }
            //This is used to close the notification tray, don't want this as the notification is ongoing
            //Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            //context.sendBroadcast(it);
        }

        public void refreshNotificationData(){
            getJSONData(new ServerCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    generateNotification();
                }
            });
        }

        void generateNotification() {
            // CONTENT INTENT
            Intent notifyIntent = new Intent(mContext, FeedActivity.class);
            // Set the Activity to start in a new, empty task
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Create the PendingIntent
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                    mContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );
            // ACTION INTENT
            Intent intentAction = new Intent(mContext,ActionReceiver.class);
            intentAction.putExtra("action","refresh");
            PendingIntent pIntentLogin = PendingIntent.getBroadcast(mContext,1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                NotificationManager mNotificationManager =
                        (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

                /* Create or update. */
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_LOW);
                mNotificationManager.createNotificationChannel(channel);
            }
            Date currentTime = Calendar.getInstance().getTime();


            getAllUserSelectedCryptos();
            // generate notification text from current selected cryptos
            String notificationText = Utilities.getNotificationCryptoText(cryptos);


            // create notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_blockchain_1)
                    .setContentTitle("Crypto Feed")
                    //.setContentText("info...")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notificationText + "\n" + "Last Updated: " + currentTime.toString()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .setSound(Uri.EMPTY)
                    .setContentIntent(notifyPendingIntent)
                    .addAction(R.drawable.ic_refresh_24px, "Refresh Data", pIntentLogin)
                    ;



            // show notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(FEED_NOTIFICATION_ID, builder.build());
        }

    void getJSONData(final ServerCallback callback) {


        //build the url
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
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

        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonObjReq);
    }

    private void getAllUserSelectedCryptos() {

        //ArrayList<Cryptocurrency> cryptos = new ArrayList<>();
        cryptos.clear();


        Log.i("JSON 2", "ddd");

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
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

    }

    }

