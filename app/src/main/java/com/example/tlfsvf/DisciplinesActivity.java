package com.example.tlfsvf;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.google.android.gms.tasks.OnFailureListener;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DisciplinesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton, floatingActionButtonProgress;

    private DatabaseReference reference;
    private DatabaseReference referenceTask;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;

    private String discipline;
    private String descriptionDiscipline;
    private String credits, endDateDiscipline, disciplineYear, disciplineType, disciplineContext;

    private String key="";
    private String course;
    private String descriptionCourse;
    private String locationCourse;
    private String instructorCourse;

    private String minGradeCourse;
    private String examCourse;
    private String endDateCourse;
    List<String> mMarksCourse = new ArrayList<>();
    List<String> mMarksMaxCourse = new ArrayList<>();
    List<String> mMarksPercentCourse = new ArrayList<>();
    List<String> gradeDateCourse = new ArrayList<>();
    CourseModel courseModel = new CourseModel();

    private String lab;
    private String descriptionLab;
    private String locationLab;
    private String instructorLab;

    private String minGradeLab;
    private String endDateLab;
    List<String> mMarksLab = new ArrayList<>();
    List<String> mMarksMaxLab = new ArrayList<>();
    List<String> mMarksPercentLab = new ArrayList<>();
    List<String> gradeDateLab = new ArrayList<>();
    LabModel labModel = new LabModel();


    private ProgressDialog loader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disciplines);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        toolbar = findViewById(R.id.disciplinesToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My disciplines");

        recyclerView = findViewById(R.id.recyclerViewDisciplines);
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
        referenceTask = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID);


        floatingActionButton = findViewById(R.id.fabDisciplines);


        floatingActionButtonProgress = findViewById(R.id.disciplinesProgress);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDiscipline();
            }
        });

        floatingActionButtonProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
            }
        });


    }


    private void showProgress(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        DatePickerDialog.OnDateSetListener setListener;
        View myView = inflater.inflate(R.layout.discipline_stat, null);
        myDialog.setView(myView);

        AlertDialog dialog = myDialog.create();

        dialog.show();

        final TextView passed = myView.findViewById(R.id.passedDisciplines);
        final TextView undone = myView.findViewById(R.id.StillInProgressDisciplines);
        final TextView failed = myView.findViewById(R.id.failedDisciplines);
        final EditText context = myView.findViewById(R.id.contextStat);
        AppCompatButton contextBtn = myView.findViewById(R.id.contextStatBtn);







        contextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contextTxt = context.getText().toString().trim();
                List<String> undoneLst = new ArrayList<>();
                List<String> passedLst = new ArrayList<>();
                List<String> failedLst = new ArrayList<>();
                List<DisciplineModel> disciplinesLstCredits = new ArrayList<>();
                List<DisciplineModel> disciplinesLstInProgress = new ArrayList<>();
                //List<DisciplineModel> disciplinesLstPassed = new ArrayList<>();
                List<DisciplineModel> disciplinesLstFailed = new ArrayList<>();

                Map<String, Integer> mapYearCredits = new HashMap<>();
                Map<String, Integer> mapYearInProgress = new HashMap<>();
                Map<String, Integer> mapYearPassed = new HashMap<>();
                Map<String, Integer> mapYearFailed = new HashMap<>();

                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String getCurrentDateTime = sdf.format(c.getTime());
                TableLayout tableDisciplinesCredits = myView.findViewById(R.id.tableDisciplineStat);
                TableLayout tableDisciplinesInProgress= myView.findViewById(R.id.tableDisciplineInProgressStat);
                TableLayout tableDisciplinesPassed = myView.findViewById(R.id.tableDisciplinePassedStat);
                TableLayout tableDisciplinesFailed = myView.findViewById(R.id.tableDisciplineFailedStat);
                tableDisciplinesCredits.removeAllViews();
                tableDisciplinesInProgress.removeAllViews();
                tableDisciplinesPassed.removeAllViews();
                tableDisciplinesFailed.removeAllViews();

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ss: snapshot.getChildren()) {

                            DisciplineModel dmodel = ss.getValue(DisciplineModel.class);
                            if (contextTxt.toLowerCase(Locale.ROOT).compareTo(dmodel.getContext().toLowerCase(Locale.ROOT)) == 0) {
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

                                for (int i = 1; i < cMarks.size(); i++) {
                                    cMed = cMed + Double.parseDouble(cMarks.get(i)) * (Double.parseDouble(cMarksPercent.get(i)) / 100.0);
                                }

                                for (int i = 1; i < lMarks.size(); i++) {
                                    lMed = lMed + Double.parseDouble(lMarks.get(i)) * (Double.parseDouble(lMarksPercent.get(i)) / 100.0);
                                }
                                if (cMed >= cMinMark) {
                                    cPass = true;
                                }
                                if (lMed >= lMinMark) {
                                    lPass = true;
                                }


                                if (!isDateAfter(getCurrentDateTime, dmodel.getEndDate())) {
                                    //undoneLst.add("text");
                                    disciplinesLstInProgress.add(dmodel);
                                } else {
                                    if (cPass && lPass) {

                                        //passedLst.add("text");
                                        disciplinesLstCredits.add(dmodel);
                                    } else {

                                        //failedLst.add("text");
                                        disciplinesLstFailed.add(dmodel);
                                    }

                                }
                            }
                        }
                        passed.setText(""+ disciplinesLstCredits.size());
                        undone.setText(""+disciplinesLstInProgress.size());
                        failed.setText(""+disciplinesLstFailed.size());
                        reference.removeEventListener(this);


                        //credits table
                        for(DisciplineModel el : disciplinesLstCredits){
                            String keyStr = el.getType() + ", Year: " + el.getYear();
                            if(!mapYearCredits.containsKey(keyStr)){
                                mapYearCredits.put(keyStr, Integer.parseInt(el.getCredits()));
                            }else{
                                int c = mapYearCredits.get(keyStr);
                                c = c + Integer.parseInt(el.getCredits());
                                mapYearCredits.put(keyStr, c);
                            }
                        }
                        TableLayout.LayoutParams tableRowParams=
                                new TableLayout.LayoutParams
                                        (TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);




                        TableRow.LayoutParams text1Params = new TableRow.LayoutParams();
                        text1Params.width = 0;
                        text1Params.weight = (float) 0.33333;
                        TableRow.LayoutParams text2Params = new TableRow.LayoutParams();
                        text2Params.weight = (float) 0.33333;
                        text2Params.width = 0;
                        TableRow.LayoutParams text3Params = new TableRow.LayoutParams();
                        text2Params.weight = (float) 0.33333;
                        text2Params.width = 0;

                        TableRow tbrow0 = new TableRow(DisciplinesActivity.this);
                        TextView tv0 = new TextView(DisciplinesActivity.this);
                        tv0.setText(" Type & Year ");
                        tv0.setTextColor(Color.BLACK);
                        tv0.setElegantTextHeight(true);
                        tv0.setTextSize(17);
                        tv0.setTypeface(null, Typeface.BOLD);
                        tv0.setLayoutParams(text1Params);
                        tbrow0.addView(tv0);
                        TextView tv1 = new TextView(DisciplinesActivity.this);
                        tv1.setText(" Credits ");
                        tv1.setTextColor(Color.BLACK);
                        tv1.setTypeface(null, Typeface.BOLD);

                        tv1.setElegantTextHeight(true);
                        tv1.setTextSize(17);
                        tv1.setLayoutParams(text2Params);
                        tbrow0.addView(tv1);
                        tbrow0.setLayoutParams(tableRowParams);
                        tbrow0.setBackgroundResource(R.drawable.border);

                        tableDisciplinesCredits.addView(tbrow0);

                        if (mapYearCredits.isEmpty()){


                            TableRow tbrow1 = new TableRow(DisciplinesActivity.this);
                            TextView tv2 = new TextView(DisciplinesActivity.this);
                            tv2.setText(" None ");
                            tv2.setTextColor(Color.BLACK);

                            tv2.setElegantTextHeight(true);
                            tv2.setTextSize(17);
                            tv2.setLayoutParams(text1Params);
                            tbrow1.addView(tv2);
                            TextView tv3 = new TextView(DisciplinesActivity.this);
                            tv3.setText(" None ");
                            tv3.setTextColor(Color.BLACK);

                            tv3.setElegantTextHeight(true);
                            tv3.setTextSize(17);
                            tv3.setLayoutParams(text2Params);
                            tbrow1.addView(tv3);
                            tbrow1.setLayoutParams(tableRowParams);
                            tbrow1.setBackgroundResource(R.drawable.border);
                            tableDisciplinesCredits.addView(tbrow1);


                        }else{

                            for (Map.Entry<String, Integer> me : mapYearCredits.entrySet()){
                                String year = me.getKey() ;
                                int credits = me.getValue();


                                TableRow tbrow1 = new TableRow(DisciplinesActivity.this);
                                TextView tv2 = new TextView(DisciplinesActivity.this);
                                tv2.setText(year);
                                tv2.setTextColor(Color.BLACK);

                                tv2.setElegantTextHeight(true);
                                tv2.setTextSize(17);
                                tv2.setLayoutParams(text1Params);
                                tbrow1.addView(tv2);
                                TextView tv3 = new TextView(DisciplinesActivity.this);
                                tv3.setText(""+credits);

                                tv3.setTextColor(Color.BLACK);

                                tv3.setElegantTextHeight(true);
                                tv3.setTextSize(17);
                                tv3.setLayoutParams(text2Params);
                                tbrow1.addView(tv3);
                                tbrow1.setLayoutParams(tableRowParams);
                                tbrow1.setBackgroundResource(R.drawable.border);
                                tableDisciplinesCredits.addView(tbrow1);
                            }
                        }

                        //in progress table
                        TableRow tbrow0InProgress = new TableRow(DisciplinesActivity.this);
                        TextView tv0InProgress = new TextView(DisciplinesActivity.this);
                        tv0InProgress.setText(" Name ");
                        tv0InProgress.setTextColor(Color.BLACK);
                        tv0InProgress.setElegantTextHeight(true);
                        tv0InProgress.setTextSize(17);
                        tv0InProgress.setLayoutParams(text1Params);
                        tv0InProgress.setTypeface(null, Typeface.BOLD);
                        tbrow0InProgress.addView(tv0InProgress);



                        TextView tv1InProgress = new TextView(DisciplinesActivity.this);
                        tv1InProgress.setText(" Credits ");
                        tv1InProgress.setTextColor(Color.BLACK);
                        tv1InProgress.setTypeface(null, Typeface.BOLD);

                        tv1InProgress.setElegantTextHeight(true);
                        tv1InProgress.setTextSize(17);
                        tv1InProgress.setLayoutParams(text2Params);
                        tbrow0InProgress.addView(tv1InProgress);

                        TextView tv01InProgress = new TextView(DisciplinesActivity.this);
                        tv01InProgress.setText(" Type & Year ");
                        tv01InProgress.setTextColor(Color.BLACK);
                        tv01InProgress.setElegantTextHeight(true);
                        tv01InProgress.setTextSize(17);
                        tv01InProgress.setLayoutParams(text3Params);
                        tv01InProgress.setTypeface(null, Typeface.BOLD);
                        tbrow0InProgress.addView(tv01InProgress);

                        tbrow0InProgress.setLayoutParams(tableRowParams);
                        tbrow0InProgress.setBackgroundResource(R.drawable.border);

                        tableDisciplinesInProgress.addView(tbrow0InProgress);

                        if (disciplinesLstInProgress.isEmpty()){


                            TableRow tbrow1InProgress = new TableRow(DisciplinesActivity.this);
                            TextView tv2InProgress = new TextView(DisciplinesActivity.this);
                            tv2InProgress.setText(" None ");
                            tv2InProgress.setTextColor(Color.BLACK);

                            tv2InProgress.setElegantTextHeight(true);
                            tv2InProgress.setTextSize(17);
                            tv2InProgress.setLayoutParams(text1Params);
                            tbrow1InProgress.addView(tv2InProgress);



                            TextView tv3InProgress = new TextView(DisciplinesActivity.this);
                            tv3InProgress.setText(" None ");
                            tv3InProgress.setTextColor(Color.BLACK);

                            tv3InProgress.setElegantTextHeight(true);
                            tv3InProgress.setLayoutParams(text2Params);
                            tv3InProgress.setTextSize(17);

                            tbrow1InProgress.addView(tv3InProgress);

                            TextView tv21InProgress = new TextView(DisciplinesActivity.this);
                            tv21InProgress.setText(" None ");
                            tv21InProgress.setTextColor(Color.BLACK);

                            tv21InProgress.setElegantTextHeight(true);
                            tv21InProgress.setTextSize(17);
                            tv21InProgress.setLayoutParams(text3Params);
                            tbrow1InProgress.addView(tv21InProgress);

                            tbrow1InProgress.setLayoutParams(tableRowParams);
                            tbrow1InProgress.setBackgroundResource(R.drawable.border);
                            tableDisciplinesInProgress.addView(tbrow1InProgress);


                        }else{

                            for (DisciplineModel el : disciplinesLstInProgress){
                                String name =el.getName() ;
                                String year = el.getYear();
                                String type = el.getType();
                                int credits = Integer.parseInt(el.getCredits());


                                TableRow tbrow1InProgress = new TableRow(DisciplinesActivity.this);
                                TextView tv2InProgress = new TextView(DisciplinesActivity.this);
                                tv2InProgress.setText(name);
                                tv2InProgress.setTextColor(Color.BLACK);

                                tv2InProgress.setElegantTextHeight(true);
                                tv2InProgress.setTextSize(17);
                                tv2InProgress.setLayoutParams(text1Params);
                                tbrow1InProgress.addView(tv2InProgress);



                                TextView tv3InProgress = new TextView(DisciplinesActivity.this);
                                tv3InProgress.setText("  "+credits);
                                tv3InProgress.setTextColor(Color.BLACK);

                                tv3InProgress.setElegantTextHeight(true);
                                tv3InProgress.setTextSize(17);
                                tv3InProgress.setLayoutParams(text2Params);
                                tbrow1InProgress.addView(tv3InProgress);

                                TextView tv21InProgress = new TextView(DisciplinesActivity.this);
                                tv21InProgress.setText(type + ", Year: "+year);
                                tv21InProgress.setTextColor(Color.BLACK);
                                tv21InProgress.setElegantTextHeight(true);
                                tv21InProgress.setTextSize(17);
                                tv21InProgress.setLayoutParams(text3Params);
                                tbrow1InProgress.addView(tv21InProgress);

                                tbrow1InProgress.setLayoutParams(tableRowParams);
                                tbrow1InProgress.setBackgroundResource(R.drawable.border);
                                tableDisciplinesInProgress.addView(tbrow1InProgress);
                            }
                        }

                        // passed table
                        TableRow tbrow0Passed = new TableRow(DisciplinesActivity.this);
                        TextView tv0Passed = new TextView(DisciplinesActivity.this);
                        tv0Passed.setText(" Name ");
                        tv0Passed.setTextColor(Color.BLACK);
                        tv0Passed.setElegantTextHeight(true);
                        tv0Passed.setTextSize(17);
                        tv0Passed.setLayoutParams(text1Params);
                        tv0Passed.setTypeface(null, Typeface.BOLD);

                        tbrow0Passed.addView(tv0Passed);



                        TextView tv1Passed = new TextView(DisciplinesActivity.this);
                        tv1Passed.setText(" Credits ");
                        tv1Passed.setTextColor(Color.BLACK);
                        tv1Passed.setTypeface(null, Typeface.BOLD);

                        tv1Passed.setElegantTextHeight(true);
                        tv1Passed.setTextSize(17);
                        tv1Passed.setLayoutParams(text2Params);
                        tbrow0Passed.addView(tv1Passed);

                        TextView tv01Passed = new TextView(DisciplinesActivity.this);
                        tv01Passed.setText(" Type & Year ");
                        tv01Passed.setTextColor(Color.BLACK);
                        tv01Passed.setElegantTextHeight(true);
                        tv01Passed.setTextSize(17);
                        tv01Passed.setLayoutParams(text3Params);
                        tv01Passed.setTypeface(null, Typeface.BOLD);

                        tbrow0Passed.addView(tv01Passed);

                        tbrow0Passed.setLayoutParams(tableRowParams);
                        tbrow0Passed.setBackgroundResource(R.drawable.border);
                        tableDisciplinesPassed.addView(tbrow0Passed);

                        if (disciplinesLstCredits.isEmpty()){


                            TableRow tbrow1Passed = new TableRow(DisciplinesActivity.this);
                            TextView tv2Passed = new TextView(DisciplinesActivity.this);
                            tv2Passed.setText(" None ");
                            tv2Passed.setTextColor(Color.BLACK);

                            tv2Passed.setElegantTextHeight(true);
                            tv2Passed.setLayoutParams(text1Params);
                            tv2Passed.setTextSize(17);
                            tbrow1Passed.addView(tv2Passed);



                            TextView tv3Passed = new TextView(DisciplinesActivity.this);
                            tv3Passed.setText(" None ");
                            tv3Passed.setTextColor(Color.BLACK);

                            tv3Passed.setElegantTextHeight(true);
                            tv3Passed.setTextSize(17);
                            tv3Passed.setLayoutParams(text2Params);
                            tbrow1Passed.addView(tv3Passed);

                            TextView tv21Passed = new TextView(DisciplinesActivity.this);
                            tv21Passed.setText(" None ");
                            tv21Passed.setTextColor(Color.BLACK);

                            tv21Passed.setElegantTextHeight(true);
                            tv21Passed.setLayoutParams(text3Params);
                            tv21Passed.setTextSize(17);
                            tbrow1Passed.addView(tv21Passed);

                            tbrow1Passed.setLayoutParams(tableRowParams);
                            tbrow1Passed.setBackgroundResource(R.drawable.border);
                            tableDisciplinesPassed.addView(tbrow1Passed);


                        }else{

                            for (DisciplineModel el : disciplinesLstCredits){
                                String name = el.getName();
                                String year = el.getYear();
                                String type = el.getType();
                                int credits = Integer.parseInt(el.getCredits());


                                TableRow tbrow1Passed = new TableRow(DisciplinesActivity.this);
                                TextView tv2Passed = new TextView(DisciplinesActivity.this);
                                tv2Passed.setText(name);
                                tv2Passed.setTextColor(Color.BLACK);

                                tv2Passed.setElegantTextHeight(true);
                                tv2Passed.setTextSize(17);
                                tv2Passed.setLayoutParams(text1Params);
                                tbrow1Passed.addView(tv2Passed);



                                TextView tv3Passed = new TextView(DisciplinesActivity.this);
                                tv3Passed.setText("  "+credits);
                                tv3Passed.setTextColor(Color.BLACK);

                                tv3Passed.setElegantTextHeight(true);
                                tv3Passed.setTextSize(17);
                                tv3Passed.setLayoutParams(text2Params);
                                tbrow1Passed.addView(tv3Passed);

                                TextView tv21Passed = new TextView(DisciplinesActivity.this);
                                tv21Passed.setText(type+", Year: "+year);
                                tv21Passed.setTextColor(Color.BLACK);

                                tv21Passed.setElegantTextHeight(true);
                                tv21Passed.setTextSize(17);
                                tv21Passed.setLayoutParams(text3Params);
                                tbrow1Passed.addView(tv21Passed);

                                tbrow1Passed.setLayoutParams(tableRowParams);
                                tbrow1Passed.setBackgroundResource(R.drawable.border);
                                tableDisciplinesPassed.addView(tbrow1Passed);
                            }
                        }

                        //failed table
                        TableRow tbrow0Failed = new TableRow(DisciplinesActivity.this);
                        TextView tv0Failed = new TextView(DisciplinesActivity.this);
                        tv0Failed.setText(" Name ");
                        tv0Failed.setTextColor(Color.BLACK);
                        tv0Failed.setElegantTextHeight(true);
                        tv0Failed.setTextSize(17);
                        tv0Failed.setLayoutParams(text1Params);
                        tv0Failed.setTypeface(null, Typeface.BOLD);

                        tbrow0Failed.addView(tv0Failed);



                        TextView tv1Failed = new TextView(DisciplinesActivity.this);
                        tv1Failed.setText(" Credits ");
                        tv1Failed.setTextColor(Color.BLACK);
                        tv1Failed.setTypeface(null, Typeface.BOLD);

                        tv1Failed.setElegantTextHeight(true);
                        tv1Failed.setTextSize(17);
                        tv1Failed.setLayoutParams(text2Params);
                        tbrow0Failed.addView(tv1Failed);

                        TextView tv01Failed = new TextView(DisciplinesActivity.this);
                        tv01Failed.setText(" Type & Year ");
                        tv01Failed.setTextColor(Color.BLACK);
                        tv01Failed.setElegantTextHeight(true);
                        tv01Failed.setTextSize(17);
                        tv01Failed.setLayoutParams(text3Params);
                        tv01Failed.setTypeface(null, Typeface.BOLD);

                        tbrow0Failed.addView(tv01Failed);

                        tbrow0Failed.setLayoutParams(tableRowParams);
                        tbrow0Failed.setBackgroundResource(R.drawable.border);
                        tableDisciplinesFailed.addView(tbrow0Failed);

                        if (disciplinesLstFailed.isEmpty()){


                            TableRow tbrow1Failed = new TableRow(DisciplinesActivity.this);
                            TextView tv2Failed = new TextView(DisciplinesActivity.this);
                            tv2Failed.setText(" None ");
                            tv2Failed.setTextColor(Color.BLACK);

                            tv2Failed.setElegantTextHeight(true);
                            tv2Failed.setTextSize(17);
                            tv2Failed.setLayoutParams(text1Params);
                            tbrow1Failed.addView(tv2Failed);



                            TextView tv3Failed = new TextView(DisciplinesActivity.this);
                            tv3Failed.setText(" None ");
                            tv3Failed.setTextColor(Color.BLACK);

                            tv3Failed.setElegantTextHeight(true);
                            tv3Failed.setTextSize(17);
                            tv3Failed.setLayoutParams(text2Params);
                            tbrow1Failed.addView(tv3Failed);

                            TextView tv21Failed = new TextView(DisciplinesActivity.this);
                            tv21Failed.setText(" None ");
                            tv21Failed.setTextColor(Color.BLACK);
                            tv21Failed.setElegantTextHeight(true);
                            tv21Failed.setTextSize(17);
                            tv21Failed.setLayoutParams(text3Params);
                            tbrow1Failed.addView(tv21Failed);

                            tbrow1Failed.setLayoutParams(tableRowParams);
                            tbrow1Failed.setBackgroundResource(R.drawable.border);
                            tableDisciplinesFailed.addView(tbrow1Failed);


                        }else{

                            for (DisciplineModel el : disciplinesLstFailed){
                                String name =  el.getName();
                                String year = el.getYear();
                                String type = el.getType();
                                int credits = Integer.parseInt(el.getCredits());


                                TableRow tbrow1Failed = new TableRow(DisciplinesActivity.this);
                                TextView tv2Failed = new TextView(DisciplinesActivity.this);
                                tv2Failed.setText(name);
                                tv2Failed.setTextColor(Color.BLACK);

                                tv2Failed.setElegantTextHeight(true);
                                tv2Failed.setTextSize(17);
                                tv2Failed.setLayoutParams(text1Params);
                                tbrow1Failed.addView(tv2Failed);



                                TextView tv3Failed = new TextView(DisciplinesActivity.this);
                                tv3Failed.setText("  "+credits);
                                tv3Failed.setTextColor(Color.BLACK);

                                tv3Failed.setElegantTextHeight(true);
                                tv3Failed.setTextSize(17);
                                tv3Failed.setLayoutParams(text2Params);
                                tbrow1Failed.addView(tv3Failed);

                                TextView tv21Failed = new TextView(DisciplinesActivity.this);
                                tv21Failed.setText(type+", Year: "+year);
                                tv21Failed.setTextColor(Color.BLACK);

                                tv21Failed.setElegantTextHeight(true);
                                tv21Failed.setTextSize(17);
                                tv21Failed.setLayoutParams(text3Params);
                                tbrow1Failed.addView(tv21Failed);

                                tbrow1Failed.setLayoutParams(tableRowParams);
                                tbrow1Failed.setBackgroundResource(R.drawable.border);
                                tableDisciplinesFailed.addView(tbrow1Failed);
                            }
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });





        AppCompatButton cancel = myView.findViewById(R.id.cancelButtonDisciplinesStat);
        cancel.setOnClickListener((v)->{dialog.dismiss();});
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

    private void addDiscipline() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_file_discipline, null);
        myDialog.setView(myView);

        AlertDialog dialog = myDialog.create();
        dialog.setCancelable(true);
        dialog.show();

        //discipline
        final EditText discipline = myView.findViewById(R.id.discipline);
        final EditText descriptionDiscipline = myView.findViewById(R.id.disciplineDescription);
        final EditText credits = myView.findViewById(R.id.disciplineCredits);
        final TextView endDateDiscipline = myView.findViewById(R.id.disciplineDueDate);
        final TextView disciplineYearTv = myView.findViewById(R.id.disciplineYear);
        final TextView disciplineContextTv = myView.findViewById(R.id.disciplineContext);
        final TextView disciplineTypeTv = myView.findViewById(R.id.disciplineType);
        AppCompatButton save = myView.findViewById(R.id.saveButtonDiscipline);
        AppCompatButton cancel = myView.findViewById(R.id.cancelButtonDiscipline);
        AppCompatButton courseBtn = myView.findViewById(R.id.courseButtonDiscipline);
        AppCompatButton labBtn = myView.findViewById(R.id.labButtonDiscipline);
        Calendar calendar1 =  Calendar.getInstance();
        final int year1 = calendar1.get(Calendar.YEAR);
        final int month1 = calendar1.get(Calendar.MONTH);
        final int day1 = calendar1.get(Calendar.DAY_OF_MONTH);

        endDateDiscipline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(DisciplinesActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        String examDateStr = day + "/"+month+"/"+year;
                        endDateDiscipline.setText(examDateStr);
                    }
                }, year1, month1, day1);
                datePickerDialog.show();
            }
        });

        AtomicBoolean courseAdded = new AtomicBoolean(false);
        AtomicBoolean labAdded = new AtomicBoolean(false);

        courseBtn.setOnClickListener((v)->{
            AlertDialog.Builder myDialogCourse = new AlertDialog.Builder(this);
            LayoutInflater inflaterCourse = LayoutInflater.from(this);

            View myViewCourse = inflaterCourse.inflate(R.layout.input_file_courses, null);
            myDialogCourse.setView(myViewCourse);

            AlertDialog dialogCourse = myDialogCourse.create();
            dialogCourse.setCancelable(true);
            dialogCourse.show();

            final EditText course = myViewCourse.findViewById(R.id.course);
            final EditText descriptionCourse = myViewCourse.findViewById(R.id.courseDescription);
            final EditText percentCourse = myViewCourse.findViewById(R.id.coursePercentGrade);
            final EditText minGradeCourse = myViewCourse.findViewById(R.id.courseMinGrade);
            final EditText instructorCourse = myViewCourse.findViewById(R.id.courseInstructor);
            final EditText locationCourse = myViewCourse.findViewById(R.id.courseLocation);
            final TextView examDateCourse = myViewCourse.findViewById(R.id.courseExamDate);




            Calendar calendar =  Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);

            examDateCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(DisciplinesActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            month = month+1;
                            String examDateStr = day + "/"+month+"/"+year;
                            examDateCourse.setText(examDateStr);
                        }
                    }, year, month, day);
                    datePickerDialog.show();
                }
            });


            AppCompatButton saveCourse = myViewCourse.findViewById(R.id.saveButtonCourse);
            AppCompatButton cancelCourse = myViewCourse.findViewById(R.id.cancelButtonCourse);



            cancelCourse.setOnClickListener((v1)->{dialogCourse.dismiss();});

            saveCourse.setOnClickListener((v1)->{
                String mCourse = course.getText().toString();
                String mDescription = descriptionCourse.getText().toString().trim();
                String mLocation = locationCourse.getText().toString().trim();
                String mInstructor = instructorCourse.getText().toString().trim();
                String mPercent = percentCourse.getText().toString().trim();
                String mMinGrade = minGradeCourse.getText().toString().trim();
                String id = reference.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());
                String mExamDate = examDateCourse.getText().toString();


                List<String> mMarks1 = new ArrayList<>();
                List<String> mMarks1Max = new ArrayList<>();
                List<String> mMarks1Percent = new ArrayList<>();
                List<String> mGradeDate = new ArrayList<>();
                mMarks1.add(""+0.0);
                mMarks1Max.add(""+0.0);
                mMarks1Percent.add(""+0);
                mGradeDate.add(""+0);

                if(TextUtils.isEmpty(mCourse)){
                    course.setError("Course name required");
                    return;
                }
                if(TextUtils.isEmpty(mExamDate)){
                    examDateCourse.setError("Exam date required");
                    return;
                }
                if(TextUtils.isEmpty(mDescription)){
                    descriptionCourse.setError("Course description required");
                    return;
                }
                if(TextUtils.isEmpty(mMinGrade)){
                    minGradeCourse.setError("Minimum grade is required");
                    return;
                }
                if(TextUtils.isEmpty(mPercent)){
                    percentCourse.setError("Percent required");
                    return;
                }

                if(TextUtils.isEmpty(mInstructor)){
                    instructorCourse.setError("Course instructor required");
                    return;
                }


                if(TextUtils.isEmpty(mLocation)){
                    locationCourse.setError("Course location required");
                    return;
                }else{
                    loader.setMessage("Adding your new course");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    CourseModel modelCourse = new CourseModel(mCourse, mDescription, id, date, mMarks1, mInstructor, mLocation, mMarks1Max, mMarks1Percent, mMinGrade, mGradeDate, mPercent, mExamDate);
                    courseModel = modelCourse;
                    courseAdded.set(true);
                    loader.dismiss();
                }
                dialogCourse.dismiss();
            });


            //dialogCourse.dismiss();



        });

        labBtn.setOnClickListener((v)->{
            AlertDialog.Builder myDialogLab = new AlertDialog.Builder(this);
            LayoutInflater inflaterLab = LayoutInflater.from(this);

            View myViewLab = inflaterLab.inflate(R.layout.input_file_labs, null);
            myDialogLab.setView(myViewLab);

            AlertDialog dialogLab = myDialogLab.create();
            dialogLab.setCancelable(true);
            dialogLab.show();

            final EditText lab = myViewLab.findViewById(R.id.lab);
            final EditText descriptionLab = myViewLab.findViewById(R.id.LabDescription);
            final EditText percentLab = myViewLab.findViewById(R.id.LabPercentGrade);
            final EditText minGradeLab = myViewLab.findViewById(R.id.LabMinGrade);
            final EditText instructorLab = myViewLab.findViewById(R.id.LabInstructor);
            final EditText locationLab = myViewLab.findViewById(R.id.LabLocation);





            Calendar calendar =  Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);


            AppCompatButton saveLab= myViewLab.findViewById(R.id.saveButtonLab);
            AppCompatButton cancelLab = myViewLab.findViewById(R.id.cancelButtonLab);



            cancelLab.setOnClickListener((v1)->{dialogLab.dismiss();});

            saveLab.setOnClickListener((v1)->{
                String mLab = lab.getText().toString();
                String mDescription = descriptionLab.getText().toString().trim();
                String mLocation = locationLab.getText().toString().trim();
                String mInstructor = instructorLab.getText().toString().trim();
                String mPercent = percentLab.getText().toString().trim();
                String mMinGrade = minGradeLab.getText().toString().trim();
                String id = reference.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                List<String> mMarks1 = new ArrayList<>();
                List<String> mMarks1Max = new ArrayList<>();
                List<String> mMarks1Percent = new ArrayList<>();
                List<String> mGradeDate = new ArrayList<>();
                mMarks1.add(""+0.0);
                mMarks1Max.add(""+0.0);
                mMarks1Percent.add(""+0);
                mGradeDate.add(""+0);

                if(TextUtils.isEmpty(mLab)){
                    lab.setError("Laboratory name required");
                    return;
                }
                if(TextUtils.isEmpty(mDescription)){
                    descriptionLab.setError("Laboratory description required");
                    return;
                }
                if(TextUtils.isEmpty(mMinGrade)){
                    minGradeLab.setError("Minimum grade is required");
                    return;
                }
                if(TextUtils.isEmpty(mInstructor)){
                    instructorLab.setError("Laboratory instructor required");
                    return;
                }


                if(TextUtils.isEmpty(mLocation)){
                    locationLab.setError("Laboratory location required");
                    return;
                }else{
                    loader.setMessage("Adding your new laboratory");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    LabModel modelLab = new LabModel(mLab, mDescription, id, date,mLocation, mInstructor,mMinGrade,mPercent, mMarks1, mMarks1Max, mMarks1Percent, mGradeDate);
                    labModel = modelLab;
                    labAdded.set(true);
                    loader.dismiss();
                }
                dialogLab.dismiss();
            });


            //dialogCourse.dismiss();



        });

        cancel.setOnClickListener((v)->{dialog.dismiss();});

        save.setOnClickListener((v)->{
            String mDiscipline = discipline.getText().toString();
            String mDescription = descriptionDiscipline.getText().toString().trim();
            String mCredits = credits.getText().toString().trim();
            String mDisciplineYear = disciplineYearTv.getText().toString().trim();
            String mDisciplineContext = disciplineContextTv.getText().toString().trim();
            String mDisciplineType = disciplineTypeTv.getText().toString().trim();
            String id = reference.push().getKey();
            String mDisciplineEndDate = endDateDiscipline.getText().toString().trim();
            String date = DateFormat.getDateInstance().format(new Date());



            if(TextUtils.isEmpty(mDiscipline)){
                discipline.setError("Discipline name required");
                return;
            }
            if(TextUtils.isEmpty(mDisciplineType)){
                disciplineTypeTv.setError("Discipline type required");
                return;
            }
            if(TextUtils.isEmpty(mDisciplineContext)){
                disciplineContextTv.setError("Discipline context required");
                return;
            }
            if(TextUtils.isEmpty(mDisciplineYear)){
                disciplineYearTv.setError("Discipline year required");
                return;
            }
            if(TextUtils.isEmpty(mDescription)){
                descriptionDiscipline.setError("Discipline description required");
                return;
            }
            if (!courseAdded.get()){
                Toast.makeText(DisciplinesActivity.this, "You need to add course details ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!labAdded.get()){
                Toast.makeText(DisciplinesActivity.this, "You need to add laboratory details ", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(mCredits)){
                credits.setError("Discipline credits required");
                return;
            }else{
                loader.setMessage("Adding your new discipline");
                loader.setCanceledOnTouchOutside(false);
                loader.show();
                Model tmodel = new Model(courseModel.getCourse() + " Exam", "This is the exam for " + courseModel.getCourse(),id, date, false, courseModel.getExamDate());
                DisciplineModel disciplineModel = new DisciplineModel(mDiscipline, mDescription, id, date, mCredits,mDisciplineType, mDisciplineContext,mDisciplineYear, mDisciplineEndDate, courseModel, labModel);
                referenceTask.child(id).setValue(tmodel);
                reference.child(id).setValue(disciplineModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(DisciplinesActivity.this, "The discipline was added successfully", Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(DisciplinesActivity.this, "Failed " + error, Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }
                    }
                });
            }
            dialog.dismiss();
        });







    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<DisciplineModel> options = new FirebaseRecyclerOptions.Builder<DisciplineModel>().setQuery(reference, DisciplineModel.class).build();
        FirebaseRecyclerAdapter<DisciplineModel, MyViewHolder> adapter = new FirebaseRecyclerAdapter<DisciplineModel, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull DisciplineModel dmodel) {
                holder.setDate(dmodel.getEndDate());
                holder.setTask(dmodel.getName());
                holder.setStatus(dmodel.getContext());






                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(position).getKey();
                        discipline = dmodel.getName();
                        descriptionDiscipline = dmodel.getDescription();
                        credits = dmodel.getCredits();
                        endDateDiscipline = dmodel.getEndDate();
                        disciplineYear = dmodel.getYear();
                        disciplineType = dmodel.getType();
                        disciplineContext = dmodel.getContext();
                        courseModel = dmodel.getCmodel();
                        labModel = dmodel.getLabModel();
                        //reff.removeEventListener(eventListener);

                        updateCourse();
                    }
                });


            }

            private void updateCourse() {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(DisciplinesActivity.this);
                LayoutInflater inflater =LayoutInflater.from(DisciplinesActivity.this);
                View view = inflater.inflate(R.layout.update_data_discipline, null);
                mDialog.setView(view);

                AlertDialog dialog = mDialog.create();
                dialog.setCancelable(true);
                dialog.show();

                EditText mDiscipline = view.findViewById(R.id.discipline);
                EditText mDescription = view.findViewById(R.id.disciplineDescription);
                EditText mDisciplineYearTv = view.findViewById(R.id.disciplineYear);
                EditText mDisciplineTypeTv = view.findViewById(R.id.disciplineTypeUpdate);
                EditText mDisciplineContextTv = view.findViewById(R.id.disciplineContextUpdate);
                EditText mCredits = view.findViewById(R.id.disciplineCredits);
                TextView mEndDate = view.findViewById(R.id.disciplineDueDateUpdate);
                AppCompatButton delBtn = view.findViewById(R.id.delButtonDiscipline);
                AppCompatButton closeBtn = view.findViewById(R.id.cancelButtonDiscipline);
                AppCompatButton updateBtn = view.findViewById(R.id.updateButtonDiscipline);
                AppCompatButton courseBtn = view.findViewById(R.id.courseButtonDiscipline);
                AppCompatButton labBtn = view.findViewById(R.id.labButtonDiscipline);

                mDiscipline.setText(discipline);
                mDiscipline.setSelection(discipline.length());

                mDisciplineYearTv.setText(disciplineYear);
                mDisciplineYearTv.setSelection(disciplineYear.length());

                mDisciplineTypeTv.setText(disciplineType);
                mDisciplineTypeTv.setSelection(disciplineType.length());

                mDisciplineContextTv.setText(disciplineContext);
                mDisciplineContextTv.setSelection(disciplineContext.length());

                mDescription.setText(descriptionDiscipline);
                mDescription.setSelection(descriptionDiscipline.length());

                mCredits.setText(credits);
                mCredits.setSelection(credits.length());

                mEndDate.setText(endDateDiscipline);
                mCredits.setSelection(credits.length());
                Calendar calendar1 =  Calendar.getInstance();
                final int year1 = calendar1.get(Calendar.YEAR);
                final int month1 = calendar1.get(Calendar.MONTH);
                final int day1 = calendar1.get(Calendar.DAY_OF_MONTH);

                mEndDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(DisciplinesActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                month = month+1;
                                String examDateStr = day + "/"+month+"/"+year;
                                mEndDate.setText(examDateStr);
                            }
                        }, year1, month1, day1);
                        datePickerDialog.show();
                    }
                });



                courseBtn.setOnClickListener((v)->{
                    AlertDialog.Builder myDialogCourse = new AlertDialog.Builder(DisciplinesActivity.this);
                    LayoutInflater inflaterCourse = LayoutInflater.from(DisciplinesActivity.this);

                    View myViewCourse = inflaterCourse.inflate(R.layout.update_data_course, null);
                    myDialogCourse.setView(myViewCourse);

                    AlertDialog dialogCourse = myDialogCourse.create();
                    dialogCourse.setCancelable(true);
                    dialogCourse.show();

                    mMarksCourse = courseModel.getMarks();
                    mMarksMaxCourse = courseModel.getMarksMax();
                    mMarksPercentCourse = courseModel.getmMarksPercent();
                    gradeDateCourse = courseModel.getGradeDate();

                    final EditText course = myViewCourse.findViewById(R.id.mEditTextCourse);
                    final EditText descriptionCourse = myViewCourse.findViewById(R.id.mEditTextDescriptionCourse);
                    final EditText percentCourse = myViewCourse.findViewById(R.id.mEditTextPercentCourse);
                    final EditText minGradeCourse = myViewCourse.findViewById(R.id.courseMinGradeUpdate);
                    final EditText instructorCourse = myViewCourse.findViewById(R.id.mEditTextInstructorCourse);
                    final EditText locationCourse = myViewCourse.findViewById(R.id.mEditTextLocationCourse);
                    EditText mMark = myViewCourse.findViewById(R.id.mEditTextGrade);
                    EditText mMarkMax = myViewCourse.findViewById(R.id.mEditTextGradeMax);
                    EditText mMarkPercent = myViewCourse.findViewById(R.id.mEditTextPercentageGrade);
                    TextView mGradeDate = myViewCourse.findViewById(R.id.courseGradeDateUpdate);
                    final TextView examDateCourse = myViewCourse.findViewById(R.id.courseExamDateUpdate);


                    course.setText(courseModel.getCourse());
                    course.setSelection(courseModel.getCourse().length());

                    descriptionCourse.setText(courseModel.getDescription());
                    descriptionCourse.setSelection(courseModel.getDescription().length());

                    percentCourse.setText(courseModel.getPercent());
                    percentCourse.setSelection(courseModel.getPercent().length());


                    minGradeCourse.setText(courseModel.getMinGrade());
                    minGradeCourse.setSelection(courseModel.getMinGrade().length());

                    instructorCourse.setText(courseModel.getInstructor());
                    instructorCourse.setSelection(courseModel.getInstructor().length());

                    locationCourse.setText(courseModel.getLocation());
                    locationCourse.setSelection(courseModel.getLocation().length());

                    examDateCourse.setText(courseModel.getExamDate());

                    Calendar calendar =  Calendar.getInstance();
                    final int year = calendar.get(Calendar.YEAR);
                    final int month = calendar.get(Calendar.MONTH);
                    final int day = calendar.get(Calendar.DAY_OF_MONTH);

                    examDateCourse.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatePickerDialog datePickerDialog = new DatePickerDialog(DisciplinesActivity.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                    month = month+1;
                                    String examDateStr = day + "/"+month+"/"+year;
                                    examDateCourse.setText(examDateStr);
                                }
                            }, year, month, day);
                            datePickerDialog.show();
                        }
                    });



                    mGradeDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatePickerDialog datePickerDialog = new DatePickerDialog(DisciplinesActivity.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                    month = month+1;
                                    String endDateStr = day + "/"+month+"/"+year;
                                    mGradeDate.setText(endDateStr);
                                }
                            }, year, month, day);
                            datePickerDialog.show();
                        }
                    });

                    AppCompatButton updateCourse = myViewCourse.findViewById(R.id.UpdateBtnCourse);
                    //AppCompatButton deleteCourse = myViewCourse.findViewById(R.id.deleteBtnCourse);
                    AppCompatButton addGrade = myViewCourse.findViewById(R.id.AddBtnCourse);





                    updateCourse.setOnClickListener((v1)->{
                        String mCourse = course.getText().toString();
                        String mDescriptionCourse = descriptionCourse.getText().toString().trim();
                        String mLocation = locationCourse.getText().toString().trim();
                        String mInstructor = instructorCourse.getText().toString().trim();
                        String mPercent = percentCourse.getText().toString().trim();
                        String mMinGrade = minGradeCourse.getText().toString().trim();
                        String id = reference.push().getKey();
                        String date = DateFormat.getDateInstance().format(new Date());

                        String mExamDate = examDateCourse.getText().toString();


                        if(TextUtils.isEmpty(mCourse)){
                            course.setError("Course name required");
                            return;
                        }
                        if(TextUtils.isEmpty(mDescriptionCourse)){
                            descriptionCourse.setError("Course description required");
                            return;
                        }

                        if(TextUtils.isEmpty(mExamDate)){
                            examDateCourse.setError("Exam date required");
                            return;
                        }
                        if(TextUtils.isEmpty(mMinGrade)){
                            minGradeCourse.setError("Minimum grade is required");
                            return;
                        }
                        if(TextUtils.isEmpty(mPercent)){
                            percentCourse.setError("Percent required");
                            return;
                        }

                        if(TextUtils.isEmpty(mInstructor)){
                            instructorCourse.setError("Course instructor required");
                            return;
                        }



                        if(TextUtils.isEmpty(mLocation)){
                            locationCourse.setError("Course location required");
                            return;
                        }else{
                            loader.setMessage("Adding your new course");
                            loader.setCanceledOnTouchOutside(false);
                            loader.show();

                            CourseModel modelCourse = new CourseModel(mCourse, mDescriptionCourse, id, date, mMarksCourse, mInstructor, mLocation, mMarksMaxCourse, mMarksPercentCourse, mMinGrade, gradeDateCourse, mPercent, mExamDate);
                            courseModel = modelCourse;

                            loader.dismiss();
                        }
                        dialogCourse.dismiss();
                    });

                    addGrade.setOnClickListener((v1)->{
                            String markText, markTextMax, percentText, gradeDateText;

                            markText = mMark.getText().toString().trim();
                            markTextMax = mMarkMax.getText().toString().trim();
                            percentText= mMarkPercent.getText().toString().trim();
                            gradeDateText= mGradeDate.getText().toString().trim();

                            if(TextUtils.isEmpty(markText)){
                                mMark.setError("Grade is required");
                                return;
                            }
                            if(TextUtils.isEmpty(markTextMax)){
                                mMarkMax.setError("Max grade is required");
                                return;
                            }
                            if(TextUtils.isEmpty(gradeDateText)){
                                mGradeDate.setError("Grade date is required");
                                return;
                            }
                            if(TextUtils.isEmpty(percentText)){
                                mMarkPercent.setError("Percent is required");
                                return;
                            }else {
                                String mCourse = course.getText().toString();
                                String mDescriptionCourse = descriptionCourse.getText().toString().trim();
                                String mLocation = locationCourse.getText().toString().trim();
                                String mInstructor = instructorCourse.getText().toString().trim();
                                String mPercent = percentCourse.getText().toString().trim();
                                String mMinGrade = minGradeCourse.getText().toString().trim();
                                String id = reference.push().getKey();


                                String mExamDate = examDateCourse.getText().toString();


                                if (TextUtils.isEmpty(mCourse)) {
                                    course.setError("Course name required");
                                    return;
                                }
                                if (TextUtils.isEmpty(mDescriptionCourse)) {
                                    descriptionCourse.setError("Course description required");
                                    return;
                                }
                                if (TextUtils.isEmpty(mMinGrade)) {
                                    minGradeCourse.setError("Minimum grade is required");
                                    return;
                                }
                                if (TextUtils.isEmpty(mPercent)) {
                                    percentCourse.setError("Percent required");
                                    return;
                                }

                                if (TextUtils.isEmpty(mInstructor)) {
                                    instructorCourse.setError("Course instructor required");
                                    return;
                                }

                                if (TextUtils.isEmpty(mExamDate)) {
                                    examDateCourse.setError("End date required");
                                    return;
                                }

                                if (TextUtils.isEmpty(mLocation)) {
                                    locationCourse.setError("Course location required");
                                    return;
                                } else {
                                    loader.setMessage("Adding your new grade");
                                    loader.setCanceledOnTouchOutside(false);
                                    loader.show();
                                    mMarksCourse.add(markText);
                                    mMarksMaxCourse.add(markTextMax);
                                    mMarksPercentCourse.add(percentText);
                                    gradeDateCourse.add(gradeDateText);
                                    String date = DateFormat.getDateInstance()
                                            .format(new Date());
                                    CourseModel modelCourse = new CourseModel(mCourse, mDescriptionCourse, id, date, mMarksCourse, mInstructor, mLocation, mMarksMaxCourse, mMarksPercentCourse, mMinGrade, gradeDateCourse, mPercent, mExamDate);
                                    courseModel = modelCourse;
                                    loader.dismiss();
                                    dialogCourse.dismiss();
                                }
                            }
                    });





                });


                labBtn.setOnClickListener((v)->{
                    AlertDialog.Builder myDialogLab = new AlertDialog.Builder(DisciplinesActivity.this);
                    LayoutInflater inflaterLab = LayoutInflater.from(DisciplinesActivity.this);

                    View myViewLab = inflaterLab.inflate(R.layout.update_data_lab, null);
                    myDialogLab.setView(myViewLab);

                    AlertDialog dialogLab = myDialogLab.create();
                    dialogLab.setCancelable(true);
                    dialogLab.show();

                    mMarksCourse = courseModel.getMarks();
                    mMarksMaxCourse = courseModel.getMarksMax();
                    mMarksPercentCourse = courseModel.getmMarksPercent();
                    gradeDateCourse = courseModel.getGradeDate();
                    mMarksLab = labModel.getMarks();
                    mMarksMaxLab = labModel.getMarksMax();
                    mMarksPercentLab = labModel.getmMarksPercent();
                    gradeDateLab = labModel.getGradeDate();


                    final EditText lab = myViewLab.findViewById(R.id.mEditTextLab);
                    final EditText descriptionLab = myViewLab.findViewById(R.id.mEditTextDescriptionLab);
                    final EditText minGradeLab = myViewLab.findViewById(R.id.labMinGradeUpdate);
                    final EditText percentLab = myViewLab.findViewById(R.id.mEditTextPercentLab);

                    final EditText instructorLab = myViewLab.findViewById(R.id.mEditTextInstructorLab);
                    final EditText locationLab = myViewLab.findViewById(R.id.mEditTextLocationLab);
                    EditText mMark = myViewLab.findViewById(R.id.mEditTextGrade);
                    EditText mMarkMax = myViewLab.findViewById(R.id.mEditTextGradeMax);
                    EditText mMarkPercent = myViewLab.findViewById(R.id.mEditTextPercentageGrade);
                    TextView mGradeDate = myViewLab.findViewById(R.id.labGradeDateUpdate);



                    lab.setText(labModel.getLab());
                    lab.setSelection(labModel.getLab().length());

                    descriptionLab.setText(labModel.getDescription());
                    descriptionLab.setSelection(labModel.getDescription().length());

                    percentLab.setText(labModel.getPercent());
                    percentLab.setSelection(labModel.getPercent().length());


                    minGradeLab.setText(labModel.getMinGrade());
                    minGradeLab.setSelection(labModel.getMinGrade().length());

                    instructorLab.setText(labModel.getInstructor());
                    instructorLab.setSelection(labModel.getInstructor().length());

                    locationLab.setText(labModel.getLocation());
                    locationLab.setSelection(labModel.getLocation().length());



                    Calendar calendar =  Calendar.getInstance();
                    final int year = calendar.get(Calendar.YEAR);
                    final int month = calendar.get(Calendar.MONTH);
                    final int day = calendar.get(Calendar.DAY_OF_MONTH);


                    mGradeDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatePickerDialog datePickerDialog = new DatePickerDialog(DisciplinesActivity.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                    month = month+1;
                                    String endDateStr = day + "/"+month+"/"+year;
                                    mGradeDate.setText(endDateStr);
                                }
                            }, year, month, day);
                            datePickerDialog.show();
                        }
                    });

                    AppCompatButton updateCourse = myViewLab.findViewById(R.id.UpdateBtnLab);
                    //AppCompatButton deleteCourse = myViewCourse.findViewById(R.id.deleteBtnCourse);
                    AppCompatButton addGrade = myViewLab.findViewById(R.id.AddBtnLab);





                    updateCourse.setOnClickListener((v1)->{
                        String mLab = lab.getText().toString();
                        String mDescriptionLab = descriptionLab.getText().toString().trim();
                        String mLocation = locationLab.getText().toString().trim();
                        String mInstructor = instructorLab.getText().toString().trim();
                        String mPercent = percentLab.getText().toString().trim();
                        String mMinGrade = minGradeLab.getText().toString().trim();
                        String id = reference.push().getKey();
                        String date = DateFormat.getDateInstance().format(new Date());



                        if(TextUtils.isEmpty(mLab)){
                            lab.setError("Laboratory name required");
                            return;
                        }
                        if(TextUtils.isEmpty(mDescriptionLab)){
                            descriptionLab.setError("Laboratory description required");
                            return;
                        }
                        if(TextUtils.isEmpty(mMinGrade)){
                            minGradeLab.setError("Minimum grade is required");
                            return;
                        }
                        if(TextUtils.isEmpty(mPercent)){
                            percentLab.setError("Percent required");
                            return;
                        }

                        if(TextUtils.isEmpty(mInstructor)){
                            instructorLab.setError("Laboratory instructor required");
                            return;
                        }

                        if(TextUtils.isEmpty(mLocation)){
                            locationLab.setError("Laboratory location required");
                            return;
                        }else{
                            loader.setMessage("Adding your new Laboratory");
                            loader.setCanceledOnTouchOutside(false);
                            loader.show();

                            LabModel modelLab = new LabModel(mLab, mDescriptionLab, id, date,mLocation, mInstructor,mMinGrade,mPercent, mMarksLab, mMarksMaxLab, mMarksPercentLab, gradeDateLab);
                            labModel = modelLab;

                            loader.dismiss();
                        }
                        dialogLab.dismiss();
                    });

                    addGrade.setOnClickListener((v1)->{
                        String markText, markTextMax, percentText, gradeDateText;

                        markText = mMark.getText().toString().trim();
                        markTextMax = mMarkMax.getText().toString().trim();
                        percentText= mMarkPercent.getText().toString().trim();
                        gradeDateText= mGradeDate.getText().toString().trim();

                        if(TextUtils.isEmpty(markText)){
                            mMark.setError("Grade is required");
                            return;
                        }
                        if(TextUtils.isEmpty(markTextMax)){
                            mMarkMax.setError("Max grade is required");
                            return;
                        }
                        if(TextUtils.isEmpty(gradeDateText)){
                            mGradeDate.setError("Grade date is required");
                            return;
                        }
                        if(TextUtils.isEmpty(percentText)){
                            mMarkPercent.setError("Percent is required");
                            return;
                        }else {
                            String mlab = lab.getText().toString();
                            String mDescriptionLab = descriptionLab.getText().toString().trim();
                            String mLocation = locationLab.getText().toString().trim();
                            String mInstructor = instructorLab.getText().toString().trim();
                            String mPercent = percentLab.getText().toString().trim();
                            String mMinGrade = minGradeLab.getText().toString().trim();
                            String id = reference.push().getKey();




                            if (TextUtils.isEmpty(mlab)) {
                                lab.setError("Laboratory name required");
                                return;
                            }
                            if (TextUtils.isEmpty(mDescriptionLab)) {
                                descriptionLab.setError("Laboratory description required");
                                return;
                            }
                            if (TextUtils.isEmpty(mMinGrade)) {
                                minGradeLab.setError("Minimum grade is required");
                                return;
                            }
                            if (TextUtils.isEmpty(mPercent)) {
                                percentLab.setError("Percent required");
                                return;
                            }

                            if (TextUtils.isEmpty(mInstructor)) {
                                instructorLab.setError("Laboratory instructor required");
                                return;
                            }


                            if (TextUtils.isEmpty(mLocation)) {
                                locationLab.setError("Laboratory location required");
                                return;
                            } else {
                                loader.setMessage("Adding your new laboratory");
                                loader.setCanceledOnTouchOutside(false);
                                loader.show();
                                mMarksLab.add(markText);
                                mMarksMaxLab.add(markTextMax);
                                mMarksPercentLab.add(percentText);
                                gradeDateLab.add(gradeDateText);
                                String date = DateFormat.getDateInstance()
                                        .format(new Date());
                                LabModel modelLab = new LabModel(mlab, mDescriptionLab, id, date,mLocation, mInstructor,mMinGrade,mPercent, mMarksLab, mMarksMaxLab, mMarksPercentLab, gradeDateLab);
                                labModel = modelLab;
                                loader.dismiss();
                                dialogLab.dismiss();
                            }
                        }
                    });





                });

                updateBtn.setOnClickListener((v)->{
                    String mDisciplineTxt = mDiscipline.getText().toString();
                    String mDescriptionTxt = mDescription.getText().toString().trim();
                    String mCreditsTxt = mCredits.getText().toString().trim();
                    String mDisciplineYearTxt = mDisciplineYearTv.getText().toString().trim();
                    String mDisciplineTypeTxt = mDisciplineTypeTv.getText().toString().trim();
                    String mDisciplineContextTxt = mDisciplineContextTv.getText().toString().trim();
                    String mEndDateTxt = mEndDate.getText().toString().trim();
                    String id = reference.push().getKey();

                    String date = DateFormat.getDateInstance().format(new Date());


                    if(TextUtils.isEmpty(mDisciplineTxt)){
                        mDiscipline.setError("Discipline name required");
                        return;
                    }
                    if(TextUtils.isEmpty(mDisciplineYearTxt)){
                        mDisciplineYearTv.setError("Discipline year required");
                        return;
                    }
                    if(TextUtils.isEmpty(mDisciplineTypeTxt)){
                        mDisciplineTypeTv.setError("Discipline type required");
                        return;
                    }
                    if(TextUtils.isEmpty(mDisciplineContextTxt)){
                        mDisciplineContextTv.setError("Discipline context required");
                        return;
                    }

                    if(TextUtils.isEmpty(mDescriptionTxt)){
                        mDescription.setError("Discipline description required");
                        return;
                    }
                    if(TextUtils.isEmpty(mEndDateTxt)){
                        mEndDate.setError("Discipline end date required");
                        return;
                    }
                    if(TextUtils.isEmpty(mCreditsTxt)){
                        mCredits.setError("Discipline credits required");
                        return;
                    }else{
                        loader.setMessage("Updating your discipline");
                        loader.setCanceledOnTouchOutside(false);
                        loader.show();

                        DisciplineModel disciplineModel = new DisciplineModel(mDisciplineTxt, mDescriptionTxt, id, date, mCreditsTxt,mDisciplineTypeTxt, mDisciplineContextTxt, mDisciplineYearTxt, mEndDateTxt, courseModel, labModel);
                        Model tmodel = new Model(courseModel.getCourse() + " Exam", "This is the exam for " + courseModel.getCourse(),id, date, false, courseModel.getExamDate());

                        referenceTask.child(key).setValue(tmodel);

                        reference.child(key).setValue(disciplineModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(DisciplinesActivity.this, "The discipline was updated successfully", Toast.LENGTH_SHORT).show();
                                    loader.dismiss();
                                }else{
                                    String error = task.getException().toString();
                                    Toast.makeText(DisciplinesActivity.this, "Failed " + error, Toast.LENGTH_SHORT).show();
                                    loader.dismiss();
                                }
                            }
                        });
                    }
                    dialog.dismiss();
                });

                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (referenceTask.child(key) != null)
                            referenceTask.child(key).removeValue();
                        reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(DisciplinesActivity.this, "Discipline deleted successfully", Toast.LENGTH_SHORT).show();
                                }else{
                                    String error = task.getException().toString();
                                    Toast.makeText(DisciplinesActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialog.dismiss();
                    }
                });

                closeBtn.setOnClickListener((v)->{dialog.dismiss();});
        }







            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrived_layout_discipline, parent, false);
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
            TextView courseTV = mView.findViewById(R.id.disciplineTv);
            courseTV.setText (course);
        }

        public void setStatus (String status){
            TextView descriptionTV = mView.findViewById(R.id.descriptionTvDiscipline);
            descriptionTV.setText(status);
        }

        public void setDate(String date){
            TextView dateTV = mView.findViewById(R.id.dateTvDiscipline);
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
                break;
            case R.id.deleteAcc:
                //TextView text = new TextView(HomeActivity.this);
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(DisciplinesActivity.this);
                deleteDialog.setTitle("Delete account?");
                deleteDialog.setMessage("Are you sure?");



                deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                Toast.makeText(DisciplinesActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "onFailure: User not deleted "+e.getMessage());
                            }
                        });;
                        deleteDialog.create().dismiss();}
                });

                deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteDialog.create().dismiss();
                    }
                });

                deleteDialog.create().show();
                break;


        }

        return super.onOptionsItemSelected(item);
    }

}