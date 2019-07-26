package com.madcamp.petclub.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.madcamp.petclub.MainActivity.MainActivity;
import com.madcamp.petclub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    EditText checkID;
    EditText checkPassword;
    Button login;
    TextView register;

    String userID;
    String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        checkID = (EditText)findViewById(R.id.checkEmail);
        checkPassword = (EditText)findViewById(R.id.checkPassword);

        login =(Button)findViewById(R.id.btn_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                        while(child.hasNext())
                        {
                            DataSnapshot user = child.next();

                            userID = user.getKey();
                            userPassword = user.child("Password").getValue().toString();

                            if(userID.equals(checkID.getText().toString()) && userPassword.equals(checkPassword.getText().toString()))
                            {
                                    Log.e("로그인","로그인성공$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
                                    Toast.makeText(getApplicationContext(), "로그인!", Toast.LENGTH_LONG).show();

                                    SharedPreference.setAttribute(getApplicationContext(), "userID",userID);
                                    SharedPreference.setAttribute(getApplicationContext(), "userPassword ", userPassword);
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);

                            }else if(userID.equals(checkID.getText().toString())){
                                Toast.makeText(getApplicationContext(),"비밀번호를 확인해주세요.",Toast.LENGTH_LONG).show();
                            }else{
                                //Toast.makeText(getApplicationContext(),"존재하지 않는 아이디입니다.",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        register = (TextView)findViewById(R.id.txt_create_account);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
