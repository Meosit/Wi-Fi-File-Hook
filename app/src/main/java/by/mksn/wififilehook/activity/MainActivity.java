package by.mksn.wififilehook.activity;

import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.net.MalformedURLException;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.adapter.ScanResultArrayAdapter;
import jcifs.smb.SmbFile;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_DEFAULT_WIFI_SSID = "default_wifi_ssid";
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

    private SmbFile smbFile;
    private Handler handler;
    private long syncTime;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                smbFile = new SmbFile("smb://" + filePathEdit.getText().toString());
                startUpdate();
            } catch (MalformedURLException e) {
                Toast.makeText(getApplicationContext(), "Cannot find or open file now", Toast.LENGTH_LONG).show();
            }
        }
    };;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getPreferences(MODE_PRIVATE);
        /*wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        enableWifiIfDisabled();

        final Spinner wifiSpinner = (Spinner) findViewById(R.id.activity_main_spinner_wifi_list);
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

        Button connectButton = (Button) findViewById(R.id.activity_main_button_wifi_connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ScanResult result = (ScanResult) wifiSpinner.getSelectedItem();
                if (WifiUtil.getScanResultSecurity(result).equals(WifiUtil.SECURITY_TYPE_PSK)) {
                    LayoutInflater li = LayoutInflater.from(MainActivity.this);
                    View promptsView = li.inflate(R.layout.password_dialog, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            MainActivity.this);

                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.password_dialog_input_password);

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //Boolean b = connectToAccessPoint(result.SSID, userInput.getText().toString());
                                            //Toast.makeText(MainActivity.this, b.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            }
        });
        wifiStatusEdit = (EditText) findViewById(R.id.activity_main_input_status);
        wifiStatusEdit.setText("Connected to " + WifiUtil.getActiveWifiInfo(this)[0]);
        */
        showIfCanCheckBox = (CheckBox) findViewById(R.id.activity_main_checkbox_open_if_can);
        syncTimeEdit = (EditText) findViewById(R.id.activity_main_input_sync_time);
        filePathEdit = (EditText) findViewById(R.id.activity_main_input_resource_path);
        Button applyButton = (Button) findViewById(R.id.activity_main_button_apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    syncTime = Long.parseLong(syncTimeEdit.getText().toString()) * 1000;
                    if (syncTime <= 0) {
                        throw new NumberFormatException();
                    }
                    settings.edit()
                            .putString(PREF_DEFAULT_WIFI_FILE_PATH, filePathEdit.getText().toString())
                            .putString(PREF_DEFAULT_WIFI_SYNC_TIME, String.valueOf(syncTime))
                    .apply();
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Sync time must be long positive value", Toast.LENGTH_LONG).show();
                }

            }
        });

        loadSettings();
    }

    public void callStartTimer() {
        handler = new Handler();
        handler.post(runnable);
    }

    private void startUpdate() {
        handler.postDelayed(runnable, syncTime);
    }

    private void loadSettings() {
        showIfCanCheckBox.setSelected(settings.getBoolean(PREF_DEFAULT_WIFI_SHOW_IF_CAN, false));
        syncTimeEdit.setText(settings.getString(PREF_DEFAULT_WIFI_SYNC_TIME, "60"));
        syncTime = Long.parseLong(syncTimeEdit.getText().toString()) * 1000;
        filePathEdit.setText(settings.getString(PREF_DEFAULT_WIFI_FILE_PATH, ""));
    }

  /*  private boolean connectToAccessPoint(String ssid, String psk) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = WifiUtil.surroundWithQuotes(ssid);
        configuration.preSharedKey = WifiUtil.surroundWithQuotes(psk);

        wifiManager.addNetwork(configuration);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if ((i.SSID != null && i.SSID.equals(WifiUtil.surroundWithQuotes(ssid)))) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }

        return WifiUtil.getActiveWifiInfo(getApplicationContext())[0].equals(ssid);
    }
*/
    private void enableWifiIfDisabled() {
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
