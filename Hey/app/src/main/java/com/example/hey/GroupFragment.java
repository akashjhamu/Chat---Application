package com.example.hey;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {


    private View groupView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> group_list=new ArrayList<String>();

    private DatabaseReference grpRef;

    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupView= inflater.inflate(R.layout.fragment_group, container, false);

        grpRef= FirebaseDatabase.getInstance().getReference().child("Group");

        inItialise();

        retriveGroup();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                String currentUser=adapterView.getItemAtPosition(position).toString();

                Intent toGroupActivity=new Intent(getContext(),groupChatActivity.class);
                toGroupActivity.putExtra("groupName",currentUser);
                startActivity(toGroupActivity);

            }
        });

        return groupView;
    }



    private void inItialise() {

        listView=groupView.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,group_list);
        listView.setAdapter(adapter);

    }


    private void retriveGroup() {

        grpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> temp=new HashSet<String>();
                Iterator it=dataSnapshot.getChildren().iterator();

                while(it.hasNext())
                {
                    temp.add(((DataSnapshot)it.next()).getKey());
                }

                group_list.clear();

                group_list.addAll(temp);

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
