package com.malinduliyanage.elixir;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    LinearLayout profileLayout, accSettingsBtn, appSettingsBtn, logoutBtn;
    TextView userName, userStatus, userArea;
    CircleImageView userImage;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        // Optionally, set a title for the toolbar
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My Profile");
        }

        profileLayout = view.findViewById(R.id.profile_layout);
        accSettingsBtn = view.findViewById(R.id.accsettings_layout);
        appSettingsBtn = view.findViewById(R.id.appsetting_layout);
        logoutBtn = view.findViewById(R.id.logout_layout);
        userName = view.findViewById(R.id.user_nameTxt);
        userStatus = view.findViewById(R.id.user_statusTxt);
        userArea = view.findViewById(R.id.user_areaTxt);
        userImage = view.findViewById(R.id.profile_img);
        progressBar = view.findViewById(R.id.loadingPanel_profile);

        profileLayout.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        accSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                startActivity(intent);
            }
        });

        appSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "App Settings", Toast.LENGTH_SHORT).show();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                //builder.setIcon(R.drawable.ic_welcome);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout from Elixir?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    logOut();
                });
                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                    dialog.cancel();
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {

        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String area = snapshot.child("area").getValue(String.class);
                    String image = snapshot.child("profilepic").getValue(String.class);

                    userName.setText(name);
                    userStatus.setText(status);
                    userArea.setText(area);

                    if(!image.isEmpty()){
                        Glide.with(getContext())
                                .load(image)
                                .apply(new RequestOptions().placeholder(R.drawable.ic_user_profile)) // optional placeholder
                                .into(userImage);
                    }else{
                        userImage.setImageResource(R.drawable.ic_user_profile);
                    }

                    progressBar.setVisibility(View.GONE);
                    profileLayout.setVisibility(View.VISIBLE);

                }else{
                    profileLayout.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void logOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}