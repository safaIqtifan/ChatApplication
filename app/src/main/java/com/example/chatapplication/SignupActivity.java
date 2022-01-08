package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.classes.Constants;
import com.example.chatapplication.classes.GlobalHelper;
import com.example.chatapplication.classes.UtilityApp;
import com.example.chatapplication.databinding.ActivitySignupBinding;
import com.example.chatapplication.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    FirebaseFirestore fireStoreDB;
    private FirebaseAuth fAuth;
    StorageReference storageRef;

    ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fireStoreDB = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fname = binding.nameET.getText().toString();
                String email = binding.emailET.getText().toString();
                String mpassword = binding.passwordET.getText().toString();
                firebaseAuth(fname, email, mpassword);
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    private boolean checkData(String fname, String email, String mpassword) {

        boolean hasError = false;
        if (!GlobalHelper.validateFieldValue(fname)) {
            binding.nameET.setError("name is Requird");
            hasError = true;
        }
        if (!GlobalHelper.validateFieldValue(email)) {
            binding.emailET.setError("Email is Requird");
            hasError = true;
        }

        if (!GlobalHelper.validateFieldValue(mpassword)) {
            binding.passwordET.setError("password is Requird");
            hasError = true;
        }

        if (!GlobalHelper.validatePassword(mpassword, 6)) {
            binding.passwordET.setError("password Must be 6 or more characters");
            hasError = true;
        }
        return hasError;
    }

    private void firebaseAuth(String fname, String email, String mpassword) {

        boolean isInValid = checkData(fname, email, mpassword);

        if (isInValid)
            return;

        GlobalHelper.showProgressDialog(this, getString(R.string.please_wait_sending));
        fAuth.createUserWithEmailAndPassword(email, mpassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                GlobalHelper.hideProgressDialog();
                FirebaseUser firebaseUser = fAuth.getCurrentUser();

//                assert firebaseUser != null ;
                if (firebaseUser != null) {
                    String userid = firebaseUser.getUid();
                    sendFirebaseData(userid, fname, "default");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                GlobalHelper.hideProgressDialog();
                Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFirebaseData(String userId, String username, String avatar) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user_id", userId);
        userMap.put("username", username);
        userMap.put("imageURL", avatar);

        GlobalHelper.showProgressDialog(this, getString(R.string.please_wait_sending));
        fireStoreDB.collection(Constants.USER).document(userId).set(userMap, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        GlobalHelper.hideProgressDialog();
                        if (task.isSuccessful()) {
                            saveUserToLocal(userId, username, avatar);

                            Toast.makeText(SignupActivity.this, "User created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, HomeActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();

                        } else {
                            Toast.makeText(SignupActivity.this, "fail_add_user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToLocal(String userId, String username, String avatar) {
        UserModel userModel = new UserModel();
        userModel.user_id = userId;
        userModel.username = username;
        userModel.imageURL = avatar;

        UtilityApp.setUserData(userModel);
    }
}