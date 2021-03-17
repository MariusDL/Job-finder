package com.marius.jobfinder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddNewJobActivityPart2 extends AppCompatActivity {

    private CheckBox fixedTime, dueBy;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button next;

    private String date, time, typeOfDeadline;
    private String jobTitle, jobDescription, chosenTypeOfJob, money, imageUrl, jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_job_part_2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add job");

        fixedTime = findViewById(R.id.addNewJobActivity2SpecificTimeCheckBox);
        dueBy = findViewById(R.id.addNewJobActivity2DueByCheckBox);
        datePicker = findViewById(R.id.addNewJobActivity2SelectDate);
        timePicker = findViewById(R.id.addNewJobActivity2SelectTime);
        next = findViewById(R.id.addNewJobActivity2NextButton);




        fixedTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeOfDeadline = "Fixed";
                dueBy.setChecked(false);
            }
        });

        dueBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeOfDeadline = "dueBy";
                fixedTime.setChecked(false);
            }
        });

        Bundle extras = getIntent().getExtras();
            if (extras != null){
            jobId = extras.getString("jobId");
            jobTitle = extras.getString("jobTitle");
            jobDescription = extras.getString("jobDescription");
            chosenTypeOfJob = extras.getString("chosenTypeOfJob");
            money = extras.getString("money");
            imageUrl = extras.getString("imageUrl");
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(typeOfDeadline)){
                    Toast.makeText(AddNewJobActivityPart2.this, "Please select a box", Toast.LENGTH_SHORT).show();
                } else {
                    String day = "" + datePicker.getDayOfMonth();
                    String month = monthToText(datePicker.getMonth());
                    String year = "" + datePicker.getYear();
                    String hour = "" + timePicker.getHour();
                    String min = "" + timePicker.getMinute();

                    date = day+"/"+month+"/"+year+" at "+hour+":"+min;
                    Log.d("###########", "date: "+ date);

                    Intent intent = new Intent(AddNewJobActivityPart2.this, AddNewJobActivityPart3.class);
                    intent.putExtra("jobId", jobId);
                    intent.putExtra("jobTitle", jobTitle);
                    intent.putExtra("jobDescription", jobDescription);
                    intent.putExtra("chosenTypeOfJob", chosenTypeOfJob);
                    intent.putExtra("money", money);
                    intent.putExtra("imageUrl", imageUrl);
                    intent.putExtra("date", date);
                    intent.putExtra("typeOfDeadline", typeOfDeadline);

                    startActivity(intent);

                }
            }
        });

    }

    private String monthToText(int month){
        String monthText = "";
        switch (month){
            case 1:
                monthText = "Jan";
                break;
            case 2:
                monthText = "Feb";
                break;
            case 3:
                monthText = "Mar";
                break;
            case 4:
                monthText = "Apr";
                break;
            case 5:
                monthText = "May";
                break;
            case 6:
                monthText = "Jun";
                break;
            case 7:
                monthText = "Jul";
                break;
            case 8:
                monthText = "Aug";
                break;
            case 9:
                monthText = "Sep";
                break;
            case 10:
                monthText = "Oct";
                break;
            case 11:
                monthText = "Nov";
                break;
            case 12:
                monthText = "Dec";
                break;
        }

        return monthText;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}