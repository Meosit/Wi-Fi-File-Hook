package by.mksn.wififilehook.task;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.logic.CsvFurnaceTemperatureTable;
import by.mksn.wififilehook.logic.ProgressResult;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class UpdateGraphTask extends AsyncTask<String, ProgressResult, CsvFurnaceTemperatureTable> {


    private int maxProgressValue = 100;
    private AsyncTaskCallback<ProgressResult, CsvFurnaceTemperatureTable> callback;
    private Context context;

    public UpdateGraphTask(AsyncTaskCallback<ProgressResult, CsvFurnaceTemperatureTable> callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        callback.onAsyncTaskPreExecute();
    }

    @Override
    protected CsvFurnaceTemperatureTable doInBackground(String... strings) {
        SmbFile smbFile;
        try {
            smbFile = new SmbFile("smb://" + strings[0]);
            publishProgress(new ProgressResult(1,
                    context.getString(R.string.asynctask_message_file_opened)));
            String[] readFile = readFileContent(smbFile);
            if (isCancelled()) {
                return null;
            }
            publishProgress(new ProgressResult(50, context.getString(R.string.asynctask_message_parsing_file)));
            return new CsvFurnaceTemperatureTable(readFile);
        } catch (Exception e) {
            publishProgress(new ProgressResult(maxProgressValue,
                    context.getString(R.string.asynctask_message_error, e.getMessage())));
            return null;
        }
    }

    private String[] readFileContent(SmbFile sFile) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(sFile)));
        ArrayList<String> result = new ArrayList<>();
        String readLine;
        try {
            while ((readLine = reader.readLine()) != null) {
                result.add(readLine);
                if (isCancelled()) {
                    return null;
                }
            }
        } finally {
            reader.close();
        }
        return (String[]) result.toArray();
    }

    @Override
    protected void onProgressUpdate(ProgressResult... values) {
        callback.onAsyncTaskProgressUpdate(values[0]);
    }

    @Override
    protected void onCancelled(CsvFurnaceTemperatureTable csvFurnaceTemperatureTable) {
        callback.onAsyncTaskCancelled(csvFurnaceTemperatureTable);
    }

    @Override
    protected void onPostExecute(CsvFurnaceTemperatureTable result) {
        callback.onAsyncTaskPostExecute(result);
    }
}
