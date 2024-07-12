package com.malinduliyanage.elixir;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder>{

    private List<User> friendList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String state = null, currentUserId;

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

        if(!user.getprofilepic().isEmpty()){
            Picasso.get().load(user.getprofilepic()).into(holder.profileImageView);
        }

        holder.userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String conversationId = createConversation(currentUserId, user.getUserId());
                if (conversationId != null) {
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    intent.putExtra("receiverId", user.getUserId());
                    intent.putExtra("receiverName", user.getName());
                    intent.putExtra("receiverPic", user.getprofilepic());
                    intent.putExtra("conversationId", conversationId);
                    v.getContext().startActivity(intent);
                }else{
                    Toast.makeText(v.getContext(), "Failed to create conversation", Toast.LENGTH_SHORT).show();
                }



            }
        });


    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        ImageView profileImageView;
        Button userBtn;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.friend_nameTxt);
            profileImageView = itemView.findViewById(R.id.profile_img);
            userBtn = itemView.findViewById(R.id.chat_btn);
        }
    }

    private String createConversation(String senderId, String receiverId) {
        String conversationId = mDatabase.child("conversations").push().getKey();
        String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (conversationId != null) {
            // Create conversation data
            Map<String, Object> conversationData = new HashMap<>();
            conversationData.put("participants" , "");
            conversationData.put("lastMessage", "Conversation started");
            conversationData.put("timestamp", creationDate);

            // Set conversation data in the database
            mDatabase.child("Conversations").child(conversationId).setValue(conversationData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Send the first message
                            //sendMessage(conversationId, senderId, "Conversation started");

                            Map<String, Object> participantData = new HashMap<>();
                            participantData.put("user1" , senderId);
                            participantData.put("user2" , receiverId);

                            mDatabase.child("Conversations").child(conversationId).child("participants").setValue(participantData);

                        } else {
                            //Toast.makeText(ChatActivity.this, "Failed to create conversation", Toast.LENGTH_SHORT).show();
                        }
                    });
            return conversationId;
        }
        return null;
    }
}
