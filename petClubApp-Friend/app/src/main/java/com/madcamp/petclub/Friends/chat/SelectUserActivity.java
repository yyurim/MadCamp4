package com.madcamp.petclub.Friends.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.madcamp.petclub.Friends.common.Util9;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SelectUserActivity extends AppCompatActivity {
    private String roomID;
    public Map<String, String> selectedUsers = new HashMap<>() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        roomID = getIntent().getStringExtra("roomID");
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( new LinearLayoutManager((this)));
        recyclerView.setAdapter(new UserListRecyclerViewAdapter());
        Button makeRoomBtn = findViewById(R.id.makeRoomBtn);

        if (roomID==null)
            makeRoomBtn.setOnClickListener(makeRoomClickListener);
        else
            makeRoomBtn.setOnClickListener(addRoomUserClickListener);
    }

    Button.OnClickListener makeRoomClickListener = new View.OnClickListener() {

        public void onClick(View view) {
            if (selectedUsers.size() <2) {
                Util9.showMessage(getApplicationContext(), "Please select 2 or more user");
                return;
            }

            String uid = SharedPreference.getAttribute(getApplicationContext(),"userID");
            selectedUsers.put(uid, "i");
            final String room_id = FirebaseDatabase.getInstance().getReference().child("rooms").push().getKey();

            FirebaseDatabase.getInstance().getReference().child("rooms/"+room_id).child("users").setValue(selectedUsers).addOnSuccessListener(new OnSuccessListener<Void>() {

                @Override

                public void onSuccess(Void aVoid) {
                    Intent intent = new Intent(SelectUserActivity.this, ChatActivity.class);
                    intent.putExtra("roomID", room_id);
                    startActivity(intent);
                    SelectUserActivity.this.finish();
                }

            });
        }

    };

    Button.OnClickListener addRoomUserClickListener = new View.OnClickListener() {

        public void onClick(View view) {
            if (selectedUsers.size() <1) {
                Util9.showMessage(getApplicationContext(), "Please select 1 or more user");
                return;
            }

            FirebaseDatabase.getInstance().getReference().child("rooms").child(roomID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot item : dataSnapshot.getChildren()) {
                        selectedUsers.put(item.getKey(), (String) item.getValue());
                    }

                    FirebaseDatabase.getInstance().getReference().child("rooms/"+roomID).child("users").setValue(selectedUsers).addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override

                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(SelectUserActivity.this, ChatActivity.class);
                            intent.putExtra("roomID", roomID);
                            startActivity(intent);
                            SelectUserActivity.this.finish();
                        }

                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });

        }

    };

    class UserListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));
        List<DataSnapshot> userModels;
        private StorageReference storageReference;

        public UserListRecyclerViewAdapter() {
            storageReference  = FirebaseStorage.getInstance().getReference();
            userModels = new ArrayList<>();
            final String myUid = SharedPreference.getAttribute(getApplicationContext(),"userID");

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModels.clear();
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_user, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final CustomViewHolder customViewHolder = (CustomViewHolder) holder;
            //final UserModel user = userModels.get(position);
            final DataSnapshot user = userModels.get(position);

            customViewHolder.user_name.setText(user.child("Name").getValue().toString());

            FirebaseStorage.getInstance().getReference(user.getKey()+"/profile").child("profileImage").getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Glide.with(getApplicationContext()).load(R.drawable.user)
                            .apply(requestOptions)
                            .into(customViewHolder.user_photo);
                }
            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext())
                            .load(uri)
                            .apply(requestOptions)
                            .into(customViewHolder.user_photo);
                }
            });


            ((CustomViewHolder)holder).userChk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override

                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedUsers.put(user.getKey(), "i");
                    } else {
                        selectedUsers.remove(user.getKey());
                    }
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
        public CheckBox userChk;

        public CustomViewHolder(View view) {
            super(view);
            user_photo = view.findViewById(R.id.user_photo);
            user_name = view.findViewById(R.id.user_name);
            userChk = view.findViewById(R.id.userChk);
        }

    }



}