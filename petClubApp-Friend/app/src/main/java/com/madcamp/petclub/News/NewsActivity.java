package com.madcamp.petclub.News;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.madcamp.petclub.Board.BoardActivity;
import com.madcamp.petclub.Friends.SplashActivity;
import com.madcamp.petclub.Hospital.HospitalActivity;
import com.madcamp.petclub.MainActivity.MainActivity;
import com.madcamp.petclub.MyInfo.MyInfoActivity;
import com.madcamp.petclub.MyPets.MyPetActivity;
import com.madcamp.petclub.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class NewsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private RecyclerView mPeopleRV;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<News, NewsViewHolder> mPeopleRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setTitle("News");

        //"News" here will reflect what you have called your database in Firebase.
        mDatabase = FirebaseDatabase.getInstance().getReference().child("News");
        mDatabase.keepSynced(true);

        mPeopleRV = (RecyclerView) findViewById(R.id.myRecycleView);

        DatabaseReference personsRef = FirebaseDatabase.getInstance().getReference().child("News");
        Query personsQuery = personsRef.orderByKey();

        mPeopleRV.hasFixedSize();
        mPeopleRV.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions personsOptions = new FirebaseRecyclerOptions.Builder<News>().setQuery(personsQuery, News.class).build();

        mPeopleRVAdapter = new FirebaseRecyclerAdapter<News, NewsActivity.NewsViewHolder>(personsOptions) {
            @Override
            protected void onBindViewHolder(NewsActivity.NewsViewHolder holder, final int position, final News model) {
                String title, desc;
                title = model.getNewsTitle();
                desc = model.getNewsDesc();
                if (title.length() > 10) {title = title.substring(0, 10)+"...";}
                if (desc.length()>15) {desc = desc.substring(0, 15)+"...";}

                holder.setTitle(title);
                holder.setDesc(desc);
                holder.setImage(getBaseContext(), model.getNewsImage());

                holder.mView.setOnClickListener(new View.OnClickListener() {
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
                        .inflate(R.layout.news_row, parent, false);

                return new NewsActivity.NewsViewHolder(view);
            }
        };

        mPeopleRV.setAdapter(mPeopleRVAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_news_menu, menu);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.nav_home:
                intent = new Intent(NewsActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_myCompanion:
                intent = new Intent(NewsActivity.this, MyPetActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_friends:
                intent = new Intent(NewsActivity.this, SplashActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_board:
                intent = new Intent(NewsActivity.this, BoardActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                intent = new Intent(NewsActivity.this, NewsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_hospital_info:
                intent = new Intent(NewsActivity.this, HospitalActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.nav_myInfo:
                intent = new Intent(NewsActivity.this, MyInfoActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public static class NewsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public NewsViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setTitle(String title){
            TextView post_title = (TextView)mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }
        public void setDesc(String desc){
            TextView post_desc = (TextView)mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }
        public void setImage(Context ctx, String image){
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            Glide.with(ctx).load(image).into(post_image);
        }

        public View getmView(){
            return mView;
        }
    }

}