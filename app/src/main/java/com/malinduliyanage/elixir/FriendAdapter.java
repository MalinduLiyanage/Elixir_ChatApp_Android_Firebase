package com.malinduliyanage.elixir;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder>{

    private List<User> friendList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String state = null, currentUserId, conversationId = null;
    private Handler handler;
    private Runnable statusChecker;

    public FriendAdapter(List<User> friendList) {
        this.friendList = friendList;
        this.state = state;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        currentUserId = firebaseUser.getUid();
    }

    @NonNull
    @Override
    public FriendAdapter.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_friend, parent, false);
        return new FriendAdapter.FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.FriendViewHolder holder, int position) {
        User user = friendList.get(position);
        holder.nameTextView.setText(user.getName());
        holder.activeIndicator.setVisibility(View.INVISIBLE);

        // Initialize the Handler and Runnable
        handler = new Handler();
        statusChecker = new Runnable() {
            @Override
            public void run() {
                onlineStatus(user.getUserId(), holder.activeIndicator);
                handler.postDelayed(this, 5000); // Re-run this runnable in 5 seconds
            }
        };

        // Start the Runnable
        handler.post(statusChecker);

        if(!user.getprofilepic().isEmpty()){
            Picasso.get().load(user.getprofilepic()).into(holder.profileImageView);
        }

        holder.userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                conversationId = checkConversationExists(currentUserId, user.getUserId());
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("receiverId", user.getUserId());
                intent.putExtra("receiverName", user.getName());
                intent.putExtra("receiverPic", user.getprofilepic());
                v.getContext().startActivity(intent);

            }
        });


    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        ImageView profileImageView, activeIndicator;
        Button userBtn;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.friend_nameTxt);
            profileImageView = itemView.findViewById(R.id.profile_img);
            userBtn = itemView.findViewById(R.id.chat_btn);
            activeIndicator = itemView.findViewById(R.id.active_indicator);
        }
    }


    private String checkConversationExists(String senderId, String receiverId) {

        mDatabase.child("Conversations").child(senderId).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Conversation exists
                    conversationId = dataSnapshot.child("Conversation_Id").getValue(String.class);
                } else {
                    mDatabase.child("Conversations").child(receiverId).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Conversation exists
                                conversationId = dataSnapshot.child("Conversation_Id").getValue(String.class);
                            } else {
                                conversationId = createSingleConversation(senderId, receiverId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return conversationId;
    }

    private String createSingleConversation(String senderId, String receiverId) {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        Map<String, Object> conversationMap = new HashMap<>();
        conversationMap.put("Conversation_Id", sb.toString());
        conversationMap.put("Last_Message", "EmptyElixirConversation");

        if (senderId != null && receiverId != null) {

            mDatabase.child("Conversations").child(senderId).child(receiverId).setValue(conversationMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mDatabase.child("Conversations").child(receiverId).child(senderId).setValue(conversationMap);

                            }
                        }
                    });
            return sb.toString();
        }

        return null;
    }

    private void onlineStatus(String receivingUser, ImageView receiverLastseen) {
        mDatabase.child("Users").child(receivingUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String lastSeen = dataSnapshot.child("active").getValue(String.class);
                    if (lastSeen != null) {
                        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        showActiveStatus(receiverLastseen, lastSeen, currentTime);
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

    private void showActiveStatus(ImageView receiverLastseen, String lastSeen, String currentTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date lastSeenDate = sdf.parse(lastSeen);
            Date currentDate = sdf.parse(currentTime);

            long diffInMillis = currentDate.getTime() - lastSeenDate.getTime();
            long diffInSeconds = diffInMillis / 1000;

            if (diffInSeconds <= 10) {
                receiverLastseen.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
