package com.malinduliyanage.elixir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Context context;
    private OnResumeCallback onResumeCallback;

    private static final int VIEW_TYPE_SENDER = 0;
    private static final int VIEW_TYPE_RECEIVER = 1;

    public interface OnResumeCallback {
        void triggerOnResume();
    }

    public MessageAdapter(List<Message> messageList, Context context, OnResumeCallback onResumeCallback) {
        this.messageList = messageList;
        this.context = context;
        this.onResumeCallback = onResumeCallback;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && message.getSenderId().equals(currentUser.getUid())) {
            return VIEW_TYPE_SENDER;
        } else {
            return VIEW_TYPE_RECEIVER;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENDER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_msg_layout_sender, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_msg_layout_receiver, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.msgTextView.setText(message.getMessage());
        holder.dateTextView.setText(message.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView msgTextView;
        TextView dateTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            msgTextView = itemView.findViewById(R.id.msgTxt);
            dateTextView = itemView.findViewById(R.id.dateTxt);
        }
    }
}
