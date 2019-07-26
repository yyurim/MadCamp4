package com.madcamp.petclub.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.madcamp.petclub.Friends.model.UserModel;
import com.madcamp.petclub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    private UserModel userModel;

    private EditText editID;
    private EditText editPassword;
    private EditText editPasswordConfirmation;
    private EditText editPhone;
    private EditText editName;
    private EditText editEmail;
    private EditText editEmailID;
    private EditText editEmailDomainFirst;
    private EditText editEmailDomainLast;

    private ValueEventListener checkRegister = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
            while (child.hasNext()) {
                Log.e("디버그","find identical id");
                if (editID.getText().toString().equals(child.next().getKey())) {
                    Toast.makeText(getApplicationContext(), "ID already exists.", Toast.LENGTH_LONG).show();
                    databaseReference.removeEventListener(this);
                    return;
                }
            }
            makeNewUser();
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        databaseReference  = FirebaseDatabase.getInstance().getReference("users");
        firestore = FirebaseFirestore.getInstance();

        editID = (EditText)findViewById(R.id.edt_id);
        editPassword = (EditText)findViewById(R.id.edt_password);
        editPasswordConfirmation = (EditText)findViewById(R.id.edt_password_confirm);
        editPhone = (EditText)findViewById(R.id.edt_phone);
        editName = (EditText)findViewById(R.id.edt_name);
        editEmail = (EditText)findViewById(R.id.edt_email);

        Button Submit = (Button)findViewById(R.id.submit);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(editPassword.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Password cannot be null or empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editPasswordConfirmation.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Password confirmation cannot be null or empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editPhone.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Phone cannot be null or empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
/*
                if(editPassword.getText().toString() != editPasswordConfirmation.getText().toString()){
                    Toast.makeText(getApplicationContext(),"Please check your password.",Toast.LENGTH_LONG).show();
                    return;
                }
                */

                databaseReference.addListenerForSingleValueEvent(checkRegister);

                Intent intent  = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    void makeNewUser()
    {
        Date date = new Date(System.currentTimeMillis());

        // firestore
        Map<String, Object> user = new HashMap<>();
        user.put("ID",editID.getText().toString());
        //user.put("UserID",editID.getText().toString());
        if(editName.getText().toString() != null) {
            user.put("name", editName.getText().toString());
        }
        else{
            user.put("name", "");
        }
        user.put("usermsg", "hello world!");
        user.put("token", "");
        user.put("userphoto", "");

        firestore.collection("users").document(editID.getText().toString()).set(user);

        databaseReference.child(editID.getText().toString()).child("ID").setValue(editID.getText().toString());
        databaseReference.child(editID.getText().toString()).child("Password").setValue(editPassword.getText().toString());
        databaseReference.child(editID.getText().toString()).child("Phone").setValue(editPhone.getText().toString());
        databaseReference.child(editID.getText().toString()).child("userMsg").setValue("hello world!");

        if(editName.getText().toString()!=null){
            Log.e("디버그","name can be null");
            databaseReference.child(editID.getText().toString()).child("Name").setValue(editName.getText().toString());
        }
        else{
            Log.e("디버그","name no data");
            databaseReference.child(editID.getText().toString()).child("Name").setValue("No Data");
        }

        if(editEmail.getText().toString()!= null){
            Log.e("디버그","email can be null");
            databaseReference.child(editID.getText().toString()).child("Email").setValue(editEmail.getText().toString());        }
        else {
            Log.e("디버그","email no data");
            databaseReference.child(editID.getText().toString()).child("Email").setValue("No Data");
        }

        databaseReference.child(editID.getText().toString()).child("RegistrationDate").setValue(date.toString());
        Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_LONG).show();
    }
}
