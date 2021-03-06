package com.example.hey;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class ContactFragment extends Fragment {

    private  View contactsView;
    private RecyclerView contactRecyclerList;
    private DatabaseReference contactRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUid;

    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        contactsView = inflater.inflate(R.layout.fragment_contact, container, false);

        contactRecyclerList=(RecyclerView)contactsView.findViewById(R.id.contactsRecyclerView);
        contactRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUid=mAuth.getCurrentUser().getUid();
        contactRef= FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUid);

        userRef= FirebaseDatabase.getInstance().getReference().child("User");





        return  contactsView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts> options=
                new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(contactRef,contacts.class)
                .build();


        FirebaseRecyclerAdapter<contacts,myViewHolder>  adapter=new FirebaseRecyclerAdapter<contacts,
                myViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final myViewHolder holder, int position, @NonNull contacts model) {

              String  userId=getRef(position).getKey();

              userRef.child(userId).addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      if (dataSnapshot.exists()) {


                          if(dataSnapshot.child("userState").hasChild("state"))
                          {
                              String date=dataSnapshot.child("userState").child("date").getValue().toString();
                              String time=dataSnapshot.child("userState").child("time").getValue().toString();
                              String state=dataSnapshot.child("userState").child("state").getValue().toString();


                              if(state.equals("Offline"))
                              {
                                  holder.onlineIcon.setVisibility(View.INVISIBLE);
                              }
                              else if(state.equals("Online"))
                              {
                                  holder.onlineIcon.setVisibility(View.VISIBLE);
                              }
                              else
                              {
                                  holder.onlineIcon.setVisibility(View.INVISIBLE);

                              }
                          }


                          if (dataSnapshot.hasChild("image")) {
                              String userName1 = dataSnapshot.child("name").getValue().toString();
                              String userStatus1 = dataSnapshot.child("status").getValue().toString();
                              String userImage = dataSnapshot.child("image").getValue().toString();

                              holder.userName.setText(userName1);
                              holder.userStatus.setText(userStatus1);


                              Glide.with(getContext())
                                      .load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                          } else {

                              String userName1 = dataSnapshot.child("name").getValue().toString();
                              String userStatus1 = dataSnapshot.child("status").getValue().toString();

                              holder.userName.setText(userName1);
                              holder.userStatus.setText(userStatus1);

                          }

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

                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,
                false);
                myViewHolder holder=new myViewHolder(view);

                return  holder;

            }
        };

        contactRecyclerList.setAdapter(adapter);

        adapter.startListening();

    }


    public static  class myViewHolder extends  RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;

        ImageView onlineIcon;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.findFriendName);
            userStatus=itemView.findViewById(R.id.findFriendSatus);
            profileImage=itemView.findViewById(R.id.findProfileImage);
            onlineIcon=itemView.findViewById(R.id.onlineStatus);
        }
    }



}
