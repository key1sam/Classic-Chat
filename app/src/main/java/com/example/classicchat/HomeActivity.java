package com.example.classicchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView mainUserRecyclerView;
    UserAdapter adapter;
    FirebaseDatabase database;
    ArrayList<Users> usersArrayList;
    ImageView imgLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); // firebase의 데이터베이스를 가져온다.

        usersArrayList = new ArrayList<>();

        DatabaseReference reference = database.getReference().child("user"); // 여기 디버깅하는데 최소 40시간은 걸린 듯 하다...
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    usersArrayList.add(users);
                }
                    adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        imgLogout = findViewById(R.id.img_logOut);
        mainUserRecyclerView=findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(HomeActivity.this, usersArrayList);
        mainUserRecyclerView.setAdapter(adapter);

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(HomeActivity.this, R.style.Dialog);
                dialog.setContentView(R.layout.dialog_layout);
                TextView yesBtn, noBtn; // 로그아웃할 때 나올 대화상자 버튼 아이디

                yesBtn = dialog.findViewById(R.id.yesBtn);
                noBtn = dialog.findViewById(R.id.noBtn);

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(HomeActivity.this, RegistrationActivity.class));
                    }
                });

                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss(); // 대화상자 종료
                    }
                });

                dialog.show();
            }
        });

        if(auth.getCurrentUser() == null)
        {
            startActivity(new Intent (HomeActivity.this,RegistrationActivity.class)); // 정보를 넘겨줌
        }

    }
}