package com.madcamp.petclub.MyInfo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.madcamp.petclub.Friends.FriendActivity;
import com.madcamp.petclub.Hospital.HospitalActivity;
import com.madcamp.petclub.MainActivity.MainActivity;
import com.madcamp.petclub.MyPets.MyPetActivity;
import com.madcamp.petclub.News.NewsActivity;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class MyInfoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    final int PICK_FROM_ALBUM = 1;

    String userID;
    String imgPath;

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private TextView profileUserID;
    private ImageView profileImage;

    private StorageReference mStorage;
    private FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // for profile
        mStorage = FirebaseStorage.getInstance().getReference();

        userID = SharedPreference.getAttribute(getApplicationContext(), "userID");

        profileUserID = (TextView) findViewById(R.id.userID);
        profileUserID.setText(userID);

        profileImage = (ImageView) findViewById(R.id.profileImg);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        FirebaseStorage.getInstance().getReference(userID+"/profile").child("profileImage").getDownloadUrl().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Glide.with(getApplicationContext()).load(R.drawable.ic_user)
                        .into(profileImage);
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext())
                        .load(uri)
                        .into(profileImage);
            }
        });


        // FRAGMENT
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new MyInfoFragment(),
                    new MyPostsFragment(),
                    new MyTopPostsFragment()
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_my_info),
                    getString(R.string.heading_my_posts),
                    getString(R.string.heading_my_top_posts)
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
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


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
                intent = new Intent(MyInfoActivity.this, MainActivity.class);
                startActivity(intent);
            case R.id.nav_myCompanion:
                intent = new Intent(MyInfoActivity.this, MyPetActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_friends:
                intent = new Intent(MyInfoActivity.this, FriendActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_share:
                intent = new Intent(MyInfoActivity.this, NewsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_hospital_info:
                intent = new Intent(MyInfoActivity.this, HospitalActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_myInfo:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Date date = new Date(System.currentTimeMillis());
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == PICK_FROM_ALBUM) {
                    Uri selectedImageUri = data.getData();

                    // Set the image in ImageView
                    profileImage.setImageURI(selectedImageUri);

                    // [START upload_memory]
                    profileImage.setDrawingCacheEnabled(true);
                    profileImage.buildDrawingCache();

                    Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
                    ByteArrayOutputStream uploadStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, uploadStream);
                    byte[] bytes = uploadStream.toByteArray();

                    UploadTask uploadTask = mStorage.child(userID + "/profile/profileImage").putBytes(bytes);
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
                            // ...Log.e("디버그", "업로드됨!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11*****************************");}
                        }
                    });
                    // [END upload_memory]
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}