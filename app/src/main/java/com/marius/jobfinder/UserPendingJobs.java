package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

public class UserPendingJobs extends AppCompatActivity {

    private RecyclerView recyclerView;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pending_jobs);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Your pending jobs");

        recyclerView = findViewById(R.id.userPendingJobsRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Jobs");


    }

    @Override
    protected void onStart() {
        super.onStart();

        // IMPORTANT - QUERY IN FIREBASE
        Query query = databaseReference.orderByChild("postedById").equalTo(userId);

        FirebaseRecyclerOptions<Job> options = new FirebaseRecyclerOptions.Builder<Job>()
                .setQuery(query, Job.class).build();


        FirebaseRecyclerAdapter<Job, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Job, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull final Job model) {

                holder.setJobTitle(model.getTitle());
                holder.setTypeOfJob(model.getTypeOfJob());
                holder.setDueBy(model.getDueDate());
                holder.setJobStatus(model.getStatus());

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String jobId = model.getId();
                        Intent intent = new Intent(UserPendingJobs.this, UserViewsPendingJobDetails.class);
                        intent.putExtra("jobId", jobId);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_pending_job_in_list, parent,false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }
        public void setJobTitle(String title){
            TextView jobTitleTextView = myView.findViewById(R.id.layoutUserPendingJobInListJobTitle);
            jobTitleTextView.setText("Job title: " + title);
        }

        public void setTypeOfJob(String typeOfJob){
            TextView typeOfJobTextView = myView.findViewById(R.id.layoutUserPendingJobInListTypeOfJob);
            typeOfJobTextView.setText("Type of job: "+typeOfJob);
        }
        public void setDueBy(String date){
            TextView dueByTextView = myView.findViewById(R.id.layoutUserPendingJobInListDueBy);
            dueByTextView.setText("Due date: "+date);
        }

        public void setJobStatus(String status){
            TextView jobStatusTextView = myView.findViewById(R.id.layoutUserPendingJobInListStatus);
            jobStatusTextView.setText("Status: "+status);
        }
    }
}