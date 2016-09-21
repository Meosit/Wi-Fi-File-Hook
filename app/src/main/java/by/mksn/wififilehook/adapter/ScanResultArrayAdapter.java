package by.mksn.wififilehook.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.wifi.WifiUtil;

public class ScanResultArrayAdapter extends ArrayAdapter<ScanResult> {

    public ScanResultArrayAdapter(Context context, List<ScanResult> objects) {
        super(context, R.layout.scan_result_spinner_item, objects);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getDropDownView(position, convertView, parent);

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
        View rootView = super.getDropDownView(position, convertView, parent);

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
