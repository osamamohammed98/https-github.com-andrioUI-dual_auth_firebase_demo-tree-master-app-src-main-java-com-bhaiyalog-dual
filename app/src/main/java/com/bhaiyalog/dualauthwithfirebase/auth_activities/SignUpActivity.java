package com.bhaiyalog.dualauthwithfirebase.auth_activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bhaiyalog.dualauthwithfirebase.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private EditText etUserMobile, etUserEmail, etUserName, etUserPassword;
    private Button btnSignUp;
    private FirebaseAuth firebaseAuth;
    private HashMap<String, Object> hm;
    private DatabaseReference userDataSubmittionRef;
    private String userMobile;
    private String userEmail;
    private String userName;
    private String userPassword;
    private String mobileNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        firebaseAuth = FirebaseAuth.getInstance();
        mobileNumber = getIntent().getStringExtra("mobile");
        etUserMobile = findViewById(R.id.etUserMobile);
        etUserEmail = findViewById(R.id.etUserEmail);
        etUserName = findViewById(R.id.etUserName);
        etUserPassword = findViewById(R.id.etUserPassword);
        etUserMobile.setText(mobileNumber);
        userDataSubmittionRef = FirebaseDatabase.getInstance().getReference("customer");
    }
    public void signUpNewUser(View view) {
        userMobile = etUserMobile.getText().toString().trim();
        userEmail = etUserEmail.getText().toString().trim();
        userName = etUserName.getText().toString().trim();
        userPassword = etUserPassword.getText().toString().trim();

        if (userMobile.isEmpty()) {
            etUserMobile.setError("Enter 10 digit mobile number");
            etUserMobile.requestFocus();
        } else if (userEmail.isEmpty()) {
            etUserEmail.setError("Enter email Id");
            etUserEmail.requestFocus();
        }else if (userName.isEmpty()) {
            etUserName.setError("Enter your name");
            etUserName.requestFocus();
        }else if (userPassword.isEmpty()) {
            etUserPassword.setError("Enter password");
            etUserPassword.requestFocus();
        }else if (userPassword.length()<6) {
            etUserPassword.setError("Enter 6-12 character password");
            etUserPassword.requestFocus();
        }
        else
        {
            registerByEmail();

        }

    }
    public void registerByEmail() {
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if (task.isSuccessful()) {
                            hm = new HashMap<>();
                            hm.put("name", userName);
                            hm.put("email", userEmail);
                            hm.put("mobile", userMobile);
                        }
                        userDataSubmittionRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(hm, ( new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Intent intentSignUp = new Intent(SignUpActivity.this, VerifyActivity.class);
                                            intentSignUp.putExtra("mobile", userMobile);
                                            intentSignUp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intentSignUp);
                                            Toast.makeText(SignUpActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Please have a look on your data", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }));

                    }
                });
    }
}
