package by.mksn.wififilehook.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class ReadTextSmbTask extends AsyncTask<String, String, String> {

    private TextView syncStatus;
    private TextView fileView;
    private long syncTime;
    private Context context;

    public ReadTextSmbTask(TextView syncStatus,TextView textView, Context context, long syncTime) {
        this.syncStatus = syncStatus;
        fileView = textView;
        this.syncTime = syncTime;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        SmbFile smbFile;
        try {
            smbFile = new SmbFile("smb://" + strings[0]);
            StringBuilder content = new StringBuilder();
            readFileContent(smbFile, content);
            return content.toString();
        } catch (Exception e) {
            return "Cannot find or open or read file now. \nCaused by: \n" + e.getClass().getSimpleName() + "(" + e.getMessage() + ")";
        }
    }

    private StringBuilder readFileContent(SmbFile sFile, StringBuilder builder) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(sFile)));
        String lineReader;
        try {
            while ((lineReader = reader.readLine()) != null) {
                builder.append(">").append(lineReader).append("\n");
                if (isCancelled()) {
                    break;
                }
            }
            publishProgress("Updated");
        } finally {
            reader.close();
        }
        return builder;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        //fileView.setText(values[0]);
        if (syncTime >= 10000) {
            Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostExecute(String string) {
        fileView.setText(string);
        Calendar c = Calendar.getInstance();
        String time = String.format(Locale.ROOT, "%02d:%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND));
        time = "Synchronization time: " + time;
        syncStatus.setText(time);
    }
}
