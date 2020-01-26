package com.mimik.smarthome.userinterface.common;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.mimik.smarthome.R;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private LayoutInflater _inflater;
    private Typeface _typeface;

    public SpinnerAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        _inflater = LayoutInflater.from(context);
        String fontPath = "fonts/vazir_bold.ttf";
        _typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*
        final ListContent holder;
        View v = convertView;
        if (v == null) {
            v = _inflater.inflate(R.layout.spinner_item, null);
            holder = new ListContent();

            holder.name = (TextView) v.findViewById(R.id.textView1);

            v.setTag(holder);
        } else {
            holder = (ListContent) v.getTag();
        }

        holder.name.setTypeface(_typeface);
        holder.name.setText("" + getItem(position));

        return v;
        */

        View v = super.getView(position, convertView, parent);
        ((TextView)v).setTypeface(_typeface);
        v.setBackgroundResource(0);
        ((TextView)v).setTextColor(parent.getResources().getColor(R.color.color_primary));
        return v;


    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
        ((CheckedTextView)v).setTypeface(_typeface);
        return v;
    }
}