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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class CreateTeam extends AppCompatActivity implements View.OnClickListener {
    private Button buttonRegister;
    private Button buttonMap;
    private Button buttonLeave;
    private TextView textCode;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef, mRef2;
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
        buttonLeave = (Button) findViewById(R.id.buttonLeave);
        buttonMap.setOnClickListener(this);
        buttonRegister.setOnClickListener(this);
        buttonLeave.setOnClickListener(this);

        mRef = FirebaseDatabase.getInstance().getReference("Teams");
        mRef2 = FirebaseDatabase.getInstance().getReference("User");
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        teamJoin = new TeamJoin();


        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                        final String id = ds.getKey();

                        //check if Team Member ID in DB is equal to current user ID
                        final Query query = mRef.orderByChild("teamMemberID").equalTo(userID);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //checks if the teamID is equal
                                Query query1 = mRef.orderByChild("teamID").equalTo(id);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot dataSnapshotTest) {

                                        if (dataSnapshotTest.child(String.valueOf(id)).child("TeamMembers").child(String.valueOf(userID)).exists()) {
                                            mRef2.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot ds2 : dataSnapshot.getChildren()) {
                                                        //gets all the userID's of users in the team
                                                        final String TeamMemberIDs = ds2.getKey();

                                                        //if user is in team set register team button as disabled and display to team join code
                                                        buttonRegister.setEnabled(false);
                                                        String code = ds.child("teamCode").getValue().toString();
                                                        textCode.setText(code);

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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

    public void leave() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                        final String id = ds.getKey();

                        //check if Team Member ID in DB is equal to current user ID
                        final Query query = mRef.orderByChild("teamMemberID").equalTo(userID);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final Task<Void> leave = mRef.child(id).child("TeamMembers").child(userID).removeValue();
                                buttonRegister.setEnabled(true);
                                textCode.setText("XXXXXX");

                                leave.addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (leave.isSuccessful()) {
                                            Toast.makeText(CreateTeam.this, "You have left a team!", Toast.LENGTH_LONG).show();

                                        }
                                    }
                                });

                                final Query query = mRef.child(id).child("ownerID").equalTo(userID);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        mRef.child(id).removeValue();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == buttonRegister) {
            TeamCode();
            buttonRegister.setEnabled(false);
        }

        if (view == buttonMap) {
            finish();
            startActivity(new Intent(this, MapsActivity.class));
        }

        if (view == buttonLeave) {
            leave();
        }
    }

}
