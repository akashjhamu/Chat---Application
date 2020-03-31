package com.example.hey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class profileActivity extends AppCompatActivity {

    private  String anotherUserProfile,currentSenderType,senderUserId;

    private Button sendRequest,cancelRequest;
    private TextView anotherUsername,anotherUserStatus;
    private CircleImageView anotherUserProfilePhoto;
    private FirebaseAuth mAuth;



    private DatabaseReference dataRef,chatRequestRef,contactRef,notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        dataRef= FirebaseDatabase.getInstance().getReference().child("User");
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactRef= FirebaseDatabase.getInstance().getReference().child("contacts");
        notificationRef= FirebaseDatabase.getInstance().getReference().child("Notification");



        anotherUserProfile=getIntent().getExtras().get("anotherUserProfile").toString();
        senderUserId=mAuth.getCurrentUser().getUid();

        Toast.makeText(this, "Profile Activity  "+anotherUserProfile, Toast.LENGTH_LONG).show();

        inItialise();

        RetriveData();


    }


    private void inItialise() {

        anotherUserProfilePhoto=(CircleImageView)findViewById(R.id.anotherProfile);
        anotherUsername=(TextView)findViewById(R.id.anotherName);
        anotherUserStatus=(TextView)findViewById(R.id.anotherStatus);
        sendRequest=(Button)findViewById(R.id.anotherRequestButton);
        cancelRequest=(Button)findViewById(R.id.anotherDeclineButton);
        currentSenderType="new";

    }


    private void RetriveData() {

        dataRef.child(anotherUserProfile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists())&&(dataSnapshot.hasChild("image")))
                {
                      String image=dataSnapshot.child("image").getValue().toString();
                    String name=dataSnapshot.child("name").getValue().toString();
                    String status=dataSnapshot.child("status").getValue().toString();

                    Glide.with(profileActivity.this)
                            .load(image).placeholder(R.drawable.profile_image).into(anotherUserProfilePhoto);

                    anotherUsername.setText(name);
                    anotherUserStatus.setText(status);

                    sendRequestMessage();

                }
                else
                {
                    String name=dataSnapshot.child("name").getValue().toString();
                    String status=dataSnapshot.child("status").getValue().toString();

                    anotherUsername.setText(name);
                    anotherUserStatus.setText(status);

                    sendRequestMessage();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendRequestMessage() {

        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(anotherUserProfile))
                {
                    String requestType=dataSnapshot.child(anotherUserProfile)
                            .child("requestType").getValue().toString();

                    if(requestType.equals("send"))
                    {
                        currentSenderType="requestSend";
                        sendRequest.setText("Cancel chat request");
                    }

                    else if(requestType.equals("recieved"))
                    {

                        currentSenderType="requestRecieved";
                        sendRequest.setText("Accept Request");

                        cancelRequest.setVisibility(View.VISIBLE);
                        cancelRequest.setEnabled(true);

                        cancelRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelRequest();
                            }
                        });

                    }

                }

                else
                {
                    contactRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(anotherUserProfile)) {
                                currentSenderType = "friends";
                                sendRequest.setText("Remove Friend");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!senderUserId.equals(anotherUserProfile))
        {

            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {

                    sendRequest.setEnabled(false);

                    if(currentSenderType.equals("new"))
                    {
                        sendRequestPage();
                    }

                    if(currentSenderType.equals("requestSend"))
                    {
                        cancelRequest();
                    }
                    if(currentSenderType.equals("requestRecieved"))
                    {

                        acceptChatRequest();

                    }
                    if(currentSenderType.equals("friends"))
                    {
                        deleteContactRequest();
                    }

                }
            });

        }
        else
        {

            sendRequest.setVisibility(View.INVISIBLE);

        }


    }




    private void sendRequestPage() {

        chatRequestRef.child(senderUserId).child(anotherUserProfile).child("requestType")
                .setValue("send").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                chatRequestRef.child(anotherUserProfile).child(senderUserId).child("requestType")
                        .setValue("recieved").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        HashMap<String,String> chatNotificationMap=new HashMap<String, String>();
                        chatNotificationMap.put("from",senderUserId);
                        chatNotificationMap.put("type","request");

                        notificationRef.child(anotherUserProfile).push().setValue(chatNotificationMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            sendRequest.setEnabled(true);
                                            currentSenderType="requestSend";
                                            sendRequest.setText("Cancel chat request");
                                        }

                                    }
                                });



                    }
                });

            }
        });

    }
    private void cancelRequest() {

        chatRequestRef.child(senderUserId).child(anotherUserProfile).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    chatRequestRef.child(anotherUserProfile).child(senderUserId).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        sendRequest.setEnabled(true);
                                        currentSenderType="new";
                                        sendRequest.setText("Send Request Message");

                                        cancelRequest.setVisibility(View.INVISIBLE);
                                        cancelRequest.setEnabled(false);
                                    }

                                }
                            });
                }

            }
        });

    }

    private void acceptChatRequest() {
        contactRef.child(senderUserId).child(anotherUserProfile).child("Contacts").setValue("accepted")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                      if(task.isSuccessful())
                      {
                          contactRef.child(anotherUserProfile).child(senderUserId).child("Contacts").setValue("accepted")
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task)
                                      {
                                          if(task.isSuccessful())
                                          {
                                              chatRequestRef.child(senderUserId).child(anotherUserProfile).removeValue()
                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                          @Override
                                                          public void onComplete(@NonNull Task<Void> task) {
                                                              if(task.isSuccessful())
                                                              {
                                                                  chatRequestRef.child(anotherUserProfile).child(senderUserId).removeValue()
                                                                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                              @Override
                                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                                  if(task.isSuccessful())
                                                                                  {
                                                                                      sendRequest.setEnabled(true);
                                                                                      currentSenderType="friends";
                                                                                      sendRequest.setText("Remove Friend");

                                                                                      cancelRequest.setVisibility(View.INVISIBLE);
                                                                                      cancelRequest.setEnabled(false);
                                                                                  }

                                                                              }
                                                                          });
                                                              }

                                                          }
                                                      });

                                          }
                                      }
                                  });
                      }
                    }
                });

    }


    private void deleteContactRequest() {

        contactRef.child(senderUserId).child(anotherUserProfile).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactRef.child(anotherUserProfile).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendRequest.setEnabled(true);
                                                currentSenderType="new";
                                                sendRequest.setText("Send Request Message");

                                                cancelRequest.setVisibility(View.INVISIBLE);
                                                cancelRequest.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }


}
