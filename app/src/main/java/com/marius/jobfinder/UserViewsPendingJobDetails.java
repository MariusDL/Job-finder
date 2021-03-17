package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UserViewsPendingJobDetails extends AppCompatActivity {

    private TextView jobIdTextView, jobTitleTextView, typeOfJobTextView, dueByTextView, moneyTextView,
                    jobDescriptionTextView, jobStatusTextView, acceptedByTextView;

    private ImageView jobImage;
    private Button cancelJob, jobCompleted, contactWorker;

    private String jobId, workerPhone;

    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_views_pending_job_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Job details");

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            jobId = extras.getString("jobId");
        }

        jobIdTextView = findViewById(R.id.userViewPendingJobDetailsJobIdTextView);
        jobTitleTextView = findViewById(R.id.userViewPendingJobDetailsJobTitle);
        typeOfJobTextView = findViewById(R.id.userViewPendingJobDetailsTypeOfJob);
        dueByTextView = findViewById(R.id.userViewPendingJobDetailsDueBy);
        moneyTextView = findViewById(R.id.userViewPendingJobDetailsMoneyOffered);
        jobDescriptionTextView = findViewById(R.id.userViewPendingJobDetailsJobDescription);
        jobStatusTextView = findViewById(R.id.userViewPendingJobDetailsJobStatus);
        acceptedByTextView = findViewById(R.id.userViewPendingJobDetailsAcceptedBy);
        jobImage = findViewById(R.id.userViewPendingJobDetailsJobImage);
        cancelJob = findViewById(R.id.userViewPendingJobDetailsCancelJobButton);
        jobCompleted = findViewById(R.id.userViewPendingJobDetailsJobCompletedButton);
        contactWorker = findViewById(R.id.userViewPendingJobDetailsContactWorkerButton);


        databaseReference = FirebaseDatabase.getInstance().getReference();

        jobCompleted.setEnabled(false);
        contactWorker.setEnabled(false);

        fillFieldsWithData();


        jobCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(UserViewsPendingJobDetails.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Complete job")
                        .setMessage("Are you sure you want to complete this job?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                completeJob();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

            }
        });

        cancelJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(UserViewsPendingJobDetails.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Cancel job")
                        .setMessage("Are you sure you want to cancel this job?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelJob();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        contactWorker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri number = Uri.parse("tel:" + workerPhone);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            }
        });


    }

    private void cancelJob(){
        databaseReference.child("Jobs").child(jobId).removeValue();
        Intent intent = new Intent(UserViewsPendingJobDetails.this, HomeActivityUser.class);
        startActivity(intent);
        finish();
    }

    private void completeJob(){
        jobCompleted.setEnabled(false);

        databaseReference.child("Jobs").child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Job job = snapshot.getValue(Job.class);

                job.setCompletedBy(job.getAcceptedById());

                DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy, HH:mm");
                String date = df.format(Calendar.getInstance().getTime());

                job.setCompletedDate(date);
                job.setStatus("Completed");

                databaseReference.child("CompletedJobs").child(jobId).setValue(job);
                databaseReference.child("Jobs").child(jobId).removeValue();

                Toast.makeText(UserViewsPendingJobDetails.this, "Jobs successfully completed", Toast.LENGTH_SHORT);

                Intent intent = new Intent(UserViewsPendingJobDetails.this, HomeActivityUser.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void fillFieldsWithData(){
        databaseReference.child("Jobs").child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Job job = snapshot.getValue(Job.class);

                jobIdTextView.setText("Job ID: " + job.getId());
                jobTitleTextView.setText("Job title: " + job.getTitle());
                typeOfJobTextView.setText("Type of job: "+ job.getTypeOfJob());
                dueByTextView.setText("Due by: " + job.getDueDate());
                moneyTextView.setText("Money offered: £" + job.getMoney());
                jobDescriptionTextView.setText("Description: "+job.getDescription());
                jobStatusTextView.setText("Status: " + job.getStatus());
                if(TextUtils.isEmpty(job.getAcceptedByName())){
                    acceptedByTextView.setText("Accepted by: Pending");
                } else {
                    acceptedByTextView.setText("Accepted by: " + job.getAcceptedByName());
                }
                if (!job.getImageUrl().equals("")) {
                    Picasso.get().load(job.getImageUrl()).into(jobImage);
                }

                if(!TextUtils.isEmpty(job.getAcceptedByName())){
                    jobCompleted.setEnabled(true);
                }

                if(job.getStatus().equals("Accepted")){
                    cancelJob.setEnabled(false);
                    contactWorker.setEnabled(true);

                    // get the phone number of the user that posted the job
                    databaseReference.child("Users").child(job.getAcceptedById()).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            workerPhone = String.valueOf(snapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}