package com.madcamp.petclub.Friends;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.madcamp.petclub.Board.BoardActivity;
import com.madcamp.petclub.Friends.chat.SelectUserActivity;
import com.madcamp.petclub.Friends.fragment.ChatRoomFragment;
import com.madcamp.petclub.Friends.fragment.UserFragment;
import com.madcamp.petclub.Friends.fragment.UserListFragment;
import com.madcamp.petclub.Hospital.HospitalActivity;
import com.madcamp.petclub.MainActivity.MainActivity;
import com.madcamp.petclub.MyInfo.MyInfoActivity;
import com.madcamp.petclub.MyPets.MyPetActivity;
import com.madcamp.petclub.News.NewsActivity;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.LoginActivity;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class FriendActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private StorageReference mStorage;
    private String userID;

    private SectionsPagerAdapter mSectionsPagerAdapter;


    private ViewPager mViewPager;
    private FloatingActionButton makeRoomBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStorage = FirebaseStorage.getInstance().getReference();
        userID = SharedPreference.getAttribute(getApplicationContext(), "userID");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {     // char room
                    makeRoomBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                makeRoomBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        sendRegistrationToServer();


        makeRoomBtn = findViewById(R.id.makeRoomBtn);
        makeRoomBtn.setVisibility(View.INVISIBLE);
        makeRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SelectUserActivity.class));
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


    void sendRegistrationToServer() {
        String uid = SharedPreference.getAttribute(getApplicationContext(),"userID");
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            SharedPreference.removeAttribute(getApplicationContext(),"userID");
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            this.finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();
        Fragment fragment = null;
        Intent intent;

        switch (id) {
            case R.id.nav_home:
                intent = new Intent(FriendActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_myCompanion:
                intent = new Intent(FriendActivity.this, MyPetActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_friends:
                break;
            case R.id.nav_board:
                intent = new Intent(FriendActivity.this, BoardActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                intent = new Intent(FriendActivity.this, NewsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_hospital_info:
                intent = new Intent(FriendActivity.this, HospitalActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_myInfo:
                intent = new Intent(FriendActivity.this, MyInfoActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new UserListFragment();
                case 1: return new ChatRoomFragment();
                default: return new UserFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}