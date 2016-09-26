package by.mksn.wififilehook.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import by.mksn.wififilehook.R;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_DEFAULT_WIFI_SSID = "default_wifi_ssid";
    private static final String PREF_DEFAULT_WIFI_PSK = "default_wifi_psk";
    private static final String PREF_DEFAULT_WIFI_SYNC_TIME = "sync_time";
    private static final String PREF_DEFAULT_WIFI_FILE_PATH = "file_path";
    private static final String PREF_DEFAULT_WIFI_SHOW_IF_CAN = "show_if_can";

    private SharedPreferences settings;
    private CheckBox showIfCanCheckBox;
    private EditText syncTimeEdit;
    private EditText filePathEdit;
    private TextView fileView;
    private Button showButton;


    private SmbFile smbFile;
    private Handler handler;
    private long syncTime;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                smbFile = new SmbFile("smb://" + filePathEdit.getText().toString());
                showButton.setEnabled(true);
                if (showIfCanCheckBox.isChecked()) {
                    showButton.callOnClick();
                }
                startUpdate();
            } catch (Exception e) {
                smbFile = null;
                showButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Cannot find or open file now", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getPreferences(MODE_PRIVATE);
        showIfCanCheckBox = (CheckBox) findViewById(R.id.activity_main_checkbox_open_if_can);
        syncTimeEdit = (EditText) findViewById(R.id.activity_main_input_sync_time);
        filePathEdit = (EditText) findViewById(R.id.activity_main_input_resource_path);

        showButton = (Button) findViewById(R.id.activity_main_button_show_file);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showButton.isEnabled() && smbFile != null) {
                    String fileString = readFileContent(smbFile, new StringBuilder()).toString();
                    fileView.setText(fileString);
                }
            }
        });

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
        startScanFile();
    }

    private StringBuilder readFileContent(SmbFile sFile, StringBuilder builder) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(sFile)));
        } catch (SmbException | MalformedURLException | UnknownHostException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            Toast.makeText(getApplicationContext(), "Cannot read file", Toast.LENGTH_LONG).show();
        }
        String lineReader = null;
        {
            try {
                if (reader != null) {
                    while ((lineReader = reader.readLine()) != null) {
                        builder.append(lineReader).append("\n");
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                    Toast.makeText(getApplicationContext(), "Cannot read file", Toast.LENGTH_LONG).show();
                }
            }
        }
        return builder;
    }

    public void startScanFile() {
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

}
