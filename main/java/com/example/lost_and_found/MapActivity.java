package com.example.lost_and_found;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TaskDAO taskDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        taskDAO = new TaskDAO(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Get all tasks from the database
        List<TaskClass> tasks = taskDAO.getAllTasks();

        // Add a marker for each task
        for (TaskClass task : tasks) {
            LatLng location = new LatLng(task.getLatitude(), task.getLongitude());
            Log.d("MapActivity", "Adding marker for task: " + task);
            mMap.addMarker(new MarkerOptions().position(location).title(task.getName()));
        }

        if (!tasks.isEmpty()) {
            // Move the camera to the first task's location
            LatLng firstLocation = new LatLng(tasks.get(0).getLatitude(), tasks.get(0).getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10));
        }
    }
}
