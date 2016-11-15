package by.mksn.wififilehook.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Locale;

import by.mksn.wififilehook.R;

public class SelectDateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private DialogCallback callback;

    public static SelectDateDialog newInstance(DialogCallback callback) {
        SelectDateDialog dateFragment = new SelectDateDialog();
        dateFragment.setCallback(callback);
        return dateFragment;
    }

    public void setCallback(DialogCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), this, yy, mm, dd);
        pickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.dialog_datepicker_positive), pickerDialog);
        pickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.dialog_negative), pickerDialog);

        return pickerDialog;
    }

    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        callback.onSelectDateDialogDateSet(String.format(Locale.ROOT, "%02d.%02d.%04d", dd, mm + 1, yy));
    }

    public interface DialogCallback {

        void onSelectDateDialogDateSet(String chosenDate);

    }

}
