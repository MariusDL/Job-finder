package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CompletedJobDetails extends AppCompatActivity {

    private TextView jobIdTextView, jobTitleTextView, typeOfJobTextView, dateCompletedTextView, moneyTextView,
            jobDescriptionTextView, locationTextView, completedByTextView;

    private ImageView jobImage;
    private Button contactWorker;

    private String jobId, accountType, userPhone, workerPhone;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_job_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Job details");

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            jobId = extras.getString("jobId");
            accountType = extras.getString("typeOfUser");
        }

        jobIdTextView = findViewById(R.id.completedJobDetailsJobIdTextView);
        jobTitleTextView = findViewById(R.id.completedJobDetailsJobTitle);
        typeOfJobTextView = findViewById(R.id.completedJobDetailsTypeOfJob);
        dateCompletedTextView = findViewById(R.id.completedJobDateCompleted);
        moneyTextView = findViewById(R.id.completedJobDetailsMoneyOffered);
        jobDescriptionTextView = findViewById(R.id.completedJobDetailsJobDescription);
        locationTextView = findViewById(R.id.completedJobDetailsLocation);
        completedByTextView = findViewById(R.id.completedJobDetailsCompletedBy);
        jobImage = findViewById(R.id.completedJobDetailsJobImage);
        contactWorker = findViewById(R.id.completedJobDetailsContactWorkerButton);

        if(!accountType.equals("User")){
            contactWorker.setText("Contact user");
        }


        databaseReference = FirebaseDatabase.getInstance().getReference();

        fillFieldsWithData();

        contactWorker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri number;

                if(!accountType.equals("User")){
                    number = Uri.parse("tel:" + userPhone);
                } else {
                    number = Uri.parse("tel:" + workerPhone);
                }

                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            }
        });

    }

    private void fillFieldsWithData(){

        databaseReference.child("CompletedJobs").child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Job job = snapshot.getValue(Job.class);

                jobIdTextView.setText("Job ID: " + job.getId());
                jobTitleTextView.setText("Job title: " + job.getTitle());
                typeOfJobTextView.setText("Type of job: "+ job.getTypeOfJob());
                dateCompletedTextView.setText("Date completed: " + job.getCompletedDate());
                moneyTextView.setText("Money offered: £" + job.getMoney());
                jobDescriptionTextView.setText("Description: "+job.getDescription());
                locationTextView.setText("Location: " + job.getAddress());
                completedByTextView.setText("Completed by: " + job.getAcceptedByName());

                if (!job.getImageUrl().equals("")) {
                    Picasso.get().load(job.getImageUrl()).into(jobImage);
                }

                if(!accountType.equals("User")){
                    completedByTextView.setText("Posted by: " + job.getPostedByName());
                }

                //get the phone number of user that posted the job
                databaseReference.child("Users").child(job.getPostedById()).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userPhone = String.valueOf(snapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //get the phone number of the worker that completed the job
                databaseReference.child("Users").child(job.getCompletedBy()).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        workerPhone = String.valueOf(snapshot.getValue(String.class));
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