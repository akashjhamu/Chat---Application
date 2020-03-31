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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class loginActivity extends AppCompatActivity {


    private Button butonLogin,buttonphoneLogin;
    private EditText loginEmail,loginPassword;
    private TextView loginForget,loginNew;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

       userRef= FirebaseDatabase.getInstance().getReference().child("User");

        inItialise();

        loginNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goRegisterActivity();
            }
        });


        butonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                directToMainPage();
            }
        });

        buttonphoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneActivity=new Intent(loginActivity.this,phoneNumberActivity.class);
                startActivity(phoneActivity);
            }
        });
    }

    private void directToMainPage() {



        String email=loginEmail.getText().toString();
        String password=loginPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Email Reqired", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Password Reqired", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog.setTitle("Sign In");
            progressDialog.setMessage("Please Wait... ");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        String currentUserId=mAuth.getCurrentUser().getUid();
                        String deviceToken= FirebaseInstanceId.getInstance().getToken();

                        userRef.child(currentUserId).child("deviceToken").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful()) {
                                           sendTOMain();
                                           Toast.makeText(loginActivity.this, "Login succesful", Toast.LENGTH_LONG).show();
                                           progressDialog.dismiss();
                                       }

                                    }
                                });



                    }

                    else
                    {
                        Toast.makeText(loginActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                }
            });

        }

    }



    private void goRegisterActivity() {
        Intent intent=new Intent(loginActivity.this,Register.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void inItialise() {
        butonLogin=(Button)findViewById(R.id.loginButton);
        buttonphoneLogin=(Button)findViewById((R.id.loginPhone));
        loginEmail=(EditText)findViewById(R.id.loginEmail);
        loginPassword=(EditText)findViewById(R.id.loginPassword);
        loginForget=(TextView)findViewById(R.id.loginForget);
        loginNew=(TextView)findViewById(R.id.loginNew);
        progressDialog=new ProgressDialog(this);


    }



    private void sendTOMain() {
        Intent toMain=new Intent(loginActivity.this,MainActivity.class);
        toMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMain);
        finish();

    }
}
