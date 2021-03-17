package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivityUser extends AppCompatActivity {

    private Menu menu;

    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference;

    private Button addJob, myJobs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#10116b")));

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.darkBlue));
        }

        addJob = findViewById(R.id.homeActivityUserAddJobButton);
        myJobs = findViewById(R.id.homeActivityUserMyJobsButton);

        addJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivityUser.this, AddNewJobActivityPart1.class);
                startActivity(intent);
            }
        });

        myJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivityUser.this, UserPendingJobs.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


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
            Toast.makeText(HomeActivityUser.this,"Signed out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivityUser.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()== R.id.optionMenuHome){
            startActivity(new Intent(HomeActivityUser.this, HomeActivityUser.class));
            finish();
        }
        if(item.getItemId()==R.id.optionMenuMyAccount){
            startActivity(new Intent(HomeActivityUser.this, MyAccountActivity.class));

        }
        if(item.getItemId()==R.id.optionMenuJobsHistory){
            Intent intent = new Intent(HomeActivityUser.this, JobsHistory.class);
            intent.putExtra("typeOfUser", "User");
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
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}