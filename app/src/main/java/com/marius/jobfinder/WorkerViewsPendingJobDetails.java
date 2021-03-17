package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class WorkerViewsPendingJobDetails extends AppCompatActivity {

    private TextView jobIdTextView, jobTitleTextView, typeOfJobTextView, dueByTextView, moneyTextView,
            jobDescriptionTextView, jobLocationTextView, postedByTextView;

    private ImageView jobImage;
    private Button cancelJob, contactUser, seeLocation;

    private String jobId, userPhone;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_views_pending_job_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Job details");

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            jobId = extras.getString("jobId");
        }

        jobIdTextView = findViewById(R.id.workerViewPendingJobDetailsJobIdTextView);
        jobTitleTextView = findViewById(R.id.workerViewPendingJobDetailsJobTitle);
        typeOfJobTextView = findViewById(R.id.workerViewPendingJobDetailsTypeOfJob);
        dueByTextView = findViewById(R.id.workerViewPendingJobDetailsDueBy);
        moneyTextView = findViewById(R.id.workerViewPendingJobDetailsMoneyOffered);
        jobDescriptionTextView = findViewById(R.id.workerViewPendingJobDetailsJobDescription);
        jobLocationTextView = findViewById(R.id.workerViewPendingJobDetailsLocation);
        postedByTextView = findViewById(R.id.workerViewPendingJobDetailsPostedBy);
        jobImage = findViewById(R.id.workerViewPendingJobDetailsJobImage);
        cancelJob = findViewById(R.id.workerViewPendingJobDetailsCancelJobButton);
        contactUser = findViewById(R.id.workerViewPendingJobDetailsContactUser);
        seeLocation = findViewById(R.id.workerViewPendingJobDetailsSeeLocationOnMap);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fillFieldsWithData();

        cancelJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmChoice();
            }
        });

        // start the dial intent to call the user that posted the job
        contactUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri number = Uri.parse("tel:" + userPhone);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            }
        });
    }

    public void confirmChoice(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkerViewsPendingJobDetails.this);

        alertDialogBuilder.setTitle("Confirm ");

        alertDialogBuilder.setMessage("Are you sure you want to cancel the job?");
        alertDialogBuilder.setCancelable(false);



        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                databaseReference.child("Jobs").child(jobId).child("acceptedById").removeValue();
                databaseReference.child("Jobs").child(jobId).child("acceptedByName").removeValue();
                databaseReference.child("Jobs").child(jobId).child("status").setValue("Pending");

                startActivity(new Intent(WorkerViewsPendingJobDetails.this, HomeActivityWorker.class));
                finish();
            }
        });

        alertDialogBuilder.setNeutralButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void fillFieldsWithData(){
        databaseReference.child("Jobs").child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Job job = snapshot.getValue(Job.class);

                jobIdTextView.setText("Job ID: " + job.getId());
                jobTitleTextView.setText("Job title: " + job.getTitle());
                typeOfJobTextView.setText("Type of job: "+ job.getTypeOfJob());
                dueByTextView.setText("Due by: " + job.getDueDate());
                moneyTextView.setText("Money offered: £" + job.getMoney());
                jobDescriptionTextView.setText("Description: "+job.getDescription());
                jobLocationTextView.setText("Location: " + job.getAddress());
                postedByTextView.setText("Posted by: " + job.getPostedByName());

                if (!job.getImageUrl().equals("")) {
                    Picasso.get().load(job.getImageUrl()).into(jobImage);
                }

                seeLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strUri = "http://maps.google.com/maps?q=loc:" + job.getLatitude() + "," + job.getLongitude() + " (" + "User location" + ")";
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }
                });

                // get the phone number of the user that posted the job
                databaseReference.child("Users").child(job.getPostedById()).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userPhone = String.valueOf(snapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}