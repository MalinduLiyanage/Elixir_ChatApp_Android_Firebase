package com.malinduliyanage.elixir;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String state = null;
    private Context context;
    private OnResumeCallback onResumeCallback;

    public interface OnResumeCallback {
        void triggerOnResume();
    }

    public UserAdapter(List<User> userList, String state, Context context, OnResumeCallback onResumeCallback) {
        this.userList = userList;
        this.state = state;
        this.context = context;
        this.onResumeCallback = onResumeCallback;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_friend_suggestions, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nameTextView.setText(user.getName());
        holder.statusTextView.setText(user.getStatus());
        holder.areaTextView.setText("From : " + user.getArea());

        if(!user.getprofilepic().isEmpty()){
            Glide.with(context)
                    .load(user.getprofilepic())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_user_profile)) // optional placeholder
                    .into(holder.profileImageView);
        }else{
            holder.profileImageView.setImageResource(R.drawable.ic_user_profile);
        }

        if(state.equals("Suggestions")){
            holder.userBtn.setText("Add Friend");

            holder.userBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Request Sent", Toast.LENGTH_SHORT).show();
                    sendRequest(user.getUserId());

                    if (onResumeCallback != null) {
                        onResumeCallback.triggerOnResume();
                    }

                }
            });

        }else if(state.equals("Sent")){
            holder.userBtn.setText("Cancel");

            holder.userBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    //builder.setIcon(R.drawable.ic_welcome);
                    builder.setTitle("Cancel Request to " + user.getName() + "?");
                    builder.setMessage("They will see you have cancelled the request.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                        Toast.makeText(v.getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                        cancelRequest(user.getUserId());

                    });
                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                        dialog.cancel();
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            });

        }else if(state.equals("Received")){
            holder.userBtn.setText("Accept");

            holder.userBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Now You are friends!", Toast.LENGTH_SHORT).show();
                    addFriend(user.getUserId());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView statusTextView;
        TextView areaTextView;
        ImageView profileImageView;
        Button userBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.friend_nameTxt);
            statusTextView = itemView.findViewById(R.id.friend_statusTxt);
            areaTextView = itemView.findViewById(R.id.friend_areaTxt);
            profileImageView = itemView.findViewById(R.id.profile_img);
            userBtn = itemView.findViewById(R.id.suggest_btn);
        }
    }

    private void sendRequest(String friendId) {

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            mDatabase.child("SentRequests").child(userId).child(friendId).child("Status").setValue("Sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mDatabase.child("ReceivedRequests").child(friendId).child(userId).child("Status").setValue("Received")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                } else {

                                                }
                                            }
                                        });
                            } else {

                            }
                        }
                    });
        }
    }

    private void cancelRequest(String friendId) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // Remove the sent request from the current user's sent requests
            mDatabase.child("SentRequests").child(userId).child(friendId).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Remove the received request from the friend's received requests
                                mDatabase.child("ReceivedRequests").child(friendId).child(userId).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Request successfully cancelled

                                                } else {
                                                    // Handle the failure to remove the received request

                                                }
                                            }
                                        });
                            } else {
                                // Handle the failure to remove the sent request

                            }
                        }
                    });
        }
    }

    private void addFriend(String friendId) {

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            mDatabase.child("Friends").child(userId).child(friendId).child("Status").setValue("Mutual")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mDatabase.child("Friends").child(friendId).child(userId).child("Status").setValue("Mutual")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    mDatabase.child("SentRequests").child(friendId).child(userId).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        // Remove the received request from the friend's received requests
                                                                        mDatabase.child("ReceivedRequests").child(userId).child(friendId).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            // Request successfully cancelled

                                                                                        } else {
                                                                                            // Handle the failure to remove the received request

                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        // Handle the failure to remove the sent request

                                                                    }
                                                                }
                                                            });

                                                } else {

                                                }
                                            }
                                        });
                            } else {

                            }
                        }
                    });
        }
    }

}

