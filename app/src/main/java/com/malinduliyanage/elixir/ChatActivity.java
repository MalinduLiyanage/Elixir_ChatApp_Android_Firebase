package com.malinduliyanage.elixir;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatActivity extends AppCompatActivity {

    FrameLayout fragLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String receiverId = getIntent().getStringExtra("receiverId");
        String receiverName = getIntent().getStringExtra("receiverName");
        String receiverPic = getIntent().getStringExtra("receiverPic");
        String conversationId = getIntent().getStringExtra("conversationId");

        fragLayout = findViewById(R.id.conversation_fragment_container);

        Bundle bundle = new Bundle();
        bundle.putString("receiverId", receiverId);
        bundle.putString("receiverName", receiverName);
        bundle.putString("receiverPic", receiverPic);
        bundle.putString("conversationId", conversationId);


        ConversationFragment conversationFragment = new ConversationFragment();
        conversationFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.conversation_fragment_container, conversationFragment)
                .commit();
    }
}