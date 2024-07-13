package com.malinduliyanage.elixir;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    FrameLayout fragLayout;
    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private String currentUserId, returnedconversationId;
    private EditText msgTxt;
    private Button sendBtn;
    private Handler handler;
    private Runnable statusUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        currentUserId = firebaseUser.getUid();

        String receiverId = getIntent().getStringExtra("receiverId");
        String receiverName = getIntent().getStringExtra("receiverName");
        String receiverPic = getIntent().getStringExtra("receiverPic");

        fragLayout = findViewById(R.id.conversation_fragment_container);
        msgTxt = findViewById(R.id.message_input);
        sendBtn = findViewById(R.id.send_button);
        sendBtn.setEnabled(false);

        Bundle bundle = new Bundle();
        bundle.putString("receiverId", receiverId);
        bundle.putString("receiverName", receiverName);
        bundle.putString("receiverPic", receiverPic);

        handler = new Handler();
        statusUpdater = new Runnable() {
            @Override
            public void run() {
                onlineStatus(currentUserId);
                handler.postDelayed(this, 5000);
            }
        };


        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                obtainConversationId(currentUserId, receiverId, new ConversationFragment.OnConversationIdRetrievedListener() {
                    @Override
                    public void onConversationIdRetrieved(String conversationId) {
                        if (conversationId != null) {
                            ConversationFragment conversationFragment = new ConversationFragment();
                            conversationFragment.setArguments(bundle);

                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.conversation_fragment_container, conversationFragment)
                                    .commit();

                            sendBtn.setEnabled(true);
                            returnedconversationId = conversationId;
                            //Toast.makeText(ChatActivity.this, "Receiver ID: " + receiverId + " Current User ID: " + currentUserId + " Conversation ID: " + returnedconversationId, Toast.LENGTH_LONG).show();
                        } else {
                            sendBtn.setEnabled(false);
                            returnedconversationId = null;
                            Toast.makeText(ChatActivity.this, "Conversation ID not found", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            }
        }, 1000);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(msgTxt.getText().toString().trim().isEmpty())){

                    String messageSt = msgTxt.getText().toString();
                    String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    HashMap<String, Object> message = new HashMap<>();

                    message.put("message", messageSt);
                    message.put("senderId", currentUserId);
                    message.put("timestamp", creationDate);

                    database.child("MessageThread").child(returnedconversationId).child(creationDate).setValue(message);
                    msgTxt.setText("");

                    Map<String, Object> conversationMap = new HashMap<>();
                    conversationMap.put("Conversation_Id", returnedconversationId);
                    conversationMap.put("Last_Message", messageSt);

                    database.child("Conversations").child(currentUserId).child(receiverId).updateChildren(conversationMap);
                    database.child("Conversations").child(receiverId).child(currentUserId).updateChildren(conversationMap);

                }else{
                    Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void obtainConversationId(String senderId, String receiverId, ConversationFragment.OnConversationIdRetrievedListener listener) {
        database.child("Conversations").child(senderId).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Conversation exists
                    String conversationId = dataSnapshot.child("Conversation_Id").getValue(String.class);
                    listener.onConversationIdRetrieved(conversationId);
                } else {
                    database.child("Conversations").child(receiverId).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Conversation exists
                                String conversationId = dataSnapshot.child("Conversation_Id").getValue(String.class);
                                listener.onConversationIdRetrieved(conversationId);
                            } else {
                                listener.onConversationIdRetrieved(null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            listener.onConversationIdRetrieved(null);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onConversationIdRetrieved(null);
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
}