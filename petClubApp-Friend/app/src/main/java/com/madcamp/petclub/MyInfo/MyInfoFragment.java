package com.madcamp.petclub.MyInfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyInfoFragment extends Fragment {
    DatabaseReference mDatabaseReference;
    StorageReference mStorageReference;
    String userID;

    TextView userIDView;
    TextView userNameView;
    TextView userPhoneView;

    EditText userPasswordView;
    EditText userPasswordConfrimView;

    Button modify;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        userID = SharedPreference.getAttribute(getContext(),"userID");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_info, container, false);
        userIDView = view.findViewById(R.id.userID);
        userNameView = view.findViewById(R.id.userName);
        userPhoneView = view.findViewById(R.id.userPhone);
        userPasswordView = view.findViewById(R.id.edt_password);
        userPasswordConfrimView = view.findViewById(R.id.edt_password_confirm);

        modify = view.findViewById(R.id.submit);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseReference.child(userID).child("Password").setValue(userPasswordView.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(),"MODIFIED!",Toast.LENGTH_SHORT);
                    }
                });
            }
        });

        mDatabaseReference.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            //ataSnapshot user;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userIDView.setText(dataSnapshot.child("ID").getValue().toString());
                userNameView.setText(dataSnapshot.child("Name").getValue().toString());
                userPhoneView.setText(dataSnapshot.child("Phone").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}