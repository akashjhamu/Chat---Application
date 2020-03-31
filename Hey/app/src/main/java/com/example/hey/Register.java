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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Register extends AppCompatActivity {

    private Button butonregiter;
    private EditText registerEmail,registerPassword;
    private TextView registerDirect;
    private FirebaseAuth mAuth;

    private DatabaseReference rootRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();

        inItialise();
        registerDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLoginActivity();
            }
        });

        butonregiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration();
            }
        });
    }

    private void registration() {
        String email=registerEmail.getText().toString();
        String password=registerPassword.getText().toString();

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

            progressDialog.setTitle("Creating new Account");
            progressDialog.setMessage("Please Wait... while creating new account");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();




            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {

                        String deviceToken= FirebaseInstanceId.getInstance().getToken();


                        String user=mAuth.getCurrentUser().getUid();
                        rootRef.child("User").child(user).setValue("");

                        rootRef.child("User").child(user).child("deviceToken").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            goToMainActivity();
                                            Toast.makeText(Register.this, "Registration succesful", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }

                                    }
                                });


                    }

                    else
                    {
                        Toast.makeText(Register.this, "Error: "+task.getException().toString(), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }


    }

    private void goToLoginActivity() {
        Intent intent=new Intent(Register.this,loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void goToMainActivity() {
        Intent mainintent=new Intent(Register.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    private void inItialise() {

        butonregiter=(Button)findViewById(R.id.registerButton);
        registerEmail=(EditText)findViewById(R.id.registerEmail);
        registerPassword=(EditText)findViewById(R.id.registerPassword);
        registerDirect=(TextView)findViewById(R.id.registerBactLogin);
        progressDialog=new ProgressDialog(this);

    }
}
