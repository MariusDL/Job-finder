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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddNewJobActivityPart3 extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gmap;
    private MapView mapView;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private LocationManager locationManager;
    private String currentLat, currentLon;
    private static final int REQUEST_LOCATION = 1;

    private EditText addressEditText, postcodeEditText;
    private Button findAddress, yourLocation, createJob;

    private String date, typeOfDeadline, address, city, postcode, longitude, latitude;
    private String jobId, jobTitle, jobDescription, chosenTypeOfJob, money, imageUrl, nameOfTheUser;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser mUser;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_job_part_3);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add job");

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();

        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);


        addressEditText = findViewById(R.id.addNewJobActivity3AddressEditText);
        postcodeEditText = findViewById(R.id.addNewJobActivity3PostcodeEditText);
        findAddress = findViewById(R.id.addNewJobActivity3FindAddressButton);
        yourLocation = findViewById(R.id.addNewJobActivity3YourLocationButton);
        createJob = findViewById(R.id.addNewJobActivity3CreateJobButton);

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

        mapView = findViewById(R.id.addNewJobMapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            jobId = extras.getString("jobId");
            jobTitle = extras.getString("jobTitle");
            jobDescription = extras.getString("jobDescription");
            chosenTypeOfJob = extras.getString("chosenTypeOfJob");
            money = extras.getString("money");
            imageUrl = extras.getString("imageUrl");
            date = extras.getString("date");
            typeOfDeadline = extras.getString("typeOfDeadline");
        }

        //get the name of the current user that is posting the job from database
        databaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nameOfTheUser = String.valueOf(snapshot.getValue(User.class).getFirstName()+ " " + snapshot.getValue(User.class).getLastName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        createJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(address)){
                    addressEditText.setError("Complete the address field");
                } else if(TextUtils.isEmpty(postcode)) {
                    postcodeEditText.setError("Complete the postcode field");
                } else {

                    Job job = new Job(jobId, jobTitle, chosenTypeOfJob, jobDescription, money, imageUrl, typeOfDeadline, address,
                            postcode, longitude, latitude, date, userId,"Pending");

                    job.setPostedByName(nameOfTheUser);

                    Log.d("######", "ID: "+ job.getId() + "\n" +
                            "Title: "+job.getTitle() + "\n" +
                            "Type: "+job.getTypeOfJob()  + "\n" +
                            "Description: "+job.getDescription()  + "\n" +
                            "Money: "+job.getMoney()  + "\n" +
                            "Image: "+job.getImageUrl() + "\n" +
                            "Type of Deadline: "+job.getTypeOfDeadline()  + "\n" +
                            "Address: "+job.getAddress()  + "\n" +
                            "Postcode: "+job.getPostcode()  + "\n" +
                            "Longitude: "+job.getLongitude()  + "\n" +
                            "Latitude: "+job.getLatitude() + "\n" +
                            "Due date: "+job.getDueDate()  + "\n" +
                            "Posted By: "+job.getPostedById()  + "\n" +
                            "Status: "+job.getStatus()  + "\n"+
                            "PostedByName: "+job.getPostedByName()  + "\n");

                    databaseReference.child("Jobs").child(jobId).setValue(job).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(AddNewJobActivityPart3.this, "Job successfully posted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddNewJobActivityPart3.this, HomeActivityUser.class);
                            startActivity(intent);
                        }
                    });
                }
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
                AddNewJobActivityPart3.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                AddNewJobActivityPart3.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                gmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            } else {
                Toast.makeText(AddNewJobActivityPart3.this, "Unable to find location", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void setLocationInTextBoxes(double lat, double lon)  {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String returnedAddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String returnedCity = addresses.get(0).getLocality();
            String returnedPostalCode = addresses.get(0).getPostalCode();

            address = returnedAddress;
            city = returnedCity;
            postcode = returnedPostalCode;

            addressEditText.setText(returnedAddress);
            postcodeEditText.setText(returnedPostalCode);

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
        gmap.setMinZoomPreference(12);
        LatLng ny = new LatLng(52.60807969917166, -1.6623741364015132);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
    }

    public void onMapReady(View view){
        String add = addressEditText.getText().toString().trim();
        String pc = postcodeEditText.getText().toString().trim();

        String locationTextToSearch = add + " " + pc;

        List<Address>  addressList = null;

        if(TextUtils.isEmpty(add)){
            addressEditText.setError("Complete the address field");
        } else if(TextUtils.isEmpty(pc)) {
            postcodeEditText.setError("Complete the postcode field");
        } else {
            Geocoder geocoder = new Geocoder(this);

            try {
                addressList = geocoder.getFromLocationName(locationTextToSearch,1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);

            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            latitude = String.valueOf(address.getLatitude());
            longitude = String.valueOf(address.getLongitude());

            gmap.clear();
            gmap.addMarker(new MarkerOptions().position(latLng));

            gmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            setLocationInTextBoxes(address.getLatitude(), address.getLongitude());
        }
    }
}