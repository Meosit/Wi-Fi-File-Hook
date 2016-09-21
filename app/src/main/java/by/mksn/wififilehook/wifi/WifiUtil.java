package by.mksn.wififilehook.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;

import java.util.List;

public class WifiUtil {

    public static final String SECURITY_TYPE_WPA2 = "WPA2";
    public static final String SECURITY_TYPE_WPA = "WPA";
    public static final String SECURITY_TYPE_PSK = "PSK";
    public static final String SECURITY_TYPE_WEP = "WEP";
    public static final String SECURITY_TYPE_EAP = "EAP";
    public static final String SECURITY_TYPE_OPEN = "Open";

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
        final String[] securityModes = {SECURITY_TYPE_WEP, SECURITY_TYPE_PSK, SECURITY_TYPE_EAP, SECURITY_TYPE_WPA, SECURITY_TYPE_WPA2};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return SECURITY_TYPE_OPEN;
    }

    public static String surroundWithQuotes(String string) {
        return "\"" + string + "\"";
    }

    public static String cutQuotes(String string) {
        return string.replace("\"", "");
    }

}
