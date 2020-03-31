package com.example.hey;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private  View privateView;
    private RecyclerView privateRecycler;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference contactRef,userRef;


    public ChatFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        privateView=inflater.inflate(R.layout.fragment_chat, container, false);
        privateRecycler=(RecyclerView)privateView.findViewById(R.id.privateChatList);
        privateRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        contactRef= FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUserId);
        userRef=FirebaseDatabase.getInstance().getReference().child("User");

        return privateView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts> option=new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(contactRef,contacts.class)
                .build();


        FirebaseRecyclerAdapter<contacts,myViewHolder> adapter=new FirebaseRecyclerAdapter<contacts, myViewHolder>(option) {
            @Override
            protected void onBindViewHolder(@NonNull final myViewHolder holder, int position, @NonNull contacts model) {
                final String friends=getRef(position).getKey();
                final String[] userImage = {"default_image"};

                userRef.child(friends).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {

                                userImage[0] = dataSnapshot.child("image").getValue().toString();


                                Glide.with(getContext())
                                        .load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);

                            }

                            final String userName1 = dataSnapshot.child("name").getValue().toString();
                            String userStatus1 = dataSnapshot.child("status").getValue().toString();

                            holder.userStatus.setText("Offline Register");


                            holder.userName.setText(userName1);

                            if (dataSnapshot.child("userState").hasChild("state")) {

                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();


                                if (state.equals("Online")) {
                                    holder.userStatus.setText("Online");
                                }
                                else if (state.equals("Offline")) {
                                    holder.userStatus.setText("Last Seen " + date + " " + time);

                                }

                            }
                            else {
                                holder.userStatus.setText("Offline Register");

                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    Intent privateChat = new Intent(getContext(), chatActivity.class);

                                    privateChat.putExtra("friendsUserId", friends);
                                    privateChat.putExtra("friendsUserName", userName1);
                                    privateChat.putExtra("friendsImage", userImage[0]);

                                    startActivity(privateChat);

                                }
                            });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent
                ,false);

                myViewHolder holder=new myViewHolder(view);

                return holder;
            }
        };

        privateRecycler.setAdapter(adapter);
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
