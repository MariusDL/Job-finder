package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyAccountActivity extends AppCompatActivity {

    private static final int GALLERY_CODE = 1;
    private Uri mImageUri;

    private Button selectServices, editProfile, selectProfilePhoto;
    private ImageView profilePhoto;
    private EditText firstNameEditText, lastNameEditText, phoneEditText, passwordEditText;
    private TextView totalJobsTextView;
    private Menu menu;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser mUser;
    private String userId;
    private StorageReference mStorage;

    private User currentUser;
    private Boolean checkEditProfileFlag = false;

    AlertDialog.Builder alertDialogBuilder;

    private ArrayList<String> servicesFromDatabase = new ArrayList<>();

    boolean[] workerServicesBoolean;
    String[] AlertDialogItemsDatabase;

    List<String> selectedItems = new ArrayList<>();

    private String accountType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#10116b")));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My account");

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.darkBlue));
        }



        selectServices = findViewById(R.id.myAccountActivitySelectServicesButton);
        editProfile = findViewById(R.id.myAccountActivityEditProfileButton);
        selectProfilePhoto = findViewById(R.id.myAccountActivitySelectProfilePhotoButton);
        profilePhoto = findViewById(R.id.myAccountActivityProfilePhotoImageView);
        firstNameEditText = findViewById(R.id.myAccountActivityFirstNameEditText);
        lastNameEditText = findViewById(R.id.myAccountActivityLastNameEditText);
        phoneEditText = findViewById(R.id.myAccountActivityPhoneEditText);
        passwordEditText = findViewById(R.id.myAccountActivityPasswordEditText);
        totalJobsTextView = findViewById(R.id.myAccountActivityTotalJobsTextView);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance().getReference("ProfilePhotos");

        // get the services list from database
        servicesFromDatabase = getServicesFromDatabase();


        checkTypeOfUser();
        getCurrentUserFromDatabase();
        enableDisableBoxes(false);

        selectProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

                // SET THE THE TYPE OF THE DATA TO PHOTOS
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkEditProfileFlag){
                    enableDisableBoxes(true);
                    checkEditProfileFlag = true;
                } else {
                    // get the values from the text boxes
                    String firstName = firstNameEditText.getText().toString().trim();
                    String lastName = lastNameEditText.getText().toString().trim();
                    String phone = phoneEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();

                    // check if the text boxes are empty
                    if(!checkEmptyField(firstName)){
                        firstNameEditText.setError("Please complete your first name");
                    } else if(!checkEmptyField(lastName)){
                        lastNameEditText.setError("Please complete your last name");
                    } else if(!checkEmptyField(phone)){
                        phoneEditText.setError("Please complete your phone");
                    } else if(!checkEmptyField(password)){
                        passwordEditText.setError("Please complete your password");
                    } else {
                        // set the new values in the firebase
                        mUser.updatePassword(password);
                        databaseReference.child("Users").child(userId).child("firstName").setValue(firstName);
                        databaseReference.child("Users").child(userId).child("lastName").setValue(lastName);
                        databaseReference.child("Users").child(userId).child("phone").setValue(phone);
                        databaseReference.child("Users").child(userId).child("password").setValue(password);
                    }

                    enableDisableBoxes(false);
                    checkEditProfileFlag=false;

                    Toast.makeText(MyAccountActivity.this, "Profile successfully updated", Toast.LENGTH_SHORT).show();
                }
            }
        });


        selectServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialogBuilder = new AlertDialog.Builder(MyAccountActivity.this);

                // pu the services list taken from database in an array to pass it to the alert dialog
                AlertDialogItemsDatabase = new String[servicesFromDatabase.size()];
                for(int i =0; i<servicesFromDatabase.size();i++){
                    String service = servicesFromDatabase.get(i);
                    AlertDialogItemsDatabase[i]=service;

                }

                // start the alerdialog
                alertDialogBuilder.setMultiChoiceItems(AlertDialogItemsDatabase, workerServicesBoolean, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        workerServicesBoolean[which]=isChecked;
                    }
                });

                alertDialogBuilder.setCancelable(false);


                alertDialogBuilder.setTitle("Select services to provide");
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int a = 0;
                        selectedItems.clear();
                        //put the selected items in an arraylist
                        while(a < workerServicesBoolean.length)
                        {
                            boolean value = workerServicesBoolean[a];
                            if(value){
                                selectedItems.add(servicesFromDatabase.get(a));
                            }
                            a++;
                        }
                        addSelectedServicesToDatabase(selectedItems);
                    }
                });

                alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
            }
        });


    }

    private void enableDisableBoxes(Boolean flag){
        if(!flag){

            // disable the edit text fields with user details
            firstNameEditText.setEnabled(false);
            lastNameEditText.setEnabled(false);
            phoneEditText.setEnabled(false);
            passwordEditText.setEnabled(false);
            editProfile.setText("Edit profile");

            // change the color of the edit texts
            firstNameEditText.setBackgroundResource(R.drawable.disabled_edit_text);
            lastNameEditText.setBackgroundResource(R.drawable.disabled_edit_text);
            phoneEditText.setBackgroundResource(R.drawable.disabled_edit_text);
            passwordEditText.setBackgroundResource(R.drawable.disabled_edit_text);

        } else {
            // enable the edit text fields with user details
            firstNameEditText.setEnabled(true);
            lastNameEditText.setEnabled(true);
            phoneEditText.setEnabled(true);
            passwordEditText.setEnabled(true);
            editProfile.setText("Save changes");

            // change the color of the edit texts
            firstNameEditText.setBackgroundResource(R.drawable.inputs);
            lastNameEditText.setBackgroundResource(R.drawable.inputs);
            phoneEditText.setBackgroundResource(R.drawable.inputs);
            passwordEditText.setBackgroundResource(R.drawable.inputs);
        }
    }


        private void getCurrentUserFromDatabase(){
            // get the user details from firebase
            databaseReference.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    firstNameEditText.setText(user.getFirstName());
                    lastNameEditText.setText(user.getLastName());
                    phoneEditText.setText(user.getPhone());
                    passwordEditText.setText(user.getPassword());
                    totalJobsTextView.setText("Total jobs: "+user.getCompletedJobs().size());

                    //LOAD THE IMAGEVIEW WITH THE URL FROM THE USER OBJECT
                    if (!user.getProfilePhoto().equals("")) {
                        Picasso.get().load(user.getProfilePhoto()).into(profilePhoto);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        //get the services offered by the user from firebase
        databaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                // get list with offered services from the user object
                List<String> servicesOffered = user.getTypesOfOfferedServices();

                // create a boolean list with which services the user has to pass it to the alertdialog builder
                workerServicesBoolean = new boolean[servicesOffered.size()];
                for(int i =0; i<servicesOffered.size();i++){
                    String service = servicesOffered.get(i);
                    if(service.equals("No")){
                        workerServicesBoolean[i]=false;
                    }else {
                        workerServicesBoolean[i]=true;
                    }
                }

                 // THIS NEEDS TO BE CHECKED - - - PRODUCES ERROR WHEN YOU ENTER THE ACTIVITY AGAIN

//                // if there is a new service added in the database, put that in the user list with a no value
//                if(workerServicesBoolean.length != servicesFromDatabase.size()){
//                    boolean[] temp = Arrays.copyOf(workerServicesBoolean,  servicesFromDatabase.size());
//
//                    for(int i = 0; i< servicesFromDatabase.size()-workerServicesBoolean.length; i++){
//                        temp[temp.length -1 -i] = false;
//                    }
//                    workerServicesBoolean = Arrays.copyOf(temp, temp.length);
//                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void checkTypeOfUser(){
        // get the  type of user from database to start the corresponding activity
        databaseReference.child("Users").child(userId).child("accountType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String accType = snapshot.getValue(String.class);

                if(accType.equals("User")){
                    selectServices.setVisibility(View.INVISIBLE);
                    accountType = String.valueOf(accType);
                } else {
                    selectServices.setVisibility(View.VISIBLE);
                    accountType = String.valueOf(accType);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    // if an image is selected from the phone upload the image on firebase
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            profilePhoto.setImageURI(mImageUri);

//            //GET THE FILE NAME
//            List<String> pathSegments = mImageUri.getPathSegments();
//            String lastSegment = pathSegments.get(pathSegments.size() - 1);

            final StorageReference filepath = mStorage.child(userId);

            filepath.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            databaseReference.child("Users").child(userId).child("profilePhoto").setValue(uri.toString());
                            Toast.makeText(MyAccountActivity.this, "Profile photo successfully changed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private Boolean checkEmptyField(String field){
        if(TextUtils.isEmpty(field)){
            return false;
        } else {
            return true;
        }
    }

    // get the list of services from the database
    private  ArrayList<String> getServicesFromDatabase(){
        final ArrayList<String> servDB = new ArrayList<>();

        databaseReference.child("Services").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> serv = (ArrayList) snapshot.getValue();
                for(int i =0; i<serv.size();i++){
                    servDB.add(i, serv.get(i));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return servDB;
    }

    // this will update the selected services by the user in the database
    private void addSelectedServicesToDatabase(List<String> selectedItems){
        ArrayList<String> typesOfOfferedServices = new ArrayList<>();
        for(int i = 0; i<servicesFromDatabase.size(); i++){
            if(selectedItems.contains(servicesFromDatabase.get(i))){
                typesOfOfferedServices.add("Yes");
            }else{
                typesOfOfferedServices.add("No");
            }
        }
        databaseReference.child("Users").child(userId).child("typesOfOfferedServices").setValue(typesOfOfferedServices);
        Toast.makeText(MyAccountActivity.this, "Services list successfully updated", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MyAccountActivity.this,"Signed out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MyAccountActivity.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()== R.id.optionMenuHome){
            if(accountType.equals("User")){
                startActivity(new Intent(MyAccountActivity.this, HomeActivityUser.class));
            } else {
                startActivity(new Intent(MyAccountActivity.this, HomeActivityWorker.class));
            }
            finish();
        }
        if(item.getItemId()==R.id.optionMenuMyAccount){
            startActivity(new Intent(MyAccountActivity.this, MyAccountActivity.class));

        }
        if(item.getItemId()==R.id.optionMenuJobsHistory){
            Intent intent = new Intent(MyAccountActivity.this, JobsHistory.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}