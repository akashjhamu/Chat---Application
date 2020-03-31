package com.example.hey;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class requestFragment extends Fragment {

    private  View requestView;
    private RecyclerView requestList;
    private DatabaseReference requestRef,userRef,contactsRef;
    private FirebaseAuth mAuth;
    private String currentID;

    public requestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       requestView = inflater.inflate(R.layout.fragment_request, container, false);
       requestList=(RecyclerView)requestView.findViewById(R.id.requestRecyclerView);
       requestList.setLayoutManager(new LinearLayoutManager(getContext()));

       mAuth=FirebaseAuth.getInstance();
       currentID=mAuth.getCurrentUser().getUid();

       requestRef= FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userRef= FirebaseDatabase.getInstance().getReference().child("User");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("contacts");




        return requestView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<contacts> options= new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(requestRef.child(currentID),contacts.class)
                .build();


        FirebaseRecyclerAdapter<contacts,myViewHolder> adapter= new FirebaseRecyclerAdapter<contacts,
                myViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final myViewHolder holder, int position, @NonNull contacts model) {

                holder.itemView.findViewById(R.id.acceptRequest).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.cancelRequest).setVisibility(View.VISIBLE);

                final String getUserId=getRef(position).getKey();

                DatabaseReference getType=getRef(position).child("requestType").getRef();

                getType.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            String value=dataSnapshot.getValue().toString();

                            if(value.equals("recieved"))
                            {
                                userRef.child(getUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image"))
                                        {

                                           final String userImage=dataSnapshot.child("image").getValue().toString();

                                            Glide.with(getContext())
                                                    .load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                        }


                                          final String userName1=dataSnapshot.child("name").getValue().toString();
                                           final String userStatus1=dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(userName1);
                                            holder.userStatus.setText("Want to connect with you");




                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                CharSequence option[]={
                                                        "Accept","Cancel"
                                                };

                                                AlertDialog.Builder  builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(userName1+" Chat Request");
                                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if(i==0)
                                                        {
                                                          contactsRef.child(currentID).child(getUserId).
                                                                  child("Contact").setValue("Saved")
                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                      @Override
                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                         if(task.isSuccessful()) {
                                                                             contactsRef.child(getUserId).child(currentID).
                                                                                     child("Contact").setValue("Saved")
                                                                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                         @Override
                                                                                         public void onComplete(@NonNull Task<Void> task) {

                                                                                             if(task.isSuccessful())
                                                                                             {
                                                                                                 requestRef.child(currentID).child(getUserId)
                                                                                                         .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                     @Override
                                                                                                     public void onComplete(@NonNull Task<Void> task) {

                                                                                                         if(task.isSuccessful())
                                                                                                         {
                                                                                                             requestRef.child(getUserId).child(currentID)
                                                                                                                     .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                 @Override
                                                                                                                 public void onComplete(@NonNull Task<Void> task) {

                                                                                                                     if(task.isSuccessful())
                                                                                                                     {
                                                                                                                         Toast.makeText(getContext(), "Contact saved", Toast.LENGTH_SHORT).show();
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
                                                        if(i==1)
                                                        {
                                                            requestRef.child(currentID).child(getUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful())
                                                                    {
                                                                        requestRef.child(getUserId).child(currentID)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    Toast.makeText(getContext(), "Cancel successfully", Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }

                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                                builder.show();

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }

                            else if(value.equals("send"))
                            {

                                Button request_send_btn=holder.itemView.findViewById(R.id.acceptRequest);
                                request_send_btn.setText("Req send");

                                holder.itemView.findViewById(R.id.cancelRequest).setVisibility(View.INVISIBLE);


                                userRef.child(getUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image"))
                                        {

                                            final String userImage=dataSnapshot.child("image").getValue().toString();

                                            Glide.with(getContext())
                                                    .load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                        }


                                        final String userName1=dataSnapshot.child("name").getValue().toString();
                                        final String userStatus1=dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(userName1);
                                        holder.userStatus.setText("You have send request to "+userName1);




                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                CharSequence option[]={
                                                       "Cancel chat request"
                                                };

                                                AlertDialog.Builder  builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle("Request send bu you");
                                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        if(i==0)
                                                        {
                                                            requestRef.child(currentID).child(getUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful())
                                                                    {
                                                                        requestRef.child(getUserId).child(currentID)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    Toast.makeText(getContext(), "You cancel chat request", Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }

                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                                builder.show();

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


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
               View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout
               ,parent,false);
               myViewHolder holder=new myViewHolder(view);
               return  holder;
            }
        };

        requestList.setAdapter(adapter);

        adapter.startListening();




    }

    public static  class myViewHolder extends  RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button accept,cancel;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.findFriendName);
            userStatus=itemView.findViewById(R.id.findFriendSatus);
            profileImage=itemView.findViewById(R.id.findProfileImage);
            accept=itemView.findViewById(R.id.acceptRequest);
            cancel=itemView.findViewById(R.id.cancelRequest);
        }
    }
}
