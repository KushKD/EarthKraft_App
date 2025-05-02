package com.dit.hp.hospitalapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dit.hp.hospitalapp.Adapter.GenderSpinnerAdapter;
import com.dit.hp.hospitalapp.Adapter.ReferredBySpinnerAdapter;
import com.dit.hp.hospitalapp.Adapter.RegistrationModesSpinnerAdapter;
import com.dit.hp.hospitalapp.Adapter.TestSelectionAdapter;
import com.dit.hp.hospitalapp.Modals.GenderPojo;
import com.dit.hp.hospitalapp.Modals.PatientRecord;
import com.dit.hp.hospitalapp.Modals.ReferredBy;
import com.dit.hp.hospitalapp.Modals.RegistrationMode;
import com.dit.hp.hospitalapp.Modals.ResponsePojoGet;
import com.dit.hp.hospitalapp.Modals.SuccessResponse;
import com.dit.hp.hospitalapp.Modals.TestsPojo;
import com.dit.hp.hospitalapp.Modals.UploadObject;
import com.dit.hp.hospitalapp.Presentation.CustomDialog;
import com.dit.hp.hospitalapp.enums.TaskType;
import com.dit.hp.hospitalapp.interfaces.AsyncTaskListenerObject;
import com.dit.hp.hospitalapp.interfaces.OnTestSelectedListener;
import com.dit.hp.hospitalapp.network.Generic_Async_Post;
import com.dit.hp.hospitalapp.network.Generic_Async_Get;
import com.dit.hp.hospitalapp.utilities.AppStatus;
import com.dit.hp.hospitalapp.utilities.Econstants;
import com.dit.hp.hospitalapp.utilities.EncryptDecrypt;
import com.dit.hp.hospitalapp.utilities.JsonParse;
import com.dit.hp.hospitalapp.utilities.Preferences;
import com.dit.hp.hospitalapp.utilities.SamplePresenter;
import com.doi.spinnersearchable.SearchableSpinner;
import com.kushkumardhawan.locationmanager.base.LocationBaseActivity;
import com.kushkumardhawan.locationmanager.configuration.Configurations;
import com.kushkumardhawan.locationmanager.configuration.LocationConfiguration;
import com.kushkumardhawan.locationmanager.constants.FailType;
import com.kushkumardhawan.locationmanager.constants.ProcessType;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class RegisterNewPatient extends LocationBaseActivity implements SamplePresenter.SampleView, OnTestSelectedListener, AsyncTaskListenerObject {

    CustomDialog CD = new CustomDialog();
    LinearLayout receiptNoLayout;
    Button proceedBtn, backBtn;
    EditText firstName, lastName, age, recordDate, mobileNumber, amount, receiptNumber, testTV;

    // Spinners + Adapters
    SearchableSpinner genderSpinner, registrationModeSpinner, referredBySpinner;
    GenderSpinnerAdapter genderSpinnerAdapter;
    RegistrationModesSpinnerAdapter registrationModesSpinnerAdapter;
    ReferredBySpinnerAdapter referredBySpinnerAdapter;

    // Spinner Selected Items
    GenderPojo selectedGender;
    RegistrationMode selectedRegistrationMode;
    ReferredBy selectedReferredBy;

    List<TestsPojo> finalSelectionTests = new ArrayList<>();
    String GLOBAL_LOCATION_STR;
    private ProgressDialog progressDialog;

    // For Camera Image / Image Picked
    private String[] list;
    private File actualImage;
    private File compressedImage = null;

    // Global Current Dates to Preselect + Load with current dates
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private SamplePresenter samplePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_patient);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions();
        }

        samplePresenter = new SamplePresenter(this);

        // Request Permission & Get Location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Get Location and Print Location
            getLocation();
            if (GLOBAL_LOCATION_STR != null) {
                Log.d("Location", "Location: " + GLOBAL_LOCATION_STR);
            }
        }

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        age = findViewById(R.id.age);
        recordDate = findViewById(R.id.recordDate);
        mobileNumber = findViewById(R.id.mobileNumber);
        amount = findViewById(R.id.amount);
        testTV = findViewById(R.id.testTV);

        proceedBtn = findViewById(R.id.proceedBtn);
        backBtn = findViewById(R.id.backBtn);

        receiptNoLayout = findViewById(R.id.receiptNoLayout);
        receiptNumber = findViewById(R.id.receiptNumber);

        genderSpinner = findViewById(R.id.genderSpinner);
        registrationModeSpinner = findViewById(R.id.registrationMode);
        referredBySpinner = findViewById(R.id.referredBySpinner);


        // Format and Set Default Date
        String formattedDate = String.format("%02d-%02d-%04d", day, month + 1, year);
        recordDate.setText(formattedDate);
        recordDate.setEnabled(false);

//        // Date Picker For Test Date
//        recordDate.setOnClickListener(v -> {
//            DatePickerDialog datePickerDialog = new DatePickerDialog(
//                    RegisterNewPatient.this,
//                    (view, selectedYear, selectedMonth, selectedDay) -> {
//                        // Format and display the selected date
//                        String selectedFormattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear);
//                        recordDate.setText(selectedFormattedDate);
//                    },
//                    year, month, day
//            );
//
//            datePickerDialog.show();
//        });


//       #################################### SERVICE CALLS ########################################
        // GET_GENDERS
        try {
            if (AppStatus.getInstance(RegisterNewPatient.this).isOnline()) {
                UploadObject object = new UploadObject();
                object.setUrl(Econstants.base_url);
                object.setMethordName(Econstants.genderMethod);
                object.setTasktype(TaskType.GET_GENDERS);

                new Generic_Async_Get(
                        RegisterNewPatient.this,
                        RegisterNewPatient.this,
                        TaskType.GET_GENDERS).
                        execute(object);
            } else {
                CD.showDialog(RegisterNewPatient.this, Econstants.internetNotAvailable);
            }

        } catch (Exception ex) {
            CD.showDialog(RegisterNewPatient.this, "Something Bad happened . Please reinstall the application and try again.");
        }

        // GET_REGISTRATION_MODES
        try {
            if (AppStatus.getInstance(RegisterNewPatient.this).isOnline()) {
                UploadObject object = new UploadObject();
                object.setUrl(Econstants.base_url);
                object.setMethordName(Econstants.registrationModesMethod);
                object.setTasktype(TaskType.GET_REGISTRATION_MODES);

                new Generic_Async_Get(
                        RegisterNewPatient.this,
                        RegisterNewPatient.this,
                        TaskType.GET_REGISTRATION_MODES).
                        execute(object);
            } else {
                CD.showDialog(RegisterNewPatient.this, Econstants.internetNotAvailable);
            }

        } catch (Exception ex) {
            CD.showDialog(RegisterNewPatient.this, "Something Bad happened . Please reinstall the application and try again.");
        }

        // GET_REFERRED_BY
        try {
            if (AppStatus.getInstance(RegisterNewPatient.this).isOnline()) {
                UploadObject object = new UploadObject();
                object.setUrl(Econstants.base_url);
                object.setMethordName(Econstants.getReferredByMethod);
                object.setTasktype(TaskType.GET_REFERRED_BY);

                new Generic_Async_Get(
                        RegisterNewPatient.this,
                        RegisterNewPatient.this,
                        TaskType.GET_REFERRED_BY).
                        execute(object);
            } else {
                CD.showDialog(RegisterNewPatient.this, Econstants.internetNotAvailable);
            }

        } catch (Exception ex) {
            CD.showDialog(RegisterNewPatient.this, "Something Bad happened . Please reinstall the application and try again.");
        }

//        ##########################################################################################


        // Spinners: Gender, Registration Mode, Referred By
        registrationModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRegistrationMode = (RegistrationMode) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = (GenderPojo) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        referredBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedReferredBy = (ReferredBy) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        testTV.setOnClickListener(view -> {
            makeTestsServiceCall();
        });

        backBtn.setOnClickListener(v -> {
            showExitConfirmationDialog();
        });

        proceedBtn.setOnClickListener(v -> {

            // Print Location
            if (GLOBAL_LOCATION_STR != null) {
                Log.d("Location", "Location: " + GLOBAL_LOCATION_STR);
            } else {
                Log.d("Location", "Location Not Found");
            }


            if (firstName.getText().toString().isEmpty()) {
                CD.showDialog(RegisterNewPatient.this, "Please Enter First Name");
                return;
            }

            if (lastName.getText().toString().isEmpty()) {
                CD.showDialog(RegisterNewPatient.this, "Please Enter Last Name");
                return;
            }

            if (selectedGender == null) {
                CD.showDialog(RegisterNewPatient.this, "Please Select Gender");
                return;
            }

            if (age.getText().toString().isEmpty()) {
                CD.showDialog(RegisterNewPatient.this, "Please Enter Age");
                return;
            }

            if (mobileNumber.getText().toString().isEmpty()) {
                CD.showDialog(RegisterNewPatient.this, "Please Enter Mobile Number");
                return;
            }

            if (finalSelectionTests == null || finalSelectionTests.isEmpty()) {
                CD.showDialog(RegisterNewPatient.this, "Please Select Tests");
                return;
            }

            if (selectedRegistrationMode == null) {
                CD.showDialog(RegisterNewPatient.this, "Please Select Registration Mode");
                return;
            }

            if (amount.getText().toString().isEmpty()) {
                CD.showDialog(RegisterNewPatient.this, "Please Select Tests to Load Amount");
                return;
            }

            if (selectedReferredBy == null) {
                CD.showDialog(RegisterNewPatient.this, "Please Select Referred By");
                return;
            }

//            if (receiptNumber.getText().toString().isEmpty()) {
//                CD.showDialog(RegisterNewPatient.this, "Please Enter Receipt Number");
//                return;
//            }

            showAddConfirmationDialog();

        });

    }


    // Custom Method Here
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CHANGE_NETWORK_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_MEDIA_IMAGES

            }, 0);
        }

    }


    private void makeTestsServiceCall() {
        try {
            if (AppStatus.getInstance(RegisterNewPatient.this).isOnline()) {
                UploadObject object = new UploadObject();
                object.setUrl(Econstants.base_url);
                object.setMethordName(Econstants.getTests);
                object.setTasktype(TaskType.GET_TESTS);

                new Generic_Async_Get(
                        RegisterNewPatient.this,
                        RegisterNewPatient.this,
                        TaskType.GET_TESTS).
                        execute(object);
            } else {
                CD.showDialog(RegisterNewPatient.this, Econstants.internetNotAvailable);
            }

        } catch (Exception ex) {
            CD.showDialog(RegisterNewPatient.this, "Something Bad happened . Please reinstall the application and try again.");
        }
    }

    // Show Tests List Picker
    private void showTestsListsDialog(List<TestsPojo> fetchedTestList, List<TestsPojo> preSelectedTests, OnTestSelectedListener onTestSelectedListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Tests");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_test_list, null);
        builder.setView(dialogView);

        EditText searchBar = dialogView.findViewById(R.id.searchBar);
        ListView listView = dialogView.findViewById(R.id.listView);
        Button btnSelect = dialogView.findViewById(R.id.btnSelect);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnClear = dialogView.findViewById(R.id.clear);

        // Convert preSelectedTests to a list of preSelectedTestIds
        List<Integer> preSelectedTestIds = new ArrayList<>();
        for (TestsPojo test : preSelectedTests) {
            preSelectedTestIds.add(test.getTestId());
        }

        // Adapter Setup
        TestSelectionAdapter adapter = new TestSelectionAdapter(this, fetchedTestList, preSelectedTestIds);
        listView.setAdapter(adapter);

        // Item Click to Toggle Selection
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            adapter.toggleSelectionById(adapter.getItem(i).getTestId());
            adapter.notifyDataSetChanged();
        });

        // Search Functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Button Actions
        btnSelect.setOnClickListener(v -> {
            onTestSelectedListener.onTestSelected(adapter.getSelectedTests());
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnClear.setOnClickListener(v -> {
            adapter.clearSelection();
            adapter.notifyDataSetChanged();
            searchBar.setText("");
        });
    }

    // Exit confirmation dialog
    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit? All progress made will be lost.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RegisterNewPatient.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog, do nothing
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Register Patient")
                .setMessage("Are you sure you want to register this patient?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    PatientRecord patientRecord = new PatientRecord();
                    patientRecord.setFirstName(firstName.getText().toString());
                    patientRecord.setLastName(lastName.getText().toString());
                    patientRecord.setPatientAge(age.getText().toString());
                    patientRecord.setPatientMobile(mobileNumber.getText().toString());
                    patientRecord.setSampleDate(recordDate.getText().toString());

                    if (Econstants.isNotEmpty(receiptNumber.getText().toString())) {
                        patientRecord.setReceiptNumber(receiptNumber.getText().toString().trim());
                    } else {
                        patientRecord.setReceiptNumber(""); // or null, if preferred
                    }

                    patientRecord.setGender(String.valueOf(selectedGender.getGenderId()));
                    patientRecord.setReferredBy(String.valueOf(selectedReferredBy.getReferredBYId()));
                    patientRecord.setRegistrationMode(String.valueOf(selectedRegistrationMode.getRegisrationModeId()));
                    patientRecord.setTestList(finalSelectionTests);

                    // Make Service Call to Save Patient
                    savePatientServiceCall(patientRecord.getJsonToSave().toString());

                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void savePatientServiceCall(String jsonToSave) {
        if (AppStatus.getInstance(RegisterNewPatient.this).isOnline()) {

            String encryptedBody = null;
            UploadObject uploadObject = new UploadObject();
            try {
                uploadObject.setUrl(Econstants.base_url);
                uploadObject.setMethordName(Econstants.savePatient);
                uploadObject.setTasktype(TaskType.SAVE_PATIENT);

                Log.i("JSON To Save", "JSON Dec: " + jsonToSave);

                encryptedBody = EncryptDecrypt.encrypt(jsonToSave);
                Log.i("JSON To Save", "JSON Enc: " + encryptedBody.toString());

            } catch (Exception e) {
                Log.e("Any Exception: ", e.getMessage());
            }

            uploadObject.setParam(encryptedBody);

            Log.e("Object: ", "Object: " + uploadObject.getUrl() + uploadObject.getMethordName() + uploadObject.getMasterName() + " BODY: " + uploadObject.getParam());

            new Generic_Async_Post(RegisterNewPatient.this, RegisterNewPatient.this, TaskType.SAVE_PATIENT).execute(uploadObject);

        } else {
            CD.showDialog(RegisterNewPatient.this, "Internet not Available. Please Connect to the Internet and try again.");
        }
    }


    // Back Swipe
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }


    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();  // Permission granted, fetch location
            } else {
                Log.d("Location", "Permission denied");
            }
        }
    }


    // Return the list of selected Tests.. Interface Method
    @Override
    public void onTestSelected(List<TestsPojo> selectedTests) {
        finalSelectionTests = selectedTests;

        StringBuilder selectedText = new StringBuilder();
        for (TestsPojo test : selectedTests) {
            selectedText.append(test.getTestName())
                    .append(" : Rs.").append(test.getTestprice())
                    .append("\n");
        }
        testTV.setText(selectedText.toString());

        // Convert testprice from String to double and sum
        double totalAmount = selectedTests.stream()
                .mapToDouble(test -> {
                    try {
                        return Double.parseDouble(test.getTestprice()); // Convert to double
                    } catch (NumberFormatException e) {
                        return 0.0; // Default to 0 if invalid
                    }
                })
                .sum();

        amount.setText("Rs. " + totalAmount);
        amount.setEnabled(false);
    }


    /**
     * Location Interface Methords
     *
     * @return
     */
    @Override
    public LocationConfiguration getLocationConfiguration() {
        return Configurations.defaultConfiguration("Permission Required !", "GPS needs to be turned on.");
    }

    @Override
    public void onLocationChanged(Location location) {
        samplePresenter.onLocationChanged(location);
    }

    @Override
    public void onLocationFailed(@FailType int type) {
        samplePresenter.onLocationFailed(type);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getLocationManager().isWaitingForLocation()
                && !getLocationManager().isAnyDialogShowing()) {
            displayProgress();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        dismissProgress();
    }

    private void displayProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.getWindow().addFlags(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage("Getting location...");
        }

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    public String getText() {
        return "";  //locationText.getText().toString()
    }

    @Override
    public void setText(String text) {
        GLOBAL_LOCATION_STR = text; // Set location
        Log.e("Location GPS", text);
    }

    @Override
    public void updateProgress(String text) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(text);
        }
    }

    @Override
    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        samplePresenter.onProcessTypeChanged(processType);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        samplePresenter.destroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTaskCompleted(ResponsePojoGet result, TaskType taskType) throws Exception {

        // GET_GENDERS
        if (TaskType.GET_GENDERS == taskType) {
            Intent intent = getIntent();
            SuccessResponse response = null;
            System.out.println(result.toString());

            if (result.getResponseCode().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {
                response = JsonParse.getSuccessResponse(result.getResponse());

                if (response.getStatus().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {

                    Log.e("Response.getData is ", response.getData());
                    List<GenderPojo> genderPojoList = JsonParse.parseGender(response.getData());

                    if (genderPojoList != null && genderPojoList.size() > 0) {
                        genderSpinnerAdapter = new GenderSpinnerAdapter(this, android.R.layout.simple_spinner_item, genderPojoList);
                        genderSpinner.setAdapter(genderSpinnerAdapter);

                    } else {
                        CD.showDialog(RegisterNewPatient.this, "No Genders Found!");
                    }

                } else {
                    CD.showDialog(RegisterNewPatient.this, "No Data Found");
                }

            } else {
                CD.showDialog(RegisterNewPatient.this, "Error: " + result.getResponseCode());
            }
        }

        // GET_REGISTRATION_MODES
        else if (TaskType.GET_REGISTRATION_MODES == taskType) {
            Intent intent = getIntent();
            SuccessResponse response = null;
            System.out.println(result.toString());

            if (result.getResponseCode().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {
                response = JsonParse.getSuccessResponse(result.getResponse());

                if (response.getStatus().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {

                    Log.e("Response.getData is: ", response.getData());
                    List<RegistrationMode> registrationModes = JsonParse.parseRegistrationModes(response.getData());

                    if (registrationModes != null && registrationModes.size() > 0) {
                        registrationModesSpinnerAdapter = new RegistrationModesSpinnerAdapter(this, android.R.layout.simple_spinner_item, registrationModes);
                        registrationModeSpinner.setAdapter(registrationModesSpinnerAdapter);

                    } else {
                        CD.showDialog(RegisterNewPatient.this, "No Data Found!");
                    }

                } else {
                    CD.showDialog(RegisterNewPatient.this, "No Data Found");
                }

            } else {
                CD.showDialog(RegisterNewPatient.this, "Error: " + result.getResponseCode());
            }
        }

        // GET REFERRED BY
        if (TaskType.GET_REFERRED_BY == taskType) {
            Intent intent = getIntent();
            SuccessResponse response = null;
            System.out.println(result.toString());

            if (result.getResponseCode().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {
                response = JsonParse.getSuccessResponse(result.getResponse());

                if (response.getStatus().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {

                    Log.e("Response.getData is: ", response.getData());
                    List<ReferredBy> referredByList = JsonParse.parseReferredBy(response.getData());

                    if (referredByList != null && referredByList.size() > 0) {
                        referredBySpinnerAdapter = new ReferredBySpinnerAdapter(this, android.R.layout.simple_spinner_item, referredByList);
                        referredBySpinner.setAdapter(referredBySpinnerAdapter);

                    } else {
                        CD.showDialog(RegisterNewPatient.this, "No Data Found!");
                    }

                } else {
                    CD.showDialog(RegisterNewPatient.this, "No Data Found");
                }

            } else {
                CD.showDialog(RegisterNewPatient.this, "Error: " + result.getResponseCode());
            }
        }

        // GET TESTS
        if (TaskType.GET_TESTS == taskType) {

            SuccessResponse response = null;
            System.out.println(result.toString());

            if (result.getResponseCode().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {
                response = JsonParse.getSuccessResponse(result.getResponse());

                if (response.getStatus().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {

                    Log.e("Response.getData is: ", response.getData());
                    List<TestsPojo> testsPojoList = JsonParse.parseTests(response.getData());

                    if (testsPojoList != null && testsPojoList.size() > 0) {

                        showTestsListsDialog(testsPojoList, finalSelectionTests, this);

                    } else {
                        CD.showDialog(RegisterNewPatient.this, "No Data Found!");
                    }


                } else {
                    CD.showDialog(RegisterNewPatient.this, "No Data Found");
                }

            } else {
                CD.showDialog(RegisterNewPatient.this, "Error: " + result.getResponseCode());
            }
        }

        // Save Patient
        else if (TaskType.SAVE_PATIENT == taskType) {

            SuccessResponse response = null;
            System.out.println(result.toString());

            if (result.getResponseCode().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {
                response = JsonParse.getSuccessResponse(result.getResponse());

                if (response.getStatus().equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {

                    // Save Data
                    CD.showDismissActivityDialog(RegisterNewPatient.this, response.getMessage());
                } else {
                    CD.showDialog(RegisterNewPatient.this, response.getMessage());
                }


            } else {
                CD.showDialog(RegisterNewPatient.this, "No Data Found");
            }

        }


    }


}



