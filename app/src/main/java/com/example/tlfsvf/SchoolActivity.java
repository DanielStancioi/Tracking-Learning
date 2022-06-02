package com.example.tlfsvf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class SchoolActivity extends AppCompatActivity{
    private Toolbar toolbar;
    private Button taskBtn, logoutBtn, courseBtn, progressBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        courseBtn = findViewById(R.id.schoolDisciplinesButton);
        //logoutBtn = findViewById(R.id.homeLogoutButton);
        progressBtn = findViewById(R.id.schoolProgressButton);

        toolbar = findViewById(R.id.schoolToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("School");


        courseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SchoolActivity.this, DisciplinesActivity.class);
                startActivity(intent);
            }
        });

        progressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SchoolActivity.this, DisciplinesActivity.class);
                startActivity(intent);
            }
        });
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