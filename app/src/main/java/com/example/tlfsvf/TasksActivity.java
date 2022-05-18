package com.example.tlfsvf;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TasksActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private FloatingActionButton floatingActionButtonProgress;

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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
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
        View myView = inflater.inflate(R.layout.tasks_progress, null);
        myDialog.setView(myView);

        AlertDialog dialog = myDialog.create();

        dialog.show();

        final TextView done = myView.findViewById(R.id.FinishedTasks);
        final TextView undone = myView.findViewById(R.id.StillInProgressTasks);
        final TextView late = myView.findViewById(R.id.lateTasks);
        List<String> doneTasks = new ArrayList<>();
        List<String> undoneTasks = new ArrayList<>();
        List<String> lateTasks = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String getCurrentDateTime = sdf.format(c.getTime());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ss: snapshot.getChildren()){
                    boolean done = (boolean) ss.child("done").getValue();
                    String date = ss.child("dueDate").getValue().toString();

                    if (done) {
                        doneTasks.add(ss.child("task").getValue().toString());
                    } else {
                        if (getCurrentDateTime.compareTo(date) < 0)
                        {
                            undoneTasks.add(ss.child("task").getValue().toString());
                        }
                        else
                        {
                            lateTasks.add(ss.child("task").getValue().toString());
                        }

                    }
                }
                done.setText(""+doneTasks.size());
                undone.setText(""+undoneTasks.size());
                late.setText(""+lateTasks.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        AppCompatButton cancel = myView.findViewById(R.id.cancelButtonTaskProgress);
        cancel.setOnClickListener((v)->{dialog.dismiss();});
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
        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(reference, Model.class).build();
        FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Model model) {
                holder.setDate(model.getDueDate());
                holder.setTask(model.getTask());

                key = getRef(position).getKey();

                DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID).child(key);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String getCurrentDateTime = sdf.format(c.getTime());

                final ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String text;
                        boolean done = (boolean) snapshot.child("done").getValue();
                        if (done) {
                            text = "Your task is DONE ";
                        } else {
                            if (getCurrentDateTime.compareTo(model.getDueDate()) < 0)
                            {
                                text = "Your task is STILL IN PROGRESS ";
                            }
                            else
                            {
                                text = "Your task is LATE";
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


                mDueDate.setText(dueDate);
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