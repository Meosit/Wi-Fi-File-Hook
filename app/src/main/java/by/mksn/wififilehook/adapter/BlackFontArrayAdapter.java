package by.mksn.wififilehook.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BlackFontArrayAdapter extends ArrayAdapter<String> {

    public BlackFontArrayAdapter(Context context, String[] objects) {
        super(context, android.R.layout.simple_dropdown_item_1line, objects);
    }

    public BlackFontArrayAdapter(Context context, int textViewResourceId) {
        super(context, android.R.layout.simple_dropdown_item_1line, textViewResourceId);
    }

    public BlackFontArrayAdapter(Context context, List<String> objects) {
        super(context, android.R.layout.simple_dropdown_item_1line, objects);
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setTextColor(Color.BLACK);

        return view;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setTextColor(Color.BLACK);
        text.setTextSize(20);
        return view;

    }
}
