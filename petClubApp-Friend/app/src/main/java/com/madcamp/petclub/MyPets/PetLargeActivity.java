package com.madcamp.petclub.MyPets;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.madcamp.petclub.Board.BoardActivity;
import com.madcamp.petclub.Diary.DiaryActivity;
import com.madcamp.petclub.Friends.SplashActivity;
import com.madcamp.petclub.Hospital.HospitalActivity;
import com.madcamp.petclub.MainActivity.MainActivity;
import com.madcamp.petclub.MyInfo.MyInfoActivity;
import com.madcamp.petclub.News.NewsActivity;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import static com.madcamp.petclub.MyPets.MyPetActivity.pet_data;

public class PetLargeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    final int REQUEST_GET_SINGLE_FILE = 1;
    int flagImage=0;

    StorageReference mStorage;
    private ImageView profileImage;

    private static TextView species, name, firstdate, age, gender, neutralization, bestFriend;
    String petKey, userID, petName;
    public static Button edit_btn, delete_btn, diary_btn;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        petName = intent.getStringExtra("petName");
        setContentView(R.layout.pet_full_size);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID = SharedPreference.getAttribute(getApplicationContext(),"userID");

        mStorage = FirebaseStorage.getInstance().getReference();
        profileImage = (ImageView) findViewById(R.id.pet_profile);
        FirebaseStorage.getInstance().getReference("pet/" + userID + "/" + petName + "/profile").child("profileImage").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Data for "images/island.jpg" is returns, use this as needed
                Glide.with(profileImage.getContext()).load(uri).into(profileImage);
            }
        });

        species = (TextView) findViewById(R.id.pet_species);
        name = (TextView) findViewById(R.id.pet_name);
        firstdate = (TextView) findViewById(R.id.pet_firstdate);
        age = (TextView) findViewById(R.id.pet_age);
        gender = (TextView) findViewById(R.id.pet_gender);
        neutralization = (TextView) findViewById(R.id.pet_neutralization);
        bestFriend = (TextView) findViewById(R.id.pet_BFF);


        species.setText(pet_data.get(position).getPetSpecies());
        name.setText(pet_data.get(position).getPetName());
        firstdate.setText(pet_data.get(position).getPetFirstDate());
        age.setText(pet_data.get(position).getPetAge());
        gender.setText(pet_data.get(position).getPetGender());
        neutralization.setText(pet_data.get(position).getPetNeutralization());
        bestFriend.setText(pet_data.get(position).getPetBff());

        userID = SharedPreference.getAttribute(getApplicationContext(), "userID");

        edit_btn = (Button)findViewById(R.id.edit_pet_btn);
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent edit_intent = new Intent(PetLargeActivity.this, PetEditActivity.class);
                edit_intent.putExtra("position", position);
                edit_intent.putExtra("petName", pet_data.get(position).getPetName());
                startActivity(edit_intent);
            }
        });

        delete_btn = (Button)findViewById(R.id.delete_pet_btn);
        delete_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/pet/"+userID+"/"+petKey, null);
                databaseReference.updateChildren(childUpdates);
                System.out.println("done deleting");
                finish();
            }
        });

        diary_btn = (Button)findViewById(R.id.diary_btn);
        diary_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PetLargeActivity.this, DiaryActivity.class);
                intent.putExtra("petName", name.getText().toString());
                startActivity(intent);
            }
        });

        // NavigationView
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navigationHeaderView =  navigationView.getHeaderView(0);
        final ImageView navigationHeaderUserProfile = (ImageView) navigationHeaderView.findViewById(R.id.userProfileImage);
        TextView navigationHeaderUserEmail = (TextView) navigationHeaderView.findViewById(R.id.userEmail);


        if(mStorage.child(userID + "/profile/profileImage") != null){
            final long ONE_MEGABYTE = 1024 * 1024*1024;
            mStorage.child(userID + "/profile/profileImage").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                    navigationHeaderUserProfile.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    navigationHeaderUserProfile.setImageResource(R.drawable.ic_user);
                }
            });
        }
        navigationHeaderUserEmail.setText(userID);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.nav_home:
                intent = new Intent(PetLargeActivity.this, MainActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_myCompanion:
                intent = new Intent(PetLargeActivity.this, MyPetActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_friends:
                intent = new Intent(PetLargeActivity.this, SplashActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_board:
                intent = new Intent(PetLargeActivity.this, BoardActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                intent = new Intent(PetLargeActivity.this, NewsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_hospital_info:
                intent = new Intent(PetLargeActivity.this, HospitalActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_myInfo:
                intent = new Intent(PetLargeActivity.this, MyInfoActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}