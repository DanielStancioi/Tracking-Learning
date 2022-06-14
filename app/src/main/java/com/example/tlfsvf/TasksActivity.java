package com.example.tlfsvf;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TasksActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private FloatingActionButton floatingActionButtonProgress;
    private AppCompatButton searchBtn, resetBtn;
    private TextView dateSearch;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;

    private String key="";
    private String task;
    private String description;
    private String dueDate;
    private boolean done;

    private ProgressDialog loader;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        toolbar = findViewById(R.id.tasksToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My tasks");

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        onlineUserID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID);


        floatingActionButton = findViewById(R.id.fab);
        floatingActionButtonProgress = findViewById(R.id.tasksProgress);

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
        View myView = inflater.inflate(R.layout.tasks_progress, null);
        myDialog.setView(myView);

        AlertDialog dialog = myDialog.create();

        dialog.show();

        final TextView done = myView.findViewById(R.id.FinishedTasks);
        final TextView undone = myView.findViewById(R.id.StillInProgressTasks);
        final TextView late = myView.findViewById(R.id.lateTasks);
        TableLayout tableTasksUndone = myView.findViewById(R.id.tableTasksUndoneStat);
        TableLayout tableTasksDone = myView.findViewById(R.id.tableTasksDoneStat);
        TableLayout tableTasksLate = myView.findViewById(R.id.tableTasksLateStat);

        List<String> doneTasks = new ArrayList<>();
        List<String> undoneTasks = new ArrayList<>();
        List<String> lateTasks = new ArrayList<>();
        List<Model> tasksUndoneLst = new ArrayList<>();
        List<Model> tasksDoneLst = new ArrayList<>();
        List<Model> tasksLateLst = new ArrayList<>();
        Map<String, List<String>> mapDateNameUndone = new HashMap<>();
        Map<String, List<String>> mapDateNameDone = new HashMap<>();
        Map<String, List<String>> mapDateNameLate = new HashMap<>();



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ss: snapshot.getChildren()){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Calendar c = Calendar.getInstance();
                    String getCurrentDateTime = sdf.format(c.getTime());
                    boolean done = (boolean) ss.child("done").getValue();
                    String date = ss.child("dueDate").getValue().toString();
                    Model model = ss.getValue(Model.class);


                    if (done) {
                        //doneTasks.add(ss.child("task").getValue().toString());
                        tasksDoneLst.add(model);

                    } else {
                        if (isDateAfter(getCurrentDateTime, date))
                        {
                            //lateTasks.add(ss.child("task").getValue().toString());
                            tasksLateLst.add(model);
                        }
                        else
                        {
                            //undoneTasks.add(ss.child("task").getValue().toString());
                            tasksUndoneLst.add(model);
                        }

                    }
                }
                done.setText(""+tasksDoneLst.size());
                undone.setText(""+tasksUndoneLst.size());
                late.setText(""+tasksLateLst.size());


                //undone table
                for(Model el : tasksUndoneLst){
                    if(!mapDateNameUndone.containsKey(el.getDueDate())){
                       List<String> lst = new ArrayList<>();
                       lst.add("*"+el.getTask());
                       mapDateNameUndone.put(el.getDueDate(), lst);
                    }else{
                        List<String> lst = mapDateNameUndone.get(el.getDueDate());
                        lst.add("*"+el.getTask());
                        mapDateNameUndone.put(el.getDueDate(), lst);
                    }
                }
                TableLayout.LayoutParams tableRowParams=
                        new TableLayout.LayoutParams
                                (TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);




                TableRow.LayoutParams text1Params = new TableRow.LayoutParams();
                text1Params.width = 0;
                text1Params.weight = (float) 0.5;
                TableRow.LayoutParams text2Params = new TableRow.LayoutParams();
                text2Params.weight = (float) 0.5;
                text2Params.width = 0;



                TableRow tbrow0 = new TableRow(TasksActivity.this);
                TextView tv0 = new TextView(TasksActivity.this);
                tv0.setText(" Date ");
                tv0.setTextColor(Color.BLACK);
                tv0.setElegantTextHeight(true);
                tv0.setTextSize(17);
                tv0.setLayoutParams(text1Params);
                tv0.setTypeface(null, Typeface.BOLD);

                tbrow0.addView(tv0);
                TextView tv1 = new TextView(TasksActivity.this);
                tv1.setText(" Task name ");
                tv1.setTextColor(Color.BLACK);
                tv1.setTypeface(null, Typeface.BOLD);

                tv1.setElegantTextHeight(true);
                tv1.setTextSize(17);
                tv1.setLayoutParams(text2Params);
                tbrow0.addView(tv1);
                tbrow0.setLayoutParams(tableRowParams);
                tbrow0.setBackgroundResource(R.drawable.border);
                tableTasksUndone.addView(tbrow0);

                if (mapDateNameUndone.isEmpty()){


                    TableRow tbrow1 = new TableRow(TasksActivity.this);
                    TextView tv2 = new TextView(TasksActivity.this);
                    tv2.setText(" None ");
                    tv2.setTextColor(Color.BLACK);

                    tv2.setElegantTextHeight(true);
                    tv2.setTextSize(17);
                    tv2.setLayoutParams(text1Params);
                    tbrow1.addView(tv2);
                    TextView tv3 = new TextView(TasksActivity.this);
                    tv3.setText(" None ");
                    tv3.setTextColor(Color.BLACK);

                    tv3.setElegantTextHeight(true);
                    tv3.setTextSize(17);
                    tv3.setLayoutParams(text2Params);
                    tbrow1.addView(tv3);
                    tbrow1.setLayoutParams(tableRowParams);
                    tbrow1.setBackgroundResource(R.drawable.border);
                    tableTasksUndone.addView(tbrow1);


                }else{

                    for (Map.Entry<String, List<String>> me : mapDateNameUndone.entrySet()){
                        String date = me.getKey();

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < me.getValue().size()-1; i++) {
                            stringBuilder.append("\n");
                        }
                        String enter = stringBuilder.toString();

                        List<String> tasks = me.getValue();
                        String tasksStr = String.join(",\n", tasks);
                        TableRow tbrow1 = new TableRow(TasksActivity.this);
                        TextView tv2 = new TextView(TasksActivity.this);

                        tv2.setText(  date+enter);
                        tv2.setTextColor(Color.BLACK);

                        tv2.setElegantTextHeight(true);
                        tv2.setTextSize(17);
                        tv2.setLayoutParams(text1Params);
                        tbrow1.addView(tv2);
                        TextView tv3 = new TextView(TasksActivity.this);
                        tv3.setText(tasksStr);
                        tv3.setTextColor(Color.BLACK);

                        tv3.setElegantTextHeight(true);
                        tv3.setTextSize(17);
                        tv3.setLayoutParams(text2Params);
                        tbrow1.addView(tv3);
                        tbrow1.setLayoutParams(tableRowParams);
                        tbrow1.setBackgroundResource(R.drawable.border);
                        tableTasksUndone.addView(tbrow1);
                    }
                }

                //done table
                for(Model el : tasksDoneLst){
                    if(!mapDateNameDone.containsKey(el.getDueDate())){
                        List<String> lst = new ArrayList<>();
                        lst.add("*"+el.getTask());
                        mapDateNameDone.put(el.getDueDate(), lst);
                    }else{
                        List<String> lst = mapDateNameDone.get(el.getDueDate());
                        lst.add("*"+el.getTask());
                        mapDateNameDone.put(el.getDueDate(), lst);
                    }
                }



                TableRow tbrow0Done = new TableRow(TasksActivity.this);
                TextView tv0Done = new TextView(TasksActivity.this);
                tv0Done.setText(" Date ");
                tv0Done.setTextColor(Color.BLACK);
                tv0Done.setElegantTextHeight(true);
                tv0Done.setTextSize(17);
                tv0Done.setLayoutParams(text1Params);
                tv0Done.setTypeface(null, Typeface.BOLD);

                tbrow0Done.addView(tv0Done);
                TextView tv1Done = new TextView(TasksActivity.this);
                tv1Done.setText(" Task name ");
                tv1Done.setTextColor(Color.BLACK);
                tv1Done.setTypeface(null, Typeface.BOLD);

                tv1Done.setElegantTextHeight(true);
                tv1Done.setTextSize(17);
                tv1Done.setLayoutParams(text2Params);
                tbrow0Done.addView(tv1Done);
                tbrow0Done.setLayoutParams(tableRowParams);
                tbrow0Done.setBackgroundResource(R.drawable.border);
                tableTasksDone.addView(tbrow0Done);

                if (mapDateNameDone.isEmpty()){


                    TableRow tbrow1Done = new TableRow(TasksActivity.this);
                    TextView tv2Done = new TextView(TasksActivity.this);
                    tv2Done.setText(" None ");
                    tv2Done.setTextColor(Color.BLACK);

                    tv2Done.setElegantTextHeight(true);
                    tv2Done.setTextSize(17);
                    tv2Done.setLayoutParams(text1Params);
                    tbrow1Done.addView(tv2Done);
                    TextView tv3Done = new TextView(TasksActivity.this);
                    tv3Done.setText(" None ");
                    tv3Done.setTextColor(Color.BLACK);

                    tv3Done.setElegantTextHeight(true);
                    tv3Done.setTextSize(17);
                    tv3Done.setLayoutParams(text2Params);
                    tbrow1Done.addView(tv3Done);
                    tbrow1Done.setLayoutParams(tableRowParams);
                    tbrow1Done.setBackgroundResource(R.drawable.border);
                    tableTasksDone.addView(tbrow1Done);


                }else{

                    for (Map.Entry<String, List<String>> me : mapDateNameDone.entrySet()){
                        String date = me.getKey();

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < me.getValue().size()-1; i++) {
                            stringBuilder.append("\n");
                        }
                        String enter = stringBuilder.toString();

                        List<String> tasks = me.getValue();
                        String tasksStr = String.join(",\n", tasks);
                        TableRow tbrow1Done = new TableRow(TasksActivity.this);
                        TextView tv2Done = new TextView(TasksActivity.this);

                        tv2Done.setText(  date+enter);
                        tv2Done.setTextColor(Color.BLACK);

                        tv2Done.setElegantTextHeight(true);
                        tv2Done.setTextSize(17);
                        tv2Done.setLayoutParams(text1Params);
                        tbrow1Done.addView(tv2Done);
                        TextView tv3Done = new TextView(TasksActivity.this);
                        tv3Done.setText(tasksStr);
                        tv3Done.setTextColor(Color.BLACK);

                        tv3Done.setElegantTextHeight(true);
                        tv3Done.setTextSize(17);
                        tv3Done.setLayoutParams(text2Params);
                        tbrow1Done.addView(tv3Done);
                        tbrow1Done.setLayoutParams(tableRowParams);
                        tbrow1Done.setBackgroundResource(R.drawable.border);
                        tableTasksDone.addView(tbrow1Done);
                    }
                }

                //late table
                for(Model el : tasksLateLst){
                    if(!mapDateNameLate.containsKey(el.getDueDate())){
                        List<String> lst = new ArrayList<>();
                        lst.add("*"+el.getTask());
                        mapDateNameLate.put(el.getDueDate(), lst);
                    }else{
                        List<String> lst = mapDateNameLate.get(el.getDueDate());
                        lst.add("*"+el.getTask());
                        mapDateNameLate.put(el.getDueDate(), lst);
                    }
                }



                TableRow tbrow0Late = new TableRow(TasksActivity.this);
                TextView tv0Late = new TextView(TasksActivity.this);
                tv0Late.setText(" Date ");
                tv0Late.setTextColor(Color.BLACK);
                tv0Late.setElegantTextHeight(true);
                tv0Late.setTextSize(17);
                tv0Late.setLayoutParams(text1Params);
                tv0Late.setTypeface(null, Typeface.BOLD);

                tbrow0Late.addView(tv0Late);
                TextView tv1Late = new TextView(TasksActivity.this);
                tv1Late.setText(" Task name ");
                tv1Late.setTextColor(Color.BLACK);
                tv1Late.setTypeface(null, Typeface.BOLD);

                tv1Late.setElegantTextHeight(true);
                tv1Late.setTextSize(17);
                tv1Late.setLayoutParams(text2Params);
                tbrow0Late.addView(tv1Late);
                tbrow0Late.setLayoutParams(tableRowParams);
                tbrow0Late.setBackgroundResource(R.drawable.border);
                tableTasksLate.addView(tbrow0Late);

                if (mapDateNameLate.isEmpty()){


                    TableRow tbrow1Late = new TableRow(TasksActivity.this);
                    TextView tv2Late = new TextView(TasksActivity.this);
                    tv2Late.setText(" None ");
                    tv2Late.setTextColor(Color.BLACK);

                    tv2Late.setElegantTextHeight(true);
                    tv2Late.setTextSize(17);
                    tv2Late.setLayoutParams(text1Params);
                    tbrow1Late.addView(tv2Late);
                    TextView tv3Late = new TextView(TasksActivity.this);
                    tv3Late.setText(" None ");
                    tv3Late.setTextColor(Color.BLACK);

                    tv3Late.setElegantTextHeight(true);
                    tv3Late.setTextSize(17);
                    tv3Late.setLayoutParams(text2Params);
                    tbrow1Late.addView(tv3Late);
                    tbrow1Late.setLayoutParams(tableRowParams);
                    tbrow1Late.setBackgroundResource(R.drawable.border);
                    tableTasksLate.addView(tbrow1Late);


                }else{

                    for (Map.Entry<String, List<String>> me : mapDateNameLate.entrySet()){
                        String date = me.getKey();

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < me.getValue().size()-1; i++) {
                            stringBuilder.append("\n");
                        }
                        String enter = stringBuilder.toString();

                        List<String> tasks = me.getValue();
                        String tasksStr = String.join(",\n", tasks);
                        TableRow tbrow1Late = new TableRow(TasksActivity.this);
                        TextView tv2Late = new TextView(TasksActivity.this);

                        tv2Late.setText(  date+enter);
                        tv2Late.setTextColor(Color.BLACK);

                        tv2Late.setElegantTextHeight(true);
                        tv2Late.setTextSize(17);
                        tv2Late.setLayoutParams(text1Params);
                        tbrow1Late.addView(tv2Late);
                        TextView tv3Late = new TextView(TasksActivity.this);
                        tv3Late.setText(tasksStr);
                        tv3Late.setTextColor(Color.BLACK);

                        tv3Late.setElegantTextHeight(true);
                        tv3Late.setTextSize(17);
                        tv3Late.setLayoutParams(text2Params);
                        tbrow1Late.addView(tv3Late);
                        tbrow1Late.setLayoutParams(tableRowParams);
                        tbrow1Late.setBackgroundResource(R.drawable.border);
                        tableTasksLate.addView(tbrow1Late);
                    }
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        AppCompatButton cancel = myView.findViewById(R.id.cancelButtonTaskProgress);
        cancel.setOnClickListener((v)->{dialog.dismiss();});
    }

    private boolean isDateAfter (String startDate, String endDate) {

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
    private boolean isDateEqual (String startDate, String endDate) {

        try {
            String myFormatString = "dd/MM/yyyy"; // for example
            SimpleDateFormat df = new SimpleDateFormat(myFormatString);
            Date endingDate = df.parse(endDate);
            Date startingDate = df.parse(startDate);

            return endingDate.equals(startingDate);
        } catch (Exception e) {
            return false;
        }

    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        DatePickerDialog.OnDateSetListener setListener;
        View myView = inflater.inflate(R.layout.input_file, null);
        myDialog.setView(myView);

        AlertDialog dialog = myDialog.create();

        dialog.show();

        final EditText task = myView.findViewById(R.id.task);
        final EditText description = myView.findViewById(R.id.taskDescription);
        final TextView dueDate = myView.findViewById(R.id.taskDueDate);

        Calendar calendar =  Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(TasksActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        String dueDateStr = day + "/"+month+"/"+year;
                        dueDate.setText(dueDateStr);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });



        AppCompatButton save = myView.findViewById(R.id.saveButton);
        AppCompatButton cancel = myView.findViewById(R.id.cancelButton);
        cancel.setOnClickListener((v)->{dialog.dismiss();});

        save.setOnClickListener((v)->{
            String mTask = task.getText().toString();
            String mDescription = description.getText().toString().trim();
            String mDueDate = dueDate.getText().toString().trim();
            String id = reference.push().getKey();
            String date = DateFormat.getDateInstance().format(new Date());

            if(TextUtils.isEmpty(mTask)){
                task.setError("Task name required");
                return;
            }
            if(TextUtils.isEmpty(mDueDate)){
                dueDate.setError("Due date required");
                return;
            }

            if(TextUtils.isEmpty(mDescription)){
                description.setError("Description for the task is required");
                return;
            }else{
                loader.setMessage("Adding your new task");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                Model model = new Model(mTask, mDescription, id, date, false, mDueDate);
                reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(TasksActivity.this, "The task was added successfully", Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(TasksActivity.this, "Failed " + error, Toast.LENGTH_SHORT).show();
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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }


        });

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(reference, Model.class).build();
        FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Model model) {
                holder.setDate(model.getDueDate());
                holder.setTask(model.getTask());

                key = getRef(position).getKey();

                DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID).child(key);



                final ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Calendar c = Calendar.getInstance();
                        String getCurrentDateTime = sdf.format(c.getTime());
                        String text;
                        boolean done = (boolean) snapshot.child("done").getValue();
                        String date = snapshot.child("dueDate").getValue().toString();

                        if (done) {
                            text = "Your task is DONE ";
                        } else {
                            if (isDateAfter(getCurrentDateTime, date))
                            {
                                text = "Your task is LATE ";
                            }
                            else
                            {
                                text = "Your task is STILL IN PROGRESS";
                            }

                        }
                        holder.setStatus(text);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };

                reff.addValueEventListener(eventListener);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(position).getKey();
                        task = model.getTask();
                        description = model.getDescription();
                        dueDate = model.getDueDate();
                        TasksActivity.this.done = model.isDone();
                        reff.removeEventListener(eventListener);
                        reff.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean done = (boolean) snapshot.child("done").getValue();

                                if (done) {
                                    updateTaskSetUndone();
                                    reff.removeEventListener(this);
                                } else {
                                    updateTaskSetDone();
                                    reff.removeEventListener(this);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



                    }
                });


            }

            private void updateTaskSetDone() {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(TasksActivity.this);
                LayoutInflater inflater =LayoutInflater.from(TasksActivity.this);
                View view = inflater.inflate(R.layout.update_data, null);
                mDialog.setView(view);


                AlertDialog dialog = mDialog.create();

                EditText mTask = view.findViewById(R.id.mEditTextTask);
                EditText mDescription = view.findViewById(R.id.mEditTextDescription);
                TextView mDueDate = view.findViewById(R.id.mEditTextDueDate);

                mTask.setText(task);
                mTask.setSelection(task.length());




                mDescription.setText(description);
                mDescription.setSelection(description.length());
                mDueDate.setText(dueDate);

                Calendar calendar =  Calendar.getInstance();
                final int year = calendar.get(Calendar.YEAR);
                final int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);

                mDueDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(TasksActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                month = month+1;
                                String dueDateStr = day + "/"+month+"/"+year;
                                mDueDate.setText(dueDateStr);
                            }
                        }, year, month, day);
                        datePickerDialog.show();
                    }
                });


                //mDueDate.setText(dueDate);
                //mDueDate.setSelection(dueDate.length());



                AppCompatButton delBtn = view.findViewById(R.id.deleteBtn);
                AppCompatButton updateBtn = view.findViewById(R.id.UpdateBtn);
                AppCompatButton doneBtn = view.findViewById(R.id.setDoneBtn);

                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        task = mTask.getText().toString().trim();
                        description = mDescription.getText().toString().trim();
                        dueDate = mDueDate.getText().toString().trim();

                        String date = DateFormat.getDateInstance().format(new Date());

                        Model model = new Model(task, description, key, date, done, dueDate);

                        reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(TasksActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                                }else {
                                    String error = task.getException().toString();
                                    Toast.makeText(TasksActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(TasksActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                                }else{
                                    String error = task.getException().toString();
                                    Toast.makeText(TasksActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialog.dismiss();
                    }
                });
                dialog.show();


                doneBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        task = mTask.getText().toString().trim();
                        description = mDescription.getText().toString().trim();
                        dueDate = mDueDate.getText().toString().trim();

                        String date = DateFormat.getDateInstance().format(new Date());

                        Model model = new Model(task, description, key, date, true, dueDate);

                        reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(TasksActivity.this, "Task marked as done", Toast.LENGTH_SHORT).show();
                                }else {
                                    String error = task.getException().toString();
                                    Toast.makeText(TasksActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });





            }

            private void updateTaskSetUndone() {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(TasksActivity.this);
                LayoutInflater inflater =LayoutInflater.from(TasksActivity.this);
                View view = inflater.inflate(R.layout.update_data_undone, null);
                mDialog.setView(view);


                AlertDialog dialog = mDialog.create();

                EditText mTask = view.findViewById(R.id.mEditTextTaskUndone);
                EditText mDescription = view.findViewById(R.id.mEditTextDescriptionUndone);
                TextView mDueDate = view.findViewById(R.id.mEditTextDueDateUndone);

                mTask.setText(task);
                mTask.setSelection(task.length());




                mDescription.setText(description);
                mDescription.setSelection(description.length());

                mDueDate.setText(dueDate);
                Calendar calendar =  Calendar.getInstance();
                final int year = calendar.get(Calendar.YEAR);
                final int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);

                mDueDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(TasksActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                month = month+1;
                                String dueDateStr = day + "/"+month+"/"+year;
                                mDueDate.setText(dueDateStr);
                            }
                        }, year, month, day);
                        datePickerDialog.show();
                    }
                });

                //mDueDate.setText(dueDate);
                //mDueDate.setSelection(dueDate.length());



                AppCompatButton delBtn = view.findViewById(R.id.deleteBtnUndone);
                AppCompatButton updateBtn = view.findViewById(R.id.UpdateBtnUndone);
                AppCompatButton unDoneBtn = view.findViewById(R.id.setUnDoneBtn);

                updateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        task = mTask.getText().toString().trim();
                        description = mDescription.getText().toString().trim();
                        dueDate = mDueDate.getText().toString().trim();

                        String date = DateFormat.getDateInstance().format(new Date());

                        DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID).child(key);

                        reff.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                boolean done = (boolean) snapshot.child("done").getValue();
                                Model model = new Model(task, description, key, date, done, dueDate);

                                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {

                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(TasksActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                                        }else {
                                            String error = task.getException().toString();
                                            Toast.makeText(TasksActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                reff.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

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
                                    Toast.makeText(TasksActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                                }else{
                                    String error = task.getException().toString();
                                    Toast.makeText(TasksActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialog.dismiss();
                    }
                });
                dialog.show();


                unDoneBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        task = mTask.getText().toString().trim();
                        description = mDescription.getText().toString().trim();
                        dueDate = mDueDate.getText().toString().trim();

                        String date = DateFormat.getDateInstance().format(new Date());

                        Model model = new Model(task, description, key, date, false, dueDate);

                        reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(TasksActivity.this, "Task marked as undone", Toast.LENGTH_SHORT).show();
                                }else {
                                    String error = task.getException().toString();
                                    Toast.makeText(TasksActivity.this, "Failed "+error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });



            }



            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrived_layout, parent, false);
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
        public void setTask (String task){
            TextView taskTV = mView.findViewById(R.id.taskTv);
            taskTV.setText(task);
        }

        public void setStatus (String status){
            TextView statusTV = mView.findViewById(R.id.statusTv);
            statusTV.setText(status);
        }

        public void setDate(String date){
            TextView dateTV = mView.findViewById(R.id.dateTv);
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