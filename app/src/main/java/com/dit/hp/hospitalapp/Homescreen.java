package com.dit.hp.hospitalapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.dit.hp.hospitalapp.Presentation.CustomDialog;
import com.dit.hp.hospitalapp.utilities.AppStatus;
import com.dit.hp.hospitalapp.utilities.Preferences;

public class Homescreen extends AppCompatActivity {

    CustomDialog CD = new CustomDialog();
    TextView usernameTV, roleTV, welcomeTV;

    CardView cardView1, cardView2, cardView3, cardView4;
    CardView bottomCard;
    ImageButton profileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        cardView1 = findViewById(R.id.cardView1);
        cardView2 = findViewById(R.id.cardView2);
        cardView3 = findViewById(R.id.cardView3);
        cardView4 = findViewById(R.id.cardView4);

        profileButton = findViewById(R.id.profileB);
        bottomCard = findViewById(R.id.bottomCard);

        welcomeTV = findViewById(R.id.headTV);
        usernameTV = findViewById(R.id.usernameTV);
        roleTV = findViewById(R.id.roleTV);


        // Load saved preferences at the very beginning
        Preferences.getInstance().loadPreferences(this);
        reloadUserDetails();


        cardView1.setOnClickListener(v -> {

            if (!isAuthorizedUser()) {
                CD.showDialog(Homescreen.this, "You are not authorized to access this feature.");
                return;
            }

            if (AppStatus.getInstance(Homescreen.this).isOnline()) {
                Intent intent = new Intent(Homescreen.this, RegisterNewPatient.class);
                startActivity(intent);
            } else {
                CD.showDialog(this, "No Internet Connection");
            }
        });

        cardView2.setOnClickListener(v -> {
            if (!isAuthorizedUser()) {
                CD.showDialog(Homescreen.this, "You are not authorized to access this feature.");
                return;
            }

            if (AppStatus.getInstance(Homescreen.this).isOnline()) {
                Intent intent = new Intent(Homescreen.this, SearchRecord.class);
                startActivity(intent);
            } else {
                CD.showDialog(this, "No Internet Connection");
            }
        });

        cardView3.setOnClickListener(v -> {

            Intent intent = new Intent(Homescreen.this, AboutUs.class);
            startActivity(intent);
        });

        cardView4.setOnClickListener(v -> {
            if (!isAuthorizedUserSuperAdmin()) {
                CD.showDialog(Homescreen.this, "You are not authorized to access this feature.");
                return;
            }
            Intent intent = new Intent(Homescreen.this, LabReport.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.logout) {
                    showLogoutConfirmationDialog();
                    return true;
                }

                return false;
            });
            popupMenu.show();
        });



    }


    private boolean isAuthorizedUser() {
        int roleId = Preferences.getInstance().roleId;

        String roleName = Preferences.getInstance().roleName;
        return (roleId == 1 && roleName.equalsIgnoreCase("Super Admin")) ||
                (roleId == 2 && roleName.equalsIgnoreCase("Admin")) ||
                (roleId == 8 && roleName.equalsIgnoreCase("Receptionist"));
    }

    private boolean isAuthorizedUserSuperAdmin() {
        int roleId = Preferences.getInstance().roleId;

        String roleName = Preferences.getInstance().roleName;
        return (roleId == 1 && roleName.equalsIgnoreCase("Super Admin")) ||
                (roleId == 2 && roleName.equalsIgnoreCase("Admin"));
    }


    private void reloadUserDetails() {
        // Welcome Message
        String completeName = (Preferences.getInstance().firstName != null && !Preferences.getInstance().firstName.isEmpty())
                ? Preferences.getInstance().firstName + " " + (Preferences.getInstance().lastName != null ? Preferences.getInstance().lastName : "")
                : "Guest";
        welcomeTV.setText("Welcome " + completeName + " !");

        String username = Preferences.getInstance().userName;
        usernameTV.setText(username != null && !username.isEmpty() ? "Username: " + username : "Username: No Username Available");

        // Role
        String roleName = Preferences.getInstance().roleName;
        roleTV.setText(roleName != null && !roleName.isEmpty() ? "Role: " + roleName : "Role: No Role Available");
    }


    // Exit confirmation
    private void showLogoutConfirmationDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout as the current user?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Logout + clear prefs
                        Intent intent = new Intent(Homescreen.this, LoginScreen.class);
                        startActivity(intent);
                        Homescreen.this.finish();

                        // Clear
                        SharedPreferences preferences = getSharedPreferences("com.dit.hp.ekraft", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear(); // This will remove all preferences
                        editor.apply(); // or editor.commit();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Preferences.getInstance().loadPreferences(this); // reload from SharedPreferences
        reloadUserDetails(); // update UI
    }



}