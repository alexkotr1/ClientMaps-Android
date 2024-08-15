package com.alexk.clientmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;


public class AddController extends AppCompatActivity implements LocationUtilitiesListener, OnMapReadyCallback {

    private LocationUtilities locationUtilities;
    private Client client;
    private ProgressBar progressBar;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> captureImageLauncher;
    private GoogleMap mMap;
    private Marker mMarker;

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Προσθήκη Πελάτη");
        setContentView(R.layout.activity_add_controller);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = getApplicationContext();
        client = new Client();
        imageView = findViewById(R.id.imageView);

        Button add = findViewById(R.id.add_button);
        Button addImage = findViewById(R.id.select_image_button);
        Button captureImageButton = findViewById(R.id.capture_image_button);


        EditText clientName = findViewById(R.id.clientName);
        EditText phone = findViewById(R.id.phone);
        EditText comments = findViewById(R.id.comments);

        progressBar = findViewById(R.id.progress_bar);

        locationUtilities = new LocationUtilities(this, this);
        locationUtilities.setListener(this);
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
        addImage.setOnClickListener(v -> openFileChooser());
        captureImageButton.setOnClickListener(v -> openCamera());
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
                    client.setLatitude(String.valueOf(mMarker.getPosition().latitude));
                    client.setLongitude(String.valueOf(mMarker.getPosition().longitude));
                    String[] emptyNames = {};
                    client.setNames(emptyNames);
                    progressBar.setVisibility(View.VISIBLE);
                    if (!Helper.hasInternetAccess(AddController.this)){
                        client.saveClientLocally(AddController.this);
                        Helper.Toast("Ο πελάτης θα προστεθεί όταν υπάρχει σύνδεση στο διαδίκτυο",AddController.this,MainActivity.class);
                        return;
                    }
                    Helper.Request(Helper.ADD_ENDPOINT, null, Helper.ClientToRequestBody(client), AddController.this, new Helper.OnRequestSuccessListener() {
                        @Override
                        public void onSuccess(int statusCode, String clientId) {
                            progressBar.setVisibility(View.GONE);
                            Helper.Toast("Επιτυχής προσθήκη!",AddController.this,MainActivity.class);
                            if (imageView.getDrawable() == null) return;
                            Helper.uploadImage(context, ((BitmapDrawable) imageView.getDrawable()).getBitmap(), Helper.UPLOAD_ENDPOINT + Helper.PASSWORD + "/" + clientId, new Helper.OnRequestSuccessListener() {
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
            }
        });
    }

    @Override
    public void onLocationReceived(Location location) {
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        mMarker.setPosition(latlng);;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 20));
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
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(intent);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng initialPosition = new LatLng(0, 0);
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(initialPosition)
                .draggable(true));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 15));
        locationUtilities.requestLocation();

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                LatLng newPosition = marker.getPosition();
                marker.setTitle("New position: " + newPosition.latitude + ", " + newPosition.longitude);
                marker.showInfoWindow();
            }
        });
    }


}
