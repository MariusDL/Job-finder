package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WorkerSeesJobsAround2 extends AppCompatActivity {

    private ArrayList<String> jobsAvailableForTheUserFromPreviousActivity = new ArrayList<>();
    private ArrayList<String> distancesToJobsFromPreviousActivity = new ArrayList<>();

    private ArrayList<Job> jobsAvailableForTheUser = new ArrayList<>();

    private RecyclerView recyclerView;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;

    private String nameOfCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_sees_jobs_around2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Available jobs");

        // get the jobs available for the user from the previous activity
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            for(int i=0; i<extras.size()-1; i++){

                String[] arrOfStr = extras.getString(String.valueOf(i)).split(",");

                jobsAvailableForTheUserFromPreviousActivity.add(arrOfStr[0]);
                distancesToJobsFromPreviousActivity.add(arrOfStr[1]);

            }
            nameOfCurrentUser = extras.getString("nameOfCurrentUser");
        }

        // initialize the recyclerview
        recyclerView = findViewById(R.id.workerSeeJobsAround2RecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Jobs");


//        // TEST - DELETE AFTER
//        for(int i=0; i<jobsAvailableForTheUser.size(); i++){
//            Log.d("********* JOBS + DISTANCES", i + ": " +jobsAvailableForTheUser.get(i) + " -> " + distancesToJobs.get(i));
//        }

        // get the complete jobs details from the database and set them in the adapter
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //jobsAvailableForTheUser.clear();

                for (DataSnapshot jobSnapshot : snapshot.getChildren()){
                    Job job = jobSnapshot.getValue(Job.class);
                    if(jobsAvailableForTheUserFromPreviousActivity.contains(job.getId())){
                        jobsAvailableForTheUser.add(job);
                    }
                }

                //sort the jobs in the list by distance from the user
                for(int i =0; i<distancesToJobsFromPreviousActivity.size()-1;i++){
                    if(Float.valueOf(distancesToJobsFromPreviousActivity.get(i+1))>Float.valueOf(distancesToJobsFromPreviousActivity.get(i))){
                        String distanceTemp = distancesToJobsFromPreviousActivity.get(i+1);
                        distancesToJobsFromPreviousActivity.add(i, distanceTemp);
                        distancesToJobsFromPreviousActivity.remove(i+2);

                        Job tempJob = jobsAvailableForTheUser.get(i+1);
                        jobsAvailableForTheUser.add(i, tempJob);
                        jobsAvailableForTheUser.remove(i+2);

                        i=0;
                    }
                }

                RVAdapter adapter = new RVAdapter(jobsAvailableForTheUser, distancesToJobsFromPreviousActivity, WorkerSeesJobsAround2.this);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                adapter.setOnItemClickListener(new RVAdapter.MyViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        Intent intent = new Intent(WorkerSeesJobsAround2.this, WorkerSeesJobsAround3.class);
                        intent.putExtra("JobId", jobsAvailableForTheUser.get(position).getId());
                        intent.putExtra("nameOfCurrentUser", nameOfCurrentUser);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    // The recyclerview adapter class
    public static class RVAdapter extends RecyclerView.Adapter<RVAdapter.MyViewHolder> {

        private static Context context;
        List<Job> jobs;
        List<String> distances;
        private static RVAdapter.MyViewHolder.ClickListener clickListener;


        public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            View myView;

            public MyViewHolder(@NonNull View itemView, Context ctx) {
                super(itemView);
                myView = itemView;
                context = ctx;
                myView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                clickListener.onItemClick(getAdapterPosition(),view);

            }

            public interface ClickListener{
                void onItemClick(int position, View v);
            }


            public void setJobTitle(String title){
                TextView jobTitleTextView = myView.findViewById(R.id.layoutJobsAroundJobTitle);
                jobTitleTextView.setText("Job title: " + title);
            }

            public void setTypeOfJob(String typeOfJob){
                TextView typeOfJobTextView = myView.findViewById(R.id.layoutJobsAroundTypeOfJob);
                typeOfJobTextView.setText("Type of job: "+typeOfJob);
            }
            public void setDueBy(String date){
                TextView dueByTextView = myView.findViewById(R.id.layoutJobsAroundDueBy);
                dueByTextView.setText("Due by: "+date);
            }

            public void setDistance(String distance){
                TextView dueByTextView = myView.findViewById(R.id.layoutJobsAroundDistance);
                Float miles = Float.valueOf(distance)/1609;
                String milesStr = String.format("Distance: %.2fm", miles);
                dueByTextView.setText(milesStr);
            }

            public void setMoneyOffered(String moneyOffered){
                TextView dueByTextView = myView.findViewById(R.id.layoutJobsAroundMoneyOffered);
                dueByTextView.setText("Money: £"+moneyOffered);
            }

        }

        public RVAdapter(ArrayList<Job> jobs, ArrayList<String> distances, Context context){
            this.jobs = jobs;
            this.distances=distances;
            this.context = context;

        }

        public void setOnItemClickListener(MyViewHolder.ClickListener clickListener) {
            RVAdapter.clickListener = clickListener;

        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.jobs_around_job_in_list, parent, false);
            return new MyViewHolder(v,context);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.setJobTitle(jobs.get(position).getTitle());
            holder.setTypeOfJob(jobs.get(position).getTypeOfJob());
            holder.setDueBy(jobs.get(position).getDueDate());
            holder.setDistance(distances.get(position));
            holder.setMoneyOffered(jobs.get(position).getMoney());
        }

        @Override
        public int getItemCount() {
            return jobs.size();
        }
    }

}