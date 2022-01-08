package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.GlobalHelper;
import com.example.chatapplication.classes.UtilityApp;
import com.example.chatapplication.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import de.hdodenhof.circleimageview.CircleImageView;

public class profileActivity extends AppCompatActivity {

    ProgressBar loadingLY;
    TextView logout, title;
    CircleImageView profileImag, messageImag;
    EditText userName;
    Button update;

    UserModel user;
    Uri userPhotoUri;
    String name;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    FirebaseFirestore fireStoreDB;
    FirebaseAuth mFirebaseAuth;
    StorageReference storageRef;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fireStoreDB = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        loadingLY = findViewById(R.id.loadingLY);
        logout = findViewById(R.id.logoutTv);
        profileImag = findViewById(R.id.profile_imag);
        userName = findViewById(R.id.name);
        update = findViewById(R.id.changebtn);
        messageImag = findViewById(R.id.message_imag);
        title = findViewById(R.id.title);

        update.setVisibility(View.VISIBLE);
        messageImag.setVisibility(View.GONE);
        title.setText("My Profile");
        update.setText("Update");

        if (user != null)
            initData();
        else {
            getMyProfile();
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
                startActivity(new Intent(profileActivity.this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        });

        profileImag.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permission, PERMISSION_CODE);
                } else {
                    pickImageFromGallery();
                }
            } else {
                pickImageFromGallery();
            }
        });

        update.setOnClickListener(v -> {
            checkData();
        });

    }

    private void initData() {

        userName.setText(user.username);
        Glide.with(this)
                .asBitmap()
                .load(user.imageURL)
                .placeholder(R.drawable.profile)
                .into(profileImag);
    }

    private void getMyProfile() {

        user = UtilityApp.getUserData();
        if (user == null) {
            GlobalHelper.showProgressDialog(this, getString(R.string.please_wait_loading));

            FirebaseFirestore.getInstance().collection(Constants.USER).document(firebaseUser.getUid()).get().addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            GlobalHelper.hideProgressDialog();
                            if (task.isSuccessful()) {
                                user = task.getResult().toObject(UserModel.class);
                                initData();
                            } else {
                                Toast.makeText(profileActivity.this, getString(R.string.fail_get_data), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            initData();
        }
    }

    private void checkData() {

        name = userName.getText().toString();

        loadingLY.setVisibility(View.VISIBLE);
        boolean hasError = false;

        if (name.isEmpty()) {
            userName.setError(this.getString(R.string.invalid_input));
            hasError = true;
        }
        if (userPhotoUri == null) {
            hasError = true;
        }
        if (hasError)
            return;

        user.username = name;
        addUserToFirebase(user.imageURL);
    }

    private void uploadPhoto(Uri photoUri) {

        StorageReference imgRef = storageRef.child(Constants.IMAGES + "/"
                + UUID.randomUUID().toString());

        UploadTask uploadTask = imgRef.putFile(photoUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("aa", exception + "");
                Toast.makeText(profileActivity.this, "failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(taskSnapshot -> {

            imgRef.getDownloadUrl().addOnCompleteListener(task -> {
                user.imageURL = String.valueOf(task.getResult());
                Log.e("s",userPhotoUri.toString());
            });
        });
    }

    private void pickImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void addUserToFirebase(String photoUrl) {

        Map<String, Object> userModelMap = new HashMap<>();
        userModelMap.put("username", user.username);
        userModelMap.put("imageURL", photoUrl);

        fireStoreDB.collection(Constants.USER).document(firebaseUser.getUid()).update(userModelMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadingLY.setVisibility(View.VISIBLE);

                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.success_add), Toast.LENGTH_SHORT).show();
                            UtilityApp.setUserData(user);
                            startActivity(new Intent(profileActivity.this, HomeActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.fail), Toast.LENGTH_SHORT).show();
                        }
                        loadingLY.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {

           userPhotoUri = data.getData();

            Glide.with(this)
                    .asBitmap()
                    .load(userPhotoUri)
                    .placeholder(R.drawable.profile)
                    .into(profileImag);
            uploadPhoto(userPhotoUri);
        }
    }

    private void logout(){
        mFirebaseAuth.signOut();
    }
}