package com.madcamp.petclub.Friends.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.madcamp.petclub.Friends.model.ChatRoomModel;
import com.madcamp.petclub.Friends.model.Message;
import com.madcamp.petclub.Friends.model.UserModel;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatRoomFragment extends Fragment {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private RecyclerViewAdapter mAdapter;

    public ChatRoomFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatroom, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        mAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);

        RelativeLayout img = (RelativeLayout) view.findViewById(R.id.img);
        Drawable alpha = img.getBackground();
        alpha.setAlpha(50);

        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    // =============================================================================================

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));
        private List<ChatRoomModel> roomList = new ArrayList<>();
        private Map<String, UserModel> userList = new HashMap<>();
        private String myUid;
        private StorageReference storageReference;
        private FirebaseFirestore firestore;
        private ListenerRegistration listenerRegistration;
        private ListenerRegistration listenerUsers;

        RecyclerViewAdapter() {
            firestore = FirebaseFirestore.getInstance();
            storageReference  = FirebaseStorage.getInstance().getReference();
            myUid = SharedPreference.getAttribute(getContext(),"userID");

            // all users information
            listenerUsers = firestore.collection("users")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {return;}

                            for (QueryDocumentSnapshot doc : value) {
                                userList.put(doc.getId(), doc.toObject(UserModel.class));
                            }
                            getRoomInfo();
                        }
                    });
        }

        Integer unreadTotal = 0;
        public void getRoomInfo() {
            // my chatting room information
            listenerRegistration = firestore.collection("rooms").whereGreaterThanOrEqualTo("users."+myUid, 0)
//                    a.orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {return;}

                            TreeMap<Date, ChatRoomModel> orderedRooms = new TreeMap<Date, ChatRoomModel>(Collections.reverseOrder());

                            for (final QueryDocumentSnapshot document : value) {
                                Message message = document.toObject(Message.class);
                                if (message.getMsg() !=null & message.getTimestamp() == null) {continue;} // FieldValue.serverTimestamp is so late

                                ChatRoomModel chatRoomModel = new ChatRoomModel();
                                chatRoomModel.setRoomID(document.getId());

                                if (message.getMsg() !=null) { // there are no last message
                                    chatRoomModel.setLastDatetime(simpleDateFormat.format(message.getTimestamp()));
                                    switch(message.getMsgtype()){
                                        case "1": chatRoomModel.setLastMsg("Image"); break;
                                        case "2": chatRoomModel.setLastMsg("File"); break;
                                        default:  chatRoomModel.setLastMsg(message.getMsg());
                                    }
                                }
                                Map<String, Long> users = (Map<String, Long>) document.get("users");
                                chatRoomModel.setUserCount(users.size());
                                for( String key : users.keySet() ){
                                    if (myUid.equals(key)) {
                                        Integer  unread = (int) (long) users.get(key);
                                        unreadTotal += unread;
                                        chatRoomModel.setUnreadCount(unread);
                                        break;
                                    }
                                }

                                if (users.size()==2) {
                                    for( String key : users.keySet() ){
                                        if (myUid.equals(key)) continue;
                                        UserModel userModel = userList.get(key);
                                        chatRoomModel.setTitle(userModel.getUsernm());
                                        chatRoomModel.setPhoto(userModel.ID+"/profile");
                                    }
                                } else {                // group chat room
                                    chatRoomModel.setTitle(document.getString("title"));
                                }
                                if (message.getTimestamp()==null) message.setTimestamp(new Date());
                                orderedRooms.put(message.getTimestamp(), chatRoomModel);
                            }
                            roomList.clear();
                            for(Map.Entry<Date,ChatRoomModel> entry : orderedRooms.entrySet()) {
                                roomList.add(entry.getValue());
                            }
                            notifyDataSetChanged();
                            setBadge(getContext(), unreadTotal);
                        }
                    });
        }

        public void stopListening() {
            if (listenerRegistration != null) {
                listenerRegistration.remove();
                listenerRegistration = null;
            }
            if (listenerUsers != null) {
                listenerUsers.remove();
                listenerUsers = null;
            }

            roomList.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom, parent, false);
            return new RoomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            RoomViewHolder roomViewHolder = (RoomViewHolder) holder;

            final ChatRoomModel chatRoomModel = roomList.get(position);

            roomViewHolder.room_title.setText(chatRoomModel.getTitle());
            roomViewHolder.last_msg.setText(chatRoomModel.getLastMsg());
            roomViewHolder.last_time.setText(chatRoomModel.getLastDatetime());

            storageReference.child(chatRoomModel.getPhoto()).child("profileImage").getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Glide.with(getActivity()).load(R.drawable.user)
                            .apply(requestOptions)
                            .into(roomViewHolder.room_image);
                }
            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getActivity()).load(storageReference.child(chatRoomModel.getPhoto()).child("profileImage"))
                            .apply(requestOptions)
                            .into(roomViewHolder.room_image);
                }
            });



            if (chatRoomModel.getUserCount() > 2) {
                roomViewHolder.room_count.setText(chatRoomModel.getUserCount().toString());
                roomViewHolder.room_count.setVisibility(View.VISIBLE);
            } else {
                roomViewHolder.room_count.setVisibility(View.INVISIBLE);
            }

            if (chatRoomModel.getUnreadCount() > 0) {
                roomViewHolder.unread_count.setText(chatRoomModel.getUnreadCount().toString());
                roomViewHolder.unread_count.setVisibility(View.VISIBLE);
            } else {
                roomViewHolder.unread_count.setVisibility(View.INVISIBLE);
            }

            roomViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    intent.putExtra("roomID", chatRoomModel.getRoomID());
                    intent.putExtra("roomTitle", chatRoomModel.getTitle());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return roomList.size();
        }

        private class RoomViewHolder extends RecyclerView.ViewHolder {
            public ImageView room_image;
            public TextView room_title;
            public TextView last_msg;
            public TextView last_time;
            public TextView room_count;
            public TextView unread_count;

            RoomViewHolder(View view) {
                super(view);
                room_image = view.findViewById(R.id.room_image);
                room_title = view.findViewById(R.id.room_title);
                last_msg = view.findViewById(R.id.last_msg);
                last_time = view.findViewById(R.id.last_time);
                room_count = view.findViewById(R.id.room_count);
                unread_count = view.findViewById(R.id.unread_count);
            }
        }
    }

    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                return resolveInfo.activityInfo.name;
            }
        }
        return null;
    }
}