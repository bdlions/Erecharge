package com.bdlions.trustedload.recharge;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.bdlions.trustedload.recharge.Constants.FIFTH_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.FIRST_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.FOURTH_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.SECOND_COLUMN;
import static com.bdlions.trustedload.recharge.Constants.THIRD_COLUMN;

public class PaymentHistoryListViewAdapter extends BaseAdapter {
    public ArrayList<HashMap<String,String>> list;
    Activity activity;

    public PaymentHistoryListViewAdapter(Activity activity, ArrayList<HashMap<String, String>> list){
        super();
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        //implement logic if item id is required
        return position;
    }

    private class ViewHolder{
        TextView txtFirst;
        TextView txtSecond;
        TextView txtThird;
        TextView txtFourth;
        TextView txtFifth;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try
        {
            ViewHolder holder;
            LayoutInflater inflater = activity.getLayoutInflater();
            if (convertView == null){
                convertView = inflater.inflate(R.layout.payment_history_column_row, null);
            }
            holder = new ViewHolder();
            holder.txtFirst = (TextView) convertView.findViewById(R.id.tvFirst);
            holder.txtSecond = (TextView) convertView.findViewById(R.id.tvSecond);
            holder.txtThird = (TextView) convertView.findViewById(R.id.tvThird);
            holder.txtFourth = (TextView) convertView.findViewById(R.id.tvFourth);
            holder.txtFifth = (TextView) convertView.findViewById(R.id.tvFifth);

            HashMap<String,String> map = list.get(position);
            holder.txtFirst.setText(map.get(FIRST_COLUMN));
            holder.txtSecond.setText(map.get(SECOND_COLUMN));
            holder.txtThird.setText(map.get(THIRD_COLUMN));
            holder.txtFourth.setText(map.get(FOURTH_COLUMN));
            holder.txtFifth.setText(map.get(FIFTH_COLUMN));
        }
        catch(Exception ex)
        {
            //handle your exception
        }
        return convertView;
    }
}
