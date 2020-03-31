package com.example.hey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabAcessorAdapter myTabAcessorAdapter;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    private  String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();


        rootRef=FirebaseDatabase.getInstance().getReference();

        toolbar=(Toolbar) findViewById(R.id.main_page_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("HeyApp");

        myViewPager=findViewById(R.id.main_Tab_pager);
        myTabAcessorAdapter=new TabAcessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAcessorAdapter);

        myTabLayout=findViewById(R.id.main_Tab);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
       FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser==null)
        {
            sendToLogin();
        }
        else
        {
            updateUserStatus("Online");

            verifyUserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser firebaseUser=mAuth.getCurrentUser();

        if(firebaseUser!=null)
        {
            updateUserStatus("Offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser firebaseUser=mAuth.getCurrentUser();

        if(firebaseUser!=null)
        {
            updateUserStatus("Offline");
        }
    }

    private void verifyUserExistence() {

        String user=mAuth.getCurrentUser().getUid();

        rootRef.child("User").child(user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendUserToSetting();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSetting() {
        Intent toSetting=new Intent(MainActivity.this,settingActivity.class);
        toSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toSetting);
        finish();
    }

    private void sendToLogin() {
        Intent toLogin=new Intent(MainActivity.this,loginActivity.class);
        toLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(toLogin);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch (item.getItemId())
         {
             case R.id.findFriend:
                 sentToFindFriend();
                 break;

             case R.id.setting:
                 sendToSetting();
                 break;

             case R.id.createGroup:
                 groupOptionSelected();
                 break;

             case R.id.logout:
                 updateUserStatus("Offline");
                 mAuth.signOut();
                 sendToLogin();
                 break;

                 default:
                     break;

         }
         return true;
    }

    private void groupOptionSelected() {

        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Group Name");

        final EditText groupname=new EditText(MainActivity.this);
        groupname.setHint("Friends");
        builder.setView(groupname);

        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             String group=groupname.getText().toString().trim();

             if(TextUtils.isEmpty(group))
             {
                 Toast.makeText(MainActivity.this, "Creation denied", Toast.LENGTH_SHORT).show();
             }
             else
             {
                  createNewGroup(group);
             }
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               dialogInterface.cancel();
            }
        });

        builder.show();

    }

    private void createNewGroup(final String group) {

        rootRef.child("Group").child(group).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, group+" created successfully", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void sendToSetting() {
        Intent toSetting=new Intent(MainActivity.this,settingActivity.class);
        startActivity(toSetting);

    }
    private void  sentToFindFriend() {
        Intent toFriend=new Intent(MainActivity.this,findFriendActivity.class);
        startActivity(toFriend);

    }

    private void updateUserStatus(String state)
    {

        String saveCurrentTime,saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,YYYY");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap=new HashMap<>();

        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);


        currentUserId=mAuth.getCurrentUser().getUid();


        rootRef.child("User").child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);



    }
}
