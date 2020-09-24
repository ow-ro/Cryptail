package ca.owro.cryptail;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ca.owro.cryptail.R;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

final class Utilities {

    static final String SHARED_PREFS = BuildConfig.APPLICATION_ID;
    static final String AUTOREFRESH = "autorefresh";
    static final String NOTIFICATIONS = "notifications";

    static void generateToast(Context context, CharSequence message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        View toastView = toast.getView();
        toastView.getBackground().setColorFilter(context.getResources().getColor(R.color.toastBackground), PorterDuff.Mode.SRC_IN);
        TextView text = toastView.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 240);
        toast.show();
    }

    static String parseSelectedCurrency(String currency, int returnType) {
        // expected received format: $ USD
        // return type 0: return symbol, e.g. $
        // return type 1: return currency, e.g. USD

        String value = "";

        switch (returnType) {
            case 0:
                value = currency.substring(0,1);
                break;
            case 1:
                value = currency.substring(2);
                break;
        }

        return value;
    }

    static int getAutoRefreshMilliseconds(String autorefresh) {
        switch (autorefresh) {
            case "2 mins":
                return 2000;
            case "5 mins":
                return 5000;
            case "10 mins":
                return 10000;
            case "30 mins":
                return 30000;
            case "45 mins":
                return 45000;
            case "1 hour":
                return 60000;
            case "2 hours":
                return 120000;

                default: return 5000;
        }
    }

    static String buildApiURL(String cryptos, String currency) {
        return "https://min-api.cryptocompare.com/data/pricemultifull?fsyms="+cryptos+"&tsyms="+currency+"&apikey="+Constants.CRYPTO_API_KEY;
    }

    static String getNotificationCryptoText(ArrayList<Cryptocurrency> cryptos) {
        String t = "";
        // symbols: ↑↓-▼▲⁃━

        for (Cryptocurrency cur : cryptos) {
            String change = "";

            if (Float.parseFloat(cur.getHrPercent()) > 0) {
                change = "▲ " + cur.getHrValue() + "/" + cur.getHrPercent() + "% 1HR";
            } else if (Float.parseFloat(cur.getHrPercent()) == 0) {
                change = "━ " + cur.getHrValue() + "/" + cur.getHrPercent() + "% 1HR";
            } else if (Float.parseFloat(cur.getHrPercent()) < 0) {
                change = "▼ " + cur.getHrValue() + "/" + cur.getHrPercent() + "% 1HR";
            }

            t += cur.getName() + "  •  " + cur.getLivePrice() + "  •  " + change + "\n";
        }

        return t;
    }

    static boolean userAllowsNotifications(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        if (sharedPreferences.contains(NOTIFICATIONS)) {
            return sharedPreferences.getBoolean(NOTIFICATIONS,true);
        } else {
            return true;
        }
    }

}
