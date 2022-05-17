package com.example.tlfsvf;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;

    private String key="";
    private String course;
    private String description;
    private String location;
    private String instructor;
    private String credits;
    List<Double> mMarks = new ArrayList<>();
    List<Double> mMarksMax = new ArrayList<>();
    List<Integer> mMarksPercent = new ArrayList<>();

    private ProgressDialog loader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        toolbar = findViewById(R.id.coursesToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Progress");

        recyclerView = findViewById(R.id.recyclerViewCourses);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        onlineUserID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("courses").child(onlineUserID);

        /*
        floatingActionButton = findViewById(R.id.fabCourses);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCourse();
            }
        });*/


    }

    /*
    private void addCourse() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_file_courses, null);
        myDialog.setView(myView);

        AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        dialog.show();

        final EditText course = myView.findViewById(R.id.course);
        final EditText description = myView.findViewById(R.id.courseDescription);
        final EditText credits = myView.findViewById(R.id.courseCredits);
        final EditText instructor = myView.findViewById(R.id.courseInstructor);
        final EditText location = myView.findViewById(R.id.courseLocation);

        AppCompatButton save = myView.findViewById(R.id.saveButtonCourse);
        AppCompatButton cancel = myView.findViewById(R.id.cancelButtonCourse);
        cancel.setOnClickListener((v)->{dialog.dismiss();});

        save.setOnClickListener((v)->{
            String mCourse = course.getText().toString();
            String mDescription = description.getText().toString().trim();
            String mLocation = location.getText().toString().trim();
            String mInstructor = instructor.getText().toString().trim();
            String mCredits = credits.getText().toString().trim();
            String id = reference.push().getKey();
            String date = DateFormat.getDateInstance().format(new Date());
            List<Double> mMarks1 = new ArrayList<>();
            List<Double> mMarks1Max = new ArrayList<>();
            List<Integer> mMarks1Percent = new ArrayList<>();
            mMarks1.add(0.0);
            mMarks1Max.add(0.0);
            mMarks1Percent.add(0);

            if(TextUtils.isEmpty(mCourse)){
                course.setError("Course name required");
                return;
            }
            if(TextUtils.isEmpty(mDescription)){
                description.setError("Course description required");
                return;
            }
            if(TextUtils.isEmpty(mCredits)){
                credits.setError("Course credits required");
                return;
            }
            if(TextUtils.isEmpty(mInstructor)){
                instructor.setError("Course instructor required");
                return;
            }

            if(TextUtils.isEmpty(mLocation)){
                location.setError("Course location required");
                return;
            }else{
                loader.setMessage("Adding your new course");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                CourseModel modelCourse = new CourseModel(mCourse, mDescription, id, date, mMarks1, mCredits, mInstructor, mLocation, mMarks1Max, mMarks1Percent);
                reference.child(id).setValue(modelCourse).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ProgressActivity.this, "The course was added successfully", Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(ProgressActivity.this, "Failed " + error, Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }
                    }
                });
            }
            dialog.dismiss();
        });




    }*/

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<CourseModel> options = new FirebaseRecyclerOptions.Builder<CourseModel>().setQuery(reference, CourseModel.class).build();
        FirebaseRecyclerAdapter<CourseModel, MyViewHolder> adapter = new FirebaseRecyclerAdapter<CourseModel, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull CourseModel cmodel) {
                holder.setDate(cmodel.getDate());
                holder.setTask(cmodel.getCourse());
                holder.setDescription(cmodel.getDescription());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(position).getKey();
                        course = cmodel.getCourse();
                        description = cmodel.getDescription();
                        location = cmodel.getLocation();
                        instructor = cmodel.getInstructor();
                        credits = cmodel.getCredits();
                        mMarks = cmodel.getMarks();
                        mMarksMax = cmodel.getMarksMax();
                        mMarksPercent = cmodel.getmMarksPercent();


                        viewProgress();



                    }
                });


            }

            private void viewProgress() {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(ProgressActivity.this);
                LayoutInflater inflater =LayoutInflater.from(ProgressActivity.this);
                View view = inflater.inflate(R.layout.update_data_course, null);
                mDialog.setView(view);


                AlertDialog dialog = mDialog.create();

                EditText mCourse = view.findViewById(R.id.mEditTextCourse);
                EditText mDescription = view.findViewById(R.id.mEditTextDescriptionCourse);
                EditText mCredits = view.findViewById(R.id.mEditTextCreditsCourse);
                EditText mInstructor = view.findViewById(R.id.mEditTextInstructorCourse);
                EditText mLocation = view.findViewById(R.id.mEditTextLocationCourse);
                EditText mMark = view.findViewById(R.id.mEditTextGrade);
                EditText mMarkMax = view.findViewById(R.id.mEditTextGradeMax);
                EditText mMarkPercent = view.findViewById(R.id.mEditTextPercentageCourse);

                mCourse.setText(course);
                mCourse.setSelection(course.length());

                mDescription.setText(description);
                mDescription.setSelection(description.length());

                mCredits.setText(credits);
                mCredits.setSelection(credits.length());

                mInstructor.setText(instructor);
                mInstructor.setSelection(instructor.length());

                mLocation.setText(location);
                mLocation.setSelection(location.length());

                AppCompatButton delBtn = view.findViewById(R.id.deleteBtnCourse);
                AppCompatButton updateBtn = view.findViewById(R.id.UpdateBtnCourse);

                /*
                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        course = mCourse.getText().toString().trim();
                        description = mDescription.getText().toString().trim();
                        credits = mCredits.getText().toString().trim();
                        instructor = mInstructor.getText().toString().trim();
                        location = mLocation.getText().toString().trim();

                        String markText, markTextMax, percentText;
                        Double mark, markMax;
                        Integer percent;
                        markText = mMark.getText().toString().trim();
                        mark = Double.parseDouble(markText);
                        markTextMax = mMarkMax.getText().toString().trim();
                        markMax = Double.parseDouble(markTextMax);
                        percentText= mMarkPercent.getText().toString().trim();
                        percent= Integer.parseInt(percentText);
                        mMarks.add(mark);
                        mMarksMax.add(markMax);
                        mMarksPercent.add(percent);

                        String date = DateFormat.getDateInstance().format(new Date());

                        CourseModel cmodel = new CourseModel(course, description, key, date, mMarks, credits, instructor, location, mMarksMax, mMarksPercent);


                        reference.child(key).setValue(cmodel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(ProgressActivity.this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                                }else {
                                    String error = task.getException().toString();
                                    Toast.makeText(ProgressActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });


                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(ProgressActivity.this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                                }else{
                                    String error = task.getException().toString();
                                    Toast.makeText(ProgressActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialog.dismiss();
                    }
                });

                 */
                dialog.show();




            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrived_layout_course, parent, false);
                return new MyViewHolder(view);

            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setTask (String course){
            TextView courseTV = mView.findViewById(R.id.courseTv);
            courseTV.setText (course);
        }

        public void setDescription (String description){
            TextView descriptionTV = mView.findViewById(R.id.descriptionTvCourse);
            descriptionTV.setText(description);
        }

        public void setDate(String date){
            TextView dateTV = mView.findViewById(R.id.dateTvCourse);
            dateTV.setText(date);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();

        }

        return super.onOptionsItemSelected(item);
    }

}