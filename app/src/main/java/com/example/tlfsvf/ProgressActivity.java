/*
package com.example.tlfsvf;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private GraphView graphView;
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
    private String minGrade;
    private String endDate;
    List<String> mMarks = new ArrayList<>();
    List<String> mMarksMax = new ArrayList<>();
    List<String> mMarksPercent = new ArrayList<>();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY");

    private ProgressDialog loader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        toolbar = findViewById(R.id.progressToolbar);
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




    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<CourseModel> options = new FirebaseRecyclerOptions.Builder<CourseModel>().setQuery(reference, CourseModel.class).build();
        FirebaseRecyclerAdapter<CourseModel, MyViewHolder> adapter = new FirebaseRecyclerAdapter<CourseModel, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull CourseModel cmodel) {

                holder.setTask(cmodel.getCourse());

                key = getRef(position).getKey();

                DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("courses").child(onlineUserID).child(key);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String getCurrentDateTime = sdf.format(c.getTime());



                reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String text;
                        //boolean done = (boolean) snapshot.child("done").getValue();

                        List<String> marks;
                        List<String> marksPercent;

                        marks = cmodel.getMarks();
                        marksPercent = cmodel.getmMarksPercent();


                        Double med = 0.0;

                        for (int i=0; i<marks.size();i++){
                            med = med + Double.parseDouble(marks.get(i))*(Double.parseDouble(marksPercent.get(i))/100.0);
                        }


                        Double minMark = Double.parseDouble(snapshot.child("minGrade").getValue().toString());


                        if (getCurrentDateTime.compareTo(cmodel.getEndDate()) < 0) {
                            text = "STILL IN PROGRESS ";
                        } else {
                            if (med < minMark)
                            {
                                text = "FAILED";
                            }
                            else
                            {
                                text = "PASSED";
                            }

                        }


                        holder.setDate(text);
                        reff.removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
                        minGrade = cmodel.getMinGrade();
                        endDate = cmodel.getEndDate();


                        viewProgress();



                    }
                });


            }

            private void viewProgress() {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(ProgressActivity.this);
                LayoutInflater inflater =LayoutInflater.from(ProgressActivity.this);
                View view = inflater.inflate(R.layout.course_progress, null);
                mDialog.setView(view);


                AlertDialog dialog = mDialog.create();

                TextView mCourse = view.findViewById(R.id.courseText);
                TextView mDescription = view.findViewById(R.id.courseDescriptionText);
                TextView mCredits = view.findViewById(R.id.courseCreditsText);
                TextView mMinGrade = view.findViewById(R.id.courseMinGradeText);
                TextView mInstructor = view.findViewById(R.id.courseInstructorText);
                TextView mLocation = view.findViewById(R.id.courseLocationText);
                TextView mMed = view.findViewById(R.id.currentGradeText);
                TextView mMedMax = view.findViewById(R.id.currentMaxGradeText);
                TextView mStatus = view.findViewById(R.id.courseStatText);


                TextView mEndDate = view.findViewById(R.id.courseDueDateText);

                mCourse.setText(course);

                mDescription.setText(description);

                mCredits.setText(credits);

                mMinGrade.setText(minGrade);

                mInstructor.setText(instructor);

                mLocation.setText(location);
                mEndDate.setText(endDate);

                androidx.appcompat.widget.AppCompatButton cancel = view.findViewById(R.id.cancelButtonCourseProgress);
                cancel.setOnClickListener((v)->{dialog.dismiss();});


                DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("courses").child(onlineUserID).child(key);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String getCurrentDateTime = sdf.format(c.getTime());

                reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        CourseModel cModel = snapshot.getValue(CourseModel.class);
                        String text;
                        //boolean done = (boolean) snapshot.child("done").getValue();

                        List<String> marks;
                        List<String> marksMax;
                        List<String> marksPercent;
                        List<String> gradeDate;

                        marks = cModel.getMarks();
                        marksMax = cModel.getMarksMax();
                        marksPercent = cModel.getmMarksPercent();
                        gradeDate = cModel.getGradeDate();


                        Double med = 0.0;
                        Double medMax = 0.0;

                        for (int i=0; i<marks.size();i++){
                            med = med + Double.parseDouble(marks.get(i))*(Double.parseDouble(marksPercent.get(i))/100.0);
                            medMax = medMax + Double.parseDouble(marksMax.get(i))*(Double.parseDouble(marksPercent.get(i))/100.0);
                        }


                        Double minMark = Double.parseDouble(cModel.getMinGrade());


                        if (getCurrentDateTime.compareTo(cModel.getEndDate()) < 0) {
                            text = "STILL IN PROGRESS ";
                        } else {
                            if (med < minMark)
                            {
                                text = "FAILED";
                            }
                            else
                            {
                                text = "PASSED";
                            }

                        }

                        mMed.setText(""+med);
                        mMedMax.setText(""+medMax);
                        mStatus.setText(text);

                        //graphView = view.findViewById(R.id.graphViewProgress);

                        graphView = view.findViewById(R.id.graphViewProgress);



                        int index = 0;
                        DataPoint[] dp = new  DataPoint[gradeDate.size()-1];

                        for (int i = 1; i<marks.size(); i++){
                            //Date d = (Date) formatter.parse(gradeDate.get(i));
                            //long milliseconds = d.getTime();
                            DataPoint dp1 = new DataPoint(i, Double.parseDouble(marks.get(i)));
                            dp[index] = dp1;
                            index++;
                        }
                        LineGraphSeries series = new LineGraphSeries();
                        series.resetData(dp);


                        graphView.addSeries(series);


                        series.setDrawDataPoints(true);
                        series.setColor(Color.BLACK);
                        series.setThickness(10);
                        series.setDrawBackground(true);
                        series.setBackgroundColor(Color.argb(60, 255, 102, 255));
                        //graphView.getViewport().setXAxisBoundsManual(true);
                        graphView.getViewport().setMinY(0);
                        //graphView.getViewport().setMaxY(10);
                        graphView.getViewport().setScrollable(true);
                        graphView.getViewport().setScrollableY(true);
                        graphView.getViewport().setScalable(true);
                        graphView.getViewport().setScalableY(true);
                        reff.removeEventListener(this);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



                dialog.show();




            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrived_layout_progress, parent, false);
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
            TextView courseTV = mView.findViewById(R.id.progressTv);
            courseTV.setText (course);
        }

        public void setDescription (String description){
            TextView descriptionTV = mView.findViewById(R.id.statusTvProgress);
            descriptionTV.setText(description);
        }

        public void setDate(String date){
            TextView dateTV = mView.findViewById(R.id.dateTvProgress);
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

 */