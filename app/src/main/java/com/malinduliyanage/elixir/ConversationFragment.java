package com.malinduliyanage.elixir;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationFragment extends Fragment {

    private String receiverId, receiverName, receiverImage, currentUserId, conversationId;
    private FirebaseAuth mAuth;
    private CircleImageView receiverImageview;
    private TextView receiverNameTxt;
    private DatabaseReference database;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            receiverId = getArguments().getString("receiverId");
            receiverName = getArguments().getString("receiverName");
            receiverImage = getArguments().getString("receiverPic");
            conversationId = getArguments().getString("conversationId");
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        currentUserId = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        }

        Toast.makeText(getContext(), "Receiver ID: " + receiverId + " Current User ID: " + currentUserId + " Conversation ID: " + conversationId, Toast.LENGTH_LONG).show();
        receiverImageview = view.findViewById(R.id.receiver_image);
        receiverNameTxt = view.findViewById(R.id.receiver_name);

        receiverNameTxt.setText(receiverName);
        if(!receiverImage.isEmpty()){
            Glide.with(getContext())
                    .load(receiverImage)
                    .apply(new RequestOptions().placeholder(R.drawable.ic_user_profile)) // optional placeholder
                    .into(receiverImageview);
        }else{
            receiverImageview.setImageResource(R.drawable.ic_user_profile);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.conversation_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_conversation) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}