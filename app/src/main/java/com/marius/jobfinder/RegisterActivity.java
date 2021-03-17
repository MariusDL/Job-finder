package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private TextView goToLogin;
    private EditText firstName, lastName, phone, email, password;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;
    private DatabaseReference databaseReference;

    private ProgressDialog loader;

    private ArrayList<String> services = new ArrayList<>();

    private Spinner accountType;
    String[] accountTypeItems = new String[]{"User", "Worker"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //hide the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Set the spinner
        accountType = findViewById(R.id.registerActivityAccountType);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, accountTypeItems);
        //set the spinners adapter to the previously created one.
        accountType.setAdapter(adapter);


        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        firstName = findViewById(R.id.registerActivityFirstNameEditText);
        lastName = findViewById(R.id.registerActivityLastNameEditText);
        phone = findViewById(R.id.registerActivityPhoneEditText);
        email = findViewById(R.id.registerActivityEmailEditText);
        password = findViewById(R.id.registerActivityPasswordEditText);
        registerButton = findViewById(R.id.registerActivityRegisterButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fn = firstName.getText().toString().trim();
                final String ln = lastName.getText().toString().trim();
                final String ph = phone.getText().toString().trim();
                final String em = email.getText().toString().trim();
                final String pass = password.getText().toString().trim();
                final String accType = accountType.getSelectedItem().toString().trim();

                Log.d("################", "#"+ accType+"#");

                if(!checkEmptyField(fn)){
                    firstName.setError("Please complete your first name");
                } else if(!checkEmptyField(ln)){
                    lastName.setError("Please complete your last name");
                } else if(!checkEmptyField(ph)){
                    phone.setError("Please complete your phone");
                } else if(!checkEmptyField(em)){
                    email.setError("Please complete your email");
                } else if(!checkEmptyField(pass)){
                    password.setError("Please complete your password");

                } else {
                    loader.setMessage("Registration in progress");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.createUserWithEmailAndPassword(em, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                userId = mAuth.getCurrentUser().getUid();
                                User user = new User(fn, ln, ph, em, pass,accType);
                                user.setId(userId);

                                user.setTypesOfOfferedServices(services);
                                databaseReference.child("Users").child(userId).setValue(user);

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                loader.dismiss();
                                finish();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }
            }
        });


        // Go to login textView
        goToLogin = findViewById(R.id.registerActivityGoToLoginTextView);
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Get the number of offered services from database and set all to value no to the user
        services = getServicesFromDatabase();
        Log.d("##########", services.size() + " SERVICES");
    }

    private  ArrayList<String> getServicesFromDatabase(){
        final ArrayList<String> servDB = new ArrayList<>();

        databaseReference.child("Services").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> serv = (ArrayList) snapshot.getValue();

                Log.d("##########", serv.size() + " DATASNAPSHOT");

                for(int i =0; i<serv.size();i++){
                    servDB.add(i, "No");
                    Log.d("##########", services.size() + " SERVICES DATASNAPSHOT");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return servDB;
    }

    private Boolean checkEmptyField(String field){
        if(TextUtils.isEmpty(field)){
            return false;
        } else {
            return true;
        }
    }
}