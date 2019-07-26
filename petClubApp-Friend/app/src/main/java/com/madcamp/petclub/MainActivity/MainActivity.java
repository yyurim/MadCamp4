package com.madcamp.petclub.MainActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.madcamp.petclub.Board.BoardActivity;
import com.madcamp.petclub.Friends.SplashActivity;
import com.madcamp.petclub.Hospital.HospitalActivity;
import com.madcamp.petclub.MyInfo.MyInfoActivity;
import com.madcamp.petclub.MyPets.MyPetActivity;
import com.madcamp.petclub.News.News;
import com.madcamp.petclub.News.NewsActivity;
import com.madcamp.petclub.News.NewsWebView;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//import android.content.SharedPreferences;
//import com.madcamp.petclub.MyInfo.MyInfo;

//import org.w3c.dom.Text;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Intent loginIntent;
    String userID;
    StorageReference mStorage;

    private RecyclerView newsRecyclerView;
    private FirebaseRecyclerAdapter<News, NewsActivity.NewsViewHolder> mPeopleRVAdapter;
    private DatabaseReference mDatabase;

    private ImageView flag;

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    /*
    FloatingActionButton fab, fabNewPost, fabDeletePost;
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID = SharedPreference.getAttribute(getApplicationContext(), "userID");
        mStorage = FirebaseStorage.getInstance().getReference();


        // news
        newsRecyclerView = (RecyclerView) findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));


        mDatabase = FirebaseDatabase.getInstance().getReference().child("News");
        mDatabase.keepSynced(true);
        DatabaseReference personsRef = FirebaseDatabase.getInstance().getReference().child("News");
        Query personsQuery = personsRef.orderByKey();

        FirebaseRecyclerOptions personsOptions = new FirebaseRecyclerOptions.Builder<News>().setQuery(personsQuery, News.class).build();

        mPeopleRVAdapter = new FirebaseRecyclerAdapter<News, NewsActivity.NewsViewHolder>(personsOptions) {
            @Override
            protected void onBindViewHolder(NewsActivity.NewsViewHolder holder, final int position, final News model) {
                String title, desc;
                title = model.getNewsTitle();
                desc = model.getNewsDesc();
                //type = model.getNewsType();
                if (title.length() > 10) {title = title.substring(0, 10)+"...";}
                if (desc.length()>15) {desc = desc.substring(0, 15)+"...";}

                holder.setTitle(title);
                holder.setDesc(desc);
                holder.setImage(getBaseContext(), model.getNewsImage());

                /*
                if(type == "image"){
                    ImageView flag = (ImageView) newsRecyclerView.findViewById(R.id.flag);
                }
                */

                holder.getmView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String url = model.getNewsUrl();
                        Intent intent = new Intent(getApplicationContext(), NewsWebView.class);
                        intent.putExtra("id", url);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public NewsActivity.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_news_row_main, parent, false);

                //flag = view.findViewById(R.id.flag);
                return new NewsActivity.NewsViewHolder(view);
            }
        };

        newsRecyclerView.setAdapter(mPeopleRVAdapter);

        // weekly issue
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new TopPostsFragment()
                    //new RecentPostsFragment(),
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_recent)
            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        //TabLayout tabLayout = findViewById(R.id.tabs);
        //tabLayout.setupWithViewPager(mViewPager);


        /*
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        // Button launches NewPostActivity
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim();
            }
        });
        fabNewPost = (FloatingActionButton) findViewById(R.id.fabNewPost);
        fabNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                startActivity(new Intent(MainActivity.this, NewPostActivity.class));
            }
        });
        fabDeletePost = (FloatingActionButton) findViewById(R.id.fabDeletePost);
        fabDeletePost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                anim();
            }
        });
        */

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
    public void onStart() {
        super.onStart();
        mPeopleRVAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPeopleRVAdapter.stopListening();


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
        Intent intent;

        switch (id) {
            case R.id.nav_home:
                break;
            case R.id.nav_myCompanion:
                intent = new Intent(MainActivity.this, MyPetActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_friends:
                intent = new Intent(MainActivity.this, SplashActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_board:
                intent = new Intent(MainActivity.this, BoardActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                intent = new Intent(MainActivity.this, NewsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_hospital_info:
                intent = new Intent(MainActivity.this, HospitalActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_myInfo:
                intent = new Intent(MainActivity.this, MyInfoActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                break;
        }
    }


/*
    public void anim() {
        if (isFabOpen) {
            fabNewPost.startAnimation(fab_close);
            fabDeletePost.startAnimation(fab_close);
            fabNewPost.setClickable(false);
            fabDeletePost.setClickable(false);
            isFabOpen = false;
        } else {
            fabNewPost.startAnimation(fab_open);
            fabDeletePost.startAnimation(fab_open);
            fabNewPost.setClickable(true);
            fabDeletePost.setClickable(true);
            isFabOpen = true;
        }
    }
    */
}