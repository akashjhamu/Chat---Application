package com.example.hey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class phoneNumberActivity extends AppCompatActivity {

   private Button sendCode,verifyCode;
   private EditText sendNumber,getNumber;

   private  PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

   private String mVerificationId;

   private PhoneAuthProvider.ForceResendingToken  mResendToken;

   private ProgressDialog lodingBar;

   private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        inItialise();

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


               String phoneNumber=sendNumber.getText().toString().trim();

               mAuth=FirebaseAuth.getInstance();

               if(TextUtils.isEmpty(phoneNumber))
               {
                   Toast.makeText(phoneNumberActivity.this, "Phone number required", Toast.LENGTH_SHORT)
                           .show();
               }
               else
               {
                   lodingBar.setTitle("Phone verifiyinf");
                   lodingBar.setMessage("Please,while checking...");
                   lodingBar.setCanceledOnTouchOutside(false);
                   lodingBar.show();

                   PhoneAuthProvider.getInstance().verifyPhoneNumber(
                           phoneNumber,        // Phone number to verify
                           60,                 // Timeout duration
                           TimeUnit.SECONDS,   // Unit of timeout
                           phoneNumberActivity.this,               // Activity (for callback binding)
                           mCallbacks);


               }

            }
        });


        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Toast.makeText(phoneNumberActivity.this, "Verification failed",
                        Toast.LENGTH_SHORT).show();

                lodingBar.dismiss();
                sendCode.setVisibility(View.VISIBLE);
                sendNumber.setVisibility(View.VISIBLE);
                verifyCode.setVisibility(View.INVISIBLE);
                getNumber.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {



                mVerificationId = verificationId;
                mResendToken = token;
                 lodingBar.dismiss();
                Toast.makeText(phoneNumberActivity.this, "Verification successful",
                        Toast.LENGTH_SHORT).show();

                sendCode.setVisibility(View.INVISIBLE);
                sendNumber.setVisibility(View.INVISIBLE);
                verifyCode.setVisibility(View.VISIBLE);
                getNumber.setVisibility(View.VISIBLE);


            }
        };

        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendCode.setVisibility(View.INVISIBLE);
                sendNumber.setVisibility(View.INVISIBLE);

                String codeMessage=getNumber.getText().toString();

                if(TextUtils.isEmpty(codeMessage))
                {
                    Toast.makeText(phoneNumberActivity.this, "Code required",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    lodingBar.setTitle("Code verifivation");
                    lodingBar.setMessage("Please,while authenticating...");
                    lodingBar.setCanceledOnTouchOutside(false);
                    lodingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,codeMessage);

                    signInWithPhoneAuthCredential(credential);

                }



            }
        });

    }

    private void inItialise() {

        sendCode=(Button)findViewById(R.id.phoneSendOtpButton);
        verifyCode=(Button)findViewById(R.id.phoneVerifyButton);
        sendNumber=(EditText)findViewById(R.id.phoneEditText);
        getNumber=(EditText)findViewById(R.id.phoneVerifyEditText);
        lodingBar=new ProgressDialog(this);


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {

                        if(task.isSuccessful())
                        {
                            lodingBar.dismiss();
                            Toast.makeText(phoneNumberActivity.this, "LoginSuccessful",
                                    Toast.LENGTH_SHORT).show();
                            sendTOMainActivity();


                        }
                        else
                        {

                            Toast.makeText(phoneNumberActivity.this, ""+task.getException().toString()
                                    , Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    private void sendTOMainActivity() {
        Intent mainintent=new Intent(phoneNumberActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
}
