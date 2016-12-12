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
import by.mksn.wififilehook.logic.SensorsStats;
import by.mksn.wififilehook.logic.ProgressResult;
import by.mksn.wififilehook.logic.exception.CsvParseException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class UpdateGraphTask extends AsyncTask<String, ProgressResult, SensorsStats> {

    public static final int RESULT_OK = 0x01;
    public static final int RESULT_CANCELED = 0x02;
    public static final int RESULT_ERROR = 0x03;
    public static final int RESULT_NO_DATA = 0x04;

    private static final int MAX_PROGRESS_RESULT = 100;
    private AsyncTaskCallback<ProgressResult, SensorsStats> callback;
    private Context context;
    private NtlmPasswordAuthentication auth;
    private int resultCode = RESULT_OK;

    public UpdateGraphTask(AsyncTaskCallback<ProgressResult, SensorsStats> callback, Context context, NtlmPasswordAuthentication auth) {
        this.callback = callback;
        this.context = context;
        this.auth = auth;
    }

    @Override
    protected void onPreExecute() {
        callback.onAsyncTaskPreExecute();
    }

    @Override
    protected SensorsStats doInBackground(String... strings) {
        SmbFile smbFileOne;
        SmbFile smbFileTwo;
        try {
            if (auth == null) {
                smbFileOne = new SmbFile("smb://" + strings[0]);
            } else {
                smbFileOne = new SmbFile("smb://" + strings[0], auth);
            }

            publishProgress(new ProgressResult(1,
                    context.getString(R.string.asynctask_message_file_opening)));

            String[] readFile;
            try {
                if (auth == null) {
                    smbFileTwo = new SmbFile("smb://" + strings[1]);
                } else {
                    smbFileTwo = new SmbFile("smb://" + strings[1], auth);
                }
                readFile = readFileContent(smbFileOne, smbFileTwo);
            } catch (SmbException | MalformedURLException e) {
                readFile = readFileContent(smbFileOne);
            }

            if (isCancelled()) {
                resultCode = RESULT_CANCELED;
                return null;
            }
            publishProgress(new ProgressResult(50, context.getString(R.string.asynctask_message_parsing_file)));
            return new SensorsStats(readFile);
        } catch (SmbException | MalformedURLException | CsvParseException e) {
            publishProgress(new ProgressResult(MAX_PROGRESS_RESULT,
                    context.getString(R.string.message_error, e.getMessage())));
            if (e.getMessage().equals("The system cannot find the file specified.")) {
                resultCode = RESULT_NO_DATA;
            } else {
                resultCode = RESULT_ERROR;
            }
            return null;
        } catch (Exception e) {
            publishProgress(new ProgressResult(MAX_PROGRESS_RESULT,
                    context.getString(R.string.message_error, "File reading error: " + e.getMessage())));
            resultCode = RESULT_ERROR;
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
                        resultCode = RESULT_CANCELED;
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
    protected void onCancelled(SensorsStats sensorsStats) {
        callback.onAsyncTaskCancelled(sensorsStats);
    }

    @Override
    protected void onPostExecute(SensorsStats result) {
        callback.onAsyncTaskPostExecute(result, resultCode);
    }
}
