
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
    private String discipline;
    private String description;
    private String credits;
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
        reference = FirebaseDatabase.getInstance().getReference().child("disciplines").child(onlineUserID);




    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<DisciplineModel> options = new FirebaseRecyclerOptions.Builder<DisciplineModel>().setQuery(reference, DisciplineModel.class).build();
        FirebaseRecyclerAdapter<DisciplineModel, MyViewHolder> adapter = new FirebaseRecyclerAdapter<DisciplineModel, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull DisciplineModel dmodel) {

                holder.setTask(dmodel.getName());

                key = getRef(position).getKey();

                DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("disciplines").child(onlineUserID).child(key);




                reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        String getCurrentDateTime = sdf.format(c.getTime());
                        String text;

                        boolean cPass = false;
                        boolean lPass = false;
                        CourseModel cmodel = dmodel.getCmodel();
                        LabModel lmodel = dmodel.getLabModel();

                        List<String> cMarks = cmodel.getMarks();
                        List<String> lMarks = lmodel.getMarks();
                        List<String> cMarksPercent = cmodel.getmMarksPercent();
                        List<String> lMarksPercent = lmodel.getmMarksPercent();

                        Double cMinMark, lMinMark;
                        Double cMed = 0.0;
                        Double lMed = 0.0;

                        cMinMark = Double.parseDouble(cmodel.getMinGrade().trim());
                        lMinMark = Double.parseDouble(lmodel.getMinGrade().trim());

                        for(int i=1; i<cMarks.size();i++){
                            cMed = cMed + Double.parseDouble(cMarks.get(i))*(Double.parseDouble(cMarksPercent.get(i))/100.0);
                        }

                        for(int i=1; i<lMarks.size();i++){
                            lMed = lMed + Double.parseDouble(lMarks.get(i))*(Double.parseDouble(lMarksPercent.get(i))/100.0);
                        }
                        if(cMed >= cMinMark){cPass = true;}
                        if(lMed >= lMinMark){lPass = true;}



                        if (!isDateAfter(getCurrentDateTime, dmodel.getEndDate())) {
                            text = "STILL IN PROGRESS ";
                        } else {
                            if (cPass && lPass)
                            {
                                text = "PASSED";
                            }
                            else
                            {
                                text = "FAILED";
                            }

                        }


                        holder.setDate(text);
                        reff.removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.setDescription(dmodel.getDescription());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(position).getKey();
                        discipline = dmodel.getName();
                        description = dmodel.getDescription();
                        credits = dmodel.getCredits();

                        endDate = dmodel.getEndDate();


                        viewProgress();



                    }
                });


            }

            private void viewProgress() {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(ProgressActivity.this);
                LayoutInflater inflater =LayoutInflater.from(ProgressActivity.this);
                View view = inflater.inflate(R.layout.discipline_progress, null);
                mDialog.setView(view);


                AlertDialog dialog = mDialog.create();

                TextView mDiscipline = view.findViewById(R.id.disciplineProgress);
                TextView mDescription = view.findViewById(R.id.disciplineDescriptionProgress);
                TextView mCredits = view.findViewById(R.id.disciplineCreditsProgress);
                TextView mStatus = view.findViewById(R.id.disciplineStatusProgress);
                TextView mEndDate = view.findViewById(R.id.disciplineDueDateProgress);

                mDiscipline.setText(discipline);

                mDescription.setText(description);

                mCredits.setText(credits);

                mEndDate.setText(endDate);

                androidx.appcompat.widget.AppCompatButton cancel = view.findViewById(R.id.cancelButtonDisciplineProgress);
                androidx.appcompat.widget.AppCompatButton courseBtn = view.findViewById(R.id.courseButtonDisciplineProgress);
                androidx.appcompat.widget.AppCompatButton labBtn = view.findViewById(R.id.labButtonDisciplineProgress);

                cancel.setOnClickListener((v)->{dialog.dismiss();});
                courseBtn.setOnClickListener((v)->{showProgressCourse();});
                labBtn.setOnClickListener((v)->{showProgressLab();});



                DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("disciplines").child(onlineUserID).child(key);




                reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DisciplineModel dmodel = snapshot.getValue(DisciplineModel.class);

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        String getCurrentDateTime = sdf.format(c.getTime());
                        String text;

                        boolean cPass = false;
                        boolean lPass = false;
                        CourseModel cmodel = dmodel.getCmodel();
                        LabModel lmodel = dmodel.getLabModel();

                        List<String> cMarks = cmodel.getMarks();
                        List<String> lMarks = lmodel.getMarks();
                        List<String> cMarksPercent = cmodel.getmMarksPercent();
                        List<String> lMarksPercent = lmodel.getmMarksPercent();

                        Double cMinMark, lMinMark;
                        Double cMed = 0.0;
                        Double lMed = 0.0;

                        cMinMark = Double.parseDouble(cmodel.getMinGrade().trim());
                        lMinMark = Double.parseDouble(lmodel.getMinGrade().trim());

                        for(int i=1; i<cMarks.size();i++){
                            cMed = cMed + Double.parseDouble(cMarks.get(i))*(Double.parseDouble(cMarksPercent.get(i))/100.0);
                        }

                        for(int i=1; i<lMarks.size();i++){
                            lMed = lMed + Double.parseDouble(lMarks.get(i))*(Double.parseDouble(lMarksPercent.get(i))/100.0);
                        }
                        if(cMed >= cMinMark){cPass = true;}
                        if(lMed >= lMinMark){lPass = true;}



                        if (!isDateAfter(getCurrentDateTime, dmodel.getEndDate())) {
                            text = "STILL IN PROGRESS ";
                        } else {
                            if (cPass && lPass)
                            {
                                text = "PASSED";
                            }
                            else
                            {
                                text = "FAILED";
                            }

                        }


                        mStatus.setText(text);
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

    private void showProgressLab() {
        AlertDialog.Builder myDialogLab = new AlertDialog.Builder(this);
        LayoutInflater inflaterLab = LayoutInflater.from(this);

        View myViewLab = inflaterLab.inflate(R.layout.lab_progress, null);
        myDialogLab.setView(myViewLab);

        AlertDialog dialogLab = myDialogLab.create();
        dialogLab.setCancelable(true);
        dialogLab.show();

        final TextView lab = myViewLab.findViewById(R.id.labText);
        final TextView descriptionLab = myViewLab.findViewById(R.id.labDescriptionText);
        final TextView percentLab = myViewLab.findViewById(R.id.labPercentText);
        final TextView minGradeLab = myViewLab.findViewById(R.id.labMinGradeText);
        final TextView instructorLab = myViewLab.findViewById(R.id.labInstructorText);
        final TextView locationLab = myViewLab.findViewById(R.id.labLocationText);


        final TextView currentGradeLab = myViewLab.findViewById(R.id.labCurrentGradeText);
        final TextView currentMaxGradeLab = myViewLab.findViewById(R.id.labCurrentMaxGradeText);
        final GraphView graphView  = myViewLab.findViewById(R.id.labGraphViewProgress);

        androidx.appcompat.widget.AppCompatButton cancel = myViewLab.findViewById(R.id.cancelButtonLabProgress);


        cancel.setOnClickListener((v)->{dialogLab.dismiss();});
        DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("disciplines").child(onlineUserID).child(key);

        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DisciplineModel dmodel = snapshot.getValue(DisciplineModel.class);
                LabModel lmodel = dmodel.getLabModel();
                lab.setText(lmodel.getLab());
                descriptionLab.setText(lmodel.getDescription());
                percentLab.setText(lmodel.getPercent());
                minGradeLab.setText(lmodel.getMinGrade());
                instructorLab.setText(lmodel.getInstructor());
                locationLab.setText(lmodel.getLocation());


                List<String> lMarks = lmodel.getMarks();
                List<String> lMarksMax = lmodel.getMarksMax();
                List<String> lMarksPercent = lmodel.getmMarksPercent();
                List<String> cMarksDate = lmodel.getGradeDate();



                double minMed = Double.parseDouble(lmodel.getMinGrade());

                double lMed = 0.0;
                for(int i = 1; i<lMarks.size();i++){
                    lMed = lMed+ Double.parseDouble(lMarks.get(i))*(Double.parseDouble(lMarksPercent.get(i))/100.0);
                }
                double lMedMax = 0.0;
                for(int i = 1; i<lMarksMax.size();i++){
                    lMedMax = lMedMax+ Double.parseDouble(lMarksMax.get(i))*(Double.parseDouble(lMarksPercent.get(i))/100.0);
                }
                currentGradeLab.setText(lMed+"");
                currentMaxGradeLab.setText(lMedMax+"");

                int index = 0;
                DataPoint[] dp = new  DataPoint[lMarks.size()-1];

                for (int i = 1; i<lMarks.size(); i++){
                    //Date d = (Date) formatter.parse(gradeDate.get(i));
                    //long milliseconds = d.getTime();
                    DataPoint dp1 = new DataPoint(i, Double.parseDouble(lMarks.get(i)));
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

    }

    private void showProgressCourse() {
        AlertDialog.Builder myDialogCourse = new AlertDialog.Builder(this);
        LayoutInflater inflaterCourse = LayoutInflater.from(this);

        View myViewCourse = inflaterCourse.inflate(R.layout.course_progress, null);
        myDialogCourse.setView(myViewCourse);

        AlertDialog dialogCourse = myDialogCourse.create();
        dialogCourse.setCancelable(true);
        dialogCourse.show();

        final TextView course = myViewCourse.findViewById(R.id.courseText);
        final TextView descriptionCourse = myViewCourse.findViewById(R.id.courseDescriptionText);
        final TextView percentCourse = myViewCourse.findViewById(R.id.coursePercentText);
        final TextView minGradeCourse = myViewCourse.findViewById(R.id.courseMinGradeText);
        final TextView instructorCourse = myViewCourse.findViewById(R.id.courseInstructorText);
        final TextView locationCourse = myViewCourse.findViewById(R.id.courseLocationText);
        final TextView examDateCourse = myViewCourse.findViewById(R.id.courseExamDateText);

        final TextView currentGradeCourse = myViewCourse.findViewById(R.id.currentGradeText);
        final TextView currentMaxGradeCourse = myViewCourse.findViewById(R.id.currentMaxGradeText);
        final GraphView graphView  = myViewCourse.findViewById(R.id.courseGraphViewProgress);

        androidx.appcompat.widget.AppCompatButton cancel = myViewCourse.findViewById(R.id.cancelButtonCourseProgress);


        cancel.setOnClickListener((v)->{dialogCourse.dismiss();});
        DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("disciplines").child(onlineUserID).child(key);

        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DisciplineModel dmodel = snapshot.getValue(DisciplineModel.class);
                CourseModel cmodel = dmodel.getCmodel();
                course.setText(cmodel.getCourse());
                descriptionCourse.setText(cmodel.getDescription());
                percentCourse.setText(cmodel.getPercent());
                minGradeCourse.setText(cmodel.getMinGrade());
                instructorCourse.setText(cmodel.getInstructor());
                locationCourse.setText(cmodel.getLocation());
                examDateCourse.setText(cmodel.getExamDate());

                List<String> cMarks = cmodel.getMarks();
                List<String> cMarksMax = cmodel.getMarksMax();
                List<String> cMarksPercent = cmodel.getmMarksPercent();
                List<String> cMarksDate = cmodel.getGradeDate();



                double minMed = Double.parseDouble(cmodel.getMinGrade());

                double cMed = 0.0;
                for(int i = 1; i<cMarks.size();i++){
                    cMed = cMed+ Double.parseDouble(cMarks.get(i))*(Double.parseDouble(cMarksPercent.get(i))/100.0);
                }
                double cMedMax = 0.0;
                for(int i = 1; i<cMarksMax.size();i++){
                    cMedMax = cMedMax+ Double.parseDouble(cMarksMax.get(i))*(Double.parseDouble(cMarksPercent.get(i))/100.0);
                }
                currentGradeCourse.setText(cMed+"");
                currentMaxGradeCourse.setText(cMedMax+"");

                int index = 0;
                DataPoint[] dp = new  DataPoint[cMarks.size()-1];

                for (int i = 1; i<cMarks.size(); i++){
                    //Date d = (Date) formatter.parse(gradeDate.get(i));
                    //long milliseconds = d.getTime();
                    DataPoint dp1 = new DataPoint(i, Double.parseDouble(cMarks.get(i)));
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




    }

    private boolean isDateAfter(String startDate, String endDate) {
        try {
            String myFormatString = "dd/MM/yyyy"; // for example
            SimpleDateFormat df = new SimpleDateFormat(myFormatString);
            Date endingDate = df.parse(endDate);
            Date startingDate = df.parse(startDate);

            return !endingDate.equals(startingDate) && !endingDate.after(startingDate);
        } catch (Exception e) {
            return false;
        }
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

/*
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
*/