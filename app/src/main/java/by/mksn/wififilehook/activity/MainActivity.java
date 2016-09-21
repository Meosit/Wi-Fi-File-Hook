package by.mksn.wififilehook.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.adapter.ScanResultArrayAdapter;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;

    private ScanResultArrayAdapter wifiListAdapter;
    private Spinner spinner;
    private EditText wifiStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        enableWifiIsDisabled();

        Spinner wifiSpinner = (Spinner) findViewById(R.id.activity_main_spinner_wifi_list);
        wifiListAdapter = new ScanResultArrayAdapter(getApplicationContext(),
                wifiManager.getScanResults());
        wifiSpinner.setAdapter(wifiListAdapter);
        wifiSpinner.setSelection(0);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reloadWifiListAdapter();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();

    }

    private void enableWifiIsDisabled() {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Wi-Fi is disabled... Making it enabled", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void reloadWifiListAdapter() {
        wifiListAdapter.clear();
        for (ScanResult scanResult : wifiManager.getScanResults()) {
            wifiListAdapter.add(scanResult);
        }
        wifiListAdapter.notifyDataSetChanged();
    }

}
