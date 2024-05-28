package com.example.lost_and_found;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class ShowAllActivity extends AppCompatActivity {

    private ListView listView;
    private TaskAdapter taskAdapter;
    private TaskDAO taskDAO;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_all);

        listView = findViewById(R.id.listView);
        buttonBack = findViewById(R.id.buttonBack);
        taskDAO = new TaskDAO(this);
        List<TaskClass> taskClassList = taskDAO.getAllTasks();

        taskAdapter = new TaskAdapter(this, taskClassList);
        listView.setAdapter(taskAdapter);

        buttonBack.setOnClickListener(v -> finish());

        // Handle clicking on a task
        listView.setOnItemClickListener((parent, view, position, id) -> {
            TaskClass taskClass = taskClassList.get(position);
            Intent intent = new Intent(ShowAllActivity.this, AdvertDetailsActivity.class);
            intent.putExtra("item_id", taskClass.getId());
            startActivity(intent);
        });
    }
}

