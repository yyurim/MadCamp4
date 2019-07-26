package com.madcamp.petclub.Friends.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.madcamp.petclub.Friends.chat.ChatActivity;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserListFragment extends Fragment {

    public UserListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userlist, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( new LinearLayoutManager((inflater.getContext())));
        recyclerView.setAdapter(new UserFragmentRecyclerViewAdapter());

        return view;
    }

    class UserFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<DataSnapshot> userModels;
        private StorageReference storageReference;
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));

        public UserFragmentRecyclerViewAdapter() {
            storageReference  = FirebaseStorage.getInstance().getReference();
            userModels = new ArrayList<>();

            final String myUid = SharedPreference.getAttribute(getContext(),"userID");

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //userModels.clear();
                    Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                    while(child.hasNext()){
                        //UserModel userModel = child.getValue(UserModel.class);
                        DataSnapshot user = child.next();
                        Log.e("디버그",user.getKey());
                        //Log.e("디버그",userModel.getUsernm());
                        //Log.e("디버그",userModel.getUsermsg());
                        if (myUid.equals(user.getKey())) continue;

                        userModels.add(user);
                    }
                    /*
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        Log.e("디버그",snapshot.getKey());
                        //Log.e("디버그",userModel.getUsernm());
                        //Log.e("디버그",userModel.getUsermsg());
                        if (myUid.equals(snapshot.getKey())) continue;

                        userModels.add(userModel);
                    }
                    */
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            //final UserModel user = userModels.get(position);
            final DataSnapshot user = userModels.get(position);
            final CustomViewHolder customViewHolder = (CustomViewHolder) holder;
            customViewHolder.user_name.setText(user.child("Name").getValue().toString());
            customViewHolder.user_msg.setText(user.child("userMsg").getValue().toString());
            Log.e("채팅",user.child("UserID").toString());
            Log.e("채팅",user.child("Name").toString());
            Log.e("채팅",user.child("userMsg").toString());

            FirebaseStorage.getInstance().getReference(user.getKey()+"/profile").child("profileImage").getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Glide.with(getActivity()).load(R.drawable.user)
                            .apply(requestOptions)
                            .into(customViewHolder.user_photo);
                }
            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getActivity())
                            .load(uri)
                            .apply(requestOptions)
                            .into(customViewHolder.user_photo);
                }
            });


            holder.itemView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getView().getContext(), ChatActivity.class);
                    intent.putExtra("toUid", user.getKey());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView user_photo;
        public TextView user_name;
        public TextView user_msg;

        public CustomViewHolder(View view) {
            super(view);
            user_photo = view.findViewById(R.id.user_photo);
            user_name = view.findViewById(R.id.user_name);
            user_msg = view.findViewById(R.id.user_msg);
        }
    }
}