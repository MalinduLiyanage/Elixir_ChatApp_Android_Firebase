package com.malinduliyanage.elixir;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatsFragment extends Fragment implements UserAdapter.OnResumeCallback{

    private FirebaseAuth mAuth;
    private RecyclerView chatlistContainer;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference userReference, chatlistReference;
    private ProgressBar progressChatlist;
    private RelativeLayout emptyChatsTxt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_chats, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        // Optionally, set a title for the toolbar
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Elixir");
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();

        progressChatlist = view.findViewById(R.id.loadingPanel_chatlist);
        emptyChatsTxt = view.findViewById(R.id.empty_chats);

        chatlistContainer = view.findViewById(R.id.chatlist_container);
        chatlistContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        chatlistContainer.setVisibility(View.GONE);
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList,"ChatList",getContext(),this);
        chatlistContainer.setAdapter(userAdapter);
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatlistReference = FirebaseDatabase.getInstance().getReference("Conversations").child(currentUserId);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chats_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Toast.makeText(getActivity(), "Not Implemented yet", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatlist();
    }

    @Override
    public void triggerOnResume() {
        loadChatlist();
    }

    private void loadChatlist() {

        List<String> chatListIds = new ArrayList<>();

        chatlistReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatListIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    HashMap chat = (HashMap) snapshot.getValue();
                    String lastMessage = String.valueOf(chat);

                    if (userId != null && !lastMessage.contains("EmptyElixirConversation")) {
                        chatListIds.add(userId);
                    }
                }

                if (chatListIds.isEmpty()) {
                    emptyChatsTxt.setVisibility(View.VISIBLE);
                }else{
                    emptyChatsTxt.setVisibility(View.GONE);
                }
                loadUsers(chatListIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

    }

    private void loadUsers(List<String> userIds) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && userIds.contains(snapshot.getKey())) {
                        user.setUserId(snapshot.getKey());
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
                progressChatlist.setVisibility(View.GONE);
                chatlistContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}