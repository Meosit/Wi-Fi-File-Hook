package by.mksn.wififilehook.logic;

import android.support.annotation.NonNull;

import java.util.Arrays;

import by.mksn.wififilehook.logic.exception.CsvParseException;
import by.mksn.wififilehook.logic.exception.IllegalValueIndexException;

public class FurnacesStats {

    private ValuesTimestamp[] timestamps;
    private int maxValue;
    private int minValue;

    public FurnacesStats(String[] readFile) throws CsvParseException {
        try {
            int maxValue = Integer.MIN_VALUE;
            int minValue = Integer.MAX_VALUE;
            timestamps = new ValuesTimestamp[readFile.length];
            for (int i = 0; i < readFile.length; i++) {
                String[] strings = readFile[i].split(";");
                String time = strings[0];
                int[] values = new int[readFile.length - 1];
                for (int j = 0; i < values.length; j++) {
                    int value = Integer.parseInt(strings[j + 1]);
                    if (value < minValue) {
                        minValue = value;
                    }
                    if (value > maxValue) {
                        maxValue = value;
                    }
                    values[j] = value;
                }
                if (values.length == 0) {
                    throw new Exception("no values for time " + time);
                }
                timestamps[i] = new ValuesTimestamp(values, time);
            }
            Arrays.sort(timestamps);
            this.maxValue = maxValue;
            this.minValue = minValue;
        } catch (Exception e) {
            throw new CsvParseException("Parsing file failed: " + e.getMessage());
        }
        if (timestamps.length == 0) {
            throw new CsvParseException("Parsing file failed: empty csv file");
        }
    }

    public class ValuesTimestamp implements Comparable<ValuesTimestamp> {

        public final String time;
        private final int values[];

        ValuesTimestamp(int[] values, String time) {
            this.values = values;
            this.time = time;
        }

        public int getValue(int index) {
            if (index < 0 || index >= values.length) {
                throw new IllegalValueIndexException();
            }
            return values[index];
        }

        @Override
        public int compareTo(@NonNull ValuesTimestamp valuesTimestamp) {
            return time.compareTo(valuesTimestamp.time);
        }
    }


}
