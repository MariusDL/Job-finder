package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class WorkerSeesJobsAround3 extends AppCompatActivity {

    private TextView jobIdTextView, jobTitleTextView, typeOfJobTextView, dueByTextView, moneyTextView,
            jobDescriptionTextView, jobLocationTextView, postedByTextView;

    private ImageView jobImage;
    private Button acceptJob, seeLocationOnMap;

    private String jobId, nameOfCurrentUser;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_sees_jobs_around3);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Job details");

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            jobId = extras.getString("JobId");
            nameOfCurrentUser = extras.getString("nameOfCurrentUser");
        }

        jobIdTextView = findViewById(R.id.workerSeesJobsAround3JobIdTextView);
        jobTitleTextView = findViewById(R.id.workerSeesJobsAround3JobTitle);
        typeOfJobTextView = findViewById(R.id.workerSeesJobsAround3TypeOfJob);
        dueByTextView = findViewById(R.id.workerSeesJobsAround3DueBy);
        moneyTextView = findViewById(R.id.workerSeesJobsAround3MoneyOffered);
        jobDescriptionTextView = findViewById(R.id.workerSeesJobsAround3JobDescription);
        jobLocationTextView = findViewById(R.id.workerSeesJobsAround3Location);
        postedByTextView = findViewById(R.id.workerSeesJobsAround3PostedBy);
        jobImage = findViewById(R.id.workerSeesJobsAround3JobImage);
        acceptJob = findViewById(R.id.workerSeesJobsAround3AcceptJobButton);
        seeLocationOnMap = findViewById(R.id.workerSeesJobsAround3SeeLocationOnMapButton);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();

        fillFieldsWithData();

        acceptJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmChoice();
            }
        });


    }

    public void confirmChoice(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkerSeesJobsAround3.this);

        alertDialogBuilder.setTitle("Confirm ");

        alertDialogBuilder.setMessage("Are you sure you want to accept the job?");
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                databaseReference.child("Jobs").child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Job job = snapshot.getValue(Job.class);
                        job.setAcceptedByName(nameOfCurrentUser);
                        job.setAcceptedById(userId);
                        job.setStatus("Accepted");

                        databaseReference.child("Jobs").child(jobId).setValue(job);

                        Intent intent = new Intent(WorkerSeesJobsAround3.this, HomeActivityWorker.class);
                        Toast.makeText(WorkerSeesJobsAround3.this, "Job successfully accepted", Toast.LENGTH_SHORT);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

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
                postedByTextView.setText("Posted by: " + job.getPostedByName());
                jobLocationTextView.setText("Location: "+ job.getAddress());

                if (!job.getImageUrl().equals("")) {
                    Picasso.get().load(job.getImageUrl()).into(jobImage);
                }

                seeLocationOnMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strUri = "http://maps.google.com/maps?q=loc:" + job.getLatitude() + "," + job.getLongitude() + " (" + "User location" + ")";
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}