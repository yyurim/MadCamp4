package com.madcamp.petclub.MyPets;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.madcamp.petclub.Board.BoardActivity;
import com.madcamp.petclub.Friends.SplashActivity;
import com.madcamp.petclub.Hospital.HospitalActivity;
import com.madcamp.petclub.MainActivity.MainActivity;
import com.madcamp.petclub.MyInfo.MyInfoActivity;
import com.madcamp.petclub.MyPets.models.Pet;
import com.madcamp.petclub.News.NewsActivity;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyPetActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton fab_add;
    public static ArrayList<Pet> pet_data;
    RecyclerView recyclerView;
    final int EDIT_OR_DELETE = 0;
    final int REQUEST_GET_SINGLE_FILE = 1;
    CardView cardView;
    private DatabaseReference petDB;
    public static String userID;
    PetViewAdapter adapter;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent pet_intent = getIntent();
        userID = SharedPreference.getAttribute(getApplicationContext(), "userID");
        pet_data = new ArrayList<Pet>();
        setContentView(R.layout.activity_my_pet);
        mStorage = FirebaseStorage.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.myPetRecyclerView);
        recyclerView.setHasFixedSize(true);
        adapter = new PetViewAdapter(pet_data, MyPetActivity.this);
        recyclerView.setAdapter(adapter);
        System.out.println("Done setting adapter");

        petDB = FirebaseDatabase.getInstance().getReference("/pet/" + userID);
        String sort_column_name = "petName";
        Query sortbyName = petDB.orderByChild(sort_column_name);
        sortbyName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("Done clear the pet data");
                pet_data.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
                    System.out.println("key is " + key);
                    Pet get = postSnapshot.getValue(Pet.class);
                    pet_data.add(get);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fab_add = (FloatingActionButton) findViewById(R.id.fabNewPet);
        fab_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent write_intent = new Intent(MyPetActivity.this, PetWriteActivity.class);
                write_intent.putExtra("userID", userID);
                startActivity(write_intent);
            }
        });

        // NavigationView
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navigationHeaderView = navigationView.getHeaderView(0);
        final ImageView navigationHeaderUserProfile = (ImageView) navigationHeaderView.findViewById(R.id.userProfileImage);
        TextView navigationHeaderUserEmail = (TextView) navigationHeaderView.findViewById(R.id.userEmail);

        if (mStorage.child(userID + "/profile/profileImage") != null) {
            final long ONE_MEGABYTE = 1024 * 1024 * 1024;
            mStorage.child(userID + "/profile/profileImage").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        Intent intent;

        switch (id) {
            case R.id.nav_home:
                intent = new Intent(MyPetActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_myCompanion:
                break;
            case R.id.nav_friends:
                intent = new Intent(MyPetActivity.this, SplashActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_board:
                intent = new Intent(MyPetActivity.this, BoardActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                intent = new Intent(MyPetActivity.this, NewsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_hospital_info:
                intent = new Intent(MyPetActivity.this, HospitalActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_myInfo:
                intent = new Intent(MyPetActivity.this, MyInfoActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public Query getQuery(DatabaseReference databaseReference) {
        Query myPetQuery = databaseReference.child("pets")
                .child(userID);
        return myPetQuery;
    }
}