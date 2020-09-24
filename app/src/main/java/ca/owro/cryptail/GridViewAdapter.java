package ca.owro.cryptail;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ca.owro.cryptail.R;

import java.util.ArrayList;

public class GridViewAdapter extends BaseAdapter {

    Context context;
    private final ArrayList<String> cryptoNames;
    private final ArrayList<Integer> cryptoImages;
    private final ArrayList<Integer> selectedCryptos;
    View view;
    LayoutInflater layoutInflater;

    public GridViewAdapter(Context context, ArrayList<String> cryptoNames, ArrayList<Integer> cryptoImages, ArrayList<Integer> selectedCryptos) {
        this.context = context;
        this.cryptoNames = cryptoNames;
        this.cryptoImages = cryptoImages;
        this.selectedCryptos = selectedCryptos;
    }

    @Override
    public int getCount() {
        return cryptoImages.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = new View(context);
            view = layoutInflater.inflate(R.layout.single_item,null);
            ImageView cryptoLogoImageView = view.findViewById(R.id.cryptoLogoImageView);
            TextView cryptoNameTextView = view.findViewById(R.id.cryptoNameTextView);
            cryptoLogoImageView.setImageResource(cryptoImages.get(position));
            cryptoNameTextView.setText(cryptoNames.get(position));

            if (selectedCryptos.get(position) == 0) {
                //not selected
                cryptoLogoImageView.setImageAlpha(50);
            } else {
                //selected
                cryptoLogoImageView.setImageAlpha(255);
                cryptoNameTextView.setTypeface(cryptoNameTextView.getTypeface(), Typeface.BOLD);
            }


        }

        return view;
    }
}
