package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class JobsHistory extends AppCompatActivity {

    private RecyclerView recyclerView;

    private DatabaseReference databaseReference, databaseReferenceUser;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;

    private String accountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs_history);

        recyclerView = findViewById(R.id.jobsHistoryRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("CompletedJobs");

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference();

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            accountType = extras.getString("typeOfUser");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query;

        // IMPORTANT - QUERY IN FIREBASE

        if(accountType.equals("User")){
            query = databaseReference.orderByChild("postedById").equalTo(userId);
        } else {
            query = databaseReference.orderByChild("completedBy").equalTo(userId);
        }


        FirebaseRecyclerOptions<Job> options = new FirebaseRecyclerOptions.Builder<Job>()
                .setQuery(query, Job.class).build();


        FirebaseRecyclerAdapter<Job, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Job, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull final Job model) {

                holder.setJobTitle(model.getTitle());
                holder.setTypeOfJob(model.getTypeOfJob());
                holder.setDateCompleted(model.getCompletedDate());

                holder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String jobId = model.getId();
                        Intent intent = new Intent(JobsHistory.this, CompletedJobDetails.class);
                        intent.putExtra("jobId", jobId);
                        intent.putExtra("typeOfUser", accountType);
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.jobs_history_job_in_list, parent,false);
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
            TextView jobTitleTextView = myView.findViewById(R.id.layoutJobsHistoryJobTitle);
            jobTitleTextView.setText("Job title: " + title);
        }

        public void setTypeOfJob(String typeOfJob){
            TextView typeOfJobTextView = myView.findViewById(R.id.layoutJobsHistoryTypeOfJob);
            typeOfJobTextView.setText("Type of job: "+typeOfJob);
        }
        public void setDateCompleted(String date){
            TextView dueByTextView = myView.findViewById(R.id.layoutJobsHistoryDateCompleted);
            dueByTextView.setText("Date completed: "+date);
        }
    }


}