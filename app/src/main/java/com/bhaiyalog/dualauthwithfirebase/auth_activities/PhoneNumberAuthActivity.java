package com.bhaiyalog.dualauthwithfirebase.auth_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bhaiyalog.dualauthwithfirebase.MainActivity;
import com.bhaiyalog.dualauthwithfirebase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PhoneNumberAuthActivity extends AppCompatActivity {
    private DatabaseReference myDataRef;
    private EditText etPhone;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_auth);
        mAuth = FirebaseAuth.getInstance();
        myDataRef = FirebaseDatabase.getInstance().getReference().child("customer");
        myDataRef.keepSynced(true);
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        etPhone = findViewById(R.id.etPhoneNumber);
        final Button btnContinue = findViewById(R.id.btnContinue);
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    btnContinue.setEnabled(true);
                    btnContinue.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    btnContinue.setEnabled(false);
                    btnContinue.setTextColor(getResources().getColor(R.color.btn_disable));
                }
            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Intent intent = new Intent(PhoneNumberAuthActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoadFirebaseData().execute();
            }
        });
    }
    //    progress dialog before laoding data
    private class LoadFirebaseData extends AsyncTask<Void, Void, Integer> {
        ProgressDialog Dialog = new ProgressDialog(PhoneNumberAuthActivity.this);
        String phonenumber=etPhone.getText().toString().trim();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Dialog.setTitle("Please Wait");
            Dialog.setMessage("data is loading..");
            Dialog.setIndeterminate(false);
            Dialog.setCancelable(false);
            Dialog.show();
            isInternetOn();

        }

        protected void isInternetOn() {
            ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = conn.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected() == true) {
            } else {
                Dialog.setMessage("please check your internet connection...");
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            myDataRef.orderByChild("mobile").equalTo(phonenumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Intent intentSignUp = new Intent(PhoneNumberAuthActivity.this, VerifyActivity.class);
                        intentSignUp.putExtra("mobile", phonenumber);
                        intentSignUp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentSignUp);
                        Toast.makeText(PhoneNumberAuthActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                    }

                    else {
                        Intent intentSignUp = new Intent(PhoneNumberAuthActivity.this, SignUpActivity.class);
                        intentSignUp.putExtra("mobile", phonenumber);
                        intentSignUp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentSignUp);
                        Dialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return 0;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
