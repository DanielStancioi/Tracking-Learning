
package com.example.tlfsvf;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private String endDate, context, type, year;
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
        floatingActionButton = findViewById(R.id.fabProgressDisciplines);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart();
            }
        });



    }

    private void showChart() {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(ProgressActivity.this);
        LayoutInflater inflater =LayoutInflater.from(ProgressActivity.this);
        View view = inflater.inflate(R.layout.chart_layout, null);
        mDialog.setView(view);


        AlertDialog dialog = mDialog.create();
        dialog.show();
        BarChart courseBC, labBC;
        final EditText context = view.findViewById(R.id.contextChart);
        final EditText type = view.findViewById(R.id.typeChart);
        final EditText year = view.findViewById(R.id.yearChart);
        courseBC = view.findViewById(R.id.courseBarChart);
        labBC = view.findViewById(R.id.labBarChart);
        AppCompatButton searchBtn = view.findViewById(R.id.searchBtn);
        AppCompatButton cancelBtn = view.findViewById(R.id.cancelButtonChart);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contextTxt = context.getText().toString().trim().toLowerCase(Locale.ROOT);
                String typeTxt = type.getText().toString().trim().toLowerCase(Locale.ROOT);
                String yearTxt = year.getText().toString().trim().toLowerCase(Locale.ROOT);



                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<Float, Integer> gradesCourseMap = new HashMap<>();
                        Map<Float, Integer> gradesLabMap = new HashMap<>();
                        gradesLabMap.clear();
                        gradesCourseMap.clear();


                        for(DataSnapshot ss: snapshot.getChildren()) {
                            DisciplineModel dmodel = ss.getValue(DisciplineModel.class);
                            CourseModel cmodel = dmodel.getCmodel();
                            LabModel lmodel = dmodel.getLabModel();
                            List<Float> gradesCourse = new ArrayList<>();
                            List<Float> gradesLab = new ArrayList<>();


                            if((dmodel.getContext().toLowerCase(Locale.ROOT).compareTo(contextTxt) == 0) && (dmodel.getType().toLowerCase(Locale.ROOT).compareTo(typeTxt) == 0) && (dmodel.getYear().toLowerCase(Locale.ROOT).compareTo(yearTxt) == 0)){
                                for(int i = 1; i<cmodel.getMarks().size(); i++){
                                    gradesCourse.add(Float.parseFloat(cmodel.getMarks().get(i)));
                                }

                                for(int i = 1; i<lmodel.getMarks().size(); i++){
                                    gradesLab.add(Float.parseFloat(lmodel.getMarks().get(i)));
                                }

                                for(Float el : gradesCourse){
                                    if(!gradesCourseMap.containsKey(el)){
                                        gradesCourseMap.put( el, 1);
                                    }else{
                                        int c = gradesCourseMap.get(el);
                                        c = c + 1;
                                        gradesCourseMap.put(el, c);
                                    }
                                }

                                for(Float el : gradesLab){
                                    if(!gradesLabMap.containsKey(el)){
                                        gradesLabMap.put( el, 1);
                                    }else{
                                        int c = gradesLabMap.get(el);
                                        c = c + 1;
                                        gradesLabMap.put(el, c);
                                    }
                                }}





                    }
                        // for course
                        if (!gradesCourseMap.isEmpty()){
                            int i = 0;
                            ArrayList<BarEntry> barEntriesCourse = new ArrayList<>();
                            ArrayList<String> labelsName = new ArrayList<>();

                            for (Map.Entry<Float, Integer> me : gradesCourseMap.entrySet()){
                                barEntriesCourse.add(new BarEntry(i, me.getValue()));
                                labelsName.add(me.getKey().toString());
                                i++;
                            }
                            BarDataSet barDataSetCourse = new BarDataSet(barEntriesCourse, "Amount of grades");
                            Description description = new Description();
                            description.setText("Grades");
                            description.setTextSize(10f);
                            courseBC.setDescription(description);
                            barDataSetCourse.setColor(getResources().getColor(R.color.purple_300));
                            barDataSetCourse.setValueTextSize(13f);
                            barDataSetCourse.setValueTextColor(getResources().getColor(R.color.black));
                            BarData theDataCourse = new BarData(barDataSetCourse);
                            courseBC.setData(theDataCourse);



                            XAxis xaxis = courseBC.getXAxis();
                            YAxis yAxisRigth = courseBC.getAxisRight();
                            yAxisRigth.setAxisMinimum(0);
                            yAxisRigth.setAxisMaximum(gradesCourseMap.values().stream().max(Integer::compare).get());
                            yAxisRigth.setLabelCount(gradesCourseMap.values().stream().max(Integer::compare).get());
                            YAxis yAxisLeft = courseBC.getAxisLeft();
                            yAxisLeft.setAxisMinimum(0);
                            yAxisLeft.setAxisMaximum(gradesCourseMap.values().stream().max(Integer::compare).get());
                            yAxisLeft.setLabelCount(gradesCourseMap.values().stream().max(Integer::compare).get());
                            xaxis.setValueFormatter(new IndexAxisValueFormatter(labelsName));
                            xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xaxis.setTextSize(13f);
                            xaxis.setDrawGridLines(false);
                            xaxis.setDrawAxisLine(false);
                            xaxis.setGranularity(1f);
                            xaxis.setLabelCount(labelsName.size());
                            xaxis.setLabelRotationAngle(270);
                            courseBC.animateY(2000);
                            courseBC.invalidate();


                        } else {
                            //courseBC.removeAllViews();
                            courseBC.clear();
                            //labBC.removeAllViews();
                            labBC.clear();

                            //searchBtn.setError("No data found");
                            reference.removeEventListener(this);

                            return;


                        }



                        // for lab
                        if (!gradesLabMap.isEmpty()){
                            int i = 0;
                            ArrayList<BarEntry> barEntriesLab = new ArrayList<>();
                            ArrayList<String> labelsNameLab = new ArrayList<>();

                            for (Map.Entry<Float, Integer> me : gradesLabMap.entrySet()){
                                barEntriesLab.add(new BarEntry(i, me.getValue()));
                                labelsNameLab.add(me.getKey().toString());
                                i++;
                            }
                            BarDataSet barDataSetLab = new BarDataSet(barEntriesLab, "Amount of grades");
                            Description description = new Description();
                            description.setText("Grades");
                            description.setTextSize(10f);
                            labBC.setDescription(description);
                            barDataSetLab.setColor(getResources().getColor(R.color.purple_300));
                            barDataSetLab.setValueTextSize(13f);
                            barDataSetLab.setValueTextColor(getResources().getColor(R.color.black));
                            BarData theDataLab = new BarData(barDataSetLab);
                            labBC.setData(theDataLab);



                            XAxis xaxis = labBC.getXAxis();
                            YAxis yAxisRigth = labBC.getAxisRight();
                            yAxisRigth.setAxisMinimum(0);
                            yAxisRigth.setAxisMaximum(gradesLabMap.values().stream().max(Integer::compare).get());
                            yAxisRigth.setLabelCount(gradesLabMap.values().stream().max(Integer::compare).get());
                            YAxis yAxisLeft = labBC.getAxisLeft();
                            yAxisLeft.setAxisMinimum(0);
                            yAxisLeft.setAxisMaximum(gradesLabMap.values().stream().max(Integer::compare).get());
                            yAxisLeft.setLabelCount(gradesLabMap.values().stream().max(Integer::compare).get());
                            xaxis.setValueFormatter(new IndexAxisValueFormatter(labelsNameLab));
                            xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xaxis.setTextSize(13f);
                            xaxis.setDrawGridLines(false);
                            xaxis.setDrawAxisLine(false);
                            xaxis.setGranularity(1f);
                            xaxis.setLabelCount(labelsNameLab.size());
                            xaxis.setLabelRotationAngle(270);
                            labBC.animateY(2000);
                            labBC.invalidate();


                        }else {
                            //courseBC.removeAllViews();
                            courseBC.clear();
                            //labBC.removeAllViews();
                            labBC.clear();
                            //searchBtn.setError("No data found");
                            reference.removeEventListener(this);
                            return;

                        }
                        reference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });










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
                holder.setDescription(dmodel.getContext());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(position).getKey();
                        discipline = dmodel.getName();
                        description = dmodel.getDescription();
                        credits = dmodel.getCredits();
                        context = dmodel.getContext();
                        type = dmodel.getType();
                        year = dmodel.getYear();

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
                TextView mContext = view.findViewById(R.id.disciplineContextUpdate);
                TextView mType = view.findViewById(R.id.disciplineTypeUpdate);
                TextView mYear = view.findViewById(R.id.disciplineYearUpdate);

                mDiscipline.setText(discipline);

                mDescription.setText(description);

                mContext.setText(context);
                mType.setText(type);
                mYear.setText(year);

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
        TableLayout tableLab = myViewLab.findViewById(R.id.tableLabStat);


        androidx.appcompat.widget.AppCompatButton cancel = myViewLab.findViewById(R.id.cancelButtonLabProgress);


        cancel.setOnClickListener((v)->{dialogLab.dismiss();});
        DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("disciplines").child(onlineUserID).child(key);

        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DisciplineModel dmodel = snapshot.getValue(DisciplineModel.class);
                Map<String, List<Pair<String, String>>> gradesDates = new HashMap<>();
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
                List<String> lMarksDate = lmodel.getGradeDate();



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


                //table
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

                TableRow tbrow0 = new TableRow(ProgressActivity.this);
                TextView tv0 = new TextView(ProgressActivity.this);
                tv0.setText(" Date ");
                tv0.setTextColor(Color.BLACK);
                tv0.setElegantTextHeight(true);
                tv0.setTextSize(17);
                tv0.setTypeface(null, Typeface.BOLD);
                tv0.setLayoutParams(text1Params);
                tbrow0.addView(tv0);
                TextView tv1 = new TextView(ProgressActivity.this);
                tv1.setText(" Grade ");
                tv1.setTextColor(Color.BLACK);
                tv1.setTypeface(null, Typeface.BOLD);

                tv1.setElegantTextHeight(true);
                tv1.setTextSize(17);
                tv1.setLayoutParams(text2Params);
                tbrow0.addView(tv1);


                TextView tv3 = new TextView(ProgressActivity.this);
                tv3.setText(" Percent ");
                tv3.setTextColor(Color.BLACK);
                tv3.setElegantTextHeight(true);
                tv3.setTextSize(17);
                tv3.setTypeface(null, Typeface.BOLD);
                tv3.setLayoutParams(text3Params);
                tbrow0.addView(tv3);
                tbrow0.setLayoutParams(tableRowParams);
                tbrow0.setBackgroundResource(R.drawable.border);

                tableLab.addView(tbrow0);

                if (lMarks.size()>1){
                    for (int i = 1; i<lMarks.size(); i++){
                        String keyStr = lMarksDate.get(i);
                        if(!gradesDates.containsKey(keyStr)){
                            List<Pair<String, String>> lst = new ArrayList<>();
                            lst.add(new Pair<>(lMarks.get(i), lMarksPercent.get(i)+"%"));
                            gradesDates.put(keyStr, lst);
                        }else{
                            List<Pair<String, String>> lst = gradesDates.get(keyStr);
                            lst.add(new Pair<>(lMarks.get(i), lMarksPercent.get(i)+"%"));
                            gradesDates.put(keyStr, lst);
                        }
                    }

                    for (Map.Entry<String, List<Pair<String,String>>> me : gradesDates.entrySet()){
                        List<Pair<String,String>> gradesPercent = me.getValue();
                        List<String> grades = new ArrayList<>();
                        List<String> percents = new ArrayList<>();
                        for (Pair<String,String> el:gradesPercent){
                            grades.add(el.first);
                            percents.add(el.second);
                        }
                        String date = me.getKey();
                        String gradesStr = String.join(", ", grades);
                        String percentsStr = String.join(", ", percents);
                        TableRow tbrow1 = new TableRow(ProgressActivity.this);
                        TextView tv2 = new TextView(ProgressActivity.this);

                        tv2.setText("  "+date+"  ");
                        tv2.setTextColor(Color.BLACK);

                        tv2.setElegantTextHeight(true);
                        tv2.setTextSize(17);
                        tv2.setLayoutParams(text1Params);
                        tbrow1.addView(tv2);
                        TextView tv4 = new TextView(ProgressActivity.this);
                        tv4.setText("  "+gradesStr+"  ");
                        tv4.setTextColor(Color.BLACK);

                        tv4.setElegantTextHeight(true);
                        tv4.setTextSize(17);
                        tv4.setLayoutParams(text2Params);
                        tbrow1.addView(tv4);

                        TextView tv5 = new TextView(ProgressActivity.this);
                        tv5.setText("  "+percentsStr+"  ");
                        tv5.setTextColor(Color.BLACK);

                        tv5.setElegantTextHeight(true);
                        tv5.setTextSize(17);
                        tv5.setLayoutParams(text3Params);
                        tbrow1.addView(tv5);
                        tbrow1.setLayoutParams(tableRowParams);
                        tbrow1.setBackgroundResource(R.drawable.border);
                        tableLab.addView(tbrow1);


                    }
                }else {

                    TableRow tbrow1 = new TableRow(ProgressActivity.this);
                    TextView tv2 = new TextView(ProgressActivity.this);
                    tv2.setText(" None ");
                    tv2.setTextColor(Color.BLACK);

                    tv2.setElegantTextHeight(true);
                    tv2.setTextSize(17);
                    tv2.setLayoutParams(text1Params);
                    tbrow1.addView(tv2);
                    TextView tv4 = new TextView(ProgressActivity.this);
                    tv4.setText(" None ");
                    tv4.setTextColor(Color.BLACK);

                    tv4.setElegantTextHeight(true);
                    tv4.setTextSize(17);
                    tv4.setLayoutParams(text2Params);
                    tbrow1.addView(tv4);
                    TextView tv5 = new TextView(ProgressActivity.this);
                    tv5.setText(" None ");
                    tv5.setTextColor(Color.BLACK);

                    tv5.setElegantTextHeight(true);
                    tv5.setTextSize(17);
                    tv5.setLayoutParams(text3Params);
                    tbrow1.addView(tv5);

                    tbrow1.setLayoutParams(tableRowParams);
                    tbrow1.setBackgroundResource(R.drawable.border);
                    tableLab.addView(tbrow1);

                }
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
        TableLayout tableCourse = myViewCourse.findViewById(R.id.tableCourseStat);


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
                Map<String, List<Pair<String, String>>> gradesDates = new HashMap<>();



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

                //table
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

                TableRow tbrow0 = new TableRow(ProgressActivity.this);
                TextView tv0 = new TextView(ProgressActivity.this);
                tv0.setText(" Date ");
                tv0.setTextColor(Color.BLACK);
                tv0.setElegantTextHeight(true);
                tv0.setTextSize(17);
                tv0.setTypeface(null, Typeface.BOLD);
                tv0.setLayoutParams(text1Params);
                tbrow0.addView(tv0);
                TextView tv1 = new TextView(ProgressActivity.this);
                tv1.setText(" Grade ");
                tv1.setTextColor(Color.BLACK);
                tv1.setTypeface(null, Typeface.BOLD);

                tv1.setElegantTextHeight(true);
                tv1.setTextSize(17);
                tv1.setLayoutParams(text2Params);
                tbrow0.addView(tv1);


                TextView tv3 = new TextView(ProgressActivity.this);
                tv3.setText(" Percent ");
                tv3.setTextColor(Color.BLACK);
                tv3.setElegantTextHeight(true);
                tv3.setTextSize(17);
                tv3.setTypeface(null, Typeface.BOLD);
                tv3.setLayoutParams(text3Params);
                tbrow0.addView(tv3);
                tbrow0.setLayoutParams(tableRowParams);
                tbrow0.setBackgroundResource(R.drawable.border);

                tableCourse.addView(tbrow0);

                if (cMarks.size()>1){
                    for (int i = 1; i<cMarks.size(); i++){
                        String keyStr = cMarksDate.get(i);
                        if(!gradesDates.containsKey(keyStr)){
                            List<Pair<String, String>> lst = new ArrayList<>();
                            lst.add(new Pair<>(cMarks.get(i), cMarksPercent.get(i)+"%"));
                            gradesDates.put(keyStr, lst);
                        }else{
                            List<Pair<String, String>> lst = gradesDates.get(keyStr);
                            lst.add(new Pair<>(cMarks.get(i), cMarksPercent.get(i)+"%"));
                            gradesDates.put(keyStr, lst);
                        }
                    }

                    for (Map.Entry<String, List<Pair<String,String>>> me : gradesDates.entrySet()){
                        List<Pair<String,String>> gradesPercent = me.getValue();
                        List<String> grades = new ArrayList<>();
                        List<String> percents = new ArrayList<>();
                        for (Pair<String,String> el:gradesPercent){
                            grades.add(el.first);
                            percents.add(el.second);
                        }
                        String date = me.getKey();
                        String gradesStr = String.join(", ", grades);
                        String percentsStr = String.join(", ", percents);
                        TableRow tbrow1 = new TableRow(ProgressActivity.this);
                        TextView tv2 = new TextView(ProgressActivity.this);

                        tv2.setText("  "+date+"  ");
                        tv2.setTextColor(Color.BLACK);

                        tv2.setElegantTextHeight(true);
                        tv2.setTextSize(17);
                        tv2.setLayoutParams(text1Params);
                        tbrow1.addView(tv2);
                        TextView tv4 = new TextView(ProgressActivity.this);
                        tv4.setText("  "+gradesStr+"  ");
                        tv4.setTextColor(Color.BLACK);

                        tv4.setElegantTextHeight(true);
                        tv4.setTextSize(17);
                        tv4.setLayoutParams(text2Params);
                        tbrow1.addView(tv4);

                        TextView tv5 = new TextView(ProgressActivity.this);
                        tv5.setText("  "+percentsStr+"  ");
                        tv5.setTextColor(Color.BLACK);

                        tv5.setElegantTextHeight(true);
                        tv5.setTextSize(17);
                        tv5.setLayoutParams(text3Params);
                        tbrow1.addView(tv5);
                        tbrow1.setLayoutParams(tableRowParams);
                        tbrow1.setBackgroundResource(R.drawable.border);
                        tableCourse.addView(tbrow1);


                    }
                }else {

                    TableRow tbrow1 = new TableRow(ProgressActivity.this);
                    TextView tv2 = new TextView(ProgressActivity.this);
                    tv2.setText(" None ");
                    tv2.setTextColor(Color.BLACK);

                    tv2.setElegantTextHeight(true);
                    tv2.setTextSize(17);
                    tv2.setLayoutParams(text1Params);
                    tbrow1.addView(tv2);
                    TextView tv4 = new TextView(ProgressActivity.this);
                    tv4.setText(" None ");
                    tv4.setTextColor(Color.BLACK);

                    tv4.setElegantTextHeight(true);
                    tv4.setTextSize(17);
                    tv4.setLayoutParams(text2Params);
                    tbrow1.addView(tv4);
                    TextView tv5 = new TextView(ProgressActivity.this);
                    tv5.setText(" None ");
                    tv5.setTextColor(Color.BLACK);

                    tv5.setElegantTextHeight(true);
                    tv5.setTextSize(17);
                    tv5.setLayoutParams(text3Params);
                    tbrow1.addView(tv5);

                    tbrow1.setLayoutParams(tableRowParams);
                    tbrow1.setBackgroundResource(R.drawable.border);
                    tableCourse.addView(tbrow1);

                }
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
                break;
            case R.id.deleteAcc:
                //TextView text = new TextView(HomeActivity.this);
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(ProgressActivity.this);
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
                                Toast.makeText(ProgressActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
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