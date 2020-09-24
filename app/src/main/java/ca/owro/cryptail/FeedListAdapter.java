package ca.owro.cryptail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ca.owro.cryptail.R;

import java.util.ArrayList;

public class FeedListAdapter extends BaseAdapter {

    private ArrayList<Cryptocurrency> feedListData;
    private LayoutInflater layoutInflater;

    public FeedListAdapter(Context context, ArrayList<Cryptocurrency> feedListData) {
        this.feedListData = feedListData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.feed_list_row_layout, null);
            holder = new ViewHolder();
            holder.nameView = convertView.findViewById(R.id.cryptoNameTextView);
            holder.conversionView = convertView.findViewById(R.id.cryptoConversionTextView);
            holder.hrPercentView = convertView.findViewById(R.id.hrPercentTextView);
            holder.hrValueView = convertView.findViewById(R.id.hrNumTextView);
            holder.dayPercentView = convertView.findViewById(R.id.dayPercentTextView);
            holder.dayValueView = convertView.findViewById(R.id.dayNumTextView);
            holder.logoView = convertView.findViewById(R.id.cryptoLogoImageView);
            holder.dayArrowView = convertView.findViewById(R.id.dayArrowImageView);
            holder.hrArrowView = convertView.findViewById(R.id.hrArrowImageView);
            holder.volumeView = convertView.findViewById(R.id.volTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameView.setText(feedListData.get(position).getName());
        holder.conversionView.setText(feedListData.get(position).getConversion());
        holder.hrPercentView.setText(feedListData.get(position).getHrPercent() + "%");
        holder.hrValueView.setText(feedListData.get(position).getHrValue());
        holder.dayPercentView.setText(feedListData.get(position).getDayPercent() + "%");
        holder.dayValueView.setText(feedListData.get(position).getDayValue());
        holder.logoView.setImageResource(feedListData.get(position).getLogoId());

        // check if percent change is +/- to determine arrow used

        if (Float.parseFloat(feedListData.get(position).getHrPercent()) > 0) {
            holder.hrArrowView.setBackgroundResource(R.drawable.ic_arrow_drop_up_24px);
        } else if (Float.parseFloat(feedListData.get(position).getHrPercent()) == 0) {
            holder.hrArrowView.setBackgroundResource(R.drawable.ic_remove_24px);
        } else if (Float.parseFloat(feedListData.get(position).getHrPercent()) < 0) {
            holder.hrArrowView.setBackgroundResource(R.drawable.ic_arrow_drop_down_24px);
        }

        if (Float.parseFloat(feedListData.get(position).getDayPercent()) > 0) {
            holder.dayArrowView.setBackgroundResource(R.drawable.ic_arrow_drop_up_24px);
        } else if (Float.parseFloat(feedListData.get(position).getDayPercent()) == 0) {
            holder.dayArrowView.setBackgroundResource(R.drawable.ic_remove_24px);
        } else if (Float.parseFloat(feedListData.get(position).getDayPercent()) < 0) {
            holder.dayArrowView.setBackgroundResource(R.drawable.ic_arrow_drop_down_24px);
        }

        holder.volumeView.setText("24hr Vol: " + feedListData.get(position).getDayVolume());

        return convertView;
    }

    @Override
    public int getCount() {
        return feedListData.size();
    }

    @Override
    public Object getItem(int position) {
        return feedListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        TextView nameView;
        TextView conversionView;
        TextView hrPercentView;
        TextView hrValueView;
        TextView dayPercentView;
        TextView dayValueView;
        ImageView logoView;
        ImageView dayArrowView;
        ImageView hrArrowView;
        TextView volumeView;
    }
}
