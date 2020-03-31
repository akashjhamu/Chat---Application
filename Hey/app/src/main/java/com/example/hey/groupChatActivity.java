package com.example.hey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class groupChatActivity extends AppCompatActivity {

    private Toolbar groupTool;
    private EditText groupEditText;
    private ImageButton groupButton;
    private ScrollView groupScrool;
    private TextView groupTextView;
    private String  groupName;
    private FirebaseAuth mAuth;
    private DatabaseReference mReference,groupReference,groupKeyRef;

    private String currentUserId,currentUserName,currentDate,currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupName=getIntent().getExtras().get("groupName").toString();

        mAuth=FirebaseAuth.getInstance();

        mReference= FirebaseDatabase.getInstance().getReference().child("User");

        groupReference=FirebaseDatabase.getInstance().getReference().child("Group").child(groupName);

        currentUserId=mAuth.getCurrentUser().getUid();





        Initialise();
        getUserInfo();

        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               saveInfoToDatabase();

               groupEditText.setText("");

                groupScrool.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists())
                {
                    displayMessage(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists())
                {
                    displayMessage(dataSnapshot);
                }

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



    private void Initialise() {

        groupTool=(Toolbar)findViewById(R.id.groupChatLayout);
        setSupportActionBar(groupTool);
        getSupportActionBar().setTitle(groupName);
        groupEditText=(EditText)findViewById(R.id.inputGroupMessage);
        groupButton=(ImageButton)findViewById(R.id.groupSendButton);
        groupScrool=(ScrollView)findViewById(R.id.groupScrollView);
        groupTextView=(TextView)findViewById(R.id.groupTextVew);

    }
    private void getUserInfo() {

        mReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if(dataSnapshot.exists())
             {
                 currentUserName=dataSnapshot.child("name").getValue().toString();

             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void saveInfoToDatabase() {

        String message1=groupEditText.getText().toString().trim();
        String messgageKey=groupReference.push().getKey();

        if(TextUtils.isEmpty(message1))
        {

        }
        else
        {
            Calendar callForDate=Calendar.getInstance();
            SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
            currentDate=dateFormat.format(callForDate.getTime());


            Calendar callForTime=Calendar.getInstance();
            SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=timeFormat.format(callForTime.getTime());

            HashMap<String,Object> value=new HashMap<String,Object>();
            groupReference.updateChildren(value);

            groupKeyRef=groupReference.child(messgageKey);

            HashMap<String,Object> valueRecent=new HashMap<String,Object>();
            valueRecent.put("name",currentUserName);
            valueRecent.put("message",message1);
            valueRecent.put("date",currentDate);
            valueRecent.put("time",currentTime);

            groupKeyRef.updateChildren(valueRecent);






        }



    }

    private void displayMessage(DataSnapshot dataSnapshot) {

        Iterator itrates=dataSnapshot.getChildren().iterator();

        while(itrates.hasNext())
        {
            String userDate1=(String)((DataSnapshot)itrates.next()).getValue();
            String userMessage1=(String)((DataSnapshot)itrates.next()).getValue();
            String userName1=(String)((DataSnapshot)itrates.next()).getValue();
            String userTime1=(String)((DataSnapshot)itrates.next()).getValue();

            groupTextView.append(userName1+'\n'+userMessage1+'\n'+userDate1+"  "+userTime1+"\n\n\n");

            groupScrool.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}
