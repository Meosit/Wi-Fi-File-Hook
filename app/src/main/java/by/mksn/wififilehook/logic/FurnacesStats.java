package by.mksn.wififilehook.logic;

import android.support.annotation.NonNull;

import java.util.Arrays;

import by.mksn.wififilehook.logic.exception.CsvParseException;
import by.mksn.wififilehook.logic.exception.IllegalValueIndexException;

public final class FurnacesStats {

    private final ValuesTimestamp[] timestamps;
    private final int maxValue;
    private final int minValue;

    public FurnacesStats(String[] readFile) throws CsvParseException {
        int maxValue = Integer.MIN_VALUE;
        int minValue = Integer.MAX_VALUE;
        try {
            timestamps = new ValuesTimestamp[readFile.length];
            for (int i = 0; i < readFile.length; i++) {
                String[] strings = readFile[i].split(";");
                String time = strings[0];
                if (time.length() == 7) {
                    time = "0".concat(time);
                }
                int[] values = new int[strings.length - 1];
                for (int j = 0; j < values.length; j++) {
                    int value = Integer.parseInt(strings[j + 1]);
                    if (value <= minValue) {
                        minValue = value;
                    }
                    if (value >= maxValue) {
                        maxValue = value;
                    }
                    values[j] = value;
                }
                timestamps[i] = new ValuesTimestamp(values, time);
            }
            Arrays.sort(timestamps);
            this.maxValue = maxValue;
            this.minValue = minValue;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CsvParseException("Parse file failed: " + e.getMessage());
        }
        if (timestamps.length == 0) {
            throw new CsvParseException("Parsing file failed: empty csv file");
        }
        int constValueCount = timestamps[0].getValueCount();
        for (ValuesTimestamp timestamp : timestamps) {
            if (timestamp.getValueCount() == 0) {
                throw new CsvParseException("Parse file failed: no values for time " + timestamp.time);
            }
            if (timestamp.getValueCount() != constValueCount) {
                throw new CsvParseException("Parse file failed: not all timestamps have equal value count");
            }
        }
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getTimestampCount() {
        return timestamps.length;
    }

    public ValuesTimestamp getTimestamp(int index) {
        if (index < 0 || index >= timestamps.length) {
            throw new IllegalValueIndexException();
        }
        return timestamps[index];
    }

    public int[] getConcreteIndexAllValues(int index) {
        if (index < 0 || index >= timestamps.length) {
            throw new IllegalValueIndexException();
        }
        int[] values = new int[timestamps.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = timestamps[i].getValue(index);
        }
        return values;
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

        public int getValueCount() {
            return values.length;
        }

        @Override
        public int compareTo(@NonNull ValuesTimestamp valuesTimestamp) {
            return time.compareTo(valuesTimestamp.time);
        }

        public int[] getValues() {
            return values.clone();
        }
    }


}
