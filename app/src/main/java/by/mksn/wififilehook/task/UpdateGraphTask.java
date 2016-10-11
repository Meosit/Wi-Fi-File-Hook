package by.mksn.wififilehook.task;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.logic.FurnacesStats;
import by.mksn.wififilehook.logic.ProgressResult;
import jcifs.smb.SmbFile;

public class UpdateGraphTask extends AsyncTask<String, ProgressResult, FurnacesStats> {


    private int maxProgressValue = 100;
    private AsyncTaskCallback<ProgressResult, FurnacesStats> callback;
    private Context context;

    public UpdateGraphTask(AsyncTaskCallback<ProgressResult, FurnacesStats> callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        callback.onAsyncTaskPreExecute();
    }

    @Override
    protected FurnacesStats doInBackground(String... strings) {
        SmbFile smbFile;
        try {
            smbFile = new SmbFile("smb://" + strings[0]);
            publishProgress(new ProgressResult(1,
                    context.getString(R.string.asynctask_message_file_opening)));
            String[] readFile = readFileContent(smbFile);
            if (isCancelled()) {
                return null;
            }
            publishProgress(new ProgressResult(50, context.getString(R.string.asynctask_message_parsing_file)));
            return new FurnacesStats(readFile);
        } catch (Exception e) {
            publishProgress(new ProgressResult(maxProgressValue,
                    context.getString(R.string.asynctask_message_error, e.getMessage())));
            return null;
        }
    }

    private String[] readFileContent(SmbFile sFile) throws IOException {
        InputStream stream = sFile.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        ArrayList<String> result = new ArrayList<>();
        String readLine;
        try {
            int full = stream.available();
            while ((readLine = reader.readLine()) != null) {
                result.add(readLine);
                int percent = Math.round((1 - stream.available() / full) * 100);
                publishProgress(new ProgressResult(percent, context.getString(R.string.asynctask_message_file_reading, percent)));
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
    protected void onCancelled(FurnacesStats furnacesStats) {
        callback.onAsyncTaskCancelled(furnacesStats);
    }

    @Override
    protected void onPostExecute(FurnacesStats result) {
        callback.onAsyncTaskPostExecute(result);
    }
}
