package com.example.tlfsvf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText registerEmail, registerPasswd, registerPasswdConfirm;
    private Button registerButton;
    private FirebaseAuth mAuth;

    private ProgressDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        registerEmail = findViewById(R.id.registerEmail);
        registerPasswd = findViewById(R.id.registerPasswd);
        registerButton = findViewById(R.id.registerButton);
        registerPasswdConfirm = findViewById(R.id.registerPasswdconfirm);
        toolbar = findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            finish();
        }
        loader = new ProgressDialog(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s_email = registerEmail.getText().toString();
                String s_password = registerPasswd.getText().toString();
                String s_passwordConf = registerPasswdConfirm.getText().toString();

                if(TextUtils.isEmpty(s_email)){
                    registerEmail.setError("Email is required");
                    return;
                }
                if(TextUtils.isEmpty(s_password)){
                    registerPasswd.setError(" Password is required");
                    return;
                }
                if(s_password.compareTo(s_passwordConf) != 0){
                    registerPasswdConfirm.setError("Not the same as initial password");
                    return;
                }else{
                    loader.setMessage("Registration in progress");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    mAuth.createUserWithEmailAndPassword(s_email, s_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                            loader.dismiss();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Registration failed" + error, Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }
                    }
                });}

            }
        });
    }
}