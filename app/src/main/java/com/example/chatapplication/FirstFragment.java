package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapter.ChatsAdapter;
import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.UtilityApp;
import com.example.chatapplication.databinding.ActivityChatsBinding;
import com.example.chatapplication.databinding.FragmentFirstBinding;
import com.example.chatapplication.models.ChatModel;
import com.example.chatapplication.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    ChatsAdapter adapter;
    List<ChatModel> chatslist;

    FirebaseUser firebaseUser;
    FirebaseFirestore fireStoreDB;

    boolean getMyChats;
    boolean getOtherChats;
    UserModel user;

    FragmentFirstBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        binding.include.backbtn.setVisibility(View.GONE);
        binding.include.title.setVisibility(View.VISIBLE);
        binding.include.messageImag.setVisibility(View.VISIBLE);

        if (UtilityApp.isLogin()) {
            user = UtilityApp.getUserData();
            if (user != null ) {
                binding.loginLY.setVisibility(View.GONE);
            } else {
                binding.loginLY.setVisibility(View.VISIBLE);
                binding.loadingLY.setVisibility(View.VISIBLE);
            }
        } else {
            binding.loginLY.setVisibility(View.VISIBLE);
        }

        Glide.with(this)
                .asBitmap()
                .load(user.imageURL)
                .placeholder(R.drawable.profile)
                .into(binding.include.messageImag);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        fireStoreDB = FirebaseFirestore.getInstance();

        binding.include.title.setText("Chats");
        binding.include.messageImag.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), profileActivity.class));
        });

        binding.rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        chatslist = new ArrayList<>();
        adapter = new ChatsAdapter(getActivity(), chatslist, firebaseUser.getUid());
        binding.rv.setAdapter(adapter);

        binding.swipeToRefreshLY.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                readChats(false);
            }
        });

        readChats(true);
        super.onViewCreated(view, savedInstanceState);

    }

    private void readChats(boolean showLoading) {

        if (showLoading) {
            binding.loadingLY.setVisibility(View.VISIBLE);
            binding.swipeToRefreshLY.setVisibility(View.GONE);
        }
        chatslist.clear();
        getMyChats = false;
        getOtherChats = false;

        readMyChats();
        readOtherChats();
    }

    private void readMyChats() {

        fireStoreDB.collection(Constants.CHAT).whereEqualTo("sender_id", firebaseUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                binding.swipeToRefreshLY.setRefreshing(false);
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        ChatModel chat = document.toObject(ChatModel.class);
                        chat.id = document.getId();
                        chatslist.add(chat);
                    }
                    getMyChats = true;
                    if (getOtherChats) {
                        binding.loadingLY.setVisibility(View.GONE);
                        binding.swipeToRefreshLY.setVisibility(View.VISIBLE);
                        initAdapter();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                    binding.loadingLY.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void readOtherChats() {

        fireStoreDB.collection(Constants.CHAT).whereEqualTo("friend_id", firebaseUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        ChatModel chat = document.toObject(ChatModel.class);
                        chat.id = document.getId();
                        chatslist.add(chat);
                    }
                    getOtherChats = true;

                    if (getMyChats) {
                        binding.loadingLY.setVisibility(View.GONE);
                        initAdapter();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                    binding.loadingLY.setVisibility(View.VISIBLE);
                }
            }

        });


    }

    private void initAdapter() {

        adapter.list = chatslist;
        adapter.notifyDataSetChanged();


    }
}