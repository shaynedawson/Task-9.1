package com.example.lost_and_found;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class LocationActivity extends AppCompatActivity {

    private static final String TAG = "LocationActivity";
    private String location;
    private double latitude, longitude;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is all taken from googles documentation https://developers.google.com/maps/documentation/places/android-sdk/autocomplete#maps_places_autocomplete_support_fragment-java
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        saveButton = findViewById(R.id.save_button);

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getAddress());
                location = place.getAddress();
                if (place.getLatLng() != null) {
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        saveButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("location", location);
            resultIntent.putExtra("latitude", latitude);
            resultIntent.putExtra("longitude", longitude);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
