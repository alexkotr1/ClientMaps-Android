package com.alexk.clientmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.ImageView;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Arrays;

public class EditController extends AppCompatActivity implements LocationUtilitiesListener {
    private LocationUtilities locationUtilities;
    private Client client;
    private ProgressBar progressBar;
    private boolean isActivityPaused = true;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> captureImageLauncher;
    private boolean cameraLaunched = false;

    @Override
    protected void onResume(){
        super.onResume();
        if (isActivityPaused && !cameraLaunched) {
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

        imageView = findViewById(R.id.imageView);

        EditText clientName = findViewById(R.id.clientName);
        EditText phone = findViewById(R.id.phone);
        EditText comments = findViewById(R.id.comments);
        EditText addClient = findViewById(R.id.addClient);

        Button addImage = findViewById(R.id.select_image_button);
        Button captureImageButton = findViewById(R.id.capture_image_button);

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
        client.setHas_image(getIntent().getBooleanExtra("has_image", false));

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
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        imageView.setImageURI(imageUri);
                        Helper.AdjustImageView(imageView, 820 , 600);
                    }
                });
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(imageBitmap);
                        Helper.AdjustImageView(imageView, 820 , 600);
                    }
                });
        addImage.setOnClickListener(v -> {
            openFileChooser();
            cameraLaunched = true;
        });
        captureImageButton.setOnClickListener(v -> {
            openCamera();
            cameraLaunched = true;
        });
        if (client.has_image()){
            ImageDownloader.downloadImage(this, Helper.DOWNLOAD_ENDPOINT + Helper.PASSWORD + '/' + client.getId(),  new ImageDownloader.OnImageDownloadListener() {
                @Override
                public void onImageDownloaded(Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                    Helper.AdjustImageView(imageView, 820 , 600);
                }

                @Override
                public void onError(VolleyError error) {
                    Log.e("MainActivity", "Image download failed: " + error.getMessage());
                }
            });
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
                        public void onSuccess(int statusCode, String res) {
                            progressBar.setVisibility(View.GONE);
                            Helper.Toast("Επιτυχής Επεξεργασία!", EditController.this, MainActivity.class);
                            if (imageView.getDrawable() == null) return;
                            Helper.uploadImage(getApplicationContext(), ((BitmapDrawable) imageView.getDrawable()).getBitmap(), Helper.UPLOAD_ENDPOINT + Helper.PASSWORD + "/" + client.getId(), new Helper.OnRequestSuccessListener() {
                                @Override
                                public void onSuccess(int statusCode, String res) {
                                    Log.d("SUCCESS", Integer.toString(statusCode));
                                }

                                @Override
                                public void onError(int statusCode) {
                                    Log.d("ERROR",Integer.toString(statusCode));
                                }
                            });
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
            public void onSuccess(int statusCode, String response) {
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

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(intent);
    }
}