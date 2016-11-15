package by.mksn.wififilehook.logic;

import android.support.annotation.NonNull;

import by.mksn.wififilehook.logic.exception.CsvParseException;
import by.mksn.wififilehook.logic.exception.IllegalValueIndexException;

public final class FurnacesStats {

    private static int graphHourTimeRange = 10;
    private static int graphHourTimeBigStep = 8;
    private static int graphHourTimeLittleStep = 1;
    private static int graphBreakSecondRange = 120;
    private static int temperatureSensorCount = 31;
    private final ValuesTimestamp[] timestamps;

    public FurnacesStats(String[] readFile) throws CsvParseException {
        try {
            String prevTime = "00:00:00";
            timestamps = new ValuesTimestamp[readFile.length];
            for (int i = 0; i < readFile.length; i++) {
                String[] strings = readFile[i].split(";");
                String time = strings[0];
                if (time.length() == 7) {
                    time = "0".concat(time);
                }
                if (prevTime.compareTo(time) > 0) {
                    time = (Integer.parseInt(time.substring(0, 2)) + 24) + time.substring(2, 8);
                }
                prevTime = time;
                int[] values = new int[strings.length - 1];
                for (int j = 0; j < values.length; j++) {
                    int value = Integer.parseInt(strings[j + 1]);
                    values[j] = value;
                }
                timestamps[i] = new ValuesTimestamp(values, time);
            }
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

    public static int getGraphBreakSecondRange() {
        return graphBreakSecondRange;
    }

    public static void setGraphBreakSecondRange(int graphBreakSecondRange) {
        if (graphBreakSecondRange < 0) {
            throw new IllegalArgumentException();
        }
        FurnacesStats.graphBreakSecondRange = graphBreakSecondRange;
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

    public static int getGraphHourTimeRange() {
        return graphHourTimeRange;
    }

    public static void setGraphHourTimeRange(int range) {
        if (range < 2 || range > 48) {
            throw new IllegalArgumentException();
        }
        FurnacesStats.graphHourTimeRange = range;
    }

    public static int getGraphHourTimeBigStep() {
        return graphHourTimeBigStep;
    }

    public static void setGraphHourTimeBigStep(int step) {
        if (step < 1 || step > 24) {
            throw new IllegalArgumentException();
        }
        FurnacesStats.graphHourTimeBigStep = step;
    }

    public static int getGraphHourTimeLittleStep() {
        return graphHourTimeLittleStep;
    }

    public static void setGraphHourTimeLittleStep(int step) {
        if (step < 1 || step > 24) {
            throw new IllegalArgumentException();
        }
        FurnacesStats.graphHourTimeLittleStep = step;
    }

    public static int timeToSeconds(String time) {
        String[] args = time.split(":");
        if (args.length != 3) {
            throw new IllegalArgumentException("Not time string passed");
        }
        return Integer.parseInt(args[0]) * 3600 + Integer.parseInt(args[1]) * 60 + Integer.parseInt(args[2]);
    }

    public int getTimestampIndexByTime(String time) {

        int index = Integer.MIN_VALUE;
        for (int i = 1; i < timestamps.length; i++) {
            if ((timestamps[i].time.compareTo(time) >= 0)
                    && (timestamps[i - 1].time.compareTo(time) <= 0)) {
                index = i;
                break;
            }
        }
        return index;
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

    public ValuesTimestamp getNearestTimeMaxTimestamp(String time) {
        for (int i = timestamps.length - 1; i >= 0; i--) {
            if (timestamps[i].time.compareTo(time) <= 0) {
                return timestamps[i];
            }
        }
        return timestamps[0];
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
        public int compareTo(@NonNull TimeValue timeValue) {
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
