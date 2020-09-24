package ca.owro.cryptail;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import ca.owro.cryptail.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class CurrenciesActivity extends BaseActivity {

    static final String SHARED_PREFS = BuildConfig.APPLICATION_ID;
    static final String SELECTIONS = "selections";
    private static final String CHANNEL_ID = "1";

    RelativeLayout relativeLayout;
    GridView cryptoGridView;
    ArrayList<String> cryptoNames = new ArrayList<>();
    ArrayList<Integer> cryptoLogos = new ArrayList<>();
    ArrayList<Integer> selectedCryptos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Cryptocurrencies");

        // Create the notification channel
        //createNotificationChannel();

        cryptoGridView = findViewById(R.id.cryptoGridView);
        relativeLayout = findViewById(R.id.relLayoutCurr);

        fillLists();
        updateGridView();

        cryptoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(CurrenciesActivity.this,"Position clicked: " + position + " " + cryptoNames.get(position),Toast.LENGTH_SHORT).show();
                updateCryptoSelections(position);
                updateGridView();
            }
        });

        loadDataFromSharedPreferences();

    }

    private void createNotificationChannel() {

            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "crypto_feed";
                String description = "provides feed info in ongoing notification";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

    }

    void fillLists() {
        cryptoNames.addAll(Arrays.asList(Constants.CRYPTO_NAMES));
        cryptoLogos.addAll(Arrays.asList(Constants.CRYPTO_LOGOS));
        selectedCryptos.addAll(Arrays.asList(Constants.SELECTED_CRYPTOS));

    }

    boolean checkForMaxSelectedCryptos() {
        Integer count = 0;
        for (Integer i : selectedCryptos) {
            if (i==1) {
                count++;
            }
        }
        if (count >= 5) {
            return true;
        } else {
            return false;
        }
    }

    void updateCryptoSelections(int position) {
        if (selectedCryptos.get(position) == 0) {
            // Not selected, so select
            if (!checkForMaxSelectedCryptos()) {
                selectedCryptos.set(position,1);
            } else {
                Utilities.generateToast(CurrenciesActivity.this,"Max 5 trackable Cryptocurrencies",Toast.LENGTH_SHORT);
            }
        } else {
            selectedCryptos.set(position,0);
        }
        saveDataToSharedPrefs();
    }

    void updateGridView() {
        GridViewAdapter gridViewAdapter = new GridViewAdapter(this, cryptoNames, cryptoLogos, selectedCryptos);
        cryptoGridView.setAdapter(gridViewAdapter);
        gridViewAdapter.notifyDataSetChanged();
    }

    /*
    Shared Preferences
     */

    void saveDataToSharedPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < selectedCryptos.size(); i++) {
            str.append(selectedCryptos.get(i)).append(",");
        }

        editor.putString(SELECTIONS,str.toString());
        editor.commit();
    }

    void loadDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        if (sharedPreferences.contains(SELECTIONS)) {
            // shared prefs exist
            Log.i("Shared Prefs Status","selections exists!");
            String savedString = sharedPreferences.getString(SELECTIONS, "");
            StringTokenizer st = new StringTokenizer(savedString, ",");
            for (int i = 0; i < selectedCryptos.size(); i++) {
                selectedCryptos.set(i, Integer.parseInt(st.nextToken()));
            }

        } else {
            //shared prefs do not exist, do not try to read
            Log.i("Shared Prefs Status","selections do not exist!");
            selectedCryptos.set(0, 1);
            saveDataToSharedPrefs();
        }

    }

    /*
    BaseActivity abstract overrides
     */

    @Override
    int getLayoutId() {
        return R.layout.currencies_main;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.action_currencies;
    }
}
