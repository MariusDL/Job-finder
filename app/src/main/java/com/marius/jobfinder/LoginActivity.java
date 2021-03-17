package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextView goToRegister;
    private EditText email, password;
    private Button loginButton;

    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser mUser;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //hide the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        email = findViewById(R.id.loginActivityEmailEditText);
        password = findViewById(R.id.loginActivityPasswordEditText);
        loginButton = findViewById(R.id.loginActivityLoginButton);

        // checks if user is authenticated
        if(mAuth.getCurrentUser() != null){
            userId = mAuth.getCurrentUser().getUid();
            loader.setMessage("Login in progress");
            loader.setCanceledOnTouchOutside(false);
            loader.show();
            checkTypeOfUser();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String em = email.getText().toString().trim();
                final String pass = password.getText().toString().trim();

                if(!checkEmptyField(em)){
                    email.setError("Please complete your email");
                } else if(!checkEmptyField(pass)){
                    password.setError("Please complete your password");
                } else {
                    loader.setMessage("Login in progress");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.signInWithEmailAndPassword(em, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                userId = mAuth.getCurrentUser().getUid();
                                checkTypeOfUser();

                            } else {
                                String error = task.getException().toString();
                                loader.dismiss();
                                Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // go to register text view
        goToRegister = findViewById(R.id.loginActivityCreateAccountTextView);
        goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private Boolean checkEmptyField(String field){
        if(TextUtils.isEmpty(field)){
            return false;
        } else {
            return true;
        }
    }

    private void checkTypeOfUser(){
        // get the  type of user from database to start the corresponding activity(normal user/worker)
        databaseReference.child("Users").child(userId).child("accountType").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String accType = snapshot.getValue(String.class);

                if(accType.equals("User")){
                    Intent intent = new Intent(LoginActivity.this, HomeActivityUser.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LoginActivity.this, HomeActivityWorker.class);
                    startActivity(intent);
                }
                loader.dismiss();
                finish();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}