package com.bhaiyalog.dualauthwithfirebase.auth_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bhaiyalog.dualauthwithfirebase.MainActivity;
import com.bhaiyalog.dualauthwithfirebase.R;
import com.bhaiyalog.dualauthwithfirebase.customAssets.OtpEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {
    private static final String TAG = "VerifyPhoneActivity";
    //These are the objects needed
    //It is the verification id that will be sent to the user
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mVerificationToken;


    //firebase auth object
    private FirebaseAuth mAuth;
    private LinearLayout lvSignInBtn;
    private OtpEditText tvOtp;
    private TextView resendOtp;
    private ProgressBar pbStatus;
    private TextView tvVerifyStatus;
    private String mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        mobile = getIntent().getStringExtra("mobile");
        mAuth = FirebaseAuth.getInstance();
        resendOtp = findViewById(R.id.tvResendOTP);
        tvOtp = findViewById(R.id.et_otp);
        tvVerifyStatus = findViewById(R.id.tvVerificationStatus);
        pbStatus = findViewById(R.id.progressBarStatus);

        lvSignInBtn = findViewById(R.id.linearSignInBtn);
        sendVerificationCode(mobile);
        tvOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 5) {
                    InputMethodManager inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    lvSignInBtn.setEnabled(true);
                    Log.d(TAG, "onCreate: You can verify Now" + s.length());
                } else {
                    lvSignInBtn.setEnabled(false);
                    Log.d(TAG, "onCreate: enter full OTP" + s.length());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    //the method is sending verification code
    //the country id is concatenated
    //you can take the country id as user input as well
    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
        Toast.makeText(this, "OTP sent", Toast.LENGTH_SHORT).show();
        timeCount();
    }
    private void resendVerificationCode(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                mVerificationToken);             // ForceResendingToken from callbacks
        Toast.makeText(this, "OTP re-sent", Toast.LENGTH_SHORT).show();
        timeCount();
    }

    //time counter for resendOTP
    private void timeCount() {
        resendOtp.setEnabled(false);
        new CountDownTimer(1000 * 60, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                resendOtp.setText("0" + minutes + ":" + " " + seconds + " " + "left");

            }

            @Override
            public void onFinish() {
                resendOtp.setEnabled(true);
                resendOtp.setText("Resend Code");
            }
        }.start();
    }
    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                pbStatus.setVisibility(View.VISIBLE);
                tvVerifyStatus.setVisibility(View.GONE);
                lvSignInBtn.setEnabled(false);
                tvOtp.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
//            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            pbStatus.setVisibility(View.GONE);
            tvVerifyStatus.setVisibility(View.VISIBLE);
            tvVerifyStatus.setText("mobile verification failed!\nre-enter your OTP");
            lvSignInBtn.setEnabled(true);
            Toast.makeText(VerifyActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
            mVerificationToken = forceResendingToken;
            Log.e(TAG, "onCodeSent: s - " + mVerificationId + " : t - " + forceResendingToken);

        }
    };



    private void verifyVerificationCode(String code) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        } catch (Exception e) {
            //verification unsuccessful.. display an error message
            lvSignInBtn.setEnabled(true);
            pbStatus.setVisibility(View.GONE);
            tvVerifyStatus.setVisibility(View.VISIBLE);
            tvVerifyStatus.setText("mobile verification failed!\nre-enter your OTP");
        }
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
//        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            Intent intent = new Intent(VerifyActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            lvSignInBtn.setEnabled(true);
                            finish();
                        }
                        else {
                        //verification unsuccessful.. display an error message
                        lvSignInBtn.setEnabled(true);
                        String message = "Somthing is wrong, we will fix it soon...";

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            pbStatus.setVisibility(View.GONE);
                            tvVerifyStatus.setVisibility(View.VISIBLE);
                            tvVerifyStatus.setText("mobile verification failed!\nre-enter your OTP");
                            message = "Invalid code entered...";
                        }
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: " + message);
                    }
                }
    });
    }
    public void clickToSignIn(View view) {
        String code = tvOtp.getText().toString().trim();
        tvVerifyStatus.setText("mobile verification is in progress...");
        pbStatus.setVisibility(View.VISIBLE);
        tvVerifyStatus.setVisibility(View.GONE);
        lvSignInBtn.setEnabled(false);
        verifyVerificationCode(code);
    }

    public void clickToResendOtp(View view) {
        resendVerificationCode(mobile);
    }
}
