package by.mksn.wififilehook.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.wifi.WifiUtil;

public class ScanResultArrayAdapter extends ArrayAdapter<ScanResult> {

    private Context context;

    public ScanResultArrayAdapter(Context context, List<ScanResult> objects) {
        super(context, R.layout.scan_result_spinner_item, objects);
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.scan_result_spinner_item, parent, false);

        ScanResult currentItem = getItem(position);

        TextView ssidText = (TextView) rootView.findViewById(R.id.scan_result_spinner_item_ssid);
        ssidText.setText(currentItem.SSID);

        TextView bssidText = (TextView) rootView.findViewById(R.id.scan_result_spinner_item_bssid);
        bssidText.setText(currentItem.BSSID);

        TextView securityText = (TextView) rootView.findViewById(R.id.scan_result_spinner_item_security);
        securityText.setText(WifiUtil.getScanResultSecurity(currentItem));

        return rootView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.scan_result_spinner_item, parent, false);

        ScanResult currentItem = getItem(position);

        TextView ssidText = (TextView) rootView.findViewById(R.id.scan_result_spinner_item_ssid);
        ssidText.setText(currentItem.SSID);

        TextView bssidText = (TextView) rootView.findViewById(R.id.scan_result_spinner_item_bssid);
        bssidText.setText(currentItem.BSSID);

        TextView securityText = (TextView) rootView.findViewById(R.id.scan_result_spinner_item_security);
        securityText.setText(WifiUtil.getScanResultSecurity(currentItem));

        return rootView;
    }

}
