package by.mksn.wififilehook.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.logic.CsvFurnaceTemperatureTable;
import by.mksn.wififilehook.logic.ProgressResult;
import by.mksn.wififilehook.task.AsyncTaskCallback;
import by.mksn.wififilehook.task.UpdateGraphTask;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements AsyncTaskCallback<ProgressResult, CsvFurnaceTemperatureTable> {

    private static final String PREF_FILE_PATH = "file_path";

    private Menu menu;
    private boolean isShowConcreteFurnace = true;

    private UpdateGraphTask updateGraphTask;
    private CsvFurnaceTemperatureTable table;
    private String filePath;
    private boolean isAsyncTaskRunning;
    private String statusMessageSave;

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
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        statusText = (TextView) findViewById(R.id.activity_main_value_status);
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progress);

        graphOverviewImage = (ImageView) findViewById(R.id.activity_main_graph_overview);
        graphConcreteImage = (ImageView) findViewById(R.id.activity_main_graph_concrete);
        overviewZoomer = new PhotoViewAttacher(graphOverviewImage);
        concreteZoomer = new PhotoViewAttacher(graphConcreteImage);
        loadSettings();
        updateFile();
    }

    private String getSyncTime() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.ROOT, "%02d:%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND));
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(getApplicationContext());
        filePath = sharedPreferences.getString(PREF_FILE_PATH, "");
    }

    private void updateFile() {
        if (filePath.isEmpty()) {
            statusText.setText(R.string.activity_main_message_error_empty_path);
            statusMessageSave = statusText.getText().toString();
            return;
        }
        updateGraphTask = new UpdateGraphTask(this, getApplicationContext());
        updateGraphTask.execute(filePath);
    }

    private void drawGraphOverview() {
        //Create a new image bitmap and attach a brand new canvas to it
        Bitmap graphBackground = BitmapFactory.decodeResource(getResources(), R.drawable.overview);
        Bitmap tempGraph = Bitmap.createBitmap(graphBackground.getWidth(), graphBackground.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempGraph);

        //Draw the image bitmap into the canvas
        tempCanvas.drawBitmap(graphBackground, 0, 0, null);

        //Draw everything else you want into the canvas, in this example a rectangle with rounded edges
        Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setColor(0xFF00CC00);
        tempCanvas.drawCircle(100, 100, 10, myPaint);

        //Attach the canvas to the ImageView
        graphOverviewImage.setImageDrawable(new BitmapDrawable(getResources(), tempGraph));
        overviewZoomer.update();
    }

    private void stopUpdating() {
        if (isAsyncTaskRunning) {
            updateGraphTask.cancel(true);
        } else {
            statusText.setText(getString(R.string.asynctask_message_cancelled, getSyncTime()));
            statusMessageSave = statusText.getText().toString();
        }
    }

    private void updateMenuTitles() {
        MenuItem showFurnaceMenuItem = menu.findItem(R.id.action_show_concrete);
        if (isShowConcreteFurnace) {
            isShowConcreteFurnace = false;
            showFurnaceMenuItem.setTitle(R.string.menu_main_show_concrete_furnace_disable);
        } else {
            isShowConcreteFurnace = true;
            showFurnaceMenuItem.setTitle(R.string.menu_main_show_concrete_furnace_enable);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        statusText.setText(statusMessageSave);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                if (isShowConcreteFurnace) {
                    graphConcreteImage.setVisibility(View.VISIBLE);
                    graphOverviewImage.setVisibility(View.INVISIBLE);

                    //draw concrete furnace
                } else {
                    graphConcreteImage.setVisibility(View.INVISIBLE);
                    graphOverviewImage.setVisibility(View.VISIBLE);
                }
                updateMenuTitles();
                return true;
            case R.id.action_stop:
                stopUpdating();
                return true;
            case R.id.action_refresh:
                updateFile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            loadSettings();
            Toast.makeText(getApplicationContext(),
                    R.string.activity_main_message_info_settings_updated, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAsyncTaskPreExecute() {
        graphConcreteImage.setVisibility(View.INVISIBLE);
        graphOverviewImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        isShowConcreteFurnace = true;
        updateMenuTitles();
        isAsyncTaskRunning = true;
    }

    @Override
    public void onAsyncTaskProgressUpdate(ProgressResult result) {
        progressBar.setProgress(result.progressCurrent);
        statusText.setText(result.statusMessage);
        statusMessageSave = result.statusMessage;
    }

    @Override
    public void onAsyncTaskCancelled(CsvFurnaceTemperatureTable result) {
        statusText.setText(getString(R.string.asynctask_message_cancelled, getSyncTime()));
        statusMessageSave = statusText.getText().toString();
        progressBar.setProgress(progressBar.getMax());
        progressBar.setVisibility(View.GONE);
        isAsyncTaskRunning = false;
    }

    @Override
    public void onAsyncTaskPostExecute(CsvFurnaceTemperatureTable result) {
        if (result != null) {
            statusText.setText(getString(R.string.asynctask_message_sync_time, getSyncTime()));
            statusMessageSave = statusText.getText().toString();
            table = result;
            //draw table
        }
        progressBar.setProgress(progressBar.getMax());
        progressBar.setVisibility(View.GONE);
        isAsyncTaskRunning = false;
    }
}
