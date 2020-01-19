package com.project.teamtrack;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, View.OnClickListener {

    private GoogleMap mMap;
    private Button buttonmyprofile,buttonCreateTeam,buttonJoinTeam;
    private String userID;
    private DatabaseReference mRef,mRef2;
    private FirebaseAuth mAuth;

    private Marker mTeamMarker;
    private Marker mTeamMarker2;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mRef = FirebaseDatabase.getInstance().getReference("Teams");
        mRef2 = FirebaseDatabase.getInstance().getReference("User");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buttonmyprofile = (Button) findViewById(R.id.buttonmyprofile);
        buttonJoinTeam = (Button) findViewById(R.id.buttonJoinTeam);
        buttonCreateTeam = (Button) findViewById(R.id.buttonCreateTeam);
        buttonCreateTeam.setOnClickListener(this);
        buttonmyprofile.setOnClickListener(this);
        buttonJoinTeam.setOnClickListener(this);



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(mMap.MAP_TYPE_SATELLITE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(final Location location) {
        mLastLocation = location;

        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userLocation");
        final GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));

        TeamLocation();

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(200);
        mLocationRequest.setFastestInterval(200);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }




    public void TeamLocation() {

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                            mRef2.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot ds2 : dataSnapshot.getChildren()) {
                                                        //gets all the userID's of users in the team
                                                        final String TeamMemberIDs = ds2.getKey();

                                                        //counts amount of users in team excluding the user
                                                        final int TeamAmount = (int) ds2.getChildrenCount();

                                                        //checks if other users are in the team
                                                        if (dataSnapshotTest.child(String.valueOf(id)).child("TeamMembers").child(String.valueOf(TeamMemberIDs)).exists()) {
                                                            if (!Objects.equals(userID, TeamMemberIDs)) {

                                                                //gets team members usernames who arent the user
                                                                final String TeamMemberName = dataSnapshotTest.child(String.valueOf(id)).child("TeamMembers").child(TeamMemberIDs).child("teamMemberName").getValue().toString();
                                                                DatabaseReference TeamMemRef = FirebaseDatabase.getInstance().getReference("userLocation").child(TeamMemberIDs).child("l");

                                                                TeamMemRef.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        //checks if the user location exists. if so gets both Lat and Long which then is used to display a marker for each team member
                                                                        if (dataSnapshot.exists()){
                                                                            List<Object> map = (List<Object>) dataSnapshot.getValue();

                                                                            double TeamLat= 0;
                                                                            double TeamLong = 0;
                                                                            if (map.get(0) != null){
                                                                                TeamLat = Double.parseDouble(map.get(0).toString());
                                                                            }
                                                                            if (map.get(1) != null){
                                                                                TeamLong = Double.parseDouble(map.get(1).toString());
                                                                            }

                                                                            int Limit = TeamAmount;
                                                                            LatLng TeamLoc1 = new LatLng(TeamLat,TeamLong);
                                                                            LatLng TeamLoc2 = new LatLng(TeamLat,TeamLong);

                                                                             Map<String, Marker> mMarkerMap = new HashMap<>();
                                                                             Marker previousMarker = mMarkerMap.get(TeamMemberIDs);

                                                                                if (previousMarker != null) {
                                                                                    //previous marker exists, update position:
                                                                                    previousMarker.setPosition(TeamLoc1);
                                                                                } else {
                                                                                    //No previous marker, create a new one:
                                                                                    MarkerOptions markerOptions = new MarkerOptions()
                                                                                            .position(TeamLoc1)
                                                                                            .title(TeamMemberIDs)
                                                                                            .snippet(TeamMemberName);

                                                                                    Marker marker = mMap.addMarker(markerOptions);

                                                                                    //put this new marker in the HashMap:
                                                                                    mMarkerMap.put(TeamMemberIDs, marker);
                                                                                }


                                                                         //   if (mTeamMarker != null )
                                                                          //  {
                                                                           //     mMap.clear();
                                                                           // }

                                                                          //  mTeamMarker = mMap.addMarker(new MarkerOptions()
                                                                          //          .position(TeamLoc1)
                                                                          //          .title("Team Member")
                                                                          //          .snippet(TeamMemberName));

                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }
                                                        }
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


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onClick(View view) {
        if (view == buttonmyprofile) {
            finish();
            startActivity(new Intent(this, Profile.class));

        }

        if (view == buttonCreateTeam) {
            finish();
            startActivity(new Intent(MapsActivity.this, CreateTeam.class));
        }

        if (view == buttonJoinTeam) {
            finish();
            startActivity(new Intent(MapsActivity.this, JoinTeam.class));
        }

    }


    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
            }
        }
    }

  
    @Override
    protected void onStop() {
        super.onStop();

        //removes user location when not active
       // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this); String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
       // DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userLocation");
        //GeoFire geoFire = new GeoFire(ref);
       // geoFire.removeLocation(userId);

    }


}
