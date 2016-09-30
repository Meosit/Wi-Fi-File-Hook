package by.mksn.wififilehook.task;

public interface AsyncTaskCallback<Progress, Result> {

    void onAsyncTaskPreExecute();

    void onAsyncTaskProgressUpdate(Progress result);

    void onAsyncTaskCancelled(Result result);

    void onAsyncTaskPostExecute(Result result);

}
