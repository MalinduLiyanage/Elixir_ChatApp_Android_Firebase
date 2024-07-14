package com.malinduliyanage.elixir;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthResult;
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

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FrameLayout fragLayout;
    private DatabaseReference mDatabase;
    private Handler handler;
    private Runnable statusUpdater;
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    String[] REQUIRED_PERMISSIONS_ANDROID_12 = new String[]{
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CAMERA
    };

    String[] REQUIRED_PERMISSIONS_ANDROID_13 = new String[]{
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        updateUI(currentUser);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragLayout = findViewById(R.id.fragment_container);


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Menu menu = bottomNavigationView.getMenu();

                Fragment selectedFragment = null;

                menu.findItem(R.id.navigation_chats).setIcon(R.drawable.ic_chats);
                menu.findItem(R.id.navigation_friends).setIcon(R.drawable.ic_friends);
                menu.findItem(R.id.navigation_profile).setIcon(R.drawable.ic_profile);

                if (itemId == R.id.navigation_chats) {
                    item.setIcon(R.drawable.ic_chats_filled);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ChatsFragment())
                            .commit();
                    return true;

                } else if (itemId == R.id.navigation_friends) {
                    item.setIcon(R.drawable.ic_friends_filled);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FriendsFragment())
                            .commit();
                    return true;

                } else if (itemId == R.id.navigation_profile) {
                    item.setIcon(R.drawable.ic_profile_filled);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                    return true;

                }

                if (selectedFragment != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                    fragmentTransaction.commit();
                }

                return false;
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatsFragment())
                    .commit();
        }

        if(currentUser != null){
            String uid = currentUser.getUid();
            handler = new Handler();
            statusUpdater = new Runnable() {
                @Override
                public void run() {
                    onlineStatus(uid);
                    handler.postDelayed(this, 5000);
                }
            };

            if(!allPermissionsGranted()){
                requestPermissionsIfNeeded();
            }

            forceUpdateAccount(uid);
        }

    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            //Toast.makeText(MainActivity.this, "Welcome " + user.getUid(), Toast.LENGTH_SHORT).show();

        } else {
            //Toast.makeText(MainActivity.this, "No User here", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void onlineStatus(String currentUser) {
        String onlineTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        mDatabase.child("Users").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    mDatabase.child("Users").child(currentUser).updateChildren(userAttributes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.d("UserAttributes", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void forceUpdateAccount(String currentUser) {

        mDatabase.child("Users").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> userAttributes = new HashMap<>();

                    String area = dataSnapshot.child("area").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    if(area.contains("null") || name.contains("Elixir User") || status.contains("null")){
                        Toast.makeText(MainActivity.this, "Please update your account", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, AccountSettingsActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.d("UserAttributes", "Database error: " + databaseError.getMessage());
            }
        });
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

    private boolean allPermissionsGranted() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // API level 33
            for (String permission : REQUIRED_PERMISSIONS_ANDROID_13) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }else{
            for (String permission : REQUIRED_PERMISSIONS_ANDROID_12) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestPermissionsIfNeeded() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // API level 33
            for (String permission : REQUIRED_PERMISSIONS_ANDROID_13) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }else{
            for (String permission : REQUIRED_PERMISSIONS_ANDROID_12) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(permission);
                }
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Permissions not granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }




}