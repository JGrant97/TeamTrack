package com.project.teamtrack;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;

import java.util.Random;

public class CreateTeam extends AppCompatActivity implements View.OnClickListener {
    private Button buttonRegister;
    private Button buttonMap;
    private TextView textCode;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String userID;
    TeamJoin teamJoin;
    private int n;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createteam);

        textCode = (TextView) findViewById(R.id.textCode);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        buttonMap = (Button) findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(this);
        buttonRegister.setOnClickListener(this);

        mRef = FirebaseDatabase.getInstance().getReference("Teams");
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        teamJoin = new TeamJoin();

//        //checks if user is in a team and if so stops them from registering a new one
//        Query query2 = mRef.orderByChild("TeamMembers").equalTo(userID);
//        query2.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                buttonRegister.setEnabled(false);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


    }

    public void TeamCode() {

        //Code length
        int n = 6;

        //Generate team join code
        String AlphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvwxyz";
        StringBuilder stringbuilder = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            int index = (int) (AlphaNumeric.length() * Math.random());

            stringbuilder.append(AlphaNumeric.charAt(index));
        }

        String finalCode = stringbuilder.toString();
        textCode.setText(finalCode);

        String TeamCode = textCode.getText().toString();
        String OwnerID = userID;
        if (!TextUtils.isEmpty(TeamCode)) {

            String id = mRef.push().getKey();

            Team team = new Team(id, TeamCode, OwnerID);

            mRef.child(id).setValue(team);
            textCode.setText(TeamCode);
            Toast.makeText(CreateTeam.this, "Team has been registered", Toast.LENGTH_LONG).show();


            final String name = mAuth.getCurrentUser().getDisplayName();

            //checks if the team code entered matches any in the database
            Query query = mRef.orderByChild("teamCode").equalTo(TeamCode);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            teamJoin.setTeamMemberID(userID);
                            teamJoin.setTeamMemberName(name);

                            //adds user name and ID to team
                            Task<Void> Members = ds.getRef().child("TeamMembers").child(String.valueOf(userID)).setValue(teamJoin);

                            Members.addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(CreateTeam.this, "You have joined a team!", Toast.LENGTH_LONG).show();
                                        ;
                                    }
                                }
                            });
                        }
                    else {
                        //tells user that there is no team with this code
                        Toast.makeText(CreateTeam.this, "No Team with this code!", Toast.LENGTH_LONG).show();
                        ;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public void onClick(View view) {
        if (view == buttonRegister) {
            TeamCode();
        }

        if (view == buttonMap) {
            finish();
            startActivity(new Intent(this, MapsActivity.class));
        }
    }

}
