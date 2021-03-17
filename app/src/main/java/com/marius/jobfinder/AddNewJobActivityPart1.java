package com.marius.jobfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddNewJobActivityPart1 extends AppCompatActivity {

    private static final int GALLERY_CODE = 1;
    private Uri mImageUri;

    private Button nextButton, selectTypeOfJob, addImageButton;
    private EditText jobTitleEditText, jobDescriptionEditText, moneyOfferedEditText;
    private ImageView jobImageImageView;

    private String jobId, imageUrl;

    AlertDialog.Builder alertDialogBuilder;
    private ArrayList<String> servicesFromDatabase = new ArrayList<>();
    String[] AlertDialogItemsDatabase;
    private int chosenTypeOfJob = -1;


    private DatabaseReference databaseReference;
    private StorageReference mStorage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_job_part_1);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add job");

        jobId = UUID.randomUUID().toString();
        Log.d("#########", "Job id: " + jobId);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference("JobsPhotos");
        servicesFromDatabase = getServicesFromDatabase();


        nextButton = findViewById(R.id.addNewJobActivityNextButton);
        selectTypeOfJob = findViewById(R.id.addNewJobActivitySelectTypeOfJob);
        addImageButton = findViewById(R.id.addNewJobActivityAddImageButton);
        jobTitleEditText = findViewById(R.id.addNewJobActivityJobTitle);
        jobDescriptionEditText = findViewById(R.id.addNewJobActivityJobDescription);
        moneyOfferedEditText = findViewById(R.id.addNewJobActivityJobMoney);
        jobImageImageView = findViewById(R.id.addNewJobActivityJobImage);



        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);

                // SET THE THE TYPE OF THE DATA TO PHOTOS
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        selectTypeOfJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogBuilder = new AlertDialog.Builder(AddNewJobActivityPart1.this);

                // put the services list taken from database in an array to pass it to the alert dialog
                AlertDialogItemsDatabase = new String[servicesFromDatabase.size()];
                for(int i =0; i<servicesFromDatabase.size();i++){
                    String service = servicesFromDatabase.get(i);
                    AlertDialogItemsDatabase[i]=service;
                }

                alertDialogBuilder.setSingleChoiceItems(AlertDialogItemsDatabase, chosenTypeOfJob, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenTypeOfJob = which;
                    }
                });

                alertDialogBuilder.setCancelable(false);

                alertDialogBuilder.setTitle("Select the type of job");
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selectTypeOfJob.setText("Job type: " + servicesFromDatabase.get(chosenTypeOfJob));
                        dialog.dismiss();


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


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String jobTitle = jobTitleEditText.getText().toString().trim();
                String jobDescription = jobDescriptionEditText.getText().toString().trim();
                String money = moneyOfferedEditText.getText().toString().trim();

                if(!checkEmptyField(jobTitle)){
                    jobTitleEditText.setError("Please complete the job title");
                } else if(!checkEmptyField(jobDescription)){
                    jobDescriptionEditText.setError("Please complete the job description");
                } else if(!checkEmptyField(money)){
                    moneyOfferedEditText.setError("Please complete the money offered");
                } else if(!checkEmptyField(imageUrl)){
                    Toast.makeText(AddNewJobActivityPart1.this, "Please add an image", Toast.LENGTH_SHORT).show();
                } else if(chosenTypeOfJob == -1){
                    Toast.makeText(AddNewJobActivityPart1.this, "Please Select the type of job", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(AddNewJobActivityPart1.this, AddNewJobActivityPart2.class);
                    intent.putExtra("jobId", jobId);
                    intent.putExtra("jobTitle", jobTitle);
                    intent.putExtra("jobDescription", jobDescription);
                    intent.putExtra("chosenTypeOfJob", servicesFromDatabase.get(chosenTypeOfJob));
                    intent.putExtra("money", money);
                    intent.putExtra("imageUrl", imageUrl);
                    startActivity(intent);
                }

            }
        });

    }

    private ArrayList<String> getServicesFromDatabase(){
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
    private Boolean checkEmptyField(String field){
        if(TextUtils.isEmpty(field)){
            return false;
        } else {
            return true;
        }
    }

    // if an image is selected from the phone upload the image on firebase
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            jobImageImageView.setImageURI(mImageUri);

            final StorageReference filepath = mStorage.child(jobId);

            filepath.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrl=uri.toString();
                            Log.d("#########", "Image URL" + imageUrl);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}