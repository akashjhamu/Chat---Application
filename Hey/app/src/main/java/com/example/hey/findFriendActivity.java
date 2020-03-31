package com.example.hey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class findFriendActivity extends AppCompatActivity {

    private Toolbar findFriendToolbar;
    private RecyclerView findFriendRecyclerView;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        userRef= FirebaseDatabase.getInstance().getReference().child("User");

        findFriendToolbar=(Toolbar)findViewById(R.id.findFriendToolbar);
        findFriendRecyclerView=(RecyclerView)findViewById(R.id.findFriendRecyclerview);

        findFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(findFriendToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friend");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts> options=new
                FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(userRef,contacts.class)
                .build();

        FirebaseRecyclerAdapter<contacts,myViewHolder>  adapter=new FirebaseRecyclerAdapter<contacts, myViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull myViewHolder holder, final int position, @NonNull contacts model) {

                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());

                Glide.with(findFriendActivity.this)
                        .load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String anotherUserProfile=getRef(position).getKey();

                        Intent toAnotherProfile=new Intent(findFriendActivity.this,profileActivity.class);
                        toAnotherProfile.putExtra("anotherUserProfile",anotherUserProfile);
                        startActivity(toAnotherProfile);



                    }
                });

            }

            @NonNull
            @Override
            public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout
                        ,parent,false);
                myViewHolder holder=new myViewHolder(view);

                return  holder;
            }
        };
        findFriendRecyclerView.setAdapter(adapter);

        adapter.startListening();
    }

    public static  class myViewHolder extends  RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.findFriendName);
            userStatus=itemView.findViewById(R.id.findFriendSatus);
            profileImage=itemView.findViewById(R.id.findProfileImage);
        }
    }
}
