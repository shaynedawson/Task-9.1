package com.example.lost_and_found;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final String TAG = "CreateAdvertActivity";
    private EditText nameEdit, phoneEdit, descriptionEdit, dateEdit, locationEdit;
    private RadioGroup radioGroupType;
    private Button saveButton, getCurrentLocationButton;
    private TaskDAO taskDAO;
    private PlacesClient placesClient;
    private double latitude = 0.0, longitude = 0.0;

    private final ActivityResultLauncher<Intent> locationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String location = result.getData().getStringExtra("location");
                    latitude = result.getData().getDoubleExtra("latitude", 0.0);
                    longitude = result.getData().getDoubleExtra("longitude", 0.0);
                    locationEdit.setText(location);
                    Log.d(TAG, "Received location: " + location + ", lat: " + latitude + ", lon: " + longitude);
                }
            });

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentPlace();
                } else {
                    Toast.makeText(this, "Location permission is required to get the current location", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_advert);

        nameEdit = findViewById(R.id.name_edit);
        phoneEdit = findViewById(R.id.phone_edit);
        descriptionEdit = findViewById(R.id.description_edit);
        dateEdit = findViewById(R.id.date_edit);
        locationEdit = findViewById(R.id.location_edit);
        getCurrentLocationButton = findViewById(R.id.get_current_location);
        radioGroupType = findViewById(R.id.radioGroupType);
        saveButton = findViewById(R.id.button);

        taskDAO = new TaskDAO(this);
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        locationEdit.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAdvertActivity.this, LocationActivity.class);
            locationLauncher.launch(intent);
        });

        getCurrentLocationButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentPlace();
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        saveButton.setOnClickListener(v -> saveTask());
    }

    private void getCurrentPlace() {
        //https://developers.google.com/maps/documentation/places/android-sdk/current-place#maps_places_current_place-java

        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    if (response != null && !response.getPlaceLikelihoods().isEmpty()) {
                        Place place = response.getPlaceLikelihoods().get(0).getPlace();
                        if (place.getLatLng() != null) {
                            latitude = place.getLatLng().latitude;
                            longitude = place.getLatLng().longitude;
                            locationEdit.setText(place.getAddress());
                            Log.d(TAG, "Current place: " + place.getAddress() + ", lat: " + latitude + ", lon: " + longitude);
                        }
                    } else {
                        Toast.makeText(CreateAdvertActivity.this, "No current place found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Toast.makeText(CreateAdvertActivity.this, "Place not found: " + apiException.getStatusCode(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            }
        });
    }

    private void saveTask() {
        String name = nameEdit.getText().toString().trim();
        String phone = phoneEdit.getText().toString().trim();
        String description = descriptionEdit.getText().toString().trim();
        String dateString = dateEdit.getText().toString().trim();
        String location = locationEdit.getText().toString().trim();
        int selectedRadioButtonId = radioGroupType.getCheckedRadioButtonId();

        if (selectedRadioButtonId == -1) {
            Toast.makeText(CreateAdvertActivity.this, "Please select a post type", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String type = selectedRadioButton.getText().toString();

        long date = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date parsedDate = sdf.parse(dateString);
            if (parsedDate != null) {
                date = parsedDate.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Create and save the TaskClass object
        TaskClass taskClass = new TaskClass(0, name, phone, description, date, location, type, latitude, longitude);
        Log.d(TAG, "Saving Task: " + taskClass.toString());
        taskDAO.addTask(taskClass);

        finish(); // Go back to the previous activity
    }
}
