package com.dit.hp.hospitalapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.dit.hp.hospitalapp.Modals.LoginUser;
import com.dit.hp.hospitalapp.Modals.ResponsePojoGet;
import com.dit.hp.hospitalapp.Modals.SuccessResponse;
import com.dit.hp.hospitalapp.Modals.UploadObject;
import com.dit.hp.hospitalapp.Presentation.CustomDialog;
import com.dit.hp.hospitalapp.enums.TaskType;
import com.dit.hp.hospitalapp.interfaces.AsyncTaskListenerObject;
import com.dit.hp.hospitalapp.network.Generic_Async_Post;
import com.dit.hp.hospitalapp.utilities.AppStatus;
import com.dit.hp.hospitalapp.utilities.Econstants;
import com.dit.hp.hospitalapp.utilities.EncryptDecrypt;
import com.dit.hp.hospitalapp.utilities.JsonParse;
import com.dit.hp.hospitalapp.utilities.Preferences;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class LoginScreen extends AppCompatActivity implements AsyncTaskListenerObject {

    EditText username, password;
    Button signIn;
    TextView register;

    CustomDialog CD = new CustomDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions();
        }

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signIn = findViewById(R.id.signIn);


        // Login Button
        signIn.setOnClickListener(v -> {

            if (!AppStatus.getInstance(LoginScreen.this).isOnline()) {
                CD.showDialog(this, Econstants.internetNotAvailable);
                return;
            }

            if (username.getText().toString().isEmpty()) {
                CD.showDialog(this, "Please Enter Correct Username");
                return;
            }

            if (password.getText().toString().isEmpty()) {
                CD.showDialog(this, "Please Enter Correct Password");
                return;
            }

            serviceCallLogin(username.getText().toString(), password.getText().toString());

        });

//        register.setOnClickListener(view -> {
//            CD.showDialog(this, "Under Process..");
//        });


    }

    private void serviceCallLogin(String id, String pass) {
        if (AppStatus.getInstance(LoginScreen.this).isOnline()) {
            UploadObject object = new UploadObject();
            object.setUrl(Econstants.base_url);
            object.setMethordName(Econstants.loginMethod);
            object.setMasterName(null);
            object.setTasktype(TaskType.LOGIN);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", username.getText().toString());
                jsonObject.put("password", password.getText().toString());

                object.setParam(EncryptDecrypt.encrypt(jsonObject.toString()));

            } catch (Exception e) {
                Log.e("Exception: ", e.getLocalizedMessage().toString());
                e.printStackTrace();
            }

            new Generic_Async_Post(
                    LoginScreen.this,
                    LoginScreen.this,
                    TaskType.LOGIN).
                    execute(object);
        } else {
            CD.showDialog(LoginScreen.this, Econstants.internetNotAvailable);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CHANGE_NETWORK_STATE,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.VIBRATE,
                        android.Manifest.permission.SEND_SMS,
                        android.Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_MEDIA_IMAGES

                }, 0);
            }
        }


    }


    // Methods
    public static String encryptText(String plainText) {
        try {
            EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
            return encryptDecrypt.encrypt(plainText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptText(String encryptedText) {
        try {
            EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
            return encryptDecrypt.decrypt(encryptedText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onTaskCompleted(ResponsePojoGet result, TaskType taskType) throws Exception {

        // Login Task Type
        if (TaskType.LOGIN == taskType) {
            Log.i("ASYNC TASK COMPLETED", "TASK TYPE IS LOGIN.. CHECKED");
            SuccessResponse successResponse = null;

            if (result != null) {
                successResponse = JsonParse.getSuccessResponse(result.getResponse());

                String status = successResponse.getStatus();
                String responseData = successResponse.getData();

                if (status.equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {
                    Log.i("Login Response", responseData);

                    LoginUser user = JsonParse.parseLoginUser(responseData);
                    if (user != null) {
                        Log.i("LoginActivity", "User Login As: " + user.toString());

                        Preferences pref = Preferences.getInstance();
                        pref.roleId = user.getRoleId();
                        pref.roleName = user.getRoleName();
                        pref.userId = user.getUserId();
                        pref.userName = user.getUserName();
                        pref.firstName = user.getFirstName();
                        pref.lastName = user.getLastNmae();
                        pref.mobileNumber = user.getMobileNumber();
                        pref.isLoggedIn = true;
                        pref.savePreferences(this);

                        Intent loginIntent = new Intent(LoginScreen.this, Homescreen.class);
                        startActivity(loginIntent);
                        finish();
                    } else {
                        CD.showDialog(this, "Something went wrong. Try again!");
                    }

                } else if (status.equalsIgnoreCase(String.valueOf(204))) {
                    // Password doesn't match case (204)
                    Log.i("Login Response", responseData);
                    CD.showDialog(this, "Enter Correct Password");

                } else if (status.equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_INTERNAL_ERROR))) {
                    // 500 Error
                    Log.i("Login Response", responseData);
                    CD.showDialog(this, "Enter correct username.");

                } else if (status.equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_GONE))) {
                    // 410 Gone
                    CD.showDialog(this, "Please enter correct username and password");

                } else {
                    CD.showDialog(this, "Something went wrong. Check your connection.");
                }

            } else {
                Log.i("LoginHRTC", "Response is null");
                CD.showDialog(this, "Something went wrong. Check your connection.");
            }
        }


    }
}


