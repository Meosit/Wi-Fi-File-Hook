package by.mksn.wififilehook.logic;

import by.mksn.wififilehook.logic.exception.CsvParseException;
import by.mksn.wififilehook.logic.exception.IllegalFurnaceIndexException;

public class CsvFurnaceTemperatureTable {

    private String[] timeValues;
    private int[][] temperatureValues;

    public CsvFurnaceTemperatureTable(String[] readFile) throws CsvParseException {
        try {
            timeValues = new String[readFile.length];
            temperatureValues = new int[readFile.length][];

            for (int i = 0; i < readFile.length; i++) {
                String[] values = readFile[i].split(";");
                timeValues[i] = values[0];
                temperatureValues[i] = new int[readFile.length - 1];
                for (int j = 0; i < temperatureValues[i].length; j++) {
                    temperatureValues[i][j] = Integer.parseInt(values[j + 1]);
                }
            }
        } catch (Exception e) {
            throw new CsvParseException("Parsing failed.", e);
        }

        if (timeValues.length == 0) {
            throw new CsvParseException("Empty csv file");
        }
        if (temperatureValues[0].length == 0) {
            throw new CsvParseException("There is no temperature values");
        }
    }

    public int getFurnaceCount() {
        return temperatureValues[0].length;
    }

    public TemperatureStamp[] getConcreteTemperatures(int furnaceIndex) throws IllegalFurnaceIndexException {
        if (0 < furnaceIndex || furnaceIndex >= temperatureValues[0].length) {
            throw new IllegalFurnaceIndexException();
        }
        TemperatureStamp[] temperatureStamps = new TemperatureStamp[timeValues.length];
        for (int i = 0; i < timeValues.length; i++) {
            temperatureStamps[i] = new TemperatureStamp(temperatureValues[i][furnaceIndex], timeValues[i]);
        }
        return temperatureStamps;
    }

    public int[] getLastOverviewTemperatures() {
        return temperatureValues[temperatureValues.length - 1];
    }

    public static class TemperatureStamp {

        public final int temperature;
        public final String time;

        public TemperatureStamp(int temperature, String time) {
            this.temperature = temperature;
            this.time = time;
        }
    }


}
