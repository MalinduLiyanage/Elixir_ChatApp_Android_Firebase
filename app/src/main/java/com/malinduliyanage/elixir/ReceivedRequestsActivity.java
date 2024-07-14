package com.malinduliyanage.elixir;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ReceivedRequestsActivity extends AppCompatActivity implements UserAdapter.OnResumeCallback{

    private RecyclerView receivedContainer;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference receiveReference, userReference, database;
    private ProgressBar progressView;
    private Handler handler;
    private Runnable statusUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_requests);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Optionally, set a title for the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Received Requests");
        }

        progressView = findViewById(R.id.loadingPanel);
        database = FirebaseDatabase.getInstance().getReference();

        receivedContainer = findViewById(R.id.received_container);
        receivedContainer.setLayoutManager(new LinearLayoutManager(this));

        receivedContainer.setVisibility(View.GONE);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList,"Received",this,this);
        receivedContainer.setAdapter(userAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();

        receiveReference = FirebaseDatabase.getInstance().getReference("ReceivedRequests").child(currentUserId);
        userReference = FirebaseDatabase.getInstance().getReference("Users");


        loadReceivedRequests();

        handler = new Handler();
        statusUpdater = new Runnable() {
            @Override
            public void run() {
                onlineStatus(currentUserId);
                handler.postDelayed(this, 5000);
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReceivedRequests();
    }
    @Override
    protected void onStart() {
        super.onStart();
        handler.post(statusUpdater); // Start the status updater
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(statusUpdater); // Stop the status updater
    }

    @Override
    public void triggerOnResume() {

    }

    private void loadReceivedRequests() {

        List<String> receivedRequestIds = new ArrayList<>();

        receiveReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                receivedRequestIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    if (userId != null) {
                        receivedRequestIds.add(userId);
                    }
                }

                loadUsers(receivedRequestIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void loadUsers(List<String> userIds) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && userIds.contains(snapshot.getKey())) {
                        user.setUserId(snapshot.getKey());
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
                progressView.setVisibility(View.GONE);
                receivedContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onlineStatus(String currentUser) {
        String onlineTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        database.child("Users").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> userAttributes = new HashMap<>();

                    String area = dataSnapshot.child("area").getValue(String.class);
                    String compressedProfilePic = dataSnapshot.child("compressedProfilePic").getValue(String.class);
                    String creationDate = dataSnapshot.child("creationDate").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String profilepic = dataSnapshot.child("profilepic").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    userAttributes.put("area", area);
                    userAttributes.put("compressedProfilePic", compressedProfilePic);
                    userAttributes.put("creationDate", creationDate);
                    userAttributes.put("name", name);
                    userAttributes.put("profilepic", profilepic);
                    userAttributes.put("status", status);
                    userAttributes.put("active", onlineTime);

                    database.child("Users").child(currentUser).updateChildren(userAttributes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.d("UserAttributes", "Database error: " + databaseError.getMessage());
            }
        });
    }
}