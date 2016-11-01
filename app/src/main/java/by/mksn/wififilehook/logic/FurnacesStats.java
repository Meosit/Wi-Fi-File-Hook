package by.mksn.wififilehook.logic;

import android.support.annotation.NonNull;

import by.mksn.wififilehook.logic.exception.CsvParseException;
import by.mksn.wififilehook.logic.exception.IllegalValueIndexException;

public final class FurnacesStats {

    public static final int GRAPH_BREAK_SECOND_RANGE = 120;
    private static int temperatureSensorCount = 31;
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
            if (timestamp.getValueCount() < temperatureSensorCount) {
                throw new CsvParseException("Parse file failed: not enough sensor count");
            }
        }
    }

    public static int getTemperatureSensorCount() {
        return temperatureSensorCount;
    }

    public static void setTemperatureSensorCount(int temperatureSensorCount) {
        if (temperatureSensorCount <= 0) {
            throw new IllegalArgumentException();
        }
        FurnacesStats.temperatureSensorCount = temperatureSensorCount;
    }

    public static int timeToSeconds(String time) {
        String[] args = time.split(":");
        if (args.length != 3) {
            throw new IllegalArgumentException("Not time string passed");
        }
        return Integer.parseInt(args[0]) * 3600 + Integer.parseInt(args[1]) * 60 + Integer.parseInt(args[2]);
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

    public TimeValue[] getConcreteIndexAllTimeValues(int index) {
        if (index < 0 || index >= timestamps.length) {
            throw new IllegalValueIndexException();
        }
        TimeValue[] values = new TimeValue[timestamps.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = new TimeValue(timestamps[i].time, timestamps[i].getValue(index));
        }
        return values;
    }

    public final class TimeValue implements Comparable<TimeValue> {

        public final String time;
        public final int value;

        public TimeValue(String time, int value) {
            this.time = time;
            this.value = value;
        }

        @Override
        public int compareTo(TimeValue timeValue) {
            return time.compareTo(timeValue.time);
        }
    }

    public final class ValuesTimestamp implements Comparable<ValuesTimestamp> {

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
