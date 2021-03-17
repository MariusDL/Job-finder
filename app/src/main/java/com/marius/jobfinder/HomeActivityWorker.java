package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivityWorker extends AppCompatActivity {

    private Menu menu;

    private FirebaseAuth mAuth;

    private Button pendingJobs, seeJobsAround;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_worker);

        mAuth = FirebaseAuth.getInstance();

        pendingJobs = findViewById(R.id.homeActivityWorkerPendingJobs);
        seeJobsAround = findViewById(R.id.homeActivityWorkerSeeJobsAround);

        seeJobsAround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivityWorker.this, WorkerSeeJobsAround1.class);
                startActivity(intent);
            }
        });

        pendingJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivityWorker.this, WorkerViewPendingJobs.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        getMenuInflater().inflate(R.menu.user_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.signOutOptionMenu){
            mAuth.signOut();
            Toast.makeText(HomeActivityWorker.this,"Signed out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivityWorker.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()== R.id.optionMenuHome){
            startActivity(new Intent(HomeActivityWorker.this, HomeActivityWorker.class));
            finish();
        }
        if(item.getItemId()==R.id.optionMenuMyAccount){
            startActivity(new Intent(HomeActivityWorker.this, MyAccountActivity.class));

        }
        if(item.getItemId()==R.id.optionMenuJobsHistory){
            Intent intent = new Intent(HomeActivityWorker.this, JobsHistory.class);
            intent.putExtra("typeOfUser", "worker");
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HomeActivityWorker.this.finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}