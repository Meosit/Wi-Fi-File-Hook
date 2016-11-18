package by.mksn.wififilehook.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.dialog.ConcreteIndexDialog;
import by.mksn.wififilehook.dialog.SelectDateDialog;
import by.mksn.wififilehook.logic.FurnacesStats;
import by.mksn.wififilehook.logic.Graph;
import by.mksn.wififilehook.logic.ProgressResult;
import by.mksn.wififilehook.task.AsyncTaskCallback;
import by.mksn.wififilehook.task.UpdateGraphTask;
import jcifs.smb.NtlmPasswordAuthentication;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements AsyncTaskCallback<ProgressResult, FurnacesStats>, ConcreteIndexDialog.DialogCallback, SelectDateDialog.DialogCallback {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private static final String PREF_LANGUAGE = "language";
    private static final String PREF_BASE_PATH = "base_path";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_SENSOR_COUNT = "sensor_count";
    private static final String PREF_GRAPH_TIME_RANGE = "graph_time_range";
    private static final String PREF_GRAPH_BIG_STEP = "graph_big_step";
    private static final String PREF_GRAPH_LITTLE_STEP = "graph_little_step";
    private static final String PREF_GRAPH_LINE_BREAK = "graph_line_break";
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

    private String graphMinDate;
    private int graphMinHour;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //turning on wi-fi
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }

        statusText = (TextView) findViewById(R.id.activity_main_value_status);
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progress);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(getApplicationContext(), R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN);
        graphOverviewImage = (ImageView) findViewById(R.id.activity_main_graph_overview);
        graphConcreteImage = (ImageView) findViewById(R.id.activity_main_graph_concrete);
        overviewZoomer = new PhotoViewAttacher(graphOverviewImage);
        concreteZoomer = new PhotoViewAttacher(graphConcreteImage);
        loadSettings();
        graphMinHour = (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - FurnacesStats.getGraphHourTimeRange() / 2);
        if (graphMinHour < 0) {
            graphMinDate = addDaysToDate(getCurrentDate(), -1);
            graphMinHour += 24;
        } else {
            graphMinDate = getCurrentDate();
        }


        updateFiles();
    }

    private String defineResourcePath(String date) {
        String result = basePath + ((basePath.charAt(basePath.length() - 1) == '/') ? "" : "/");
        String args[] = date.split("\\.");
        if (args.length != 3) {
            return result + "1970/01/01.csv";
        }
        return result + args[2] + "/" + args[1] + "/" + args[0] + ".csv";
    }

    private String getCurrentTime(boolean isHoursOnly) {
        Calendar c = Calendar.getInstance();
        if (isHoursOnly) {
            return String.format(Locale.ROOT, "%02d:00:00",
                    c.get(Calendar.HOUR_OF_DAY));
        } else {
            return String.format(Locale.ROOT, "%02d:%02d:%02d",
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    c.get(Calendar.SECOND));
        }
    }

    private String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.ROOT, "%02d.%02d.%04d",
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.YEAR));
    }

    private String addDaysToDate(String date, int value) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(DATE_FORMAT.parse(date));
            calendar.add(Calendar.DAY_OF_MONTH, value);
            return DATE_FORMAT.format(calendar.getTime());
        } catch (ParseException e) {
            statusText.setText(getString(R.string.message_error, "Date error: " + e.getMessage()));
        }
        return getCurrentDate();
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(getApplicationContext());
        //Constraints
        FurnacesStats.setTemperatureSensorCount(sharedPreferences.getInt(PREF_SENSOR_COUNT, 31));
        FurnacesStats.setGraphBreakSecondRange(sharedPreferences.getInt(PREF_GRAPH_LINE_BREAK, 120));
        FurnacesStats.setGraphHourTimeRange(sharedPreferences.getInt(PREF_GRAPH_TIME_RANGE, 10));
        FurnacesStats.setGraphHourTimeBigStep(sharedPreferences.getInt(PREF_GRAPH_BIG_STEP, 8));
        FurnacesStats.setGraphHourTimeLittleStep(sharedPreferences.getInt(PREF_GRAPH_LITTLE_STEP, 1));

        //Visualisation
        Graph.setOverviewDrawDefaultColor(sharedPreferences.getInt(PREF_OVERVIEW_DRAW_COLOR, Color.WHITE));
        Graph.setOverviewTextDefaultColor(sharedPreferences.getInt(PREF_OVERVIEW_TEXT_COLOR, Color.YELLOW));
        Graph.setOverviewTextSizeDefault(sharedPreferences.getInt(PREF_OVERVIEW_TEXT_SIZE, 30));
        Graph.setConcreteDrawDefaultColor(sharedPreferences.getInt(PREF_CONCRETE_DRAW_COLOR, Color.WHITE));
        Graph.setConcreteTextDefaultColor(sharedPreferences.getInt(PREF_CONCRETE_TEXT_COLOR, Color.YELLOW));
        Graph.setConcreteTextSizeDefault(sharedPreferences.getInt(PREF_CONCRETE_TEXT_SIZE, 30));
        Graph.setDefaultDotRadius(sharedPreferences.getInt(PREF_CONCRETE_DOT_RADIUS, 8));
        Graph.setDefaultLineWidth(sharedPreferences.getInt(PREF_CONCRETE_LINE_WIDTH, 4));
        Graph.setDefaultColumnWidth(sharedPreferences.getInt(PREF_OVERVIEW_COLUMN_WIDTH, 10));

        //Access
        basePath = sharedPreferences.getString(PREF_BASE_PATH, "");
        String username = sharedPreferences.getString(PREF_USERNAME, "");
        String password = sharedPreferences.getString(PREF_PASSWORD, "");
        if (username.isEmpty() || password.isEmpty()) {
            auth = null;
        } else {
            auth = new NtlmPasswordAuthentication("", username, password);
        }

        String languageCode = sharedPreferences.getString(PREF_LANGUAGE, null);
        if (languageCode != null && !getCurrentLocale().getLanguage().equals(languageCode)) {
            setLocale(languageCode);
            recreate();
        }
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setSystemLocale(config, locale);
        } else {
            setSystemLocaleLegacy(config, locale);
        }
        getApplicationContext().getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());
    }

    private Locale getCurrentLocale() {
        Configuration config = getResources().getConfiguration();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? getSystemLocale(config) : getSystemLocaleLegacy(config);
    }

    @SuppressWarnings("deprecation")
    private Locale getSystemLocaleLegacy(Configuration config) {
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Locale getSystemLocale(Configuration config) {
        return config.getLocales().get(0);
    }

    @SuppressWarnings("deprecation")
    public void setSystemLocaleLegacy(Configuration config, Locale locale) {
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void setSystemLocale(Configuration config, Locale locale) {
        config.setLocale(locale);
    }

    private void switchLayout(boolean isConcrete) {
        if (menu != null) {
            MenuItem showFurnaceMenuItem = menu.findItem(R.id.action_show_concrete);
            if (isConcrete) {
                showFurnaceMenuItem.setTitle(R.string.menu_main_concrete_furnace_visible);
                showFurnaceMenuItem.setIcon(R.drawable.ic_overview);
                graphConcreteImage.setVisibility(View.VISIBLE);
                graphOverviewImage.setVisibility(View.INVISIBLE);
            } else {
                showFurnaceMenuItem.setTitle(R.string.menu_main_concrete_furnace_invisible);
                showFurnaceMenuItem.setIcon(R.drawable.ic_concrete);
                graphConcreteImage.setVisibility(View.INVISIBLE);
                graphOverviewImage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateFiles() {
        if (basePath.isEmpty()) {
            statusText.setText(R.string.activity_main_message_error_empty_path);
            return;
        }
        updateGraphTask = new UpdateGraphTask(this, getApplicationContext(), auth);
        updateGraphTask.execute(defineResourcePath(graphMinDate), defineResourcePath(addDaysToDate(graphMinDate, 1)));
    }

    private void redrawGraph() {
        if (furnacesStats != null) {
            if (isConcreteGraphVisible) {
                drawGraphConcrete(concreteFurnaceIndex);
                statusText.setText(getString(R.string.asynctask_message_graph_for_concrete, concreteFurnaceIndex + 1,
                        (graphMinHour < 24) ? graphMinDate : addDaysToDate(graphMinDate, 1),
                        graphMinHour % 24, (graphMinHour + FurnacesStats.getGraphHourTimeRange()) % 24));
            } else {
                FurnacesStats.ValuesTimestamp timestamp = furnacesStats.getNearestTimeMaxTimestamp(
                        String.format(Locale.ROOT, "%02d:00:00", graphMinHour + FurnacesStats.getGraphHourTimeRange() / 2 + 1));
                drawGraphOverview(timestamp);
                if (timestamp.time.compareTo("24:00:00") < 0) {
                    statusText.setText(getString(R.string.asynctask_message_graph_for_overview,
                            graphMinDate,
                            timestamp.time,
                            (graphMinHour + FurnacesStats.getGraphHourTimeRange() / 2 + 1) % 24
                    ));
                } else {
                    statusText.setText(getString(R.string.asynctask_message_graph_for_overview,
                            addDaysToDate(graphMinDate, 1),
                            (Integer.parseInt(timestamp.time.substring(0, 2)) % 24) + timestamp.time.substring(2, 8),
                            (graphMinHour + FurnacesStats.getGraphHourTimeRange() / 2 + 1) % 24
                    ));
                }
            }
        } else {
            statusText.setText(getString(R.string.asynctask_message_graph_for_no_data,
                    (graphMinHour < 24) ? graphMinDate : addDaysToDate(graphMinDate, 1),
                    graphMinHour % 24, (graphMinHour + FurnacesStats.getGraphHourTimeRange()) % 24));
            graphConcreteImage.setImageResource(R.drawable.no_data);
            graphOverviewImage.setImageResource(R.drawable.no_data);
        }
    }

    private void drawGraphOverview(FurnacesStats.ValuesTimestamp timestamp) {
        Graph graph = new Graph(this, R.drawable.overview);
        graph.drawOverviewGraph(timestamp);
        graphOverviewImage.setImageDrawable(graph.getResultBitmapDrawable());
        overviewZoomer.update();
    }

    private void drawGraphConcrete(int furnaceIndex) {
        Graph graph = new Graph(this, R.drawable.concrete);
        graph.drawConcreteGraph(furnacesStats.getConcreteIndexAllTimeValues(furnaceIndex),
                graphMinHour, graphMinHour + FurnacesStats.getGraphHourTimeRange());
        graphConcreteImage.setImageDrawable(graph.getResultBitmapDrawable());
        concreteZoomer.update();
    }

    private void moveGraphConstraints(int step) {
        graphMinHour += step;
        if (graphMinHour < 0) {
            graphMinDate = addDaysToDate(graphMinDate, -1);
            graphMinHour = 24 + graphMinHour;
            updateFiles();
        } else if (graphMinHour > 48 - FurnacesStats.getGraphHourTimeRange()) {
            graphMinDate = addDaysToDate(graphMinDate, 1);
            graphMinHour -= 24;
            updateFiles();
        }
    }

    private void stopUpdating() {
        if (isAsyncTaskRunning) {
            updateGraphTask.cancel(true);
        } else {
            statusText.setText(getString(R.string.asynctask_message_cancelled, getCurrentTime(false)));
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
                        switchLayout(false);
                        isConcreteGraphVisible = false;
                        redrawGraph();
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
            case R.id.action_next_big_step:
                moveGraphConstraints(FurnacesStats.getGraphHourTimeBigStep());
                redrawGraph();
                return true;
            case R.id.action_next_little_step:
                moveGraphConstraints(FurnacesStats.getGraphHourTimeLittleStep());
                redrawGraph();
                return true;
            case R.id.action_prev_big_step:
                moveGraphConstraints(-FurnacesStats.getGraphHourTimeBigStep());
                redrawGraph();
                return true;
            case R.id.action_prev_little_step:
                moveGraphConstraints(-FurnacesStats.getGraphHourTimeLittleStep());
                redrawGraph();
                return true;
            case R.id.action_refresh:
                SelectDateDialog.newInstance(this).show(getSupportFragmentManager(), "DatePicker");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadSettings();
        updateFiles();
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
        statusText.setText(getString(R.string.asynctask_message_cancelled, getCurrentTime(false)));
        progressBar.setProgress(progressBar.getMax());
        progressBar.setVisibility(View.GONE);
        graphOverviewImage.setImageResource(R.drawable.dummy_overview);
        graphConcreteImage.setImageResource(R.drawable.dummy_concrete);
        isAsyncTaskRunning = false;
    }

    @Override
    public void onAsyncTaskPostExecute(FurnacesStats result, int resultCode) {
        furnacesStats = result;
        switch (resultCode) {
            case UpdateGraphTask.RESULT_OK:
                furnacesStats = result;
                redrawGraph();
                break;
            case UpdateGraphTask.RESULT_ERROR:
                graphConcreteImage.setImageResource(R.drawable.error);
                graphOverviewImage.setImageResource(R.drawable.error);
                break;
            case UpdateGraphTask.RESULT_CANCELED:
                graphConcreteImage.setImageResource(R.drawable.dummy_concrete);
                graphOverviewImage.setImageResource(R.drawable.dummy_overview);
                break;
            case UpdateGraphTask.RESULT_NO_DATA:
                statusText.setText(getString(R.string.asynctask_message_graph_for_no_data,
                        (graphMinHour < 24) ? graphMinDate : addDaysToDate(graphMinDate, 1),
                        graphMinHour % 24, (graphMinHour + FurnacesStats.getGraphHourTimeRange()) % 24));
                graphConcreteImage.setImageResource(R.drawable.no_data);
                graphOverviewImage.setImageResource(R.drawable.no_data);
                break;
        }
        progressBar.setProgress(progressBar.getMax());
        progressBar.setVisibility(View.GONE);
        isAsyncTaskRunning = false;
    }

    @Override
    public void onConcreteIndexDialogPositiveClick(int index) {
        concreteFurnaceIndex = index;
        switchLayout(true);
        isConcreteGraphVisible = true;
        redrawGraph();
    }

    @Override
    public void onConcreteIndexDialogNegativeClick() {
        switchLayout(false);
        isConcreteGraphVisible = false;
    }

    @Override
    public void onSelectDateDialogDateSet(String chosenDate) {
        graphMinDate = chosenDate;
        graphMinHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - FurnacesStats.getGraphHourTimeRange() / 2;
        updateFiles();
    }
}
