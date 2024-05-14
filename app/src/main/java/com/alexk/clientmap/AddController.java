package com.alexk.clientmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class AddController extends AppCompatActivity implements LocationUtilitiesListener {

    static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    LocationUtilities locationUtilities;
    Client client;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Προσθήκη Πελάτη");
        setContentView(R.layout.activity_add_controller);
        client = new Client();
        Button add = findViewById(R.id.add_button);
        EditText clientName = findViewById(R.id.clientName);
        EditText phone = findViewById(R.id.phone);
        EditText comments = findViewById(R.id.comments);
        progressBar = findViewById(R.id.progress_bar);
        locationUtilities = new LocationUtilities(this, this);
        locationUtilities.setListener(this);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name_text = clientName.getText().toString();
                String phone_text = phone.getText().toString();
                String comments_text = comments.getText().toString();
                if (name_text.isEmpty()) {
                    Helper.Alert("Προσοχή", "Το πεδίο «Ονοματεπώνυμο» δεν μπορεί να είναι κενό!", AddController.this, null);
                } else if (!phone_text.matches("\\d{10}") && !phone_text.isEmpty()) {
                    Helper.Alert("Προσοχή", "Το πεδίο «Τηλέφωνο» δεν είναι έγκυρο!", AddController.this, null);
                } else {
                    client.setName(name_text);
                    client.setPhone(phone_text);
                    client.setComments(comments_text);
                    String[] emptyNames = {};
                    client.setNames(emptyNames);
                    progressBar.setVisibility(View.VISIBLE);
                    locationUtilities.requestLocation();
                }
            }
        });
    }

    @Override
    public void onLocationReceived(Location location) {
        Log.d("Location123123",Double.toString(location.getLatitude()));
        client.setLatitude(Double.toString(location.getLatitude()));
        client.setLongitude(Double.toString(location.getLongitude()));
        if (!Helper.hasInternetAccess(AddController.this)){
            client.saveClientLocally(AddController.this);
            Helper.Toast("Ο πελάτης θα προστεθεί όταν υπάρχει σύνδεση στο διαδίκτυο",AddController.this,MainActivity.class);

            return;
        }
        Helper.Request(Helper.ADD_ENDPOINT, null, Helper.ClientToRequestBody(client), AddController.this, new Helper.OnRequestSuccessListener() {
            @Override
            public void onSuccess(int statusCode) {
                progressBar.setVisibility(View.GONE);
                    Helper.Toast("Επιτυχής προσθήκη!",AddController.this,MainActivity.class);
            }

            @Override
            public void onError(int statusCode) {
                progressBar.setVisibility(View.GONE);
                Log.d("status",Integer.toString(statusCode));
                switch (statusCode){
                    case 200:Helper.Toast("Επιτυχής προσθήκη!",AddController.this,MainActivity.class);
                    break;
                    case 409:Helper.Alert("Προσοχή","Αυτός ο πελάτης υπάρχει ήδη!",AddController.this,null);
                    break;
                    case 400:
                        Helper.Toast("Ο πελάτης θα προστεθεί όταν υπάρχει σύνδεση στο διαδίκτυο",AddController.this,MainActivity.class);
                        client.saveClientLocally(AddController.this);
                    break;
                        default:
                            Helper.Alert("Προσοχή","Κάτι πήγε στραβά",AddController.this,MainActivity.class);
                    break;
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationUtilities.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        locationUtilities.onActivityResult(requestCode, resultCode, data);
    }
}
