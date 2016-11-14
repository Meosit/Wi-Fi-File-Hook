package by.mksn.wififilehook.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.dialog.ConcreteIndexDialog;
import by.mksn.wififilehook.logic.FurnacesStats;
import by.mksn.wififilehook.logic.Graph;
import by.mksn.wififilehook.logic.ProgressResult;
import by.mksn.wififilehook.task.AsyncTaskCallback;
import by.mksn.wififilehook.task.UpdateGraphTask;
import jcifs.smb.NtlmPasswordAuthentication;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

@SuppressWarnings("unchecked")
public class MainActivity extends AppCompatActivity implements AsyncTaskCallback<ProgressResult, FurnacesStats>, ConcreteIndexDialog.ConcreteIndexDialogCallback {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private static final String PREF_BASE_PATH = "base_path";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_SENSOR_COUNT = "sensor_count";
    private static final String PREF_OVERVIEW_TEXT_COLOR = "overview_text_color";
    private static final String PREF_OVERVIEW_TEXT_SIZE = "overview_text_size";
    private static final String PREF_OVERVIEW_DRAW_COLOR = "overview_draw_color";
    private static final String PREF_OVERVIEW_COLUMN_WIDTH = "overview_column_width";
    private static final String PREF_CONCRETE_TEXT_COLOR = "concrete_text_color";
    private static final String PREF_CONCRETE_TEXT_SIZE = "concrete_text_size";
    private static final String PREF_CONCRETE_DRAW_COLOR = "concrete_draw_color";
    private static final String PREF_CONCRETE_DOT_RADIUS = "concrete_dot_radius";
    private static final String PREF_CONCRETE_LINE_WIDTH = "concrete_line_width";

    private Menu menu;
    private boolean isConcreteGraphVisible = false;

    private UpdateGraphTask updateGraphTask;
    private FurnacesStats furnacesStats;
    private String basePath;
    private boolean isAsyncTaskRunning;
    private NtlmPasswordAuthentication auth;
    private int concreteFurnaceIndex;

    private ProgressBar progressBar;
    private TextView statusText;
    private ImageView graphOverviewImage;
    private PhotoViewAttacher overviewZoomer;
    private ImageView graphConcreteImage;
    private PhotoViewAttacher concreteZoomer;
    private RangeSeekBar<Integer> timeRangeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        statusText = (TextView) findViewById(R.id.activity_main_value_status);
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progress);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(getApplicationContext(), R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN);
        graphOverviewImage = (ImageView) findViewById(R.id.activity_main_graph_overview);
        graphConcreteImage = (ImageView) findViewById(R.id.activity_main_graph_concrete);
        overviewZoomer = new PhotoViewAttacher(graphOverviewImage);
        concreteZoomer = new PhotoViewAttacher(graphConcreteImage);
        timeRangeBar = (RangeSeekBar<Integer>) findViewById(R.id.activity_main_time_range);
        timeRangeBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                if (!minValue.equals(maxValue)) {
                    drawGraphConcrete(concreteFurnaceIndex, ((Integer) minValue), ((Integer) minValue) + 1);
                } else {
                    Toast.makeText(MainActivity.this, R.string.activity_main_message_warning_range, Toast.LENGTH_SHORT).show();
                }
            }
        });
        loadSettings();
        updateFile(getCurrentDate());
    }

    private String defineResourcePath(String date) {
        String result = basePath + ((basePath.charAt(basePath.length() - 1) == '/') ? "" : "/");
        String args[] = date.split(".");
        if (args.length != 3) {
            return result + "1970/01/01.csv";
        }
        return result + args[2] + "/" + args[1] + "/" + args[0] + ".csv";
    }

    private String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.ROOT, "%02d:%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND));
    }

    private String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.ROOT, "%02d.%02d.%04d",
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.YEAR));
    }

    private String getNextDayDate(String date) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(DATE_FORMAT.parse(date));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return DATE_FORMAT.format(calendar.getTime());
        } catch (ParseException e) {
            statusText.setText("Date error: " + e.getMessage());
        }
        return getCurrentDate();
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(getApplicationContext());
        FurnacesStats.setTemperatureSensorCount(sharedPreferences.getInt(PREF_SENSOR_COUNT, 31));
        Graph.setOverviewDrawDefaultColor(sharedPreferences.getInt(PREF_OVERVIEW_DRAW_COLOR, Color.WHITE));
        Graph.setOverviewTextDefaultColor(sharedPreferences.getInt(PREF_OVERVIEW_TEXT_COLOR, Color.YELLOW));
        Graph.setOverviewTextSizeDefault(sharedPreferences.getInt(PREF_OVERVIEW_TEXT_SIZE, 30));
        Graph.setConcreteDrawDefaultColor(sharedPreferences.getInt(PREF_CONCRETE_DRAW_COLOR, Color.WHITE));
        Graph.setConcreteTextDefaultColor(sharedPreferences.getInt(PREF_CONCRETE_TEXT_COLOR, Color.YELLOW));
        Graph.setConcreteTextSizeDefault(sharedPreferences.getInt(PREF_CONCRETE_TEXT_SIZE, 30));
        Graph.setDefaultDotRadius(sharedPreferences.getInt(PREF_CONCRETE_DOT_RADIUS, 8));
        Graph.setDefaultLineWidth(sharedPreferences.getInt(PREF_CONCRETE_LINE_WIDTH, 4));
        Graph.setDefaultColumnWidth(sharedPreferences.getInt(PREF_OVERVIEW_COLUMN_WIDTH, 10));
        basePath = sharedPreferences.getString(PREF_BASE_PATH, "");
        String username = sharedPreferences.getString(PREF_USERNAME, "");
        String password = sharedPreferences.getString(PREF_PASSWORD, "");
        if (username.isEmpty() || password.isEmpty()) {
            auth = null;
        } else {
            auth = new NtlmPasswordAuthentication("", username, password);
        }
    }

    private void switchToLayout(boolean isConcrete) {
        if (menu != null) {
            MenuItem showFurnaceMenuItem = menu.findItem(R.id.action_show_concrete);
            if (isConcrete) {
                showFurnaceMenuItem.setTitle(R.string.menu_main_concrete_furnace_visible);
                showFurnaceMenuItem.setIcon(R.drawable.ic_overview);
                graphConcreteImage.setVisibility(View.VISIBLE);
                graphOverviewImage.setVisibility(View.INVISIBLE);
                timeRangeBar.setVisibility(View.VISIBLE);
                timeRangeBar.setSelectedMinValue(0);
                timeRangeBar.setSelectedMaxValue(24);
            } else {
                showFurnaceMenuItem.setTitle(R.string.menu_main_concrete_furnace_invisible);
                showFurnaceMenuItem.setIcon(R.drawable.ic_concrete);
                graphConcreteImage.setVisibility(View.INVISIBLE);
                graphOverviewImage.setVisibility(View.VISIBLE);
                //timeRangeBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void updateFile(String date) {
        if (basePath.isEmpty()) {
            statusText.setText(R.string.activity_main_message_error_empty_path);
            return;
        }
        updateGraphTask = new UpdateGraphTask(this, getApplicationContext(), auth);
        updateGraphTask.execute(defineResourcePath(date), defineResourcePath(getNextDayDate(date)), date);
    }

    private void drawGraphOverview() {
        Graph graph = new Graph(this, R.drawable.overview);
        graph.drawOverviewGraph(furnacesStats.getTimestamp(furnacesStats.getTimestampCount() - 1));
        graphOverviewImage.setImageDrawable(graph.getResultBitmapDrawable());
        overviewZoomer.update();
    }

    private void drawGraphConcrete(int furnaceIndex, int minHour, int maxHour) {
        Graph graph = new Graph(this, R.drawable.concrete);
        graph.drawConcreteGraph(furnacesStats.getConcreteIndexAllTimeValues(furnaceIndex), minHour, maxHour);
        graphConcreteImage.setImageDrawable(graph.getResultBitmapDrawable());
        concreteZoomer.update();
    }

    private void stopUpdating() {
        if (isAsyncTaskRunning) {
            updateGraphTask.cancel(true);
        } else {
            statusText.setText(getString(R.string.asynctask_message_cancelled, getCurrentTime()));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getMenuInflater().inflate(R.menu.menu_main_portrait, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main_landscape, menu);
        }
        this.menu = menu;
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
            case R.id.action_show_concrete:
                if (furnacesStats != null) {
                    if (isConcreteGraphVisible) {
                        switchToLayout(false);
                        isConcreteGraphVisible = false;
                    } else {
                        ConcreteIndexDialog.newInstance(this).show(getSupportFragmentManager(), "IndexPicker");
                    }
                } else {
                    Toast.makeText(this, R.string.activity_main_message_warning_switch, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_stop:
                stopUpdating();
                return true;
            case R.id.action_refresh:
                updateFile(getCurrentDate());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadSettings();
    }

    @Override
    public void onAsyncTaskPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        isAsyncTaskRunning = true;
    }

    @Override
    public void onAsyncTaskProgressUpdate(ProgressResult result) {
        progressBar.setProgress(result.progressCurrent);
        statusText.setText(result.statusMessage);
    }

    @Override
    public void onAsyncTaskCancelled(FurnacesStats result) {
        statusText.setText(getString(R.string.asynctask_message_cancelled, getCurrentTime()));
        progressBar.setProgress(progressBar.getMax());
        progressBar.setVisibility(View.GONE);
        isAsyncTaskRunning = false;
    }

    @Override
    public void onAsyncTaskPostExecute(FurnacesStats result) {
        if (result != null) {
            statusText.setText(getString(R.string.asynctask_message_sync_time, getCurrentTime()));
            furnacesStats = result;
            if (isConcreteGraphVisible) {
                drawGraphConcrete(concreteFurnaceIndex, 0, 1);
            } else {
                drawGraphOverview();
            }

        }
        progressBar.setProgress(progressBar.getMax());
        progressBar.setVisibility(View.GONE);
        isAsyncTaskRunning = false;
    }

    @Override
    public void onPositiveClick(int index) {
        concreteFurnaceIndex = index;
        switchToLayout(true);
        isConcreteGraphVisible = true;
        drawGraphConcrete(concreteFurnaceIndex, 0, 24);
    }

    @Override
    public void onNegativeClick() {
        switchToLayout(false);
        isConcreteGraphVisible = false;
    }
}
