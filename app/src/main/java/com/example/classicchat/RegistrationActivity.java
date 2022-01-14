package com.example.classicchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {

    TextView txt_signin, btn_SignUp;
    CircleImageView profile_image;
    EditText reg_name, reg_email, reg_password, reg_cPassword;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Uri imageUri;
    FirebaseDatabase database; // Tools 에서 연동해주었던 realtime Database
    FirebaseStorage storage; // Tools 에서 연동해주었던 cloud storage
    String  imageURI;
    ProgressDialog progressDialog; // 잠시 기다려달라는 메세지 출력

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("잠시만 기다려주세요...");
        progressDialog.setCancelable(false); // 다른 화면을 터치해도 프로그래스바가 꺼지지 않는다.

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        txt_signin = findViewById(R.id.txt_signin);
        profile_image = findViewById(R.id.profile_image);
        reg_name = findViewById(R.id.reg_name);
        reg_email = findViewById(R.id.reg_email);
        reg_password = findViewById(R.id.reg_password);
        reg_cPassword = findViewById(R.id.reg_cPassword);
        btn_SignUp = findViewById(R.id.btn_Signup);



        btn_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show(); // 프로그래스바 출력
                String name = reg_name.getText().toString();
                String email = reg_email.getText().toString();
                String password = reg_password.getText().toString();
                String cPassword = reg_cPassword.getText().toString();
                String staus = "노는게 제일 좋아~";

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(cPassword)) //한개의 항목이라도 비었을 경우
                {
                    progressDialog.dismiss(); // 프로그래스바 사라지게 하기
                    Toast.makeText(RegistrationActivity.this, "모든 항목에 기입하세요.", Toast.LENGTH_SHORT).show();
                }else if (!email.matches(emailPattern)) // 위에서 지정해준 emailPattern 범위 내의 String 이 아닐 경우
                {
                    reg_email.setError("올바른 이메일 양식을 입력하세요"); // 에러발생 시
                    Toast.makeText(RegistrationActivity.this, "올바른 이메일 양식을 입력하세요", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }else if (!password.equals(cPassword)) // 패스워드가 일치하지 않을 경우
                {
                    Toast.makeText(RegistrationActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else if (password.length() < 6)
                {
                    Toast.makeText(RegistrationActivity.this, "6자 이상의 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                DatabaseReference reference = database.getReference().child("user").child(auth.getUid()); // 디버깅 80시간이 걸린 원인. 이 뜻은 user child에 child를 만들고 거기에는 Uid를 입력한다.
                                StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());
                                //Toast.makeText(RegistrationActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                                if (imageUri != null) {
                                    storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageURI = uri.toString();
                                                        Users users = new Users(auth.getUid(), name, email, imageURI, staus);
                                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    progressDialog.dismiss();
                                                                    startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                                                                } else {
                                                                    Toast.makeText(RegistrationActivity.this, "유저 생성에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    //Image가 선택되지 않았을 시 firebase Storage에 올려놓은 기본 이미지를 연결해서 사용.
                                    String staus = "크롱 크롱 크로롱~";
                                    imageURI = "https://firebasestorage.googleapis.com/v0/b/classic-chat-3b3c6.appspot.com/o/profile_image.png?alt=media&token=6c04267e-c1f4-47d4-889f-49383ec73665";
                                    Users users = new Users(auth.getUid(), name, email, imageURI, staus);
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                                            } else {
                                                Toast.makeText(RegistrationActivity.this, "유저 생성에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }else {
                                progressDialog.dismiss();
                                Toast.makeText(RegistrationActivity.this, "중복 아이디가 존재합니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() { // 갤러리에서 사진을 선택해서 프로필에 등록하고자 할때
            @Override // https://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "그림을 선택하세요."), 10);
            }
        }); // requestCode 란 어느 Activity 에 방문 후 돌아왔는지 구별하기 위해 사용하는 코드이다.

        txt_signin.setOnClickListener(new View.OnClickListener() {
            @Override   
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class)); // Sign in 이 클릭될경우 LoginActivity로 이동한다.
            }
        });
    }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==10)
        {
            if (data!=null)
            {
                imageUri = data.getData();
                profile_image.setImageURI(imageUri);
            }
        }

     }
}