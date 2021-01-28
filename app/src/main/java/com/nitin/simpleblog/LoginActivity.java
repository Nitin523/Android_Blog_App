package com.nitin.simpleblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;
    private FirebaseAuth mAuth;

    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        loginEmailText=(EditText)findViewById(R.id.reg_email);
        loginPassText=(EditText)findViewById(R.id.reg_confirm_pass);
        loginBtn=(Button)findViewById(R.id.reg_btn);
        loginRegBtn=(Button)findViewById(R.id.reg_login_btn);
        loginProgress=(ProgressBar)findViewById(R.id.reg_progress);

        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regIntent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String loginEmail=loginEmailText.getText().toString();
                String loginPass=loginPassText.getText().toString();

                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)){
                    loginProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail,loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                sendToMain();

                            }else {
                                String errorMessage=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error in Login Please Enter correct Details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            sendToMain();

    }
}
    public void sendToMain(){
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}