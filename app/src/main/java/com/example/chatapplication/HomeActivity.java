package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;

import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.UtilityApp;
import com.example.chatapplication.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import static com.example.chatapplication.classes.UtilityApp.logout;

public class HomeActivity extends AppCompatActivity {

    FloatingActionButton fab;
    FirebaseUser firebaseUser;
    UserModel user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fab = findViewById(R.id.fab);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user = UtilityApp.getUserData();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, UsersActivity.class));
            }
        });

        if (UtilityApp.isLogin()) {
            getMyProfile();
        }
    }

    private void getMyProfile() {
        FirebaseFirestore.getInstance().collection(Constants.USER)
                .document(firebaseUser.getUid()).get().addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);
                        UtilityApp.setUserData(userModel);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_message) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}