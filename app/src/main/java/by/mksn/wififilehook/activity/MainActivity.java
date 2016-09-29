package by.mksn.wififilehook.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.task.UpdateGraphTask;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_FILE_PATH = "file_path";

    private UpdateGraphTask updateGraphTask;
    private String filePath;
    private ProgressBar progressBar;
    private TextView statusText;
    private ImageView graphOverviewImage;
    private PhotoViewAttacher overviewZoomer;
    private ImageView graphConcreteImage;
    private PhotoViewAttacher concreteZoomer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graphOverviewImage = (ImageView) findViewById(R.id.activity_main_graph);
        overviewZoomer = new PhotoViewAttacher(graphOverviewImage);

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar); // Attaching the layout to the toolbar object
        //toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);


        loadSettings();
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(getApplicationContext());
        filePath = sharedPreferences.getString(PREF_FILE_PATH, "");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(
                        new Intent(MainActivity.this, SettingsActivity.class),
                        SettingsActivity.REQUEST_CODE);
                return true;
            case R.id.action_stop:
                return true;
            case R.id.action_refresh:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            loadSettings();
            Toast.makeText(getApplicationContext(), R.string.activity_main_message_info_settings_updated, Toast.LENGTH_LONG).show();
        }
    }


}
