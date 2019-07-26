package com.madcamp.petclub.Friends.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.madcamp.petclub.Friends.common.Util9;
import com.madcamp.petclub.Friends.model.UserModel;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class UserFragment extends Fragment {
    // login cache
    private String userID;

    private static final int PICK_FROM_ALBUM = 1;
    private ImageView user_photo;
    private EditText user_id;
    private EditText user_name;
    private EditText user_msg;

    private UserModel userModel;
    private Uri userPhotoUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        userID = SharedPreference.getAttribute(container.getContext(),"userID");
        //user_id = view.findViewById(R.id.user_id);
        //user_id.setEnabled(false);
        user_name = view.findViewById(R.id.user_name);
        user_name.setEnabled(false);
        user_msg = view.findViewById(R.id.user_msg);
        user_photo = view.findViewById(R.id.user_photo);
        user_photo.setOnClickListener(userPhotoIVClickListener);

        Button saveBtn = view.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(saveBtnClickListener);
        //Button changePWBtn = view.findViewById(R.id.changePWBtn);
        //changePWBtn.setOnClickListener(changePWBtnClickListener);

        getUserInfoFromServer();
        return view;
    }

    void getUserInfoFromServer(){
        //String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String uid = SharedPreference.getAttribute(getContext(), "userID");

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userModel = documentSnapshot.toObject(UserModel.class);
                //user_id.setText(userModel.getUserid());
                user_name.setText(userModel.getUsernm());
                user_msg.setText(userModel.getUsermsg());

                FirebaseStorage.getInstance().getReference(userModel.ID+"/profile").child("profileImage").getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Glide.with(getActivity()).load(R.drawable.user)
                                .into(user_photo);
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getActivity())
                                .load(uri)
                                .into(user_photo);
                    }
                });
            }
        });
    }

    Button.OnClickListener userPhotoIVClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==PICK_FROM_ALBUM && resultCode== getActivity().RESULT_OK) {
            user_photo.setImageURI(data.getData());
            userPhotoUri = data.getData();
        }
    }

    Button.OnClickListener saveBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            if (!validateForm()) return;
            userModel.setUsernm(user_name.getText().toString());
            userModel.setUsermsg(user_msg.getText().toString());

            final String uid = SharedPreference.getAttribute(getContext(), "userID");
            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            if (userPhotoUri!=null) {
                userModel.setUserphoto( uid );
            }

            db.collection("users").document(userID)
                    .set(userModel)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {
                            if (userPhotoUri==null) {
                                Util9.showMessage(getActivity(), "Success to Save.");
                            } else {
                                // small image
                                Glide.with(getContext())
                                        .asBitmap()
                                        .load(userPhotoUri)
                                        .apply(new RequestOptions().override(150, 150))
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                byte[] data = baos.toByteArray();
                                                UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child(userID+"/profile/profileImage").putBytes(data);
                                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        // Handle unsuccessful uploads
                                                        Log.e("디버그", "업로드안됨*****************************");
                                                    }
                                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                                        // ...
                                                        Log.e("디버그", "업로드됨!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11*****************************");
                                                        Util9.showMessage(getActivity(), "Success to Save Image.");
                                                    }
                                                });
                                            }
                                        });
                            }
                        }
                    });
        }
    };

    Button.OnClickListener changePWBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            //startActivity(new Intent(getActivity(), UserPWActivity.class));
        }
    };

    private boolean validateForm() {
        boolean valid = true;

        String userName = user_name.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            user_name.setError("Required.");
            valid = false;
        } else {
            user_name.setError(null);
        }

        String userMsg = user_msg.getText().toString();
        if (TextUtils.isEmpty(userMsg)) {
            user_msg.setError("Required.");
            valid = false;
        } else {
            user_msg.setError(null);
        }
        Util9.hideKeyboard(getActivity());

        return valid;
    }

}
