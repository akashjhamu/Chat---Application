package com.example.hey;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class privateMessageAdapter extends RecyclerView.Adapter<privateMessageAdapter.myViewHolder>{

private List<Message> userMessageList;
private FirebaseAuth mAuth;
private DatabaseReference userRef;
Context context;

public privateMessageAdapter(List<Message> userMessage, Context context)
{
    this.userMessageList=userMessage;
    this.context=context;
}

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.private_message_layout
        ,parent,false);

        mAuth=FirebaseAuth.getInstance();

        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final myViewHolder holder, final int position) {

    String mesSenderId=mAuth.getCurrentUser().getUid();

    Message data=userMessageList.get(position);

    String fromUserId=data.getFrom();
    String dataMessage=data.getMessage();
    String dataType=data.getType();

        holder.reciver.setVisibility(View.GONE);
        holder.sender.setVisibility(View.GONE);
        holder.recieverImage.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);


        if(dataType.equals("text"))
    {

        if(fromUserId.equals(mesSenderId))
        {
            holder.sender.setVisibility(View.VISIBLE);
            holder.sender.setBackgroundResource(R.drawable.sender_message_layout);
            holder.sender.setTextColor(Color.BLACK);
            holder.sender.setText(dataMessage+"\n\n"+data.getTime()+" - "+data.getDate());

        }

        else
        {
            holder.reciver.setVisibility(View.VISIBLE);

            holder.reciver.setBackgroundResource(R.drawable.reciever_message_layout);
            holder.reciver.setTextColor(Color.BLACK);
            holder.reciver.setText(dataMessage+"\n\n"+data.getTime()+" - "+data.getDate());
        }

    }
        else if(dataType.equals("image")) {

            if(fromUserId.equals(mesSenderId))
            {
                holder.senderImage.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(data.getMessage()).into(holder.senderImage);
            }
            else
            {
                holder.recieverImage.setVisibility(View.VISIBLE);


                Glide.with(context)
                        .load(data.getMessage()).into(holder.recieverImage);
            }

        }

        else if(dataType.equals("pdf")||dataType.equals("docx")) {
            if (fromUserId.equals(mesSenderId)) {
                holder.senderImage.setVisibility(View.VISIBLE);

                //https://firebasestorage.googleapis.com/v0/b/heyfirebaseproject-5a4b9.appspot.com/o/Image%20File%2Ffile.png?alt=media&token=c9d21615-ed78-456e-8eb8-4b49cedb80ce

                Glide.with(context)
                        .load("https://firebasestorage.googleapis.com/v0/b/heyfirebaseproject-5a4b9.appspot.com/o/Image%20File%2Ffile.png?alt=media&token=c9d21615-ed78-456e-8eb8-4b49cedb80ce")
                        .into(holder.senderImage);

                //holder.senderImage.setBackgroundResource(R.drawable.file);

//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
//                        holder.itemView.getContext().startActivity(intent);
//                    }
//                });

            } else {
                holder.recieverImage.setVisibility(View.VISIBLE);
                //holder.recieverImage.setBackgroundResource(R.drawable.file);

                Glide.with(context)
                        .load("https://firebasestorage.googleapis.com/v0/b/heyfirebaseproject-5a4b9.appspot.com/o/Image%20File%2Ffile.png?alt=media&token=c9d21615-ed78-456e-8eb8-4b49cedb80ce")
                        .into(holder.recieverImage);

//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
//                        holder.itemView.getContext().startActivity(intent);
//                    }
//                });


            }
        }

        if (fromUserId.equals(mesSenderId)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (userMessageList.get(position).getType().equals("pdf")||userMessageList.get(position).getType().equals("docx"))
                    {
                     CharSequence[] option={
                             "Delete for me",
                             "Download and view documents",
                             "Cancel",
                             "Delete for everyone"
                     };

                        AlertDialog.Builder  alertDialog= new AlertDialog.Builder(holder.itemView.getContext());

                        alertDialog.setTitle("Choose one for action");

                        alertDialog.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(i==0)
                                {
                                  deleteSendMessage(position,holder);
                                  Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                  holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==3)
                                {
                                 deleteForEveryone(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });
                         alertDialog.show();
                    }

                    else if (userMessageList.get(position).getType().equals("text"))
                    {
                        CharSequence[] option={
                                "Delete for me",
                                "Cancel",
                                "Delete for everyone"
                        };

                        AlertDialog.Builder  alertDialog= new AlertDialog.Builder(holder.itemView.getContext());

                        alertDialog.setTitle("Choose one for action");

                        alertDialog.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(i==0)
                                {
                                   deleteSendMessage(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==2)
                                {
                                   deleteForEveryone(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        alertDialog.show();
                    }

                    else if (userMessageList.get(position).getType().equals("image"))
                    {
                        CharSequence[] option={
                                "Delete for me",
                                "Show full size",
                                "Cancel",
                                "Delete for everyone"
                        };

                        AlertDialog.Builder  alertDialog= new AlertDialog.Builder(holder.itemView.getContext());

                        alertDialog.setTitle("Choose one for action");

                        alertDialog.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(i==0)
                                {
                                 deleteSendMessage(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==1)
                                {
                                 Intent intent=new Intent(holder.itemView.getContext(),imageViewerActivity.class);
                                 intent.putExtra("url",userMessageList.get(position).getMessage());
                                 holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==3)
                                {
                               deleteForEveryone(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        alertDialog.show();
                    }

                }
            });
        }
        else
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (userMessageList.get(position).getType().equals("pdf")||userMessageList.get(position).getType().equals("docx"))
                    {
                        CharSequence[] option={
                                "Delete for me",
                                "Download and view documents",
                                "Cancel",

                        };

                        AlertDialog.Builder  alertDialog= new AlertDialog.Builder(holder.itemView.getContext());

                        alertDialog.setTitle("Choose one for action");

                        alertDialog.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(i==0)
                                {
                                deleteRecieveMessage(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        alertDialog.show();
                    }

                    else if (userMessageList.get(position).getType().equals("text"))
                    {
                        CharSequence[] option={
                                "Delete for me",
                                "Cancel",
                        };

                        AlertDialog.Builder  alertDialog= new AlertDialog.Builder(holder.itemView.getContext());

                        alertDialog.setTitle("Choose one for action");

                        alertDialog.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(i==0)
                                {
                                deleteRecieveMessage(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        alertDialog.show();
                    }

                    else if (userMessageList.get(position).getType().equals("image"))
                    {
                        CharSequence[] option={
                                "Delete for me",
                                "Show full size",
                                "Cancel",
                        };

                        AlertDialog.Builder  alertDialog= new AlertDialog.Builder(holder.itemView.getContext());

                        alertDialog.setTitle("Choose one for action");

                        alertDialog.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(i==0)
                                {
                                 deleteRecieveMessage(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==1)
                                {
                                    Intent intent=new Intent(holder.itemView.getContext(),imageViewerActivity.class);
                                    intent.putExtra("url",userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        alertDialog.show();
                    }

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


    private void deleteSendMessage(final int position,final myViewHolder holder)
    {
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error!!", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    private void deleteRecieveMessage(final int position,final myViewHolder holder)
    {
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error!!", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }


    private void deleteForEveryone(final int position,final myViewHolder holder)
    {
       final DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    rootRef.child("Message")
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Succesfully Deleted", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(holder.itemView.getContext(), "Error!!", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error!!", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }


    public class myViewHolder extends RecyclerView.ViewHolder {
    TextView sender,reciver;
    ImageView senderImage,recieverImage;


    public myViewHolder(@NonNull View itemView) {
        super(itemView);
        sender=itemView.findViewById(R.id.senderPart);
        reciver=itemView.findViewById(R.id.recieverPart);
        senderImage=itemView.findViewById(R.id.senderImagePart);
        recieverImage=itemView.findViewById(R.id.recieverImagePart);

    }
}


}
