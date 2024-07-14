package com.malinduliyanage.elixir;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment implements UserAdapter.OnResumeCallback{

    private RecyclerView suggestionsContainer, friendsContainer;
    private UserAdapter userAdapter;
    private FriendAdapter friendAdapter;
    private List<User> userList;
    private List<User> friendList;
    private DatabaseReference userReference, friendReference, sentReference, receiveReference;
    private ProgressBar progressUsers, progressFriends;
    private RelativeLayout emptyFriendTxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_friends, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        // Optionally, set a title for the toolbar
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Friends Zone");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();

        emptyFriendTxt = view.findViewById(R.id.empty_friends);
        progressUsers = view.findViewById(R.id.loadingPanel);
        progressFriends = view.findViewById(R.id.loadingPanel_friends);

        suggestionsContainer = view.findViewById(R.id.suggestions_container);
        suggestionsContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestionsContainer.setVisibility(View.GONE);
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList,"Suggestions",getContext(),this);
        suggestionsContainer.setAdapter(userAdapter);
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        friendsContainer = view.findViewById(R.id.friends_container);
        friendsContainer.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        friendList = new ArrayList<>();
        friendAdapter = new FriendAdapter(friendList);
        friendsContainer.setAdapter(friendAdapter);

        friendReference = FirebaseDatabase.getInstance().getReference("Friends").child(currentUserId);
        sentReference = FirebaseDatabase.getInstance().getReference("SentRequests").child(currentUserId);
        receiveReference = FirebaseDatabase.getInstance().getReference("ReceivedRequests").child(currentUserId);

        loadUsers();
        loadFriends();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
        loadFriends();
    }

    @Override
    public void triggerOnResume() {
        loadUsers();
        loadFriends();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.friends_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sentrequests) {
            Intent intent = new Intent(getContext(), SentRequestsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_receivedrequests) {
            Intent intent = new Intent(getContext(), ReceivedRequestsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUsers() {

        List<String> friendIds = new ArrayList<>();
        List<String> sentRequestIds = new ArrayList<>();
        List<String> receivedRequestIds = new ArrayList<>();

        friendReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    if (userId != null) {
                        friendIds.add(userId);



                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        sentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sentRequestIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    if (userId != null) {
                        sentRequestIds.add(userId);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        receiveReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                receivedRequestIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    if (userId != null) {
                        receivedRequestIds.add(userId);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String currentUserId = currentUser.getUid();
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null && !snapshot.getKey().equals(currentUserId) && !friendIds.contains(snapshot.getKey())
                                    && !sentRequestIds.contains(snapshot.getKey()) && !receivedRequestIds.contains(snapshot.getKey())) {
                                user.setUserId(snapshot.getKey());
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                        progressUsers.setVisibility(View.GONE);
                        suggestionsContainer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors.
                    }
                });
            }
        }, 2000);


    }

    private void loadFriends() {

        List<String> friendIds = new ArrayList<>();

        friendReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    if (userId != null) {
                        friendIds.add(userId);
                    }
                }

                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friendList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null && friendIds.contains(snapshot.getKey())) {
                                user.setUserId(snapshot.getKey());
                                friendList.add(user);
                            }
                        }
                        friendAdapter.notifyDataSetChanged();
                        progressFriends.setVisibility(View.GONE);
                        friendsContainer.setVisibility(View.VISIBLE);

                        // Check if the friend list is empty and show a toast
                        if (friendList.isEmpty()) {
                            emptyFriendTxt.setVisibility(View.VISIBLE);
                        }else{
                            emptyFriendTxt.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
}