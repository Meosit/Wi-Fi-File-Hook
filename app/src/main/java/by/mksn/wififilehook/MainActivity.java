package by.mksn.wififilehook;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.List;

import by.mksn.wififilehook.adapter.BlackFontArrayAdapter;

public class MainActivity extends AppCompatActivity {


    public static final String WPA2 = "WPA2";
    public static final String WPA = "WPA";
    public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String EAP = "EAP";
    public static final String OPEN = "Open";

    private WifiManager wifiManager;
    private List<ScanResult> wifiScans;
    private SpinnerAdapter wifiListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Wi-Fi is disabled... Making it enabled", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        }
        if (wifiManager.isWifiEnabled()) {
            Spinner wifiSpinner = (Spinner) findViewById(R.id.activity_main_spinner_wifi_list);
            wifiScans = wifiManager.getScanResults();
            wifiListAdapter = new BlackFontArrayAdapter(getApplicationContext(), getWifiInfoList(wifiScans));
            wifiSpinner.setAdapter(wifiListAdapter);
            wifiSpinner.setSelection(0);

            EditText connectedTo = (EditText) findViewById(R.id.activity_main_input_connected_to);
            WifiInfo currentAccessPoint = wifiManager.getConnectionInfo();

        }

    }

    private String[] getWifiInfoList(List<ScanResult> wifiScans) {
        String[] wifiSSIDs = new String[wifiScans.size()];
        for (int i = 0; i < wifiSSIDs.length; i++) {
            wifiSSIDs[i] = wifiScans.get(i).SSID + " (" + getScanResultSecurity(wifiScans.get(i)) + ")";
        }
        return wifiSSIDs;
    }

    private static boolean hasPassword(WifiConfiguration wifiConfig) {
        return !TextUtils.isEmpty(wifiConfig.preSharedKey)
                || !TextUtils.isEmpty(wifiConfig.wepKeys[0])
                || !TextUtils.isEmpty(wifiConfig.wepKeys[1])
                || !TextUtils.isEmpty(wifiConfig.wepKeys[2])
                || !TextUtils.isEmpty(wifiConfig.wepKeys[3]);
    }

    private static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = { WPA2, WPA, WEP, PSK, EAP };
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return OPEN;
    }

    private static String surroundWithQuotes(String string) {
        return "\"" + string + "\"";
    }

    private static String cutQuotes(String string) {
        return string.replace("\"", "");
    }
}
