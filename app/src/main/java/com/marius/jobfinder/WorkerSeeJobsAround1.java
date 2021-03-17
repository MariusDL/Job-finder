package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkerSeeJobsAround1 extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;
    private UiSettings mUiSettings;
    private MapView mapView;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private LocationManager locationManager;
    private String currentLat, currentLon;
    private static final int REQUEST_LOCATION = 1;

    private EditText addressEditText;
    private Button selectRange, findJobs, yourLocation, next;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser mUser;
    private String userId, nameOfCurrentUser;

    private String address;


    private AlertDialog.Builder alertDialogBuilder;
    private ArrayList<String> rangeOptions = new ArrayList<>();
    private String[] rangeOptionsItems;
    private int chosenRange = -1;

    private ArrayList<String> servicesFromDatabase = new ArrayList<>();
    private ArrayList<String> servicesOfferedByTheUser = new ArrayList<>();
    private ArrayList<String> jobsAvailableForTheUser = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_see_jobs_around1);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Find job");

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();

        servicesFromDatabase = getServicesFromDatabase();

        addressEditText = findViewById(R.id.workerSeeJobsAround1AddressEditText);
        selectRange = findViewById(R.id.workerSeeJobsAround1SelectRangeButton);
        findJobs = findViewById(R.id.workerSeeJobsAround1FindJobsButton);
        yourLocation = findViewById(R.id.workerSeeJobsAround1YourLocationButton);
        next = findViewById(R.id.workerSeeJobsAround1NextButton);

        next.setEnabled(false);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkerSeeJobsAround1.this, WorkerSeesJobsAround2.class);
                for(int i =0; i<jobsAvailableForTheUser.size(); i++){
                    intent.putExtra(String.valueOf(i), jobsAvailableForTheUser.get(i));

                }
                intent.putExtra("nameOfCurrentUser", nameOfCurrentUser);
                startActivity(intent);
            }
        });

        yourLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                } else {
                    getLocation();
                }
            }
        });

        // required for maps
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.workerSeeJobs1AroundMap);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);




        rangeOptions.add("5 miles");
        rangeOptions.add("10 miles");
        rangeOptions.add("25 miles");
        rangeOptions.add("50 miles");
        rangeOptions.add("100 miles");

        selectRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogBuilder = new AlertDialog.Builder(WorkerSeeJobsAround1.this);

                // put the services list taken from database in an array to pass it to the alert dialog
                rangeOptionsItems = new String[rangeOptions.size()];
                for(int i =0; i<rangeOptions.size();i++){
                    String service = rangeOptions.get(i);
                    rangeOptionsItems[i]=service;
                }

                alertDialogBuilder.setSingleChoiceItems(rangeOptionsItems, chosenRange, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenRange = which;
                    }
                });

                alertDialogBuilder.setCancelable(false);

                alertDialogBuilder.setTitle("Select the distance range");
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(chosenRange>=0){
                            selectRange.setText("Range: " + rangeOptions.get(chosenRange));
                            dialog.dismiss();
                        }
                    }
                });

                alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
            }
        });


    }


    private void OnGPS(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(
                WorkerSeeJobsAround1.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                WorkerSeeJobsAround1.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                currentLat = String.valueOf(lat);
                currentLon = String.valueOf(longi);
                LatLng latLng = new LatLng(lat, longi);

                gmap.clear();
                gmap.addMarker(new MarkerOptions().position(latLng));

                setLocationInTextBoxes(lat, longi);


                gmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            } else {
                Toast.makeText(WorkerSeeJobsAround1.this, "Unable to find location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setLocationInTextBoxes(double lat, double lon)  {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to return
            String returnedAddress = addresses.get(0).getAddressLine(0);

            address = returnedAddress;
            addressEditText.setText(returnedAddress);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(5);
        mUiSettings = gmap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);

        LatLng ny = new LatLng(52.60807969917166, -1.6623741364015132);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
    }

    public void onMapReady(View view){
        String add = addressEditText.getText().toString().trim();

        List<Address>  addressList = null;

        if(TextUtils.isEmpty(add)){
            addressEditText.setError("Complete the address field");
        } else if(chosenRange<0){
            Toast.makeText(WorkerSeeJobsAround1.this, "Select the range", Toast.LENGTH_SHORT).show();
        } else {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(add,1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);

            final LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            gmap.clear();


            // set the range and zoom level for the circle on the map
            // circleRadius contains the distance is meters
            int circleRadius = 1000;
            int zoom = 10;
            if(chosenRange>=0){
                switch(rangeOptions.get(chosenRange)){
                    case "5 miles":
                        circleRadius = 8046;
                        zoom = 10;
                        break;
                    case "10 miles":
                        circleRadius = 16093;
                        zoom = 9;
                        break;
                    case "25 miles":
                        circleRadius = 40233;
                        zoom = 8;
                        break;
                    case "50 miles":
                        circleRadius = 80467;
                        zoom = 7;
                        break;
                    case "100 miles":
                        circleRadius = 160934;
                        zoom = 6;
                        break;
                }
            }

            //add the circle on the map
            final Circle circle = gmap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(circleRadius)
                    .strokeColor(Color.TRANSPARENT)
                    .fillColor(0x40ff0000)
                    .strokeWidth(2));

            // move the map camera to the circle location
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));


            //get the services offered by the user from firebase so only the worker sees only the jobs types he selected
            databaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);

                    // get list with offered services from the user object and put them in a list with services offered by th user
                    List<String> servicesOffered = user.getTypesOfOfferedServices();
                    for(int i =0; i<servicesOffered.size();i++){
                        String service = servicesOffered.get(i);
                        if(service.equals("Yes")){
                            servicesOfferedByTheUser.add(servicesFromDatabase.get(i));
                        }
                    }
                    nameOfCurrentUser = String.valueOf(user.getFirstName()+" " + user.getLastName());

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });


            final float[] distance = new float[2];

            // get the all the jobs from the database
            databaseReference.child("Jobs").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //iterate through the jobs
                    jobsAvailableForTheUser.clear();
                    int numberOfJobsFoundInRange = 0;
                    for (DataSnapshot jobSnapshot : snapshot.getChildren()){
                        Job job = jobSnapshot.getValue(Job.class);

                        //get the job coordinates
                        LatLng latLong = new LatLng(Double.valueOf(job.getLatitude()), Double.valueOf(job.getLongitude()));

                        // calculate the distance between the job and circle center
                        Location.distanceBetween(latLong.latitude, latLong.longitude,
                                circle.getCenter().latitude, circle.getCenter().longitude, distance);

                        // show the job if it is in the circle range and if the user provides it and if the job has not been accepted by anyone else
                        if(distance[0] < circle.getRadius() &&
                                servicesOfferedByTheUser.contains(job.getTypeOfJob()) &&
                                job.getStatus().equals("Pending")){

                            gmap.addMarker(new MarkerOptions().position(latLong));
                            numberOfJobsFoundInRange++;
                            jobsAvailableForTheUser.add(job.getId() + "," + String.valueOf(distance[0]));
                        }

                    }

                    //enable the next button only if there are jobs around
                    if(jobsAvailableForTheUser.size()>0){
                        next.setEnabled(true);
                    }
                    Toast.makeText(WorkerSeeJobsAround1.this, "Jobs found in selected area: " + numberOfJobsFoundInRange, Toast.LENGTH_SHORT).show();

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            setLocationInTextBoxes(address.getLatitude(), address.getLongitude());
        }

    }

    // get the list of services from the database
    private  ArrayList<String> getServicesFromDatabase(){
        final ArrayList<String> servDB = new ArrayList<>();

        databaseReference.child("Services").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> serv = (ArrayList) snapshot.getValue();
                for(int i =0; i<serv.size();i++){
                    servDB.add(i, serv.get(i));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return servDB;
    }
}