package com.example.lost_and_found;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends ArrayAdapter<TaskClass> {

    public TaskAdapter(Context context, List<TaskClass> tasks) {
        super(context, 0, tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TaskClass task = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_item, parent, false);
        }

        // Lookup view for data population
        TextView textViewName = convertView.findViewById(R.id.text_view_name);
        TextView textViewPhone = convertView.findViewById(R.id.text_view_phone);
        TextView textViewDescription = convertView.findViewById(R.id.text_view_description);
        TextView textViewDate = convertView.findViewById(R.id.text_view_date);
        TextView textViewLocation = convertView.findViewById(R.id.text_view_location);
        TextView textViewType = convertView.findViewById(R.id.text_view_type);

        // Populate the data into the template view using the data object
        textViewName.setText(task.getName());
        textViewPhone.setText(task.getPhone());
        textViewDescription.setText(task.getDescription());
        textViewDate.setText(convertDateToString(task.getDate()));
        textViewLocation.setText(task.getLocation());
        textViewType.setText(task.getType());

        // Return the completed view to render on screen
        return convertView;
    }

    private String convertDateToString(long dateMillis) {
        // Convert milliseconds to a readable date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(new Date(dateMillis));
    }
}
