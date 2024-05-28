package com.example.lost_and_found;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdvertDetailsActivity extends AppCompatActivity {

    private TextView textViewName, textViewPhone, textViewDescription, textViewDate, textViewLocation, textViewType;
    private Button buttonDelete;
    private TaskDAO taskDAO;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advert_details);

        textViewName = findViewById(R.id.textViewName);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewDate = findViewById(R.id.textViewDate);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewType = findViewById(R.id.textViewType);
        buttonDelete = findViewById(R.id.buttonDelete);

        taskDAO = new TaskDAO(this);
        itemId = getIntent().getIntExtra("item_id", -1);

        if (itemId != -1) {
            TaskClass taskClass = taskDAO.getTask(itemId);
            if (taskClass != null) {
                textViewName.setText(taskClass.getName());
                textViewPhone.setText(taskClass.getPhone());
                textViewDescription.setText(taskClass.getDescription());
                textViewDate.setText(String.valueOf(taskClass.getDate()));
                textViewLocation.setText(taskClass.getLocation());
                textViewType.setText(taskClass.getType());
            }
        }

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskDAO.deleteTask(itemId);
                finish(); // Go back to the previous activity
            }
        });
    }
}
