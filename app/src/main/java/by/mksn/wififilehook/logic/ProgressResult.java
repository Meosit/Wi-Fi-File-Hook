package by.mksn.wififilehook.logic;

public class ProgressResult {

    public final int progressCurrent;
    public final String statusMessage;

    public ProgressResult(int progressCurrent, String statusMessage) {
        this.progressCurrent = progressCurrent;
        this.statusMessage = statusMessage;
    }
}
