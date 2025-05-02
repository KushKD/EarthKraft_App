package com.dit.hp.hospitalapp.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.dit.hp.hospitalapp.Modals.ResponsePojoGet;
import com.dit.hp.hospitalapp.Modals.UploadObject;
import com.dit.hp.hospitalapp.enums.TaskType;
import com.dit.hp.hospitalapp.interfaces.AsyncTaskListenerObject;

public class Generic_Async_Post extends AsyncTask<UploadObject,Void , ResponsePojoGet> {

    ProgressDialog dialog;
    Context context;
    AsyncTaskListenerObject taskListener_;
    TaskType taskType;
    private ProgressDialog mProgressDialog;

    public Generic_Async_Post(Context context, AsyncTaskListenerObject taskListener, TaskType taskType){
        this.context = context;
        this.taskListener_ = taskListener;
        this.taskType = taskType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ProgressDialog.show(context, "Loading", "Connecting to Server .. Please Wait", true);
        dialog.setCancelable(false);
    }

    @Override
    protected ResponsePojoGet doInBackground(UploadObject... uploadObjects) {
        UploadObject data = null;
        data = uploadObjects[0];
        HttpManager http_manager = null;
        ResponsePojoGet Data_From_Server = null;
        boolean save = false;

        try{
            http_manager = new HttpManager();

            if(TaskType.LOGIN.toString().equalsIgnoreCase(data.getTasktype().toString())){
                Data_From_Server = http_manager.LoginPostData(data);
                return Data_From_Server;
            }

            // Save Patient
            else if(TaskType.SAVE_PATIENT.toString().equalsIgnoreCase(data.getTasktype().toString())){
                Data_From_Server = http_manager.PostData(data);
                return Data_From_Server;
            }

            else if(TaskType.LAB_REPORTS.toString().equalsIgnoreCase(data.getTasktype().toString())){
                Data_From_Server = http_manager.PostData(data);
                return Data_From_Server;
            }


        }catch(Exception e){
            return Data_From_Server;
        }


        return Data_From_Server;
    }

    @Override
    protected void onPostExecute(ResponsePojoGet result) {
        super.onPostExecute(result);

        try {
            taskListener_.onTaskCompleted(result, taskType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.dismiss();
    }
}
