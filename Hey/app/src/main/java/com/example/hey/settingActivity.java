package com.example.hey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Iterator;

public class settingActivity extends AppCompatActivity {

    private Button update;
    private EditText username,status;
    private CircleImageView userProfile;
    private String currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private static int galleryPick=1;
    private StorageReference profileRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        firebaseAuth=FirebaseAuth.getInstance();

        currentUser=firebaseAuth.getCurrentUser().getUid();

        databaseReference= FirebaseDatabase.getInstance().getReference();

        profileRef= FirebaseStorage.getInstance().getReference().child("Profile Image");



        inItialise();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               performDatabaseUpdation();
            }
        });

        retriveData();

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pick=new Intent();
                pick.setAction(Intent.ACTION_GET_CONTENT);
                pick.setType("image/*");
                startActivityForResult(pick,galleryPick);
            }
        });
    }



    private void inItialise() {
        update=(Button)findViewById(R.id.updateButton);
        username=(EditText)findViewById(R.id.userName);
        status=(EditText)findViewById(R.id.status);
        userProfile=(CircleImageView)findViewById(R.id.profileImage);
        loadingBar=new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPick && resultCode==RESULT_OK && data!=null)
        {
            Uri image=data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("Image upload proccessing ");
                loadingBar.setMessage("Please, Wait...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri=result.getUri();

                final StorageReference filePath=profileRef.child(currentUser+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(settingActivity.this, "Image uploaded successfully",
                                    Toast.LENGTH_SHORT).show();


                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                     final String downloadUrl=uri.toString();
                                    databaseReference.child("User").child(currentUser).child("image")
                                            .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {

                                                Toast.makeText(settingActivity.this, "Image stored in database", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                            else
                                            {
                                                Toast.makeText(settingActivity.this, " "+task.getException().toString(),
                                                        Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                        }
                                    });
                                }
                            });





                        }
                        else
                        {
                            Toast.makeText(settingActivity.this, "Image uploding failed  "+
                                            task.getException().toString(),Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }

                    }
                });


            }
        }

    }

    private void performDatabaseUpdation() {
        String userName=username.getText().toString();
        String userStatus=status.getText().toString();


        if(TextUtils.isEmpty(userName))
        {
            Toast.makeText(this, "Username required", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(userStatus))
        {
            Toast.makeText(this, "Status Required", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String,Object> hashMap=new HashMap<String,Object>();
            hashMap.put("uid",currentUser);
            hashMap.put("name",userName);
            hashMap.put("status",userStatus);



          databaseReference.child("User").child(currentUser).updateChildren(hashMap).
                  addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {

                  if(task.isSuccessful())
                  {
                   sendTOMain();
                      Toast.makeText(settingActivity.this, "User updated ..", Toast.LENGTH_SHORT).show();
                  }
                  else
                  {
                      Toast.makeText(settingActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                  }

              }
          });
        }

    }


    private void retriveData() {

        databaseReference.child("User").child(currentUser)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if((dataSnapshot.exists())&&dataSnapshot.hasChild("name")&&dataSnapshot.hasChild("image"))
                        {
                            String name=dataSnapshot.child("name").getValue().toString();
                            String status1=dataSnapshot.child("status").getValue().toString();
                            String image=dataSnapshot.child("image").getValue().toString();

                            username.setText(name);
                            status.setText(status1);
                            Glide.with(settingActivity.this)
                                    .load(image).into(userProfile);

                        }
                        else if((dataSnapshot.exists())&&dataSnapshot.hasChild("name"))
                        {
                            String name=dataSnapshot.child("name").getValue().toString();
                            String status1=dataSnapshot.child("status").getValue().toString();
                            username.setText(name);
                            status.setText(status1);
                        }
                        else
                        {
                            Toast.makeText(settingActivity.this, "Username field required", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void sendTOMain() {
        Intent toMain=new Intent(settingActivity.this,MainActivity.class);
        toMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toMain);
        finish();

    }


}
