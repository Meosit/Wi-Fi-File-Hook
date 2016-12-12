package by.mksn.wififilehook.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.wefika.horizontalpicker.HorizontalPicker;

import by.mksn.wififilehook.R;
import by.mksn.wififilehook.logic.SensorsStats;

public class ConcreteIndexDialog extends DialogFragment {

    private DialogCallback callback;

    public static ConcreteIndexDialog newInstance(DialogCallback callback) {
        ConcreteIndexDialog dialog = new ConcreteIndexDialog();
        dialog.callback = callback;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_concrete_index, null);
        final HorizontalPicker indexPicker = (HorizontalPicker) rootView.findViewById(R.id.dialog_concrete_index_index);
        String[] values = new String[SensorsStats.getTemperatureSensorCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = String.valueOf(i + 1);
        }
        indexPicker.setValues(values);
        return builder.setView(rootView)
                .setPositiveButton(R.string.dialog_concrete_index_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.onConcreteIndexDialogPositiveClick(indexPicker.getSelectedItem());
                    }
                })
                .setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.onConcreteIndexDialogNegativeClick();
                    }
                })
                .create();
    }

    public interface DialogCallback {
        void onConcreteIndexDialogPositiveClick(int index);

        void onConcreteIndexDialogNegativeClick();
    }
}
