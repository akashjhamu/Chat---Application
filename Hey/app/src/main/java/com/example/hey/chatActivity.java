package com.example.hey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chatActivity extends AppCompatActivity {

    private String messageRecieveName,messageReciverId,messageRecieverImage,messageSenderId;
    private Toolbar toolbar;
    private TextView customProfileName1,customProfileSeen1;
    private CircleImageView customProfileImage1;
    private EditText inputMessage;
    private ImageButton sendButton,fileSendButton;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private final List<Message> messageDataList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private privateMessageAdapter PrivateMessageAdapter1;
    private RecyclerView mRecyclerView;
    private String saveCurrentTime,saveCurrentDate;
    private String checker="" , myUrl="" ;
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        messageRecieveName=getIntent().getExtras().get("friendsUserName").toString();
        messageReciverId=getIntent().getExtras().get("friendsUserId").toString();
        messageRecieverImage=getIntent().getExtras().get("friendsImage").toString();

        initialize();

        customProfileName1.setText(messageRecieveName);

        Glide.with(this)
                .load(messageRecieverImage).placeholder(R.drawable.profile_image).into(customProfileImage1);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });

        lastSeen();


        fileSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CharSequence[] options={
                        "Image",
                        "PDF File",
                        "MS Word File"

                };

                AlertDialog.Builder builder=new AlertDialog.Builder(chatActivity.this);
                builder.setTitle("Select an Option");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(i==0)
                        {
                           checker="image";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent,"Select Image"),69);

                        }
                        if(i==1)
                        {
                            checker="pdf";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(Intent.createChooser(intent,"Select PDF File"),69);

                        }
                        if(i==2)
                        {
                            checker="docx";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(Intent.createChooser(intent,"Select MS Word file"),69);

                        }

                    }
                });
               builder.show();
            }
        });


    }


    private void initialize() {

        toolbar=(Toolbar)findViewById(R.id.privateInsideToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View androidAppBar=layoutInflater.inflate(R.layout.custome_char_bar,null);
        actionBar.setCustomView(androidAppBar);

        customProfileName1=(TextView)findViewById(R.id.customProfileName);
        customProfileSeen1=(TextView)findViewById(R.id.customProfileSeen);
        customProfileImage1=(CircleImageView)findViewById(R.id.customProfileImage);

        inputMessage=(EditText)findViewById(R.id.inputInsidePrivateMessage);
        sendButton=(ImageButton)findViewById(R.id.privateInsideSendButton);
        fileSendButton=(ImageButton)findViewById(R.id.privateFileSend);

        loadingBar=new ProgressDialog(this);


        mRecyclerView=(RecyclerView)findViewById(R.id.privateInsideRecyclerView);
        linearLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        PrivateMessageAdapter1=new privateMessageAdapter(messageDataList,this);
        mRecyclerView.setAdapter(PrivateMessageAdapter1);

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,YYYY");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());



    }

    private void lastSeen()
    {
           rootRef.child("User").child(messageReciverId).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                   if(dataSnapshot.child("userState").hasChild("state"))
                   {
                       String date=dataSnapshot.child("userState").child("date").getValue().toString();
                       String time=dataSnapshot.child("userState").child("time").getValue().toString();
                       String state=dataSnapshot.child("userState").child("state").getValue().toString();


                       if(state.equals("Offline"))
                       {
                           customProfileSeen1.setText("Last Seen "+ date+" "+time);
                       }
                       else if(state.equals("Online"))
                       {
                          customProfileSeen1.setText("Online");
                       }
                       else
                       {
                           customProfileSeen1.setText("Offline");

                       }
                   }

               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Message").child(messageSenderId).child(messageReciverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Message  message=dataSnapshot.getValue(Message.class);

                        messageDataList.add(message);

                        PrivateMessageAdapter1.notifyDataSetChanged();


                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }



    private void sendMessage() {

        String messageText=inputMessage.getText().toString().trim();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Message Needed", Toast.LENGTH_SHORT).show();
        }
        else
        {

            String messageSenderRef="Message"+"/"+messageSenderId+"/"+messageReciverId;
            String messageRecieverRef="Message"+"/"+messageReciverId+"/"+messageSenderId;

            DatabaseReference messageRef=rootRef.child("Message").child(messageSenderId).child(messageReciverId)
                    .push();

            String key=messageRef.getKey();

            HashMap<String,Object> value=new HashMap<String, Object>();
            value.put("message",messageText);
            value.put("type","text");
            value.put("from",messageSenderId);
            value.put("to",messageReciverId);
            value.put("messageId",key);
            value.put("time",saveCurrentTime);
            value.put("date",saveCurrentDate);


            Map messageBodyDetail=new HashMap();
            messageBodyDetail.put(messageSenderRef+"/"+key,value);
            messageBodyDetail.put(messageRecieverRef+"/"+key,value);

            rootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(chatActivity.this, "Message saved", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(chatActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();

                    }
                    inputMessage.setText("");

                }
            });


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==69&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null)
        {
            loadingBar.setTitle("Image upload proccessing ");
            loadingBar.setMessage("Please, Wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            
              fileUri=data.getData();

              if(!checker.equals("image"))
              {

                  final StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document File");

                  final String messageSenderRef="Message"+"/"+messageSenderId+"/"+messageReciverId;
                  final String messageRecieverRef="Message"+"/"+messageReciverId+"/"+messageSenderId;

                  DatabaseReference messageRef=rootRef.child("Message").child(messageSenderId).child(messageReciverId)
                          .push();

                  final String key=messageRef.getKey();

                  final StorageReference filePath=storageReference.child(key+"."+checker);

                  filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                          if(task.isSuccessful())
                          {

                              filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                  @Override
                                  public void onSuccess(Uri uri) {

                                      HashMap<String,Object> valueForPictre=new HashMap<String, Object>();
                                      valueForPictre.put("message",uri.toString());
                                      valueForPictre.put("name",fileUri.getLastPathSegment());
                                      valueForPictre.put("type",checker);
                                      valueForPictre.put("from",messageSenderId);
                                      valueForPictre.put("to",messageReciverId);
                                      valueForPictre.put("messageId",key);
                                      valueForPictre.put("time",saveCurrentTime);
                                      valueForPictre.put("date",saveCurrentDate);


                                      Map messageBodyDetail=new HashMap();
                                      messageBodyDetail.put(messageSenderRef+"/"+key,valueForPictre);
                                      messageBodyDetail.put(messageRecieverRef+"/"+key,valueForPictre);

                                      rootRef.updateChildren(messageBodyDetail);
                                      loadingBar.dismiss();

                                  }


                              });

                              loadingBar.dismiss();
                          }

                      }
                  }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {

                          loadingBar.dismiss();
                          Toast.makeText(chatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                      }
                  }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                          double p=((100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount());
                          loadingBar.setMessage((int) p + "% Uploaded...");

                      }
                  });

              }
              else if(checker.equals("image"))
              {
                  StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image File");

                  final String messageSenderRef="Message"+"/"+messageSenderId+"/"+messageReciverId;
                  final String messageRecieverRef="Message"+"/"+messageReciverId+"/"+messageSenderId;

                  DatabaseReference messageRef=rootRef.child("Message").child(messageSenderId).child(messageReciverId)
                          .push();

                  final String key=messageRef.getKey();

                  final StorageReference filePath=storageReference.child(key+"."+"jpg");

                  uploadTask=filePath.putFile(fileUri);

                  uploadTask.continueWithTask(new Continuation() {
                      @Override
                      public Object then(@NonNull Task task) throws Exception {
                          if(!task.isSuccessful())
                          {
                              throw task.getException();
                          }
                          return filePath.getDownloadUrl();


                      }
                  }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                      @Override
                      public void onComplete(@NonNull Task<Uri> task) {

                           if(task.isSuccessful())
                           {
                               Uri downloadUri=task.getResult();
                               myUrl=downloadUri.toString();

                               HashMap<String,Object> valueForPictre=new HashMap<String, Object>();
                               valueForPictre.put("message",myUrl);
                               valueForPictre.put("name",fileUri.getLastPathSegment());
                               valueForPictre.put("type",checker);
                               valueForPictre.put("from",messageSenderId);
                               valueForPictre.put("to",messageReciverId);
                               valueForPictre.put("messageId",key);
                               valueForPictre.put("time",saveCurrentTime);
                               valueForPictre.put("date",saveCurrentDate);


                               Map messageBodyDetail=new HashMap();
                               messageBodyDetail.put(messageSenderRef+"/"+key,valueForPictre);
                               messageBodyDetail.put(messageRecieverRef+"/"+key,valueForPictre);

                               rootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {

                                       if(task.isSuccessful())
                                       {
                                           loadingBar.dismiss();
                                           Toast.makeText(chatActivity.this, "Message saved", Toast.LENGTH_SHORT).show();
                                       }
                                       else
                                       {
                                           loadingBar.dismiss();
                                           Toast.makeText(chatActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();

                                       }
                                       inputMessage.setText("");

                                   }
                               });
                           }



                      }
                  });
              }
              else
              {
                  loadingBar.dismiss();
                  Toast.makeText(this, "Error!!!", Toast.LENGTH_SHORT).show();
              }
        }
    }
}
