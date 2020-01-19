package com.project.teamtrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ProfileActivity";

    private Button buttonLogout;
    private Button buttonMap;

    List<String> userlist;
    ArrayAdapter<String> adapter;
    ListView UserList;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UserList = (ListView)findViewById(R.id.UserList);
        userlist = new ArrayList<>();

        buttonMap = (Button) findViewById(R.id.buttonMap);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(this);
        buttonMap.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("User");
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@Nullable FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user !=null) {

                } else {

                }
                //...
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@Nullable DataSnapshot dataSnapshot)
            {
                userlist.clear();
                String name = dataSnapshot.child(userID).child("name").getValue().toString();
                String email = dataSnapshot.child(userID).child("email").getValue().toString();

                userlist.add("Name: "+name);
                userlist.add("Email: "+email);

                adapter = new ArrayAdapter<>(Profile.this, android.R.layout.simple_list_item_1,userlist);
                UserList.setAdapter(adapter);



            }

            @Override
            public void onCancelled(@Nullable DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser()== null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);

        }
    }
    @Override
    public void onClick(View view){
        if (view == buttonMap){
            finish();
            startActivity(new Intent(this, MapsActivity.class));

        }

        if (view == buttonLogout){
            mAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));

        }


    }


}
