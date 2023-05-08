package com.alexk.clientmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Arrays;

public class EditController extends AppCompatActivity implements LocationUtilitiesListener {
    LocationUtilities locationUtilities;
    Client client;
    ProgressBar progressBar;
    private boolean isActivityPaused = true;
    @Override
    protected void onResume(){
        super.onResume();
        if (isActivityPaused) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        isActivityPaused = false;
    }
    @Override
    protected void onPause() {
        super.onPause();

        isActivityPaused = true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isActivityPaused = false;
        client = new Client();
        super.onCreate(savedInstanceState);
        setTitle("Επεξεργασία Πελάτη");
        setContentView(R.layout.activity_edit_controller);
        EditText clientName = findViewById(R.id.clientName);
        EditText phone = findViewById(R.id.phone);
        EditText comments = findViewById(R.id.comments);
        EditText addClient = findViewById(R.id.addClient);
        locationUtilities = new LocationUtilities(this, this);
        locationUtilities.setListener(this);
        progressBar = findViewById(R.id.progress_bar);
        Switch use_current_location = findViewById(R.id.locationSwitch);
        if (Helper.isDarkModeEnabled(this)){
            use_current_location.setTextColor(Color.WHITE);
        }
        Button edit = findViewById(R.id.editButton);

        client.setName(getIntent().getStringExtra("name"));
        client.setPhone(getIntent().getStringExtra("phone"));
        client.setComments(getIntent().getStringExtra("comments"));
        client.setLatitude(getIntent().getStringExtra("latitude"));
        client.setLongitude(getIntent().getStringExtra("longitude"));
        client.setId(getIntent().getStringExtra("id"));
        client.setNames(getIntent().getStringArrayExtra("names"));

        ArrayList<String> NameList = null;
        if (client.getNames().length != 0) {
            RecyclerView recyclerView = findViewById(R.id.namesRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ClientNamesAdapter adapter = new ClientNamesAdapter(new ArrayList<>(Arrays.asList(client.getNames())),client,EditController.this);
            recyclerView.setAdapter(adapter);
        }
        if (client.getName() != null){
            clientName.setText(client.getName());
        }
        if (client.getPhone() != null){
            phone.setText(client.getPhone());
        }
        if (client.getComments() != null){
            comments.setText(client.getComments());
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String name_text = clientName.getText().toString();
                String phone_text = phone.getText().toString();
                String comments_text = comments.getText().toString();

                if (name_text.isEmpty()) {
                    Helper.Alert("Προσοχή", "Το πεδίο «Ονοματεπώνυμο δεν μπορεί να είναι κενό!", EditController.this, null);
                    return;
                } else if (!phone_text.matches("\\d{10}") && !phone_text.isEmpty()) {
                    Helper.Alert("Προσοχή", "Το πεδίο «Τηλέφωνο» δεν είναι έγκυρο!", EditController.this, null);
                    return;
                }
                client.setName(name_text);
                client.setPhone(phone_text);
                client.setComments(comments_text);
                String addClient_text = addClient.getText().toString();
                if (!addClient_text.isEmpty()) {
                    String[] newNames = new String[client.getNames().length + 1];
                    System.arraycopy(client.getNames(), 0, newNames, 0, client.getNames().length);
                    newNames[client.getNames().length] = addClient_text;
                    client.setNames(newNames);
                }
                if (use_current_location.isChecked()) {
                    locationUtilities.requestLocation();

                } else {
                    Helper.Request(Helper.EDIT_ENDPOINT, client.getId(), Helper.ClientToRequestBody(client), EditController.this, new Helper.OnRequestSuccessListener() {
                        @Override
                        public void onSuccess(int statusCode) {
                            progressBar.setVisibility(View.GONE);
                            Helper.Toast("Επιτυχής Επεξεργασία!", EditController.this, MainActivity.class);
                        }

                        @Override
                        public void onError(int statusCode) {
                            progressBar.setVisibility(View.GONE);
                            Log.d("STATUS", Integer.toString(statusCode));
                            switch(statusCode) {
                                case 200:
                                    Helper.Toast("Επιτυχής Επεξεργασία!", EditController.this, MainActivity.class);
                                    break;
                                case 409:
                                    Helper.Alert("Προσοχή", "Αυτό το όνομα υπάρχει ήδη!", EditController.this, null);
                                    break;
                                case 400:
                                    Helper.Alert("Προσοχή","Δεν υπάρχει σύνδεση στο διαδίκτυο ή ο διακομιστής δεν είναι διαθέσιμος!",EditController.this,MainActivity.class);
                                    break;
                                default:
                                    Helper.Alert("Προσοχή", "Κάτι πήγε στραβά!", EditController.this, null);
                                    break;
                            }



                        }
                    });
            }

            }
        });


    }

    @Override
    public void onLocationReceived(Location location) {
        client.setLongitude(Double.toString(location.getLongitude()));
        client.setLatitude(Double.toString(location.getLatitude()));
        Helper.Request(Helper.EDIT_ENDPOINT, client.getId(), Helper.ClientToRequestBody(client), EditController.this, new Helper.OnRequestSuccessListener() {
            @Override
            public void onSuccess(int statusCode) {
                progressBar.setVisibility(View.GONE);
                Helper.Toast("Επιτυχής Επεξεργασία!", EditController.this, MainActivity.class);
            }

            @Override
            public void onError(int statusCode) {
                progressBar.setVisibility(View.GONE);
                switch(statusCode) {
                    case 200:
                        Helper.Toast("Επιτυχής Επεξεργασία!", EditController.this, MainActivity.class);
                        break;
                    case 409:
                        Helper.Alert("Προσοχή", "Αυτό το όνομα υπάρχει ήδη!", EditController.this, null);
                        break;
                    case 400:
                        Helper.Alert("Προσοχή","Δεν υπάρχει σύνδεση στο διαδίκτυο ή ο διακομιστής δεν είναι διαθέσιμος!",EditController.this,MainActivity.class);
                        break;
                    default:
                        Helper.Alert("Προσοχή", "Κάτι πήγε στραβά!", EditController.this, null);
                        break;
                }

            }
        });

    }
}