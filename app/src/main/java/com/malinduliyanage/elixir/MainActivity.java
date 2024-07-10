package com.malinduliyanage.elixir;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FrameLayout fragLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
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

    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(MainActivity.this, "Welcome " + user.getUid(), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(MainActivity.this, "No User here", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }
    }




}