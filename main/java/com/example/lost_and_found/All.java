package com.example.lost_and_found;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentPlace();
            } else {
                requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        saveButton.setOnClickListener(v -> saveTask());
    }

    private void getCurrentPlace() {
        //https://developers.google.com/maps/documentation/places/android-sdk/current-place#maps_places_current_place-java

        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

package com.example.lost_and_found;

        import android.content.Context;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lostFound.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    public static final String TABLE_NAME = "items";

    // Table columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_DATE + " INTEGER, " // Changed to INTEGER for storing long dates
                + COLUMN_LOCATION + " TEXT, "
                + COLUMN_TYPE + " TEXT, "
                + COLUMN_LATITUDE + " REAL, "
                + COLUMN_LONGITUDE + " REAL)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

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

package com.example.lost_and_found;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button createNewButton, showAllButton, showMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNewButton = findViewById(R.id.create_new);
        showAllButton = findViewById(R.id.show_all);
        showMapButton = findViewById(R.id.show_map);

        createNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateAdvertActivity.class);
                startActivity(intent);
            }
        });

        showAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowAllActivity.class);
                startActivity(intent);
            }
        });

        showMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });


    }
}

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
package com.example.lost_and_found;

public class TaskClass {
    private int id;
    private String name;
    private String phone;
    private String description;
    private long date;
    private String location;
    private String type;
    private double latitude;
    private double longitude;

    // Default constructor
    public TaskClass() {}

    // Parameterized constructor
    public TaskClass(int id, String name, String phone, String description, long date, String location, String type, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.location = location;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public long getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "TaskClass{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}

package com.example.lost_and_found;

        import android.annotation.SuppressLint;
        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.util.Log;

        import java.util.ArrayList;
        import java.util.List;

public class TaskDAO {

    private SQLiteDatabase db;

    public TaskDAO(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public long addTask(TaskClass taskClass) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, taskClass.getName());
        values.put(DBHelper.COLUMN_PHONE, taskClass.getPhone());
        values.put(DBHelper.COLUMN_DESCRIPTION, taskClass.getDescription());
        values.put(DBHelper.COLUMN_DATE, taskClass.getDate());
        values.put(DBHelper.COLUMN_LOCATION, taskClass.getLocation());
        values.put(DBHelper.COLUMN_TYPE, taskClass.getType());
        values.put(DBHelper.COLUMN_LATITUDE, taskClass.getLatitude());
        values.put(DBHelper.COLUMN_LONGITUDE, taskClass.getLongitude());

        long id = db.insert(DBHelper.TABLE_NAME, null, values);
        Log.d("TaskDAO", "Inserted Task with ID: " + id + " and LatLng: (" + taskClass.getLatitude() + ", " + taskClass.getLongitude() + ")");
        return id;
    }

    @SuppressLint("Range")
    public List<TaskClass> getAllTasks() {
        List<TaskClass> taskClasses = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TaskClass taskClass = new TaskClass();
                taskClass.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                taskClass.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
                taskClass.setPhone(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PHONE)));
                taskClass.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION)));
                taskClass.setDate(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE)));
                taskClass.setLocation(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION)));
                taskClass.setType(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TYPE)));
                taskClass.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_LATITUDE)));
                taskClass.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_LONGITUDE)));

                Log.d("TaskDAO", "Retrieved Task: " + taskClass.toString());
                taskClasses.add(taskClass);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return taskClasses;
    }

    @SuppressLint("Range")
    public TaskClass getTask(int id) {
        Cursor cursor = db.query(DBHelper.TABLE_NAME, null, DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            TaskClass taskClass = new TaskClass();
            taskClass.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
            taskClass.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
            taskClass.setPhone(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PHONE)));
            taskClass.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION)));
            taskClass.setDate(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE)));
            taskClass.setLocation(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION)));
            taskClass.setType(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TYPE)));
            taskClass.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_LATITUDE)));
            taskClass.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_LONGITUDE)));

            cursor.close();
            return taskClass;
        }
        return null;
    }

    public void deleteTask(int id) {
        db.delete(DBHelper.TABLE_NAME, DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
