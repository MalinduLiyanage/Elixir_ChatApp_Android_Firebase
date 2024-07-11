package com.malinduliyanage.elixir;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder>{

    private List<User> friendList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String state = null;

    public FriendAdapter(List<User> friendList) {
        this.friendList = friendList;
        this.state = state;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
}
