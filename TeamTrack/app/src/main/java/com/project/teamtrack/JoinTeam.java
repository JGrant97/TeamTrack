package com.project.teamtrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class JoinTeam extends AppCompatActivity implements View.OnClickListener {

    private EditText editTeamCode;
    private Button buttonJoinTeam;
    private Button buttonMap;
    private Button buttonLeaveTeam;

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private DatabaseReference mRef2;
    private String userID;
    TeamJoin teamJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_team);

        mRef = FirebaseDatabase.getInstance().getReference("Teams");
        mRef = FirebaseDatabase.getInstance().getReference("User");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        editTeamCode  = (EditText) findViewById(R.id.editTeamCode);
        buttonJoinTeam = findViewById(R.id.buttonJoinTeam);
        buttonMap = findViewById(R.id.buttonMap);
        buttonLeaveTeam = findViewById(R.id.buttonLeaveTeam);

        buttonLeaveTeam.setOnClickListener(this);
        buttonJoinTeam.setOnClickListener(this);
        buttonMap.setOnClickListener(this);

        teamJoin = new TeamJoin();
        userID = user.getUid();

    }


    public void JoinTeam() {
        final String TeamCode = editTeamCode.getText().toString().trim();

        final String name = mAuth.getCurrentUser().getDisplayName();

        //checks if the team code entered matches any in the database
        Query query = mRef.orderByChild("teamCode").equalTo(TeamCode);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    for (DataSnapshot ds: dataSnapshot.getChildren())
                    {
                        teamJoin.setTeamMemberID(userID);
                        teamJoin.setTeamMemberName(name);

                        //adds user name and ID to team
                        Task<Void> Members = ds.getRef().child("TeamMembers").child(String.valueOf(userID)).setValue(teamJoin);

                        Members.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(JoinTeam.this, "You have joined a team!", Toast.LENGTH_LONG).show();;
                                }
                            }
                        });
                    }
                else
                {
                    //tells user that there is no team with this code
                    Toast.makeText(JoinTeam.this, "No Team with this code!", Toast.LENGTH_LONG).show();;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Checks if the input field is empty, if so makes a toast
        if (TextUtils.isEmpty(TeamCode)) {
            //email is empty
            Toast.makeText(this, "Please enter a Team Code", Toast.LENGTH_SHORT).show();
            //stopping function from executing further
            return;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == buttonMap)
        {
            finish();
            startActivity(new Intent(this, MapsActivity.class));
        }

        if (view == buttonLeaveTeam)
        {
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                            final String id = ds.getKey();

                            //check is Team Member ID in DB is equal to current user ID
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
                                               dataSnapshotTest.child(id).child("TeamMembers").child(userID).getRef().removeValue();

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
        if (view == buttonJoinTeam)
        {
            JoinTeam();
        }
    }
}
