package by.mksn.wififilehook.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.custom.ZoomableImageView;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class ReadImageSmbTask extends AsyncTask<String, String, String> {

    private TextView syncStatus;
    private ImageView fileView;
    private long syncTime;
    private Context context;
    private final ScrollView textContainer;
    private final ScrollView imageContainer;
    private final TextView console;
    private byte[] result;

    public ReadImageSmbTask(TextView syncStatus, ImageView imageView, Context context, long syncTime, ScrollView textContainer, ScrollView imageContainer, TextView console) {
        this.syncStatus = syncStatus;
        fileView = imageView;
        this.syncTime = syncTime;
        this.context = context;
        this.textContainer = textContainer;
        this.imageContainer = imageContainer;
        this.console = console;
    }

    @Override
    protected String doInBackground(String... strings) {
        SmbFile smbFile;
        try {
            smbFile = new SmbFile("smb://" + strings[0]);
            readFileContent(smbFile);
            return null;
        } catch (Exception e) {
            return "Cannot find or open or read file now. \nCaused by: \n" + e.getClass().getSimpleName() + "(" + e.getMessage() + ")";
        }
    }

    private void readFileContent(SmbFile sFile) throws IOException {
        SmbFileInputStream stream = new SmbFileInputStream(sFile);
        try {
            result = new byte[(int) sFile.length()];
            int offset = 0;

            while (stream.read(result, offset, 1000) != -1) {
                offset += 1000;
                if (isCancelled()) {
                    break;
                }
            }
            publishProgress("Updated");
        } finally {
            stream.close();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        //fileView.setText(values[0]);
        if (syncTime >= 20000) {
            Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostExecute(String string) {
        if (string != null) {
            imageContainer.setVisibility(View.INVISIBLE);
            textContainer.setVisibility(View.VISIBLE);
            console.setText(string);
        } else {
            imageContainer.setVisibility(View.VISIBLE);
            textContainer.setVisibility(View.INVISIBLE);
            if (result != null) {
                Bitmap bm = BitmapFactory.decodeByteArray(result, 0, result.length);
                fileView.setImageBitmap(bm);
            }
        }
        Calendar c = Calendar.getInstance();
        String time = String.format(Locale.ROOT, "%02d:%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND));
        time = "Synchronization time: " + time;
        syncStatus.setText(time);
    }
}