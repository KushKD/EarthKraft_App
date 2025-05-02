package com.dit.hp.hospitalapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class LabReport extends AppCompatActivity implements AsyncTaskListenerObject {

    TextView from_date, to_date;
    Button back, view_report;

    LinearLayout parent_layout;

    CustomDialog CD = new CustomDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_report);

        from_date = findViewById(R.id.from_date);
        to_date = findViewById(R.id.to_date);
        back = findViewById(R.id.back);
        view_report = findViewById(R.id.view_report);
        parent_layout = findViewById(R.id.parent_layout);


        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        // Set today's date as default
        String today = sdf.format(calendar.getTime());
        from_date.setText(today);
        to_date.setText(today);

        // Show DatePicker on click
        from_date.setOnClickListener(v -> showDatePicker(from_date));
        to_date.setOnClickListener(v -> showDatePicker(to_date));

        back.setOnClickListener(v -> LabReport.this.finish());

        view_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromDateStr = from_date.getText().toString().trim();
                String toDateStr = to_date.getText().toString().trim();

                if (fromDateStr.isEmpty() || toDateStr.isEmpty()) {
                    Toast.makeText(LabReport.this, "Please select both dates", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Define input and output formats
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                try {
                    Date fromDate = inputFormat.parse(fromDateStr);
                    Date toDate = inputFormat.parse(toDateStr);

                    if (fromDate.after(toDate)) {
                        Toast.makeText(LabReport.this, "From Date cannot be after To Date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Format dates back to dd-MM-yyyy for sending to API
                    String formattedFromDate = outputFormat.format(fromDate);
                    String formattedToDate = outputFormat.format(toDate);

                    if (AppStatus.getInstance(LabReport.this).isOnline()) {
                        UploadObject object = new UploadObject();
                        object.setUrl(Econstants.base_url);
                        object.setMethordName(Econstants.labReport);
                        object.setMasterName(null);
                        object.setTasktype(TaskType.LAB_REPORTS);

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("fromDate", formattedFromDate); // 👈 use string, not Date
                            jsonObject.put("toDate", formattedToDate);
                            System.out.println(jsonObject.toString());

                            object.setParam(EncryptDecrypt.encrypt(jsonObject.toString()));

                        } catch (Exception e) {
                            Log.e("Exception: ", e.getLocalizedMessage());
                            e.printStackTrace();
                        }

                        new Generic_Async_Post(
                                LabReport.this,
                                LabReport.this,
                                TaskType.LAB_REPORTS).
                                execute(object);
                    } else {
                        CD.showDialog(LabReport.this, Econstants.internetNotAvailable);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LabReport.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showDatePicker(final TextView textView) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);


                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    textView.setText(sdf.format(selectedDate.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    @Override
    public void onTaskCompleted(ResponsePojoGet result, TaskType taskType) throws Exception {

        if (TaskType.LAB_REPORTS == taskType) {
            Log.i("ASYNC TASK COMPLETED", "TASK TYPE IS LOGIN.. CHECKED");
            SuccessResponse successResponse = null;


                successResponse = JsonParse.getSuccessResponse(result.getResponse());

                String status = successResponse.getStatus();
                String responseData = successResponse.getData();

                if (status.equalsIgnoreCase(String.valueOf(HttpsURLConnection.HTTP_OK))) {
                    Log.i("Login Response", responseData);
                    JSONObject json = new JSONObject(responseData);

                    parent_layout.removeAllViews();
                    // Key-value pairs
                    String[][] data = {
                            {"Report Period", json.optString("reportPeriod")},
                            {"Total Patients", json.optString("totalNumberOfPatients")},
                            {"Total Tests", json.optString("totalNumberOfTests")},
                            {"Online Collection", json.optString("totalOnlineCollection")},
                            {"Offline Collection", json.optString("totalOfflineCollection")},
                            {"Total Collection", json.optString("totalCollection")},
                            {"In-house Tests", json.optString("totalInHouseTestsAmount")},
                            {"Out-house Tests", json.optString("totalOuthouseTestsAmount")}
                    };

                    int padding = (int) getResources().getDimension(R.dimen.padding_standard); // or use 16 manually
                    int spacing = (int) getResources().getDimension(R.dimen.spacing_standard); // or use 12 manually

                    for (String[] pair : data) {
                        LinearLayout rowLayout = new LinearLayout(this);
                        rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        rowParams.setMargins(0, spacing, 0, spacing);
                        rowLayout.setLayoutParams(rowParams);

                        // Key TextView
                        TextView label = new TextView(this);
                        label.setText(pair[0]);
                        label.setTextColor(Color.BLUE);
                        label.setTypeface(null, Typeface.BOLD);
                        label.setGravity(Gravity.CENTER);
                        label.setPadding(padding, padding, padding, padding);
                        label.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                        // Value TextView
                        TextView value = new TextView(this);
                        value.setText(pair[1]);
                        value.setTextColor(Color.BLACK);
                        value.setTypeface(null, Typeface.BOLD);
                        value.setGravity(Gravity.LEFT);
                        value.setPadding(padding, padding, padding, padding);
                        value.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                        rowLayout.addView(label);
                        rowLayout.addView(value);

                        parent_layout.addView(rowLayout);
                    }


                }  else {
                    CD.showDialog(this, result.getResponse());
                }


        }
    }
}
