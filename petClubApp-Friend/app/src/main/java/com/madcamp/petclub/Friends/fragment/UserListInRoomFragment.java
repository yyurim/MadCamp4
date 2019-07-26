package com.madcamp.petclub.Friends.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.madcamp.petclub.Friends.chat.SelectUserActivity;
import com.madcamp.petclub.Friends.model.UserModel;
import com.madcamp.petclub.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserListInRoomFragment extends Fragment {
    private String roomID;
    private List<UserModel> userModels;
    private RecyclerView recyclerView;

    public UserListInRoomFragment() {
    }

    public static final UserListInRoomFragment getInstance(String roomID, Map<String, UserModel> userModels) {
        List<UserModel> users = new ArrayList();
        for( Map.Entry<String, UserModel> elem : userModels.entrySet() ){
            users.add(elem.getValue());
        }

        UserListInRoomFragment f = new UserListInRoomFragment();
        f.setUserList(users);
        Bundle bdl = new Bundle();
        bdl.putString("roomID", roomID);
        f.setArguments(bdl);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userlistinroom, container, false);
        if (getArguments() != null) {
            roomID = getArguments().getString("roomID");
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( new LinearLayoutManager((inflater.getContext())));
        recyclerView.setAdapter(new UserFragmentRecyclerViewAdapter());

        view.findViewById(R.id.addContactBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(getActivity(), SelectUserActivity.class);
                intent.putExtra("roomID", roomID);
                startActivity(intent);
            }
        });

        return view;
    }

    public void setUserList(List<UserModel> users) {
        userModels = users;
    }

    class UserFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private StorageReference storageReference;
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));

        public UserFragmentRecyclerViewAdapter() {
            storageReference  = FirebaseStorage.getInstance().getReference();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final UserModel user = userModels.get(position);
            final CustomViewHolder customViewHolder = (CustomViewHolder) holder;
            customViewHolder.user_name.setText(user.name);
            //customViewHolder.user_msg.setText(user.getUsermsg());

            FirebaseStorage.getInstance().getReference(user.ID+"/profile").child("profileImage").getDownloadUrl().addOnFailureListener(new OnFailureListener() {
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
            user_msg.setVisibility(View.GONE);
        }
    }
}