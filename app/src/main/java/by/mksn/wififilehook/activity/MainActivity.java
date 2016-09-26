package by.mksn.wififilehook.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.custom.ZoomableImageView;
import by.mksn.wififilehook.task.ReadImageSmbTask;
import by.mksn.wififilehook.task.ReadTextSmbTask;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_DEFAULT_WIFI_SYNC_TIME = "sync_time";
    private static final String PREF_DEFAULT_WIFI_FILE_PATH = "file_path";

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;


    private SharedPreferences settings;
    private EditText syncTimeEdit;
    private EditText filePathEdit;
    private TextView textOutputView;
    private TextView syncStatus;
    private ImageView imageOutputView;
    private ScrollView imageContainer;
    private ScrollView textContainer;

    private ReadTextSmbTask textSmbTask;
    private ReadImageSmbTask imageSmbTask;
    private long syncTime;
    private String filePath;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getPreferences(MODE_PRIVATE);
        syncTimeEdit = (EditText) findViewById(R.id.activity_main_input_sync_time);
        filePathEdit = (EditText) findViewById(R.id.activity_main_input_resource_path);
        textOutputView = (TextView) findViewById(R.id.activity_main_label_file_view);
        imageOutputView = (ImageView) findViewById(R.id.activity_main_image);
        imageContainer = (ScrollView) findViewById(R.id.activity_main_scrollview_vertical_image);
        textContainer = (ScrollView) findViewById(R.id.activity_main_scrollview_text);
        syncStatus = (TextView) findViewById(R.id.activity_main_label_sync_status);
        timer = new Timer();

        final Button refreshButton = (Button) findViewById(R.id.activity_main_button_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (syncTimeEdit.getText().toString().isEmpty()) {
                    syncTimeEdit.setText("0");
                }
                syncTime = Long.parseLong(syncTimeEdit.getText().toString()) * 1000;
                filePath = filePathEdit.getText().toString();
                if (syncTime <= 0) {
                    syncTimeEdit.setText("60");
                    syncTime = 60 * 1000;
                    Toast.makeText(getApplicationContext(), "Sync time must be long positive value", Toast.LENGTH_LONG).show();
                    return;
                }
                settings.edit()
                        .putString(PREF_DEFAULT_WIFI_FILE_PATH, filePathEdit.getText().toString())
                        .putString(PREF_DEFAULT_WIFI_SYNC_TIME, syncTimeEdit.getText().toString())
                        .apply();
                if (syncTime < 20000) {
                    Toast.makeText(getApplicationContext(), "Update", Toast.LENGTH_SHORT).show();
                }
                callAsynchronousTask();
            }
        });

        Button stopButton = (Button) findViewById(R.id.activity_main_button_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_SHORT).show();
                stopUpdating();
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        textOutputView.requestFocus();
        loadSettings();
        callAsynchronousTask();
    }

    private void stopUpdating() {
        timer.cancel();
        if (isImage()) {
            imageSmbTask.cancel(true);
        } else {
            textSmbTask.cancel(true);
        }
        imageContainer.setVisibility(View.INVISIBLE);
        textContainer.setVisibility(View.VISIBLE);
        textOutputView.setText("Updating stopped by user.");
    }

    private void callAsynchronousTask() {
        final Handler handler = new Handler();
        timer.cancel();
        timer = new Timer(true);
        TimerTask doAsynchronousTask;
        if(isImage()) {
            imageContainer.setVisibility(View.VISIBLE);
            textContainer.setVisibility(View.INVISIBLE);
            doAsynchronousTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                imageSmbTask = new ReadImageSmbTask(syncStatus ,imageOutputView, getApplicationContext(),
                                        syncTime, textContainer, imageContainer, textOutputView);
                                imageSmbTask.execute(filePath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
        } else {
            imageContainer.setVisibility(View.INVISIBLE);
            textContainer.setVisibility(View.VISIBLE);
            doAsynchronousTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                textSmbTask = new ReadTextSmbTask(syncStatus, textOutputView, getApplicationContext(), syncTime);
                                textSmbTask.execute(filePath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
        }
        timer.schedule(doAsynchronousTask, 0, syncTime);
    }

    private boolean isImage() {
        String ext = filePath.substring(filePath.lastIndexOf('.'), filePath.length()).toUpperCase();
        switch (ext) {
            case ".JPEG":
                return true;
            case ".JPG":
                return true;
            case ".GIF":
                return true;
            case ".PNG":
                return true;
            case ".BMP":
                return true;
            default:
                return false;
        }
    }

    private void loadSettings() {
        syncTimeEdit.setText(settings.getString(PREF_DEFAULT_WIFI_SYNC_TIME, "60"));
        syncTime = Long.parseLong(syncTimeEdit.getText().toString()) * 1000;
        filePathEdit.setText(settings.getString(PREF_DEFAULT_WIFI_FILE_PATH, ""));
        filePath = filePathEdit.getText().toString();
    }

}
