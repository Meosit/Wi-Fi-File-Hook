package by.mksn.wififilehook.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.adapter.ScanResultArrayAdapter;
import by.mksn.wififilehook.wifi.WifiUtil;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_DEFAULT_WIFI_SSID = "default_wifi_ssid";
    private static final String PREF_DEFAULT_WIFI_BSSID = "default_wifi_bssid";
    private static final String PREF_DEFAULT_WIFI_SECURITY = "default_wifi_security";
    private static final String PREF_DEFAULT_WIFI_PSK = "default_wifi_psk";
    private static final String PREF_DEFAULT_WIFI_SYNC_TIME = "sync_time";
    private static final String PREF_DEFAULT_WIFI_FILE_PATH = "file_path";
    private static final String PREF_DEFAULT_WIFI_SHOW_IF_CAN = "show_if_can";
    private WifiManager wifiManager;
    private SharedPreferences settings;
    private ScanResultArrayAdapter wifiListAdapter;
    private EditText wifiStatusEdit;
    private CheckBox showIfCanCheckBox;
    private EditText syncTimeEdit;
    private EditText filePathEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        enableWifiIfisabled();

        Spinner wifiSpinner = (Spinner) findViewById(R.id.activity_main_spinner_wifi_list);
        wifiListAdapter = new ScanResultArrayAdapter(getApplicationContext(),
                wifiManager.getScanResults());
        wifiSpinner.setAdapter(wifiListAdapter);
        wifiSpinner.setSelection(0);

        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                        reloadWifiListAdapter();
                        break;

                }

            }
        }, receiverFilter);
        wifiManager.startScan();
        ImageButton refreshButton = (ImageButton) findViewById(R.id.activity_main_button_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager.startScan();
            }
        });

        showIfCanCheckBox = (CheckBox) findViewById(R.id.activity_main_checkbox_open_if_can);
        syncTimeEdit = (EditText) findViewById(R.id.activity_main_input_sync_time);
        filePathEdit = (EditText) findViewById(R.id.activity_main_input_resource_path);
        wifiStatusEdit = (EditText) findViewById(R.id.activity_main_input_status);
        wifiStatusEdit.setText("Connected to " + WifiUtil.getActiveWifiInfo(this)[0]);
        loadSettings();
    }

    private void loadSettings() {
        settings = getPreferences(MODE_PRIVATE);
        showIfCanCheckBox.setSelected(settings.getBoolean(PREF_DEFAULT_WIFI_SHOW_IF_CAN, false));
        syncTimeEdit.setText(settings.getString(PREF_DEFAULT_WIFI_SYNC_TIME, "60"));
        filePathEdit.setText(settings.getString(PREF_DEFAULT_WIFI_FILE_PATH, ""));
    }

    private void enableWifiIfisabled() {
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
