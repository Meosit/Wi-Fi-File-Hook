package by.mksn.wififilehook.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.util.List;

public class WifiUtil {

    public static final String SECURITY_TYPE_WPA2 = "WPA2";
    public static final String SECURITY_TYPE_WPA = "WPA";
    public static final String SECURITY_TYPE_PSK = "PSK";
    public static final String SECURITY_TYPE_WEP = "WEP";
    public static final String SECURITY_TYPE_EAP = "EAP";
    public static final String SECURITY_TYPE_NONE = "Open";

    public static boolean hasPassword(WifiConfiguration wifiConfig) {
        return !TextUtils.isEmpty(wifiConfig.preSharedKey)
                || !TextUtils.isEmpty(wifiConfig.wepKeys[0])
                || !TextUtils.isEmpty(wifiConfig.wepKeys[1])
                || !TextUtils.isEmpty(wifiConfig.wepKeys[2])
                || !TextUtils.isEmpty(wifiConfig.wepKeys[3]);
    }

    public static String[] getWifiDisplayList(List<ScanResult> wifiScans) {
        String[] wifiSSIDs = new String[wifiScans.size()];
        for (int i = 0; i < wifiSSIDs.length; i++) {
            wifiSSIDs[i] = wifiScans.get(i).SSID + " (" + WifiUtil.getScanResultSecurity(wifiScans.get(i)) + ")";
        }
        return wifiSSIDs;
    }

    public static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {SECURITY_TYPE_WEP,
                SECURITY_TYPE_PSK, SECURITY_TYPE_EAP
        };
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return SECURITY_TYPE_NONE;
    }

    static String getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_TYPE_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_TYPE_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_TYPE_WEP : SECURITY_TYPE_NONE;
    }

    static String getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_TYPE_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_TYPE_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_TYPE_EAP;
        }
        return SECURITY_TYPE_NONE;
    }

    public static String[] getActiveWifiInfo(Context context) {
        String ssid = "NONE";
        String bssid = "NONE";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            return new String[] {ssid, bssid};
        }

        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID()) && !connectionInfo.getSSID().equals("0x")) {
                ssid = connectionInfo.getSSID();
                bssid = connectionInfo.getBSSID();

            }
        }

        return new String[]{ssid, bssid};
    }

    public static String surroundWithQuotes(String string) {
        return "\"" + string + "\"";
    }

    public static String cutQuotes(String string) {
        return string.replace("\"", "");
    }

}
