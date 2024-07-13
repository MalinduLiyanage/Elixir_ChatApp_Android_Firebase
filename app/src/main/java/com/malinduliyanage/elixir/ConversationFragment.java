package com.malinduliyanage.elixir;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationFragment extends Fragment implements MessageAdapter.OnResumeCallback {

    private String receiverId, receiverName, receiverImage, currentUserId, conversationId, lastSeen;
    private FirebaseAuth mAuth;
    private CircleImageView receiverImageview;
    private TextView receiverNameTxt, receiverLastseen;
    private DatabaseReference database, msgDatabase;
    private RecyclerView msgContainer;
    private MessageAdapter msgAdapter;
    private List<Message> msgList;
    private ProgressBar progressView;
    private Handler handler;
    private Runnable statusChecker;


    public interface OnConversationIdRetrievedListener {
        void onConversationIdRetrieved(String conversationId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            receiverId = getArguments().getString("receiverId");
            receiverName = getArguments().getString("receiverName");
            receiverImage = getArguments().getString("receiverPic");
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        currentUserId = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        }

        receiverImageview = view.findViewById(R.id.receiver_image);
        receiverNameTxt = view.findViewById(R.id.receiver_name);
        receiverLastseen = view.findViewById(R.id.receiver_activetime);

        receiverNameTxt.setText(receiverName);
        if (!receiverImage.isEmpty()) {
            Glide.with(getContext())
                    .load(receiverImage)
                    .apply(new RequestOptions().placeholder(R.drawable.ic_user_profile)) // optional placeholder
                    .into(receiverImageview);
        } else {
            receiverImageview.setImageResource(R.drawable.ic_user_profile);
        }

        progressView = view.findViewById(R.id.loadingPanel);

        msgContainer = view.findViewById(R.id.conversation_container);
        msgContainer.setLayoutManager(new LinearLayoutManager(getContext()));

        msgContainer.setVisibility(View.GONE);

        msgList = new ArrayList<>();
        msgAdapter = new MessageAdapter(msgList, getContext(), this);
        msgContainer.setAdapter(msgAdapter);

        obtainConversationId(currentUserId, receiverId, new OnConversationIdRetrievedListener() {
            @Override
            public void onConversationIdRetrieved(String conversationId) {
                if (conversationId != null) {
                    msgDatabase = database.child("MessageThread").child(conversationId);
                    loadMsgthread();
                } else {
                    // Handle conversation ID not found
                }
            }
        });

        // Initialize the Handler and Runnable
        handler = new Handler();
        statusChecker = new Runnable() {
            @Override
            public void run() {
                onlineStatus(receiverId, receiverLastseen);
                handler.postDelayed(this, 5000); // Re-run this runnable in 5 seconds
            }
        };

        // Start the Runnable
        handler.post(statusChecker);

        return view;
    }



    private void loadMsgthread() {
        if (msgDatabase != null) {
            msgDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    msgList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            msgList.add(message);
                        }
                    }
                    msgAdapter.notifyDataSetChanged();
                    progressView.setVisibility(View.GONE);
                    msgContainer.setVisibility(View.VISIBLE);

                    // Scroll to the last message
                    if (msgAdapter.getItemCount() > 0) {
                        msgContainer.scrollToPosition(msgAdapter.getItemCount() - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
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

    private void obtainConversationId(String senderId, String receiverId, OnConversationIdRetrievedListener listener) {
        database.child("Conversations").child(senderId).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String conversationId = dataSnapshot.child("Conversation_Id").getValue(String.class);
                    listener.onConversationIdRetrieved(conversationId);
                } else {
                    database.child("Conversations").child(receiverId).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
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

    @Override
    public void onResume() {
        super.onResume();
        // Do not call loadMsgthread here as it will be called after obtaining conversation ID
    }

    @Override
    public void triggerOnResume() {
        loadMsgthread();
    }

    private void onlineStatus(String currentUser, TextView receiverLastseen) {
        database.child("Users").child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String lastSeen = dataSnapshot.child("active").getValue(String.class);
                    if (lastSeen != null) {
                        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        showActiveStatus(receiverLastseen, lastSeen, currentTime);
                    } else {
                        receiverLastseen.setText("Last seen unavailable");
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

    private void showActiveStatus(TextView receiverLastseen, String lastSeen, String currentTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date lastSeenDate = sdf.parse(lastSeen);
            Date currentDate = sdf.parse(currentTime);

            long diffInMillis = currentDate.getTime() - lastSeenDate.getTime();
            long diffInSeconds = diffInMillis / 1000;
            long diffInMinutes = diffInSeconds / 60;
            long diffInHours = diffInMinutes / 60;
            long diffInDays = diffInHours / 24;

            if (diffInSeconds <= 10) {
                receiverLastseen.setText("Active Now");
            } else if (diffInMinutes < 60) {
                receiverLastseen.setText("Active " + diffInMinutes + " mins ago");
            } else if (diffInHours < 24) {
                receiverLastseen.setText("Active " + diffInHours + " hours ago");
            } else if (diffInDays == 1) {
                receiverLastseen.setText("Yesterday");
            } else if (diffInDays <= 7) {
                receiverLastseen.setText("Active " + diffInDays + " days ago");
            } else {
                receiverLastseen.setText("Active on " + lastSeen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the status checker when the view is destroyed
        handler.removeCallbacks(statusChecker);
    }


}
