package com.example.chatapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapter.MessageAdapter;
import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.GlobalHelper;
import com.example.chatapplication.classes.UtilityApp;
import com.example.chatapplication.databinding.ActivityMessageBinding;
import com.example.chatapplication.models.ChatModel;
import com.example.chatapplication.models.FmessageModel;
import com.example.chatapplication.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    MessageAdapter adapter;
    UserModel myUserModel;
    UserModel friendUserModel;

    String chatId, userName, userAvatar;

    CollectionReference msgRef;
    DocumentReference chatDoc;
    FirebaseUser firebaseUser;
    FirebaseFirestore fireStoreDB;

    ActivityMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.include.title.setVisibility(View.VISIBLE);
        binding.include.messageImag.setVisibility(View.VISIBLE);
        binding.include.backbtn.setVisibility(View.VISIBLE);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chatId = bundle.getString(Constants.KEY_CHAT_ID);

        }
        friendUserModel = (UserModel) getIntent().getSerializableExtra(Constants.KEY_USER_MODEL);
        myUserModel = UtilityApp.getUserData();

        fireStoreDB = FirebaseFirestore.getInstance();

        if (chatId == null) {
            chatId = GlobalHelper.getChatId(firebaseUser.getUid(), friendUserModel.user_id);
        }

        System.out.println("Log chatId " + chatId);
        chatDoc = fireStoreDB.collection(Constants.CHAT).document(chatId);
        msgRef = chatDoc.collection(Constants.MESSAGES);

        chatDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    error.printStackTrace();
                    return;
                }

                if (value != null && value.contains("sender_id")) {
                    // when chat users is set before
                    ChatModel chatModel = value.toObject(ChatModel.class);
                    String senderId = chatModel.sender_id;
                    String friendName, friendAvatar = "";
                    if (firebaseUser.getUid().equals(senderId)) {
                        friendName = chatModel.friend_name;
                        friendAvatar = chatModel.friend_avatar;
                    } else {
                        friendName = chatModel.sender_name;
                        friendAvatar = chatModel.sender_avatar;
                    }
                    setFriendData(friendName, friendAvatar);
                } else {
                    // when chat users not set
                    setChatUsers();
                }

            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        binding.rv.setLayoutManager(llm);

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });

        initAdapter();

    }

    private void setFriendData(String name, String avatarUrl) {
        binding.include.title.setText(name);

        Glide.with(this).asBitmap().load(avatarUrl).placeholder(R.drawable.profile).into(binding.include.messageImag);

    }

    private void sendMessage() {

        final String msg = GlobalHelper.arabicToDecimal(binding.textSend.getText().toString().trim());

        if (msg.isEmpty()) {
            binding.textSend.setError(getString(R.string.message_empty));
            return;
        }

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("user_id", firebaseUser.getUid());
        if (myUserModel != null)
            messageMap.put("user_avatar", myUserModel.imageURL);
        messageMap.put("date", FieldValue.serverTimestamp());
        messageMap.put("my_date", new Date());
        messageMap.put("type", "text");
        messageMap.put("content", msg);

        binding.textSend.setText("");
        sendFirebaseMessage(messageMap);

    }

    private void setChatUsers() {
        Map<String, Object> chatMap = new HashMap<>();

        if (myUserModel != null) {
            chatMap.put("sender_id", firebaseUser.getUid());
            chatMap.put("sender_name", myUserModel.username);
            chatMap.put("sender_avatar", myUserModel.imageURL);
        }
        if (friendUserModel != null) {
            chatMap.put("friend_id", friendUserModel.user_id);
            chatMap.put("friend_name", friendUserModel.username);
            chatMap.put("friend_avatar", friendUserModel.imageURL);
        }
        chatMap.put("created_at", FieldValue.serverTimestamp());

        chatDoc.set(chatMap, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Log success send message");

                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });

    }

    private void sendFirebaseMessage(Map<String, Object> messageMap) {

        String msg = (String) messageMap.get("msg");
        String type = (String) messageMap.get("type");

        msgRef.add(messageMap)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Log success send message");

                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    if (type.equals("text"))
                        binding.textSend.setText(msg);
                });

    }

    private void initAdapter() {

        List<FmessageModel> fmessageModelList = new ArrayList<>();
        adapter = new MessageAdapter(this, binding.rv, firebaseUser.getUid(), fmessageModelList, msgRef);
        binding.rv.setAdapter(adapter);

    }

}