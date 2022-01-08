package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Adapter.UserAdapter;
import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.DataCallBack;
import com.example.chatapplication.classes.UtilityApp;
import com.example.chatapplication.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView title;
    ProgressBar loadingLY;

    RecyclerView rv;
    UserAdapter adapter;
    List<UserModel> userModelist;
    UserModel user;

    FirebaseUser firebaseUser;
    FirebaseFirestore fireStoreDB;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        profileImage = findViewById(R.id.message_imag);
        title = findViewById(R.id.title);
        loadingLY = findViewById(R.id.loadingLY);
        rv = findViewById(R.id.rv);

        fireStoreDB = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        profileImage.setVisibility(View.VISIBLE);
        profileImage.setVisibility(View.VISIBLE);
        title.setText("New Message");

        user = UtilityApp.getUserData();
        Glide.with(this)
                .asBitmap()
                .load(user.imageURL)
                .placeholder(R.drawable.profile)
                .into(profileImage);

        rv.setLayoutManager(new LinearLayoutManager(UsersActivity.this));

        userModelist = new ArrayList<>();
        adapter = new UserAdapter(UsersActivity.this, userModelist);
        rv.setAdapter(adapter);

        readUsers(true);
        //fetchData(true);

    }

    private void readUsers(boolean showLoading) {

        if (showLoading) {
            loadingLY.setVisibility(View.VISIBLE);
        }

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        fireStoreDB.collection(Constants.USER).whereNotEqualTo("user_id", firebaseUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                loadingLY.setVisibility(View.GONE);
                if (task.isSuccessful()) {

                    userModelist.clear();

                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);
                        userModelist.add(user);

                    }

                    adapter.list = userModelist;
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(UsersActivity.this, getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                }
            }

        });


    }
}