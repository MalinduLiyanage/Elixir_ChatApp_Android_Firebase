package com.malinduliyanage.elixir;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    private DatabaseReference database;
    private Handler handler;
    private Runnable statusUpdater;
    private EditText nameTxt, StatusTxt, areaTxt, creationTxt;
    private CircleImageView profilePic;
    private ImageView changeProfilePic;
    private Button submitBtn;
    private boolean isEditing = false;
    private final int IMAGE_PICK = 100;
    private Uri profileImage = null;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private FirebaseStorage storage;
    String newprofilePicpath = null, oldprofilePicpath = null, creationDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Account Settings");
        }

        nameTxt = findViewById(R.id.view_name_txt);
        StatusTxt = findViewById(R.id.view_status_txt);
        areaTxt = findViewById(R.id.view_area_txt);
        creationTxt = findViewById(R.id.view_creation_txt);
        profilePic = findViewById(R.id.view_profile_image);
        changeProfilePic = findViewById(R.id.camera_btn);
        submitBtn = findViewById(R.id.submit_btn);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        handler = new Handler();
        statusUpdater = new Runnable() {
            @Override
            public void run() {
                onlineStatus(currentUserId);
                handler.postDelayed(this, 5000);
            }
        };

        changeUI();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEditing){
                    isEditing = true;
                    changeUI();
                }else{
                    isEditing = false;
                    changeUI();
                    if(profileImage != null){
                        uploadImageToFirebase(profileImage);
                    }else{
                        updateUser();
                    }
                }
            }
        });

        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
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

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        loadAccountInfo(currentUserId);
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

    private void loadAccountInfo(String currentUser) {

        database.child("Users").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> userAttributes = new HashMap<>();

                    String area = dataSnapshot.child("area").getValue(String.class);
                    String created = dataSnapshot.child("creationDate").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String profilepic = dataSnapshot.child("profilepic").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    oldprofilePicpath = profilepic;
                    creationDate = created;

                    userAttributes.put("area", area);
                    userAttributes.put("name", name);
                    userAttributes.put("status", status);

                    nameTxt.setText(name);
                    StatusTxt.setText(status);
                    areaTxt.setText(area);
                    creationTxt.setText(creationDate);

                    if(!oldprofilePicpath.isEmpty()){
                        Glide.with(AccountSettingsActivity.this)
                                .load(profilepic)
                                .apply(new RequestOptions().placeholder(R.drawable.ic_user_profile)) // optional placeholder
                                .into(profilePic);
                    }else{
                        profilePic.setImageResource(R.drawable.ic_user_profile);
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

    private void changeUI(){

        if(!isEditing){
            nameTxt.setEnabled(false);
            StatusTxt.setEnabled(false);
            areaTxt.setEnabled(false);
            changeProfilePic.setEnabled(false);
            changeProfilePic.setVisibility(View.INVISIBLE);
            submitBtn.setText("Change Account Info");
        }else{
            nameTxt.setEnabled(true);
            StatusTxt.setEnabled(true);
            areaTxt.setEnabled(true);
            changeProfilePic.setEnabled(true);
            changeProfilePic.setVisibility(View.VISIBLE);
            submitBtn.setText("Save Changes");
        }
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK && data != null) {
            profileImage = data.getData();
            try {
                String path = profileImage.getPath();

                int rawIndex = path.indexOf("/raw/");
                if (rawIndex != -1) {
                    path = path.substring(0, rawIndex) + path.substring(rawIndex + 5);
                }

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), profileImage);
                profilePic.setImageBitmap(bitmap);


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        if (uri != null) {
            String userId = mAuth.getCurrentUser().getUid();
            StorageReference ref = storageReference.child("profileImages/" + userId);

            ProgressDialog pd = new ProgressDialog(AccountSettingsActivity.this);
            pd.setMessage("Uploading your Profile Picture...");
            pd.setCancelable(false);
            pd.show();

            ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newprofilePicpath = uri.toString();
                                    updateUser();
                                    pd.dismiss();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle unsuccessful uploads
                            pd.dismiss();
                            Toast.makeText(AccountSettingsActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            Log.e("InitialUserDetailsActivity", "Failed to upload image", e);
                        }
                    });
        }


    }

    public void updateUser() {

        String userId = mAuth.getCurrentUser().getUid();

        String nameString = nameTxt.getText().toString();
        String areaString = areaTxt.getText().toString();
        String statusString = StatusTxt.getText().toString();
        String profilePicString = "";

        if(newprofilePicpath != null){
            profilePicString = newprofilePicpath;
        }else{
            profilePicString = oldprofilePicpath;
        }

        if(!nameString.isEmpty() || !areaString.isEmpty() || !statusString.isEmpty()){

            ProgressDialog pd = new ProgressDialog(AccountSettingsActivity.this);
            pd.setMessage("Updating your Profile...");
            pd.setCancelable(false);
            pd.show();

            User user = new User(nameString, areaString, statusString, profilePicString, "null", creationDate);

            database.child("Users").child(userId).setValue(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                Intent intent = new Intent(AccountSettingsActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                pd.dismiss();
                                Toast.makeText(AccountSettingsActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
        }
    }
}