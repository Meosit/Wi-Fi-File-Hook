package by.mksn.wififilehook.task;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.logic.FurnacesStats;
import by.mksn.wififilehook.logic.ProgressResult;
import by.mksn.wififilehook.logic.exception.CsvParseException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class UpdateGraphTask extends AsyncTask<String, ProgressResult, FurnacesStats> {


    private static final int MAX_PROGRESS_RESULT = 100;
    private AsyncTaskCallback<ProgressResult, FurnacesStats> callback;
    private Context context;
    private NtlmPasswordAuthentication auth;

    public UpdateGraphTask(AsyncTaskCallback<ProgressResult, FurnacesStats> callback, Context context, NtlmPasswordAuthentication auth) {
        this.callback = callback;
        this.context = context;
        this.auth = auth;
    }

    @Override
    protected void onPreExecute() {
        callback.onAsyncTaskPreExecute();
    }

    @Override
    protected FurnacesStats doInBackground(String... strings) {
        SmbFile smbFileOne;
        SmbFile smbFileTwo;
        try {
            if (auth == null) {
                smbFileOne = new SmbFile("smb://" + strings[0]);
                smbFileTwo = new SmbFile("smb://" + strings[1]);
            } else {
                smbFileOne = new SmbFile("smb://" + strings[0], auth);
                smbFileTwo = new SmbFile("smb://" + strings[1], auth);
            }
            publishProgress(new ProgressResult(1,
                    context.getString(R.string.asynctask_message_file_opening)));
            String[] readFile = readFileContent(smbFileOne, smbFileTwo);
            if (isCancelled()) {
                return null;
            }
            publishProgress(new ProgressResult(50, context.getString(R.string.asynctask_message_parsing_file)));
            return new FurnacesStats(readFile, strings[3]);
        } catch (SmbException | MalformedURLException | CsvParseException e) {
            publishProgress(new ProgressResult(MAX_PROGRESS_RESULT,
                    context.getString(R.string.asynctask_message_error, e.getMessage())));
            return null;
        } catch (Exception e) {
            publishProgress(new ProgressResult(MAX_PROGRESS_RESULT,
                    context.getString(R.string.asynctask_message_error, "File reading error" + e.getMessage())));
            return null;
        }
    }

    private String[] readFileContent(SmbFile... sFiles) throws IOException {
        ArrayList<String> result = new ArrayList<>();
        for (SmbFile file : sFiles) {
            InputStream stream = file.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
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
        }
        return result.toArray(new String[result.size()]);
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
