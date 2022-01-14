package com.example.classicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    TextView txt_signup;
    EditText login_email, login_password;
    TextView signIn_btn;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; // Email 을 입력할 때의 범위 (email validation in android regex)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        txt_signup = findViewById(R.id.txt_signup);
        signIn_btn = findViewById(R.id.signIn_btn);
        login_email= findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);

        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email =  login_email.getText().toString();
                String password = login_password.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))// email 혹은 Password 를 입력하지 않았을 경우
                {
                    Toast.makeText(LoginActivity.this, "값을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else if(!email.matches(emailPattern)) // Email 이 양식에 알맞지 않은 경우
                {
                    login_email.setError("Invalid Email");
                    Toast.makeText(LoginActivity.this, "적절한 이메일 양식을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else if(password.length() < 6) // Password 가 6자리 이하인 경우
                {
                    login_password.setError("Invalid Password");
                    Toast.makeText(LoginActivity.this, "6자리 이상의 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) // Login 에 성공하였을 경우
                            {
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            }
                            else // Login 에 실패하였을 경우
                            {
                                Toast.makeText(LoginActivity.this, "로그인이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    }
}